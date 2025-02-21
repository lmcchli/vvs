package com.mobeon.masp.execution_engine;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeTree;
import com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl;
import com.mobeon.masp.execution_engine.voicexml.runtime.VoiceXMLEventProcessor;
import com.mobeon.masp.util.Constructor;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.test.MASTestSwitches;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dom4j.Element;


/**
 * User: QMIAN
 * Date: 2006-okt-20
 * Time: 15:42:06
 * <p/>
 * The ApplicationWatchdog supervises that "interesting events" happen in each session, otherwise
 * there is a warning. An example of an interesting event is that a Connection goes from Alerting to
 * Connected, but the ApplicationWatchdog is agnostic to what is interesting and what is not, and
 * rather expects to be invoked when the interesting events occur.
 */
public class ApplicationWatchdog {

    private static int configuredTimeout;

    private static final ApplicationWatchdog instance = new ApplicationWatchdog();

    //TODO: Change this to use ExecutorServiceManager but for now the ExecutorServiceManager does not support
    // scheduled jobs
    private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(4);
    private static ILogger log = ILoggerFactory.getILogger(ApplicationWatchdog.class);
    private final int LOCK_COUNT = 4;
    List<Map<Id<ISession>, ApplicationWatchdogState>> sessions = new ArrayList<Map<Id<ISession>, ApplicationWatchdogState>>();
    private Object[] locks = new Object[LOCK_COUNT];

    private ApplicationWatchdog() {
        try {
            Tools.fillCollection(sessions,LOCK_COUNT, new Callable<Map<Id<ISession>, ApplicationWatchdogState>>() {
                public Map<Id<ISession>, ApplicationWatchdogState> call() {
                    return new WeakHashMap<Id<ISession>, ApplicationWatchdogState>(200);
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            Ignore.exception(e);
        }

        try {
            Tools.fillArray(locks, Constructor.OBJECT);
        } catch (Exception e) {
            throw new RuntimeException("Fatal error, ApplicationWatchdoug could not start !", e);
        }
    }

    private static class ApplicationWatchdogState implements Runnable {

        // The Boolean in WeakHashMap is a workaround since no WeakSet class exists, it's just a dummy value
        private final Map<Connection, Boolean> currentConnections = new WeakHashMap<Connection, Boolean>(2);
        private final Map<Connection, Boolean> terminalConnections = new WeakHashMap<Connection, Boolean>(2);
        private final Map<Dialog, Boolean> dialogs = new WeakHashMap<Dialog, Boolean>(2);
        private final Object lock = new Object();
        /**
         * rescheduleOnWakeup. values:
         * 0: no rescheduling event has occured, when timeout occurs and we wake up, examine the application state
         * -1: initial value before first scheduling
         * else: a time value when the most recent rescheduling event occured plus the configured watchdog timeout
         */
        private long rescheduleOnWakeup = -1;
        private final String sessionId;

        public ApplicationWatchdogState(String sessionId) {
            this.sessionId = sessionId;
        }

        public void addConnection(Connection connection) {
            currentConnections.put(connection, Boolean.TRUE);
            reschedule();
        }

        public void setTerminalState(Connection connection) {
            currentConnections.remove(connection);
            terminalConnections.put(connection, Boolean.TRUE);
            reschedule();
        }

        public void removeConnection(Connection connection) {
            currentConnections.remove(connection);
            terminalConnections.remove(connection);
            reschedule();
        }

        public void reschedule() {
            boolean firstReschedule = rescheduleOnWakeup == -1;
            if (firstReschedule) {
                ApplicationWatchdog.instance().timer.schedule(this, ApplicationWatchdog.getTimeout(), TimeUnit.MILLISECONDS);
                rescheduleOnWakeup = 0;
            } else {
                rescheduleOnWakeup = System.currentTimeMillis() + ApplicationWatchdog.getTimeout();
            }
        }

        public void run() {
            log.registerSessionInfo("session", sessionId);
            wakeup();
            log.clearSessionInfo();
        }

        public void wakeup() {
            synchronized (lock) {
                if (!performScheduling()) {
                    if (!isSessionValid()) {
                        //Disable timeout warnings during regular unit testing.
                        //TODO: Make unit testing work without this generic workaround
                        if (!MASTestSwitches.isUnitTesting() || MASTestSwitches.isWatchdogTesting()) {
                            dumpStateOfReachableEngines();
                        }
                    }
                }
            }
        }

        private boolean performScheduling() {
            long reschedule = 0;
            synchronized (lock) {
                if (rescheduleOnWakeup > 0) {
                    reschedule = rescheduleOnWakeup;
                    rescheduleOnWakeup = 0;
                }
            }
            if (reschedule > 0) {
                long delay = reschedule - System.currentTimeMillis();
                instance.timer.schedule(this, delay, TimeUnit.MILLISECONDS);
                return true;
            }
            return false;
        }

        private boolean isSessionValid() {

            // Try to determine if we have any joined active connections, this is
            // a bit tricky. Here we assume it's a valid connection if we have joins
            // between two connections, as it seems unlikely that both connections
            // would encounter an application bug at the same time.

            // We also look for a transferring Dialog at the same time. If the
            // dialog is transferring, the session is valid if we also can find
            // an active bridge. Remember that by now the transferevent should
            // by a long time ago have reached CCXML (which should have set up
            // a bridge.
            if (currentConnections.size() > 0 && dialogs.size() > 0) {
                for (Dialog dialog : dialogs.keySet()) {
                    boolean dialogInTransfer = false;
                    if (dialog.isInTransfer())
                        dialogInTransfer = true;
                    List<Bridge> bridges = dialog.bridgesOf(currentConnections.keySet(), dialogs.keySet());
                    if (bridges != null) {
                        for (Bridge bridge : bridges) {
                            if (bridge.isHomogen(Connection.class))
                                return true;
                        }
                    }
                    if (dialogInTransfer)
                        return false;
                }
            }
            //Check that we have no active connections
            boolean valid = currentConnections.isEmpty();

            //And no connectios in a terminal state
            valid &= terminalConnections.isEmpty();

            //And finally, validate that we doesn't have any
            //dialogs
            valid &= dialogs.isEmpty();

            return valid;
        }

        /**
         * Dump state of all engines we can find using our weak references
         */
        private void dumpStateOfReachableEngines() {
            ApplicationWatchdog.log.warn("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            Set<ExecutionContext> activeContexts = new HashSet<ExecutionContext>();
            for (Dialog dialog : dialogs.keySet()) {
                if (dialog.isLive()) {
                    ExecutionContext ec = dialog.getExecutionContext();
                    activeContexts.add(ec);
                    activeContexts.addAll(ec.getSubContexts());
                }
                
                if ((dialog.getExecutionContext() instanceof ExecutionContextImpl)) {
                    ExecutionContextImpl ec2 = (ExecutionContextImpl)dialog.getExecutionContext();
                   
                    ec2.dumpPendingEvents();
                    ec2.dumpState();

                    printExecutionContext(ec2);
                    
                  }                
                
                if (dialog.getConnectionString () != null) {
                    log.warn( "Connection:" + dialog.getConnectionString ());
                } else {
                    log.warn( "Connection is null ");
                }
                
                
            }
            for (Connection connection : currentConnections.keySet()) {
                activeContexts.add(connection.getExecutionContext());
            }
            for (Connection connection : terminalConnections.keySet()) {
                activeContexts.add(connection.getExecutionContext());
            }
            if (!activeContexts.isEmpty()) {
               
                log.warn("Session " + sessionId
                        + " is still active although nothing interesting have occured in " +
                        ApplicationWatchdog.getTimeout() / 60000 + " minutes");

                for (ExecutionContext ec : activeContexts) {
                    ec.dumpState();
                    ec.shutdown(true);
                }
               
            } else {
                log.warn("Active session " + sessionId + " without any EventSources");
            }
            ApplicationWatchdog.log.warn("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }

        public void printExecutionContext(ExecutionContextImpl ec)
        {
         
          ApplicationWatchdog.log.warn("context id: " + ec.getContextId());
          ApplicationWatchdog.log.warn("context type: " + ec.getContextType());

          Module m = ec.getExecutingModule();

          if (m != null) {
            if (m.getDocumentURI() != null) {
              ApplicationWatchdog.log.warn("getDocumentURI " + m.getDocumentURI());
            }
            GrammarScopeTree gst = m.getASRGrammarTree();
            if (gst != null) {
              if (gst.getCurrent() != null) {
                ApplicationWatchdog.log.warn("ASR scopeTree: " + gst.getCurrent());
              }
              printScopeNode(gst);
            }

            GrammarScopeTree gst2 = m.getDTMFGrammarTree();
            if ((gst2 != null) && 
              (gst2.getCurrent() != null)) {
              ApplicationWatchdog.log.warn("scopeTree: " + gst2.getCurrent());
              printScopeNode(gst2);
            }
          }
          VoiceXMLEventProcessor vep;
          if ((ec.getEventProcessor() instanceof VoiceXMLEventProcessor)) {
            vep = ec.getEventProcessor();
          }

          if ((ec.getExecutingForm() instanceof FormPredicate)) {
            FormPredicate fp = (FormPredicate)ec.getExecutingForm();
            printProduct(fp);
          }

          
        }

        public void printProduct(FormPredicate fp)
        {
          if (fp != null) {
            Product p = fp.getFormItemProduct();
            if ((p != null) && 
              (p.toString() != null)) {
              ApplicationWatchdog.log.warn("product: " + p.toString());
            }

            DebugInfo di = fp.getDebugInfo();
            if ((di != null) && 
              (di.toString() != null)) {
              ApplicationWatchdog.log.warn("debugInfo: " + di.toString());
            }

            List l = fp.getFormItemNames();
            if (l != null)
              for (int i = 0; i < l.size(); i++)
                if (l.get(i) != null) {
                  String str = (String)l.get(i);
                  if (str != null)
                    ApplicationWatchdog.log.warn("product: " + str.toString());
                }
          }
        }

        public void printScopeNode(GrammarScopeTree gst)
        {
          Map<Element, GrammarScopeNode> m = gst.getGrammarNodeHash();

          if (m != null)
            for (Map.Entry<Element, GrammarScopeNode> entry : m.entrySet()) {
              GrammarScopeNode gsn = entry.getValue();
              if ((gsn != null) && 
                (gsn.getDocumentURI() != null))
                ApplicationWatchdog.log.warn("GrammarScopeNode URI " + gsn.getDocumentURI());
            }
        }        
        
        public void removeDialog(Dialog dialog) {
            dialogs.remove(dialog);
            reschedule();
        }

        public void addDialog(Dialog dialog) {
            dialogs.put(dialog, Boolean.TRUE);
            reschedule();
        }
    }


    public static ApplicationWatchdog instance() {
        return instance;
    }

    private ApplicationWatchdogState summon(Id<ISession> id) {
        Map<Id<ISession>, ApplicationWatchdogState> session = sessionMap(lockId(id));
        ApplicationWatchdogState result = session.get(id);
        if (result == null) {
            ApplicationWatchdogState state = new ApplicationWatchdogState(id.toString());
            session.put(id, state);
            result = state;
        }
        return result;
    }

    public void signalConnectionInTerminalState(Id<ISession> id, Connection connection) {
        synchronized (lock(id)) {
            ApplicationWatchdogState state = summon(id);
            state.setTerminalState(connection);
        }
    }

    public void signalNewConnection(Id<ISession> id, Connection connection) {
        synchronized (lock(id)) {
            ApplicationWatchdogState state = summon(id);
            state.addConnection(connection);
        }
    }

    public void signalConnectionStateChanging(Id<ISession> identity, Connection connection) {
        synchronized (lock(identity)) {
            ApplicationWatchdogState state = summon(identity);
            state.reschedule();
        }
    }

    public void signalInputEvent(Id<ISession> identity) {
        synchronized (lock(identity)) {
            ApplicationWatchdogState state = summon(identity);
            state.reschedule();
        }
    }

    private int lockId(Id<ISession> identity) {
        return identity.hashCode() % LOCK_COUNT;
    }

    private Object lock(Id<ISession> identity) {
        return locks[lockId(identity)];
    }

    private Object lock(int id) {
        return locks[id];
    }

    private Map<Id<ISession>, ApplicationWatchdogState> sessionMap(int id) {
        return sessions.get(id);
    }

    public void signalConnectionCleanedUp(Connection connection, Id<ISession> id) {
        synchronized (lock(id)) {
            ApplicationWatchdogState state = summon(id);
            state.removeConnection(connection);
        }
    }

    public void signalDialogShutdown(Id<ISession> id, Dialog dialog) {
        synchronized (lock(id)) {
            ApplicationWatchdogState state = summon(id);
            state.removeDialog(dialog);
        }
    }

    public void signalNewDialog(Dialog dialog, Id<ISession> id) {
        synchronized (lock(id)) {
            ApplicationWatchdogState state = summon(id);
            state.addDialog(dialog);
        }
    }

    public static int getTimeout() {
        return configuredTimeout;
    }

    public static void setTimeout(int timeout) {
        if (MASTestSwitches.isWatchdogTesting()) {
            timeout = 3000;
            log.debug("Watchdog testing enabled, setting timeout to " + timeout / 1000 + "s");
        }
        configuredTimeout = timeout;
    }

}
