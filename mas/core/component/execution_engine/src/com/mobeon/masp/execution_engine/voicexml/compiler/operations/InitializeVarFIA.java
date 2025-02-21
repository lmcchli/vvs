/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class InitializeVarFIA extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(InitializeVarFIA.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();
        if (fiaState.getPhase() != FIAState.Phase.Initialization || fiaState.getErrorDuringInit()) {
            if (logger.isDebugEnabled()) logger.debug("Skipping initialization");
            return;
        }
        ex.setTransitioningState();

        FIAObjects fiaObjects = fiaState.getFIAObjects();
        if (logger.isDebugEnabled()) logger.debug("Initializing Var FIA");
        // Register catch handlers, in order to handle events occuring during initialization at dialog level.
        Product varsandscripts = fiaObjects.getVarsAndScripts();
        ex.getEngine().call(varsandscripts);

    }

    public String arguments() {
        return "";
    }

}
