/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.InputItemImpl;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class CollectPhaseRegisterPropsFIA extends VXMLOperationBase {
    private String id = "";

    public CollectPhaseRegisterPropsFIA(String id) {
        super();
        this.id = id;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();
        FIAState.NextItem nextItem = fiaState.getNextItem();
        if (nextItem.name == null) {
            return;
        }
        fiaState.setPhase(FIAState.Phase.Collect);
        ex.setTransitioningState();
        // Create a property scope for the form item.
        // The prop. scope will be destroyed when leaving the form item,
        // either throught the normal FIA operations, or through a GOTO
        Product item = fiaState.getNextItem().product;
        if (item instanceof InputItemImpl) {
            int depth = ex.getProperties().getDepth();
            ex.getProperties().enteredScope(null);
            InputItemImpl inputItem = (InputItemImpl) item;
            Product properties = inputItem.getProperties();
            ex.getEngine().call(properties);
        }
    }

    public String arguments() {
        return id;
    }
}
