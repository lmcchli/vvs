/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import org.apache.commons.lang.StringUtils;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class SetValueToDTMFInterpretation extends VXMLOperationBase {
    private static ILogger log = ILoggerFactory.getILogger(SetValueToDTMFInterpretation.class);
    

    public SetValueToDTMFInterpretation() {
        super();
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        String utterance = ex.getUtterance() ;
        FIAState.NextItem nextItem = ex.getFIAState().getNextItem();

        if (utterance != null && ex.getFIAState().matchedGrammar()) {
            Scope scope = ex.getCurrentScope();
            if (nextItem == null) {
                if (log.isDebugEnabled()) log.debug("The selected form item is null!");
            }
            else {
                
                //TR: HU98608 Debug logs in VVS which contains pin numbers of subscribers
                String utterance_obscured;      
                if (log.isDebugEnabled()) {
                    //obscure with X's instead of actual DTMF digits as can display pin numbers etc.
                    utterance_obscured =  "obscured:" + StringUtils.repeat("X", utterance.length());
                } else {
                    utterance_obscured = "";
                }
                                                
                if (scope.evaluate(nextItem.name) == scope.getUndefined()) {
                    scope.setValue(nextItem.name, utterance);
                    if (log.isDebugEnabled()) log.debug("Assigning the value " + utterance_obscured + " to " + nextItem.name);
                } else {
                    if (log.isDebugEnabled()) log.debug("The form item " + nextItem.name + " already has a value!");
                }
            }
            ex.setExecutionResult(ExecutionResult.DEFAULT);
        } else {
            // If no utterance, keep waiting
            ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
        }
    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
