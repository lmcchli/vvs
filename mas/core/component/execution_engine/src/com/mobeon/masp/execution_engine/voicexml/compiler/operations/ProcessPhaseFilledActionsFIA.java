/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.InputItemImpl;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class ProcessPhaseFilledActionsFIA extends VXMLOperationBase {
    private String formName ;
    private static final ILogger logger = ILoggerFactory.getILogger(ProcessPhaseFilledActionsFIA.class);

    public ProcessPhaseFilledActionsFIA( String id) {
        super();
        this.formName = id;
    }

    /**
     * @logs.error "Mismatching property scopes! We are at depth <n> but should be at depth <m>" - This is an internal error.
     * @param ex
     * @throws InterruptedException
     */
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        // TODO. Break out handling of filled. For now, simply itterate all filled in document order
        FIAState fiaState = ex.getFIAState();
        FIAState.NextItem nextItem = fiaState.getNextItem();
        fiaState.setPhase(FIAState.Phase.Process);
        ex.setTransitioningState();
        // Reset ASR results
        ex.getFIAState().setNLSMLResponse(null);
        Product prod = nextItem.product;
        if (prod instanceof InputItemImpl) {
            if (ex.getProperties().getDepth() != fiaState.getParentPropertyDepth()) {
                logger.error("Mismatching property scopes! We are at depth " + ex.getProperties().getDepth() +
                             " but should be at " +fiaState.getParentPropertyDepth());
            }

        }
        if (fiaState.isAnyJustFilled()) {
            fiaState.setAllInitials(ex);
        }
        Product filledProduct = fiaState.getFIAObjects().getFilled();
        ex.call(filledProduct.freezeAndGetExecutables(), filledProduct);
    }

    public String arguments() {
        return formName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
