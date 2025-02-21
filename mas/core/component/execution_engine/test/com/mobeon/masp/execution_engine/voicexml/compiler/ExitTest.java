/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.voicexml.compiler.Exit;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

public class ExitTest extends NodeCompilerCase {
    Exit exit;

    public ExitTest(String name) {
        super(name,Exit.class,"exit");
    }

    public void testCompile() throws Exception {
        die("Test is not implemented");
    }
}