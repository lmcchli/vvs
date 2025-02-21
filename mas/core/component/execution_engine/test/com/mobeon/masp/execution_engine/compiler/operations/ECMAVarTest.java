/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ECMAVar Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class ECMAVarTest extends OperationCase {
    public ECMAVarTest(String name) {
        super(name);
    }


    public void testExecute() throws Exception {
        String varName = "aVar";

        expect_ScopeContext_lastEvaluationFailed(false);
        execute_ECMAVar(varName, false);
        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(getCurrentScope()));

        op.execute(getExecutionContext());
    }

    protected void execute_ECMAVar(String varName, boolean exists) {

        expect_ScopeContext_isDeclaredInExactlyThisScope(varName, exists);
        if (exists) {
            expect_ScopeContext_setValue(varName, null);
        } else {
            expect_ScopeContext_evaluateAndDeclareVariable(varName, null);
        }
        op = new ECMAVar(varName,null);
    }

    public void testExecuteWithExistingVar() throws Exception {
        String varName = "aVar";

        execute_ECMAVar(varName, true);
        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(getCurrentScope()));
        op.execute(getExecutionContext());
    }

    public void testArguments() throws Exception {
        String varName = "aVar";
        op = new ECMAVar(varName,DebugInfo.getInstance());
        validateMnemonic("ECMAVar('" + varName + "')");
    }

    public static Test suite() {
        return new TestSuite(ECMAVarTest.class);
    }
}
