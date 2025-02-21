/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.ccxml.*;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.util.Tools;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class EventSourceManagerImpl implements EventSourceManager {

    private ISession session;

    private static ILogger log = ILoggerFactory.getILogger(EventSourceManagerImpl.class);

    // This is the CCXML interpreters executioncontext !!!
    private CCXMLExecutionContext executionContext;

    private ExternalEventTranslator eventTranslator = new ExternalEventTranslator(this);

    private HashMap<String, Connection> connectionsById = new HashMap<String, Connection>();
    private HashMap<String, Dialog> dialogsById = new HashMap<String, Dialog>();

    private Map<Call, Connection> connectionsByLeg = new IdentityHashMap<Call, Connection>();
    private CallManager callManager;

    private IEventDispatcher eventDispatcher;
    private Map<Bridge.Key, Bridge> bridges = new HashMap<Bridge.Key, Bridge>();
    private Connection initiatingConnection;

    public EventSourceManagerImpl() {
    }

    public boolean isLive() {

        if (initiatingConnection != null && ! initiatingConnection.isInTerminalState())
            return true;

        for (Map.Entry<Call, Connection>entry : connectionsByLeg.entrySet()) {
            if (!entry.getValue().isInTerminalState())
                return true;
        }

        for (Map.Entry<String, Dialog> dialog : dialogsById.entrySet()) {
            if (dialog.getValue().isLive())
                return true;
        }
        return false;
    }

    private static Connection createDummyConnection(final String id) {
        return (Connection) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{Connection.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        StringBuilder sb = new StringBuilder(96);
                        sb.append("Invocation of ");
                        sb.append(method.getName());
                        sb.append("( ");
                        if(args!= null)
                            Tools.commaSeparate(sb, args);
                        sb.append(" ) on unknown connection/session: ");
                        sb.append(id);
                        return null;
                    }
                });
    }

    public Dialog findDialog(String dialogID) {
        if (dialogID == null) return null;
        else
            return dialogsById.get(dialogID);
    }

    public Connection findConnection(String connectionID) {
        if (connectionID == null) return createDummyConnection(connectionID);
        else
            return connectionsById.get(connectionID);
    }

    public Connection findConnectionByCall(Call leg) {
        return connectionsByLeg.get(leg);
    }

    public String getConnectionByLegAsString () {
        if (connectionsByLeg != null) {
            return connectionsByLeg.toString();
        } else {
            return null;
        }
    }
    
    public Connection safeFindConnection(String connectionID) {
        if (connectionID == null) return createDummyConnection(connectionID);
        else {
            Connection c = connectionsById.get(connectionID);
            if (c == null)
                c = createDummyConnection("ConnectionID=" + connectionID);
            return c;
        }
    }
    

    public Connection safeFindConnectionByCall(Call leg) {
        Connection c = connectionsByLeg.get(leg);
        if (c == null)
            c = createDummyConnection("SessionID=" + leg.getSession().getId());
        return c;
    }

    /**
     * @param leg
     * @param outbound
     * @return
     * @logs.error "No reference to a call leg supplied" - This is an internal error/bug
     */
    public Connection createConnection(Call leg, boolean outbound) {
        if (outbound) {
            // leg is likely null here

            ConnectionImpl conn = new ConnectionImpl(executionContext);
            rememberConnection(null, conn);
            return conn;
        } else {
            if (leg == null) {
                log.error("No reference to a call leg supplied");
                return null;
            } else {
                Connection conn = new ConnectionImpl(executionContext);
                conn.setCall(leg);
                rememberConnection(leg, conn);
                return conn;
            }
        }
    }

    public void placeCall(Connection connection, CallProperties callProperties) {
        EventStream stream = executionContext.getEventStream();
        EventStream.Injector injector = stream.new EventReceiverInjector(EventRules.FALSE_RULE, EventRules.TRUE_RULE, eventDispatcher);
        stream.add(injector);

        OutboundCall call = callManager.createCall(callProperties, eventDispatcher, session);
        connection.setCall(call);
        rememberConnection(call, connection);
    }

    private void rememberConnection(Call leg, Connection conn) {
        if (leg != null) connectionsByLeg.put(leg, conn);
        connectionsById.put(conn.getBridgePartyId(), conn);
        if (initiatingConnection == null) {
            initiatingConnection = conn;
        }
        ApplicationWatchdog.instance().signalNewConnection(executionContext.getSession().getIdentity(),conn);
    }

    public void doEvent(Event event) {
        eventTranslator.translate(event);
    }

    public void doGlobalEvent(Event event) {
        if (log.isDebugEnabled()) log.debug("Unknown global event ignored, the event was " + event);
        //No known global events to handle yet
    }

    public CCXMLExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void shutdownAll() {
        for (Connection c : connectionsById.values()) {
            switch (c.getState()) {
                case DISCONNECTED:
                case START:
                case FAILED:
                    // Nothing needs to be done
                    if (log.isDebugEnabled())
                        log.debug("Connection " + c + "is in state " + c.getState() + ", " + "no action");
                    break;
                default:
                    if (log.isDebugEnabled())
                        log.debug("Connection " + c + "is in state " + c.getState() + ", " + "forced disconnect");
                    c.forcedDisconnect();
                    break;
            }
            c.cleanup();
        }
        for (Dialog d : dialogsById.values()) {
            d.shutdown();
        }
    }

    public void setOwner(CCXMLExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setSession(ISession session) {
        this.session = session;
    }


    public void join(Bridge bridge, boolean implicit) {

        if ((bridge.getIn() instanceof Connection) && (bridge.getOut() instanceof Connection)) {
            signalJoin(bridge);
            Connection connectionIn = (Connection) bridge.getIn();
            Connection connectionOut = (Connection) bridge.getOut();
            rememberBridge(bridge);
            callManager.join(connectionIn.getCall(), connectionOut.getCall(), eventDispatcher);
        } else if ((bridge.getIn() instanceof Dialog) && (bridge.getOut() instanceof Dialog)) {
            generateErrorSemanticEvent("Can not join two dialogs");
        } else {
            signalJoin(bridge);
            rememberBridge(bridge);
            if (implicit) {
                if(log.isDebugEnabled())
                    log.debug("Implicit join: no " + Constants.Event.CONFERENCE_JOINED +
                            " event generated");
            } else {
                generateJoinedEvent(bridge);
            }
        }
    }

    private void signalJoin(Bridge bridge) {
        if(bridge.getIn() instanceof Connection) {
            Connection conn = (Connection)bridge.getIn();
            ApplicationWatchdog.instance().signalConnectionStateChanging(conn.getExecutionContext().getSession().getIdentity(),conn);
        }
        if(bridge.getOut() instanceof Connection) {
            Connection conn = (Connection)bridge.getOut();
            ApplicationWatchdog.instance().signalConnectionStateChanging(conn.getExecutionContext().getSession().getIdentity(),conn);
        }
    }

    private void generateJoinedEvent(Bridge bridge) {
        // manually generate an event to the application in this case

        if (log.isDebugEnabled()) {
            log.debug("Manually generating " + Constants.Event.CONFERENCE_JOINED);
        }
        BridgeParty.EventSender e = ConnectionImpl.createEventSender(executionContext);
        final Connection conn = executionContext.getCurrentConnection();
        CCXMLEvent joinedEvent = CCXMLEvent.create(Constants.Event.CONFERENCE_JOINED,
                executionContext, conn, DebugInfo.getInstance(), null);
        joinedEvent.defineJoinRelated(bridge);
        joinedEvent.defineTarget("ccxml",conn.getExecutionContext().getContextId());
        e.sendEvent(joinedEvent);
    }

    private void generateErrorSemanticEvent(String message) {
        BridgeParty.EventSender e = ConnectionImpl.createEventSender(executionContext);
        CCXMLEvent joinedEvent = CCXMLEvent.create(Constants.Event.ERROR_SEMANTIC, message,
                executionContext, executionContext.getCurrentConnection(), DebugInfo.getInstance());
        e.sendEvent(joinedEvent);

    }

    public Bridge findBridge(BridgeParty in, BridgeParty out) {
        Bridge.Key key = Bridge.createKey(in, out);
        return bridges.get(key);
    }

    public BridgeParty findParty(String bridgePartyId) {
        return connectionsById.get(bridgePartyId);
    }

    private void rememberBridge(Bridge bridge) {
        Bridge.Key key = bridge.key();
        bridges.put(key, bridge);
    }

    public void forgetBridge(Bridge bridge) {
        Bridge.Key key = bridge.key();
        bridges.remove(key);
    }

    /**
     * Returns the set of bridges related to the given sets of
     * dialogs, and connctions.
     * This naïve implementation works under the assumption that
     * it will rarely be called. This might not be true if the system
     * has massive amount of long-lived connections joined to eachother
     * with no dialog interactions.
     * An example of this would be conference calls.
     * @param conns
     * @param dlgs
     * @return A set of related, and active bridges
     */
    public List<Bridge> bridgesOf(Set<? super Connection> conns, Set<? super Dialog> dlgs) {
        List<Bridge> activeBridges = null;
        for (Bridge bridge:bridges.values()) {
            BridgeParty in = bridge.getIn();
            BridgeParty out = bridge.getOut();

            if(conns.contains(in) || dlgs.contains(in)||conns.contains(out) || dlgs.contains(out)) {
                if(activeBridges == null) {
                    activeBridges = new ArrayList<Bridge>();
                }
                activeBridges.add(bridge);
            }
        }
        if(activeBridges != null && activeBridges.isEmpty())
            activeBridges = null;
        return activeBridges;
    }

    public void unjoin(Bridge bridge) {
        signalJoin(bridge);
        if (bridge.getIn() instanceof Connection) {
            if (bridge.getOut() instanceof Connection) {
                Connection connectionIn = (Connection) bridge.getIn();
                Connection connectionOut = (Connection) bridge.getOut();
                callManager.unjoin(connectionIn.getCall(), connectionOut.getCall(), eventDispatcher);
                return;
            }
            if (bridge.getOut() instanceof Dialog) {
                forgetBridge(bridge);
            }
        }
    }

    public void setCallManager
            (CallManager
                    callManager) {
        this.callManager = callManager;
    }

    public void setEventDispatcher
            (IEventDispatcher
                    dispatcher) {
        this.eventDispatcher = dispatcher;
    }

    private void addDialog(Dialog dialog) {
        dialogsById.put(dialog.getBridgePartyId(), dialog);
    }

    public Dialog createDialog(String src, String mimeType) {
        Dialog dialog = new Dialog(IdGeneratorImpl.PARTY_GENERATOR.generateId(), src, mimeType, executionContext);
        addDialog(dialog);
        ApplicationWatchdog.instance().signalNewDialog(dialog, session.getIdentity());
        return dialog;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void logInvalidEvent(Object ... params) {
        StringBuilder sb = new StringBuilder(96);
        sb.append("Invalid event translation attempted: ");
        Tools.commaSeparate(sb, params);
        log.warn(sb.toString());
    }

    public Connection getInitiatingConnection() {
        return initiatingConnection;
    }

    public CallManager getCallManager() {
        return callManager;
    }

}

