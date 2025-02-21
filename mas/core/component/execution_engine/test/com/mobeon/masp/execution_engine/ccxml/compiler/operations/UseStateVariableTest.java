/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * UseStateVariable Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/06/2005</pre>
 */
public class UseStateVariableTest extends CCXMLOperationCase
{
    String variable = "aVar";

    public UseStateVariableTest(String name)
    {
        super(name);
    }


    public void testToMnemonic() throws Exception
    {
        op = new UseStateVariable(variable);
        validateMnemonic("UseStateVariable('"+variable+"')");
    }


    public void testExecute() throws Exception
    {
        op = new UseStateVariable(variable);
        expect_ExecutionContext_bindStateTo(variable);
        op.execute(getExecutionContext());
    }

    public static Test suite()
    {
        return new TestSuite(UseStateVariableTest.class);
    }
}
