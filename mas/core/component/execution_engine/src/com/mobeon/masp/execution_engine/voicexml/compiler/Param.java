/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Param extends NodeCompilerBase {
    public Product compile(
    Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        parent.add(Ops.logElement(element));

        String name = element.attributeValue(Constants.VoiceXML.NAME);
        if(name != null) {
            String value = element.attributeValue(Constants.VoiceXML.VALUE);
            if(value != null) {
                parent.add(Ops.pair_P(name,value));
            } else {
                String expr = element.attributeValue(Constants.VoiceXML.EXPR);
                if(expr != null) {
                    parent.add(Ops.evaluateECMA_P(expr, module.getDocumentURI(), element.getLine()));
                    parent.add(Ops.pair_TP(name));
                }
            }
        } else {
            //TODO: Add error handling for missing name
        }
        return parent;
    }
}
