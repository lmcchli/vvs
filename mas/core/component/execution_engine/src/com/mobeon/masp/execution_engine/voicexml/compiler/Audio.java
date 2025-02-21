/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

public class Audio extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(Audio.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Audio");
        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));

        if (bothSrcAndExprAreDefined(element)) {
            compilationError(Constants.Event.ERROR_BADFETCH, parent, element, module, CompilerMacros.bothAreDefinedMessage(element, Constants.VoiceXML.EXPR, Constants.VoiceXML.SRC));
            return parent;
        }

        boolean hasAlternative = false;
        if (hasAlternativeAudio(element.content())) {
            hasAlternative = true;
            product.add(Ops.mark_P());
        }
        compileChildren(module, compilerPass, parent, product, element.content());
        if (!hasAlternative) {
            product.add(Ops.mark_P());
        }

        //TODO: handle all attributes
        unsupportedAttr(Constants.VoiceXML.FETCHHINT, element, module);
        unsupportedAttr(Constants.VoiceXML.MAXSTALE, element, module);

        CompilerMacros.stringAttribute_P(Constants.VoiceXML.MAXAGE, element, product);
        CompilerMacros.stringAttribute_P(Constants.VoiceXML.FETCHTIMEOUT, element, product);

        //TODO: implement handle mixed content but only if playing MediaContent fails

        String str = element.attributeValue(Constants.VoiceXML.SRC);
        // do we have a audio resource URI directly
        if (CompilerTools.isValidStringAttribute(str)) {
            // TODO: Handle a proper URI
            product.add(Ops.text_P(str));
            product.add(Ops.createPlayableObject_TM_P(Constants.VoiceXML.isInputItemChild(element), false));
        }
        str = element.attributeValue(Constants.VoiceXML.EXPR);
        // do we have an ECMA expression that will express an audio resource URI
        if (CompilerTools.isValidStringAttribute(str)) {
            product.add(Ops.evaluateECMA_P(str, module.getDocumentURI(), element.getLine()));
            product.add(Ops.createPlayableObject_TM_P(Constants.VoiceXML.isInputItemChild(element), false));
        }

        // If not a child to prompt or audio
        if (!element.getParent().getName().equals("prompt") && !element.getParent().getName().equals("audio")) {
            product.add(Ops.queuePlayableObject_T());
        }

        return product;
    }

    private boolean hasAlternativeAudio(List<Node> content) {
        boolean hasAlternative = false;

        for (Object o : content) {
            Node node = (Node) o;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement el = (CompilerElement) node;
                if (el.getName().equals("audio")) {
                    hasAlternative = true;
                }

            } else if (node.getNodeType() == Node.TEXT_NODE) {
                hasAlternative = true;
            }
        }
        return hasAlternative;
    }

    private boolean bothSrcAndExprAreDefined(CompilerElement element) {
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        String src = element.attributeValue(Constants.VoiceXML.SRC);
        return expr != null && src != null;
    }

}
