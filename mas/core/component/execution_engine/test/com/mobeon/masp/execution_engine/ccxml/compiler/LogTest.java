/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Constants;

import java.net.URI;

/**
 * Log Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class LogTest extends NodeCompilerCase
{
    public LogTest(String name)
    {
        super(name,Log.class,"log");
    }

    public void testCompileExpr() throws Exception
    {
        element.addAttribute(Constants.VoiceXML.EXPR, "uttryck");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.mark_P(),
                Ops.evaluateECMA_P("uttryck",new URI("a"),1),
                Ops.log_TM());

    }

    public void testCompileLabel() throws Exception
    {
        element.addAttribute(Constants.VoiceXML.EXPR, "uttryck");
        element.addAttribute(Constants.VoiceXML.LABEL, "etikett");
        Product result = compile();
        validateResultAndParent(result, parent);
        validateOperations(result,
                Ops.logElement(element),
                Ops.mark_P(),
                Ops.evaluateECMA_P("etikett", new URI("a"),1),
                Ops.text_P(" "),
                Ops.evaluateECMA_P("uttryck", new URI("a"),1),
                Ops.log_TM());

    }

    public static Test suite()
    {
        return new TestSuite(LogTest.class);
    }
}
