/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

public class Log extends NodeCompilerBase {
    ILogger logger = ILoggerFactory.getILogger(getClass());

    public Product compile(Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        /* An optional ECMAScript expression evaluating to a string. */
        String expr;
        /* An optional string which may be used, for example, to indicate the purpose of the log..*/
        String label;


        Product log = createProduct(parent,element);
        log.add(Ops.logElement(element));
        log.add(Ops.mark_P()); // set a mark so we now what to log from the ValueStack

        expr = element.attributeValue(Constants.VoiceXML.EXPR);

        if(expr != null) {
            log.add(Ops.text_P(expr));
            log.add(Ops.evaluateECMA_TP());
            log.add(Ops.text_P(" "));            
        }
        label =  element.attributeValue(Constants.VoiceXML.LABEL);

        compileChildren(app, compilerPass, parent, log, element.content());

        log.add(Ops.print_TM(app.getDocumentURI().getPath(),element.getLine(), label));
        return log;
    }
}
