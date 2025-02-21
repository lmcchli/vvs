/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Set;

/**
 * @author David Looberger
 */
public class InitializeFormItemsFIA extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(InitializeFormItemsFIA.class);
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();

        if (fiaState.getPhase() != FIAState.Phase.Initialization || fiaState.getErrorDuringInit()) {
            if (logger.isDebugEnabled()) logger.debug("Skipping initialization");
            return;
        }
        ex.setTransitioningState();

        FIAObjects fiaObjects = fiaState.getFIAObjects();
        if (logger.isDebugEnabled()) logger.debug("Initializing FormItem FIA");

        // Create variables for all form items
        Set<String> itemNames = fiaState.getFormItemNames(ex);
        Scope scope = ex.getCurrentScope();
        for (String name : itemNames) {
            Product prod = fiaObjects.getFormItems().get(name);
            String expr = null;
            if (prod instanceof Predicate) {
                Predicate predicate = (Predicate) prod;
                expr = predicate.getExpr();
            }
            if (! scope.isDeclaredInExactlyThisScope(name)) {
                scope.evaluateAndDeclareVariable(name, expr);
                if (scope.lastEvaluationFailed()) {
                    errorSemantic(ex, name);
                }
            } else {
                Object exprResult = null;
                Object wrappedEcmaObject = null;
                if (expr != null) {
                    exprResult = scope.evaluate(expr);
                    wrappedEcmaObject = scope.javaToJS(exprResult);
                }
                scope.setValue(name, wrappedEcmaObject);
            }
        }

    }

    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void errorSemantic(ExecutionContext ex, String name) {
        String msg = "Declaration of " + name + " failed";
        if (logger.isDebugEnabled()) logger.debug(msg);
        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                msg, DebugInfo.getInstance());
    }
}
