/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * EventProcessor Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class EventProcessorTest extends NodeCompilerCase
{
    public EventProcessorTest(String name) {
        super(name, EventProcessor.class, "eventprocessor");
    }

    public void testCompile() throws Exception
    {
        final String stateVar = "state_var";

        element.addAttribute(Constants.CCXML.STATE_VARIABLE, stateVar);
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.useStateVariable(stateVar),
                Ops.setEventsEnabled(true));
    }

    public void testCompileNoState() throws Exception
    {
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.introduceECMAVariable(Constants.CCXML_STATEVAR,null),
                Ops.useStateVariable(Constants.CCXML_STATEVAR),
                Ops.setEventsEnabled(true));
    }

    public static Test suite()
    {
        return new TestSuite(EventProcessorTest.class);
    }
}
