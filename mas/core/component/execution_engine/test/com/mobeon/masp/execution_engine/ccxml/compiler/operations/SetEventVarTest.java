/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SetEventVarTest extends CCXMLOperationCase {

    final String varName ="eventVAr";

    public static Test suite() {
        return new TestSuite(SetEventVarTest.class);
    }

    public SetEventVarTest(String name) {
        super(name);
    }

    /**
     * Validates that execution sets the eventVarName
     * property in the ExecutionContext to the supplied value.
     * 
     * @throws Exception
     */
    public void testExecute() throws Exception {

        expect_ExecutionContext_setEventVarName(varName);

        op = new SetEventVar(varName);
        op.execute(getExecutionContext());
    }

    public void testToMnemonic() throws Exception {
        op = new SetEventVar(varName);
        validateMnemonic("SetEventVar('"+varName+"')");
    }
}