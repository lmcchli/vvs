package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.ShadowVarBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class TerminateRecordIfASRUtterance extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(TerminateRecordIfASRUtterance.class);
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        String utterance = ex.getFIAState().getASRUtterance(ex.getASR_grammar());
        if (utterance == null )
        {
            if (log.isDebugEnabled()) log.debug("Does NOT terminate record, due to no ASR match.");
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            return;
        }
        // Might be playing a prompt in the record. If so, cancel the prompt
        if (ex.getCall().getIsPlaying() && utterance != null) {
            if (log.isDebugEnabled()) log.debug("Record bargein, but is playing prompts. Stopping ongoing play");
            final FIAState state = ex.getFIAState();
            state.getMarkInfo(ex);
            state.inputReceived(ex);
            if (!ex.getCall().stopPlay()) {
                if (log.isInfoEnabled()) log.info("Failed to stop play on outbound stream");
            }
            PromptQueue(ex).setAbortPrompts(true);
            ex.getFIAState().setInhibitRecording(true);
            ex.setExecutionResult(ExecutionResult.DEFAULT);
            return;
        }

        // Verify that the record is still running
        if (!ex.getCall().getIsRecording()) {
            if (log.isDebugEnabled()) log.debug("Not recording.");
            ex.getFIAState().setInhibitRecording(true);
            ex.setExecutionResult(ExecutionResult.DEFAULT);
            return;
        }


        String formItemName = ex.getFIAState().getNextItem().name;
        if (scope.isDeclaredInExactlyThisScope(formItemName) &&
                scope.evaluate(formItemName) != scope.getUndefined()) {
            if (log.isDebugEnabled()) log.debug("FormItem " + formItemName + " already has a value");
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            return;
        }

        if (formItemName != null && utterance != null) {

            ShadowVarBase shadow = new ShadowVarBase();
            String shadowName = formItemName + "$";
            Object wrappedEcmaObject = scope.javaToJS(shadow);
            if (! scope.isDeclaredInExactlyThisScope(shadowName)) {
                scope.declareReadOnlyVariable(shadowName, wrappedEcmaObject);
            } else {
                scope.setValue(shadowName, wrappedEcmaObject);
            }
            if (!ex.getCall().stopRecord()) {
                if (log.isInfoEnabled()) log.info("Failed to stop recording on inbound stream");
            }

        }
        ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
    }

    public String arguments() {
        return "";
    }
}
