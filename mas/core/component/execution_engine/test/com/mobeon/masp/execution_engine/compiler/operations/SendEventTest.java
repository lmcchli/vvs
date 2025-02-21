/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;

/**
 * SendEvent Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class SendEventTest extends OperationCase
{
    String event = "event.something";

    public SendEventTest(String name)
    {
        super(name);
    }

      public void testExecute() throws Exception
    {
        expect_EventHub_fireContextEvent(event, "", DebugInfo.getInstance());
        op = new SendEvent(event,"", DebugInfo.getInstance());
        op.execute(getExecutionContext());
    }

    public void testArguments() throws Exception
    {
        op = new SendEvent(event,"",null);
        validateMnemonic("SendEvent('"+event+"')");
    }

    public static Test suite()
    {
        return new TestSuite(SendEventTest.class);
    }
}
