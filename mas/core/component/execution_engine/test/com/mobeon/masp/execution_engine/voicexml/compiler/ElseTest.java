/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.voicexml.compiler.Else;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

public class ElseTest extends NodeCompilerCase {
    Else anElse;

    public ElseTest(String name) {
        super(name, Else.class, "else");
    }

    public void testCompile() throws Exception {
        die("Test is not implemented");
    }
}