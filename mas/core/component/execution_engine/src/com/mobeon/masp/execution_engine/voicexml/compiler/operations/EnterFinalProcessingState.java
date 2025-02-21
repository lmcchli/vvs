/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class EnterFinalProcessingState extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(EnterFinalProcessingState.class);
    private boolean force;

    public EnterFinalProcessingState(boolean force) {
        this.force = force;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        final EventProcessor.Entry entry = ex.getEventEntry();
        if(ex.getFinalProcessingState())
            return;

        boolean enterFinalProcessing = force;

        if (entry != null) {
            final Event event = entry.getEvent();
            if (event instanceof SimpleEvent && ((SimpleEvent) event).getEvent().startsWith(Constants.Event.CONNECTION_DISCONNECT)) {
                enterFinalProcessing = true;
            }
        } else {
            enterFinalProcessing = true;
        }
        if(enterFinalProcessing) {
            if (log.isInfoEnabled()) log.info("Entering final processing state");            
            ex.setFinalProcessingState(true);
            ex.getNoInputSender().cancelAndLock();
            ex.getMediaTranslator().cancel();
        }
    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
