/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;

/**
 * Compiles a text node.
 * <p/>
 * In the common case, the result from this compilation is
 * added to the parent {@link Product} since a text node
 * cannot have any children.
 *
 * @author Mikael Andersson
 */
public interface TextCompiler {

    /**
     * Compiles the text node.
     *
     * @param module The current compilation Module.
     * @param parent The Product that is the parent compiled node
     * @param text A text string to be compiled
     */
    public void compile(Module module, Product parent, org.dom4j.Text text);
}
