package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferCompleteEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class SetTransferVariables extends VXMLOperationBase {

    private static final ILogger logger = ILoggerFactory.getILogger(SetTransferVariables.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        String formItemName = ex.getFIAState().getNextItem().name;
        Event event = ex.getEventEntry().getEvent();
        Event related;

        if (event != null
                && event instanceof SimpleEvent
                && (related = ((SimpleEvent) event).getRelated()) != null
                && related instanceof DialogTransferCompleteEvent) {

            DialogTransferCompleteEvent completeEvent = (DialogTransferCompleteEvent) related;
            Scope currentScope = ex.getCurrentScope();

            String reason = completeEvent.getReason();

            if (reason == null) {
                currentScope.setValue(formItemName, ex.getCurrentScope().getUndefined());
                if (logger.isDebugEnabled()) {
                    logger.debug("set it to undefined");
                }
            } else {
                currentScope.setValue(formItemName, reason);
                if (logger.isDebugEnabled()) {
                    logger.debug("set it to:" + reason);
                }
            }
        }
    }

    public String arguments() {
        return "";
    }
}
