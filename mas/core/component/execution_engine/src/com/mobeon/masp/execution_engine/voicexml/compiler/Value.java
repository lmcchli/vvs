/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

//TODO: a value is like an audio in the vxml w3c-schema !! for now it can only handle expressions evaluating to a string on the value stack
public class Value extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product value = createProduct(parent, element);
        value.add(Ops.logElement(element));

        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        value.add(Ops.evaluateECMA_P(expr, module.getDocumentURI(), element.getLine()));
        parent.add(value);
        return value;
    }
}
