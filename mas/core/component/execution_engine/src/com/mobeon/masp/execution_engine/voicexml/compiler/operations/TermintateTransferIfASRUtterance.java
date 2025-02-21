package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class TermintateTransferIfASRUtterance extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(TermintateTransferIfASRUtterance.class);
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        String utterance = ex.getFIAState().getASRUtterance(ex.getASR_grammar());

        if (utterance != null) {
            log.debug("Does NOT terminate transfer, due to no match in ASR grammar.");
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            return;
        }

        // There is a match, which means either that ongoing gransfer
        // shall be cancelled, or upcoming transfer shall not be started.

        if (ex.getTransferState().getCallState() == TransferState.CallState.ONGOING) {
            log.debug("Matching utterance while transfer ongoing");
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
            if (log.isDebugEnabled()) {
                log.debug("Matching utterance before transfer started");
            }
            ex.getTransferState().setCallState(TransferState.CallState.FINALIZING);
            String formItemName = ex.getFIAState().getNextItem().name;
            ex.getCurrentScope().setValue(formItemName, Constants.VoiceXML.NEAR_END_DISCONNECT);
        }

        ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
    }

    public String arguments() {
        return "";
    }
}
