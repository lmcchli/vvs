/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2007.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.EventHandler;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for event processors implementing a reasonable default strategy for
 * event processing.
 * <p/>
 * The default strategy is to ensure that for each recieved event, an engine is
 * available, and running, after delivering the event to our event queue.
 */
public abstract class EventProcessorBase implements EventProcessor {


    private ILogger log = ILoggerFactory.getILogger(EventProcessorBase.class);
    private ILogger runnerLog = ILoggerFactory.getILogger(EngineRunner.class);

    private final EngineRunner runner = new EngineRunner();
    private final Object lock = new Object();

    // The eventQueue is created by the subclasses
    protected EventQueue<SimpleEvent> eventQueue = null;

    private Entry eventEntry;

    private ConcurrentLinkedQueue<Event> externalEventQueue = new ConcurrentLinkedQueue<Event>();

    private AtomicBoolean enabled = new AtomicBoolean(false);

    private boolean stopped = false;

    private ExecutorService exec;
    private List<Detour> detours = new ArrayList<Detour>();
    private ExecutionContext executionContext;

    protected EventProcessorBase() {
        exec = ExecutorServiceManager.getInstance().getExecutorService(EventProcessorBase.class);

    }

    abstract protected void onSetExecutionContext();

    public class EngineRunner implements Runnable {
        private boolean dying = true;
        private Semaphore notification = new Semaphore(0);

        public boolean handleInterruption(ExecutionContext context) {
            synchronized (lock) {
                //Clear interrupted here
                Thread.interrupted();
                if (stopped) {
                    dying = true;
                    context.wasInterrupted();
                    return true;
                }
                return false;
            }
        }

        public void run() {
            getExecutionContext().getSession().registerSessionInLogger();
            if (runnerLog.isDebugEnabled())
                runnerLog.debug(getContextType() + "Alive !");
            try {
                boolean acquired = false;
                ExecutionContext context = getExecutionContext();
                do {
                    try {
                        if (!Thread.currentThread().isInterrupted()) {
                            notification.acquire();
                            acquired = true;
                        } else {
                            if (handleInterruption(context))
                                return;
                        }
                    } catch (InterruptedException e) {
                        if (handleInterruption(context))
                            return;
                    }
                } while (!acquired);
                if (runnerLog.isDebugEnabled())
                    runnerLog.debug(getContextType() + "Prepared to execute: " + noNPEs(context));

                do {
                    if (context != null && !context.isInterrupted()) {
                        Engine engine = context.getEngine();
                        boolean run = false;
                        synchronized (lock) {
                            processExternalEvents();
                            run = engine != null && (engine.needsToRun());
                        }
                        if (run) {
                            engine.executeNext();
                            if(engine.getWasStopped()) engine.releaseToPool();
                        }
                    } else {
                        if (runnerLog.isDebugEnabled())
                            runnerLog.debug(getContextType() + "Interrupted status in Engine was set !");
                    }
                } while (!isStopped() && tryAquire());
            } catch (Throwable t) {

                // TODO: This is a fix for the stack trace of the exception not being avaible in the logfile.
                String trace = "";
                if (t.getStackTrace() != null && runnerLog.isDebugEnabled()) {
                    for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                        trace += stackTraceElement.toString() + "\n";
                    }
                }
                if (runnerLog.isDebugEnabled()) runnerLog.debug(getContextType() + "Uncaught exception: ", t);
                if (runnerLog.isDebugEnabled())
                    runnerLog.debug(getContextType() + "Stacktrace for exception: " + trace);
            }
        }


        private Object noNPEs(ExecutionContext executionContext) {
            try {
                return executionContext.getExecutingModule().getDocumentURI();
            } catch (Exception e) {
                return null;
            }
        }

        private boolean tryAquire() {
            synchronized (lock) {
                if (notification.tryAcquire()) {
                    return true;
                } else {
                    if (runnerLog.isDebugEnabled()) runnerLog.debug(getContextType() + "Dying");
                    dying = true;
                    return false;
                }
            }
        }

        private boolean isStopped() {
            synchronized (lock) {
                if (stopped) dying = true;
                return stopped;
            }
        }

        public void reanimate() {
            synchronized (lock) {
                dying = false;
            }
        }
    }

    public void runAsNeeded() {
        final boolean isDebug = log.isDebugEnabled();
        final boolean wasStopped;
        final boolean wasDying;
        do {
            synchronized (lock) {
                wasStopped = stopped;
                wasDying = runner.dying;
                if (wasStopped) {
                    break;
                }
                if (wasDying) {
                    // Point of no return passed, we can pick any thread and
                    // start executing this runnable again. But first we
                    // reanimate this runner.
                    runner.reanimate();
                    exec.execute(runner);

                }
                runner.notification.release();
            }
        } while (false);
        if (isDebug) {
            if (wasStopped) {
                log.trace(getContextType() + "Runnner is stopped, and won't run.");
            } else {
                if (wasDying) {
                    log.trace(getContextType() + "Runner is dead or dying, Reanimate called.");
                } else {
                    log.trace(getContextType() + "Runner is alive and will examine queue before exiting.");
                }
                log.trace(getContextType() + "Runner notification released.");
            }
        }

    }

    /**
     * Note! injectEvent may only be called from a detour. if you call inject you
     * must return true from detour().
     *
     * @param event The event to inject.
     * @logs.warn "<Context type>: event queue was not empty when inject ran ! ! Event queue is: <eventEntry>, event to inject is: <event>" - At the time execution engine was about to insert an event into a queue, the queue was expected to be empty, but it was not. <Context type> is either VXML or CCXML.
     */
    public void injectEvent(Event event) {
        if (eventEntry != null) {
            log.warn(getContextType() + ": event entry was not null when inject ran ! Event entry is: " + eventEntry + ", event to inject is: " + event);
        } else {
            eventEntry = new Entry(event, null);
        }
    }

    public void write(Event event, EventHandler eh) {
        Entry entry = new Entry(event, eh);
        eventEntry = entry;
        if (log.isDebugEnabled()) log.debug(getContextType() + "Offered : " + entry);
    }

    public Event readEvent() {
        return eventQueue.poll();
    }

    //Called from run so should be synchronized ( enabled status ).
    public boolean hasEventsInQ() {
        synchronized (lock) {
            if (isEnabled()) {
                return (eventQueue.size() > 0 || eventEntry != null);
            } else
                return false;
        }
    }

    public Entry poll() {
        if (isEnabled()) {
            if (eventEntry == null) {
                Event event = readEvent();
                if (event != null && event instanceof SimpleEvent) {
                    SimpleEvent realEvent = (SimpleEvent) event;
                    String state = getExecutionContext().getState();
                    EventHandler eh = getExecutionContext().getHandlerLocator()
                            .locateEventHandler(state, realEvent.getEvent(), getExecutionContext());
                    if (eh != null) {
                        write(event, eh);
                    } else {
                        noHandler(realEvent);
                    }
                }
            }
            Entry toReturn = eventEntry;
            eventEntry = null;
            return toReturn;
        } else {
            if (log.isTraceEnabled()) log.trace(getContextType() + "Poll: Events is disabled");
            return null;
        }
    }

    /**
     * @param realEvent The real event.
     * @logs.warn "No handler found for event: <realEvent>" - No handler wa found for an internally sent event
     */
    public void noHandler(SimpleEvent realEvent) {
        // TODO Temporary workaround for TR 29574, which causes
        // "No handler found for event: CCXMLEvent{ event=play.failed" to flood the log file
        String event = realEvent.getEvent();
        if(event != null && event.equals(Constants.Event.PLAY_FAILED))
            return;
        // Workaround for TR 30674     
        if(event != null && event.equals(Constants.Event.DTMF_WAKEUP_EVENT))
            return;
        log.warn(getContextType() + "No handler found for event: " + realEvent);
    }


    public void setEnabled(boolean enabled) {
        if (this.enabled.get() == enabled) return;
        if (log.isTraceEnabled()) {
            if (enabled) {
                log.trace(getContextType() + "Events enabled");
            } else {
                log.trace(getContextType() + "Events disabled");
            }
        }
        this.enabled.set(enabled);
    }

    public void processEvent(Event event) {
        ExecutionContext ec = getExecutionContext();
        callSimpleEvent(event);

        for (Detour detour : detours) {
            if (detour.detourEvent(event)) {
                if (event instanceof SimpleEvent) {
                    if (log.isInfoEnabled())
                        log.info(getContextType() + " detouring event " + ((SimpleEvent) event).getEvent());
                }
                return;
            }
        }
        if (event instanceof SimpleEvent) {
            eventQueue.offer((SimpleEvent) event);
            if (log.isInfoEnabled())
                log.info(getContextType() + "Event " + ((SimpleEvent) event).getEvent() + " put on queue");
        }
    }

    public void processExternalEvents() {

        if(isEnabled()){
            Event event = externalEventQueue.poll();
            while (event != null) {
                eventEntry = null;
                processEvent(event);
                if (eventEntry != null) {
                    // A new event entry is prepared for the execution context, this is it for now
                    break;
                } else {
                    event = externalEventQueue.poll();
                }
            }
        }
    }

    public void doEvent(Event event) {
        externalEventQueue.offer(event);
        if (log.isDebugEnabled()) {
            log.debug(getContextType() + "Event " + event + " put on external event queue, calling runAsNeeded ! Queue size: " + externalEventQueue.size());
        }
        runAsNeeded();
    }

    /**
     * @param event
     * @logs.error "Uncaught exception when calling SimpleEvent" - An internal event was invoked but it resulted in a thrown exception. Execution continues.
     */
    private void callSimpleEvent(Event event) {
        if (event instanceof SimpleEvent) {
            SimpleEvent e = (SimpleEvent) event;
            try {
                e.call();
            } catch (Throwable t) {
                log.error(getContextType() + "Uncaught exception when calling SimpleEvent", t);
            }
        }
    }

    public void addDetour(Detour detour) {
        detours.add(detour);
    }

    public void removeDetour(Detour detour) {
        detours.remove(detour);
    }

    public void doGlobalEvent(Event event) {
        //TODO: Tom implementation
    }

    public boolean isStopped() {
        return stopped;
    }

    final public boolean isEnabled() {
        return enabled.get();
    }

    public void stop() {
        stopped = true;
    }

    public EventQueue<SimpleEvent>getQueue() {
        return eventQueue;
    }

    public Queue<Event>getExternalEventQueue() {
        return externalEventQueue;
    }


    protected String getContextType() {
        if (getExecutionContext() != null) return "(" + getExecutionContext().getContextType() + ") ";
        else {
            return "(NO EXECUTION CONTEXT!!) ";
        }
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        onSetExecutionContext();
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void dumpPendingEvents() {
        Object[] events = eventQueue.getEvents();
        for (Object o : events)
            if (log.isInfoEnabled()) log.info(o);
    }

}
