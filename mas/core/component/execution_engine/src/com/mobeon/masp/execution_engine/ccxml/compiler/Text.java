/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.TextCompilerBase;

/**
 * Compiles a text node by adding operations to it's parent {@link Product}
 * <p/>
 * Equivalent pseudocode added to <em>parent</em> product:
 * <pre>
 * text_P($text)
 * </pre>
 *
 * @author Mikael Andersson
 */
public class Text extends TextCompilerBase {

    public void compile(Module app, Product parent, org.dom4j.Text text) {
        String textValue = text.getText();
        if (textValue.trim().length() > 0) {
            parent.add(Ops.text_P(textValue));
        }
    }
}
