/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.voicexml.compiler.Log;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

public class LogTest extends NodeCompilerCase {
    Log log;

    public LogTest(String name) {
        super(name,Log.class,"log");
    }

    public void testCompile() throws Exception {
        die("Test is not implemented");
    }
}