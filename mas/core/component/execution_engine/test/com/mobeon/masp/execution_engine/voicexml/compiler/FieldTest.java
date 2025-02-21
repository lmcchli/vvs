/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.voicexml.compiler.Field;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerCase;

public class FieldTest extends NodeCompilerCase {
    private Field field;

    public FieldTest(String name) {
        super(name, Field.class, "field");
    }

    public void testCompile() throws Exception {
        die("Test is not implemented");
    }
}