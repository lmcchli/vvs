/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class AddEventHandler  extends VXMLOperationBase   {
    private final String event;
    private final Predicate eventHandler;
    boolean setEventsEnabled;
    private static final ILogger log = ILoggerFactory.getILogger(AddEventHandler.class);

    public AddEventHandler(String event, Predicate eventHandler, boolean setEventsEnabled) {
        this.event = event;
        this.eventHandler = eventHandler;
        this.setEventsEnabled = setEventsEnabled;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (log.isDebugEnabled()) log.debug("Adding event handler for " + event + " (" + eventHandler + ")");
        ex.getHandlerLocator().addEventHandler(eventHandler, event);
        ex.getEventProcessor().setEnabled(setEventsEnabled);
    }

    public String arguments() {
        return event+", "+eventHandler+", "+setEventsEnabled;
    }

    public String getEvent() {
        return event;
    }
}
