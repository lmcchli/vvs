/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * Compiler Class for a VoiceXML block tag.
 */
public class Block extends FormItemCompiler {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        PredicateImpl block = new PredicateImpl(parent, null, DebugInfo.getInstance(element));
        block.add(Ops.logElement(element));

        block.setTagType(Constants.VoiceXML.BLOCK);
        CompilerMacros.addECMAScope(block, null);
        block.addConstructor(Ops.registerCatches());

        /* If the block element has a name it is a gotoable form item */
        String name = element.attributeValue(Constants.VoiceXML.NAME);
        if (name != null) {
            block.setName(name);
        } else {
            name = block.getName();
        }
        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            block.setCond(cond);
        }
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        if (expr != null) {
            block.setExpr(expr);
        }


        if (isCompilingFormPredicate(module, block.getName(), block)) {
            compileChildren(module, compilerPass, parent, block, element.content());
            registerContentsInContainingForm(module, block.getName(), block);
            return block;
        } else {
            compilationError(parent, element, module, "<block> should be child of a <form>");
            return parent;
        }
    }


}
