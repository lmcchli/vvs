/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Defines a new ECMAScript variable unless it's already defined.
 * <p/>
 * If the variable is already defined an <em>error.semantic</em> will
 * be thrown.
 *
 * @author Mikael Andersson
 */
public class ECMAVar extends OperationBase {
    private final String name;
    private DebugInfo debugInfo;
    private static final ILogger log = ILoggerFactory.getILogger(ECMAVar.class);

    public ECMAVar(String name, DebugInfo debugInfo) {
        this.name = name;
        this.debugInfo = debugInfo;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        if (log.isDebugEnabled()) log.debug("Setting the variable " + name + " to null");
        if (! scope.isDeclaredInExactlyThisScope(name)) {
            scope.evaluateAndDeclareVariable(name, null);
            if (scope.lastEvaluationFailed()) {
                errorSemantic(ex);
            }
        } else {
            scope.setValue(name, null);
        }
        TestEventGenerator.generateEvent(TestEvent.ECMA_VAR,name);
    }

    public String arguments() {
        return textArgument(name);
    }

    private void errorSemantic(ExecutionContext ex) {
        String msg = "Declaration of " + name + " failed";
        if (log.isDebugEnabled()) log.debug(msg);
        ex.getEventHub().fireContextEventWithLocationInfo(Constants.Event.ERROR_SEMANTIC,
                msg, DebugInfo.getInstance());
    }
}
