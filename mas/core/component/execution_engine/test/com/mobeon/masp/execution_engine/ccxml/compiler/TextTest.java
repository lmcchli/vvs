/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.masp.execution_engine.xml.CompilerElementFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Text Tester.
 *
 * @author Mikael Andersson
 * @since <pre>10/03/2005</pre>
 */
public class TextTest extends CompilerCase
{
    public TextTest(String name)
    {
        super(name);
    }

    public void testCompile() throws Exception
    {
        TextCompiler tc = new Text();
        Module module = new Module(null);
        Product parent = new ProductImpl(null, DebugInfo.getInstance());
        String expectedText = "Hello test";
        org.dom4j.Text text = CompilerElementFactory.getInstance().createText(expectedText);
        tc.compile(module,parent,text);
        if(parent.freezeAndGetExecutables() == null)
            die("Parent is broken, getOperations() should never return null");
        validateOperations(parent,
        Ops.text_P(expectedText));
    }

    public static Test suite()
    {
        return new TestSuite(TextTest.class);
    }
}
