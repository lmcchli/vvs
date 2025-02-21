/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class SetValueToASRInterpretation extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(SetValueToASRInterpretation.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        // TODO parse the NLSML document
        if (ex.getFIAState().hasAsrMatch()) {
            String res = ex.getFIAState().getASRUtterance(ex.getASR_grammar());
            ex.getFIAState().setNLSMLResponse(null);
            FIAState.NextItem nextItem = ex.getFIAState().getNextItem();
            if (res != null) {
                Scope scope = ex.getCurrentScope();
                if (nextItem == null) {
                    if (log.isDebugEnabled()) log.debug("The selected form item is null!");
                } else {
                    if (scope.evaluate(nextItem.name) == scope.getUndefined()) {
                        scope.setValue(nextItem.name, res);
                        if (log.isDebugEnabled()) log.debug("Assigning the value " + res + " to " + nextItem.name);
                        //Clear DTMF queue if we get a voice match
                        Redirector.InputAggregator(ex).clearControlTokenQ();
                    } else {
                        if (log.isDebugEnabled()) log.debug("The form item " + nextItem.name + " already has a value!");
                    }
                }
                ex.setExecutionResult(ExecutionResult.DEFAULT);
            }
        }
    }

    public String arguments() {
        return "";
    }
}
