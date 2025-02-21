/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Looberger
 */
public class SendDialogEvent extends VXMLOperationBase {
    ILogger logger = ILoggerFactory.getILogger(SendDialogEvent.class);

    private final String event;
    private final String message;
    private final DialogEventFactory factory;
    private static final Map<String, DialogEventFactory> factories = new HashMap<String, DialogEventFactory>();


    static {
        DialogEventFactory dialog = new DialogEventFactory();
        factories.put(Constants.Event.DIALOG_EXIT, dialog);
        factories.put(Constants.Event.DIALOG_STARTED,dialog);
        factories.put(Constants.Event.DIALOGDISCONNECT, dialog);
    }

    public SendDialogEvent(String event, String message) {
        this.event = event;
        this.message = message;
        factory = factories.get(event);


    }

    public String arguments() {
        return event + ", " + message;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (factory != null) {
            CCXMLEvent ev = factory.create(ex, event, message, ex.getCurrentConnection(), ex.getDialog(), null);
            ex.getEventHub().fireEvent(ev);
            if (logger.isDebugEnabled()) logger.debug("Event " + ev + " sent");
        } else {
            if (logger.isDebugEnabled()) logger.debug("No factory for creating " + event + " events");
        }
    }
}
