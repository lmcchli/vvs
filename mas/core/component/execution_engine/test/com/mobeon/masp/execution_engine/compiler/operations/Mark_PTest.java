/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Mark_P Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class Mark_PTest extends OperationCase
{
    public Mark_PTest(String name)
    {
        super(name);
    }

    public void testExecute() throws Exception
    {
        expect_ValueStack_pushMark();

        op = new Mark_P();
        op.execute(getExecutionContext());
    }

    public static Test suite()
    {
        return new TestSuite(Mark_PTest.class);
    }
}
