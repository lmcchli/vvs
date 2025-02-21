/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Link extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(Link.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Link");
        //TODO: No semantics yet

        Predicate predicate = createPredicate(parent, null, element);
        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            predicate.add(Ops.registerDTMFGrammar(grammar));
        }
         grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            predicate.add(Ops.registerASRGrammar(grammar));
        }
        compileChildren(module, compilerPass, parent, predicate, element.content());

        return predicate;
    }
}
