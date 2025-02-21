/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
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
public class TerminateRecordIfDTMFUtterance extends VXMLOperationBase {
    private static ILogger log = ILoggerFactory.getILogger(TerminateRecordIfDTMFUtterance.class);
    public TerminateRecordIfDTMFUtterance() {
        super();
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        String utterance = ex.getUtterance();
        if ( ! ex.getFIAState().matchedGrammar()){
            if (log.isDebugEnabled()) log.debug("Does NOT terminate record, due to no match in grammar.");
            if(ex.getFIAState().isNoMatch()){
                if (log.isDebugEnabled()) log.debug("Utterance is cleared since this is a nomatch.");
                ex.getFIAState().clearUtterance();
            }
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            return;
        }
        // Might be playing a prompt in the record. If so, cancel the prompt
        /*if (ex.getCall().getIsPlaying() && utterance != null) {
            if (log.isDebugEnabled()) log.debug("Record bargein, but is playing prompts. Stopping ongoing play");
            final FIAState state = ex.getFIAState();
            state.getMarkInfo(ex);
            state.inputReceived(ex);
            if (ex.getCall().stopPlay()) {
                // Need to wait for the playFinished now
                ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            }else {
                if (log.isInfoEnabled()) log.info("Failed to stop play on outbound stream");
            }
            PromptQueue(ex).setAbortPrompts(true);
            ex.getFIAState().setInhibitRecording(true);
            return;
        }*/

        if(log.isDebugEnabled())
            log.debug("Terminating record due to");
        ex.getFIAState().setInhibitRecording(true);
        ex.setExecutionResult(ExecutionResult.DEFAULT);


        String formItemName = ex.getFIAState().getNextItem().name;
         if ( scope.isDeclaredInExactlyThisScope(formItemName) &&
               scope.evaluate(formItemName) != scope.getUndefined()) {
             if (log.isDebugEnabled()) log.debug("FormItem " + formItemName + " already has a value");
             ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
             return;
         }

        //Only set lastResult if we've got an utterance and we're recording.
        //This is simplified since we haven't got any silence detection, and
        //thus the timeout property is not relevant.
        if (formItemName != null && utterance != null && ex.getCall().getIsRecording()) {

            ShadowVarBase shadow = new ShadowVarBase();
            String shadowName = formItemName + "$";
            Object wrappedEcmaObject = scope.javaToJS(shadow);
            if (! scope.isDeclaredInExactlyThisScope(shadowName)) {
                scope.declareReadOnlyVariable(shadowName, wrappedEcmaObject);
            }
            else {
                scope.setValue(shadowName, wrappedEcmaObject);
            }
            ex.getCurrentScope().evaluate(shadowName + ".termchar = '" + ex.getUtterance() + "'" );
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
