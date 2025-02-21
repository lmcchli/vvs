/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Reprompt extends NodeCompilerBase {
    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product reprompt = createProduct(parent, element);
        reprompt.add(Ops.logElement(element));

        reprompt.add(Ops.unwindToRepromptPoint());
        // No children to compile;
        parent.add(reprompt);

        return reprompt;
    }
}
