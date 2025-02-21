/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.delayed.DelayedEventImpl;
import com.mobeon.masp.execution_engine.runtime.event.delayed.DelayedEventProcessor;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author Mikael Andersson
 */
public abstract class EventHubImpl implements EventHub {
    public static final ILogger log = ILoggerFactory.getILogger(EventHubImpl.class);

    private ExecutionContext executionContext;
    private EventStream.Injector injector;

    private static final DelayedEventProcessor processor;

    static {
        processor = DelayedEventProcessor.start();
    }

    public void fireEvent(Event event) {
        if (log.isInfoEnabled()) log.info("Sending event " + event);
        injector.inject(event);
    }


    public void fireEvent(final SimpleEvent ev, int delay) {
        if (delay == 0) {
            fireEvent(ev);
        } else {
            if (delay > 60000) {
                if (! MASTestSwitches.isUnitTesting()) {
                    log.warn("<CHECKOUT> Delaying event " + delay / 60000 + " minutes ! Is this intentional ?");
                }
            }
            if (log.isDebugEnabled()) {
                processor.logState();
            }
            processor.sendDelayedEvent(new DelayedEventImpl(delay, ev, this));
        }
    }

    public void fireContextEvent(final SimpleEvent ev, int delay) {
        if(ev.getTargetType() == null)
            ev.defineTarget("context", executionContext.getContextId());
        fireEvent(ev, delay);
    }

    /**
     * Fire event without targetId, and targetType
     *
     * @param event
     * @param debugInfo
     */
    public void fireEvent(String event, DebugInfo debugInfo) {
        fireEvent(event, null, debugInfo);
    }

    public void fireEvent(String event, String message, DebugInfo debugInfo) {
        SimpleEvent ev = createEvent(event, debugInfo, message);
        fireEvent(ev);
    }

    public void fireContextEvent(String event, String msg, DebugInfo debugInfo) {
        SimpleEvent ev = createEvent(event, debugInfo, msg);
        fireContextEvent(ev);
    }

    public void fireContextEventWithLocationInfo(String event, String msg, DebugInfo debugInfo) {
        String tagName = debugInfo.getTagName();
        Object location = debugInfo.getLocation();
        String msgWithLocation = msg + " Occured at: " + executionContext.getExecutingModule().getDocumentURI() + ", tag <" + tagName + ">" +
                ". Line and column:" + location;
        SimpleEvent ev = createEvent(event, debugInfo, msgWithLocation);
        fireContextEvent(ev);
    }

    public void fireContextEvent(SimpleEvent event) {
        if(event.getTargetType() == null)
            event.defineTarget("context", executionContext.getContextId());
        fireEvent(event);
    }

    public void fireContextEvent(String event, DebugInfo debugInfo) {
        fireContextEvent(event, null, debugInfo);
    }

    protected SimpleEvent createEvent(String event, DebugInfo debugInfo, String message) {
        SimpleEvent ev = new SimpleEventImpl(event, message, debugInfo);
        ev.setExecutingURI(executionContext.getExecutingModule().getDocumentURI());
        return ev;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void close() {
        injector.close();
    }

    public void set(EventStream.Injector injector) {
        this.injector = injector;
    }

    public void setOwner(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        onOwnerSet(executionContext);
    }

    private void onOwnerSet(ExecutionContext executionContext) {//Add e generic injector
        EventStream.Injector injector = executionContext.getEventStream().new Injector(EventRules.TRUE_RULE, EventRules.TRUE_RULE);
        set(injector);
    }

    public void cancel(final String sendid, Object related) {
        if (log.isDebugEnabled()) {
            log.debug("Cancelling delayed event: " + sendid);
        }
        processor.cancel(sendid);
    }
}
