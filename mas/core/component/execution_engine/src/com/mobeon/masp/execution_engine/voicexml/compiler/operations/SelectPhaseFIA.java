/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.Exit;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * @author David Looberger
 */
public class SelectPhaseFIA extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(SelectPhaseFIA.class);
    

    String formName;

    public SelectPhaseFIA(String formName) {
        this.formName = formName;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();
        fiaState.setPhase(FIAState.Phase.Select);
        fiaState.reset(ex);
        ex.setTransitioningState();

        // Select the next form item to execute. Evaluates COND and EXPR if those attribites exist.
        // If no more items are to be executed, false is returned.
        if (!fiaState.findNext(ex)) {
            if (logger.isDebugEnabled()) logger.debug("Found no item to execute!");
            // Found no more form item to execute. Call the exit product
            //TODO: It appears as if these exitOperation should be compiled into the execution graph
            //rather then called in this awkward manner.
            ex.anonymousCall(Exit.getExitOperations());
        } else {
            FIAState.NextItem nextItexm = ex.getFIAState().getNextItem();
            if (ex.getFinalProcessingState() &&
                    (nextItexm.product.getTagType().equals(Constants.VoiceXML.FIELD) ||
                            nextItexm.product.getTagType().equals(Constants.VoiceXML.RECORD) ||
                            nextItexm.product.getTagType().equals(Constants.VoiceXML.TRANSFER))) {
                // Is in final processing state and trying to execute an input tag.
                if (logger.isDebugEnabled())
                    logger.debug("Exiting due to trying to call an input operation in final processing state.");
                ex.anonymousCall(Exit.getExitOperations());
            } else {
                String name = fiaState.getNextItem().name;
                if (logger.isDebugEnabled()) {
                    logger.debug("Selected item " + name);
                }
                setShadowVar(ex, name);

                if (nextItexm.product.getTagType().equals(Constants.VoiceXML.RECORD)) {
                    if (logger.isDebugEnabled()) logger.debug("It is a record. We will not use noinput timeout");
                    fiaState.useNoInputTimeout(false);
                }
                if (nextItexm.product.getTagType().equals(Constants.VoiceXML.TRANSFER)) {
                    if (logger.isDebugEnabled()) logger.debug("It is a transfer. We will not use noinput timeout");
                    fiaState.useNoInputTimeout(false);
                }
                fiaState.setParentPropertyDepth(ex.getProperties().getDepth());
            }
        }
    }

    private void setShadowVar(VXMLExecutionContext ex, String name) {
        String shadowName = name + "$";
        if (! ex.getCurrentScope().isDeclaredInExactlyThisScope(shadowName)) {
            ex.getCurrentScope().evaluateAndDeclareVariable(shadowName, null);
        } else {
            ex.getCurrentScope().setValue(shadowName, null);
        }
    }

    public String arguments() {
        return formName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
