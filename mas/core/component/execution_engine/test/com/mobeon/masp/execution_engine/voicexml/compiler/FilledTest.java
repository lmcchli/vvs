/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import junit.framework.*;
import org.dom4j.QName;

import com.mobeon.masp.execution_engine.voicexml.compiler.Filled;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.xml.CompilerElementFactory;

public class FilledTest extends NodeCompilerCase {

    public FilledTest(String name) {
        super(name, Filled.class, "filled");
    }

    public static Test suite() {
        return new TestSuite(FilledTest.class);
    }

    public void testCompile() throws Exception {
        element.setParent(new CompilerElement(new CompilerElementFactory(),new QName("filled"),1,1));
        Product result = nc.compile(module, Compiler.CCXML_CODEGEN,parent,element,element.content());
        validateResultAndParent(result,parent);
        validateConstructors(result, Ops.newScope(null));
        validateDestructors(result,Ops.closeScope());
    }
}