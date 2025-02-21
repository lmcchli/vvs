/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistryImpl;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * AssignECMAVar Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class AssignECMAVar_TTest extends OperationCase
{
    final String name = "ecmaVar";
    final String ecmaValue = "ecmaString";


    public AssignECMAVar_TTest(String name)
    {
        super(name);
    }

    /**
     * Test that assign of a declared variable results in that the
     * AssignECMAVar_T operation sets the value into the scopeContext.
     * @throws Exception
     */
    public void testExecute() throws Exception
    {
        ECMAObjectValue result = setupExecute();
        expect_ScopeContext_isDeclaredInAnyScope(name, true);
        expect_ScopeContext_setValue(name,ecmaValue);
        expect_ScopeContext_javaToJS(result);
        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(getCurrentScope()));
        op.execute(getExecutionContext());
    }

    /**
     * Test that assign of an undeclared variable results in
     * a "semantic error" is thrown.
     * @throws Exception
     */
    public void testExecuteUndeclared() throws Exception
    {
        setupExecute();
        expect_ScopeContext_isDeclaredInAnyScope(name,false);
        expect_EventHub_fireContextEvent(Constants.Event.ERROR_SEMANTIC, "", DebugInfo.getInstance());
        mockExecutionContext.stubs().method("getCurrentScope").will(returnValue(getCurrentScope()));        
        op.execute(getExecutionContext());
    }

    /**
     * Help method to be used by test cases/methods. Sets up everything necessary
     * to execute this.op, for example execution context.
     */
    private ECMAObjectValue setupExecute() {
    op = new AssignECMAVar_T(name);

        ScopeRegistryImpl scopeRegistryImpl = new ScopeRegistryImpl(null);
        String ecmaExpression = "'" + ecmaValue + "'";
        Object result = scopeRegistryImpl.getMostRecentScope().evaluate(ecmaExpression);

        ECMAObjectValue resultValue = new ECMAObjectValue(result);

        expect_ValueStack_pop(resultValue);
        return resultValue;
    }

    public void testArguments() throws Exception
    {
        op = new AssignECMAVar_T(name);
        validateMnemonic("AssignECMAVar_T('"+name+"')");
    }

    public static Test suite()
    {
        return new TestSuite(AssignECMAVar_TTest.class);
    }
}
