/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Mark extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(Mark.class);

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Mark");
        Product mark = createProduct(parent, element);
        mark.add(Ops.logElement(element));


        String name = element.attributeValue(Constants.VoiceXML.NAME);
        String nameexpr = element.attributeValue(Constants.VoiceXML.NAMEEXPR);

        if (name != null && nameexpr != null) {
            compilationError(mark, element, module, CompilerMacros.bothAreDefinedMessage(element, Constants.VoiceXML.NAME, Constants.VoiceXML.NAMEEXPR));
        }

        if (nameexpr != null)
            mark.add(Ops.createPlayableMarkObject_P(nameexpr, true));
        else
            mark.add(Ops.createPlayableMarkObject_P(name, false));

        parent.add(mark);
        return mark;
    }
}
