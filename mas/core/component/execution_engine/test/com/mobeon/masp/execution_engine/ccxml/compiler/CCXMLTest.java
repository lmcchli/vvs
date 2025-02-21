package com.mobeon.masp.execution_engine.ccxml.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

/**
 * CCXML Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class CCXMLTest extends NodeCompilerCase
{
    public CCXMLTest(String name)
    {
        super(name,CCXML.class,"ccxml");
    }

    /**
     * Validates that the ccxml scope is created,
     * but newer destroyed. This necessary because
     * the main CCXML application runs to the end
     * att first invocation. If it closes the scope,
     * it will
     *
     * @throws Exception
     */
    public void testCorrectScopeHandling() throws Exception
    {
        Product result = compile();
        validateResultAndParent(result, parent);
        validateConstructors(result,
                Ops.newScope("ccxml"));
    }

    public static Test suite()
    {
        return new TestSuite(CCXMLTest.class);
    }
}
