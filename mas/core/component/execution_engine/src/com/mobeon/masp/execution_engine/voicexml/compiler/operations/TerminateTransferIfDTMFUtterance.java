package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 29, 2006
 * Time: 11:37:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class TerminateTransferIfDTMFUtterance extends VXMLOperationBase {

    private static ILogger log = ILoggerFactory.getILogger(TerminateTransferIfDTMFUtterance.class);

    public TerminateTransferIfDTMFUtterance() {

    }
    public String arguments() {
        return "";

    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if ( ! ex.getFIAState().matchedGrammar()){
            if(log.isDebugEnabled()){
                log.debug("Does NOT terminate transfer, due to no match in grammar.");
            }
            if(log.isDebugEnabled())log.debug("Setting engine state to "+ExecutionResult.EVENT_WAIT);            
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            return;
        }

        // There is a match, upcoming transfer shall not be started.

        if(ex.getTransferState().getCallState() == TransferState.CallState.ONGOING){

            // Should never happen, since this event handler is used _only_ before transfer. let's
            // make best effort.

            log.debug("Matching utterance while transfer ongoing. This should never happen.");
            DialogEventFactory factory = new DialogEventFactory();
            CCXMLEvent event = factory.create(
                    ex,
                    Constants.Event.TERMINATE_TRANSFER,
                    "Not found",
                    ex.getCurrentConnection(),
                    ex.getDialog(),
                    DebugInfo.getInstance());
            ex.getEventHub().fireEvent(event);
        } else {
            if(log.isDebugEnabled()){
                log.debug("Matching utterance before transfer started");
            }
            ex.getTransferState().setCallState(TransferState.CallState.FINALIZING);
            String formItemName = ex.getFIAState().getNextItem().name;
            ex.getCurrentScope().setValue(formItemName, Constants.VoiceXML.NEAR_END_DISCONNECT);
        }
    }
}
