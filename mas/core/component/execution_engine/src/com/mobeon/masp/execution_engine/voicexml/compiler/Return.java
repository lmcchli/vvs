/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Return extends NodeCompilerBase {
    public Product compile(
            Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        String event = element.attributeValue(Constants.VoiceXML.EVENT);
        String eventExpr = element.attributeValue(Constants.VoiceXML.EVENTEXPR);
        String namelist = element.attributeValue(Constants.VoiceXML.NAMELIST);
        String message = element.attributeValue(Constants.VoiceXML.MESSAGE);
        String messageExpr = element.attributeValue(Constants.VoiceXML.MESSAGEEXPR);

        parent.add(Ops.logElement(element));

        if (!CompilerTools.isAtMostOneDefined(event, eventExpr, namelist)
                && CompilerTools.isAtMostOneDefined(message, messageExpr, namelist)
                ) {
            parent.add(Ops.sendEvent(Constants.Event.ERROR_BADFETCH, "invalid combination of attributes to <return>", DebugInfo.getInstance(element)));
            return parent;
        }
        boolean skip = false;

        if (!skip && eventExpr != null) {
            parent.add(Ops.evaluateECMA_P(eventExpr,module.getDocumentURI(), element.getLine()));
            compileMessage(parent, message, messageExpr, module, element);
            parent.add(Ops.createPair_T2P());
            skip = true;
        }

        if (!skip && event != null) {
            parent.add(Ops.text_P(event));
            compileMessage(parent, message, messageExpr, module, element);
            parent.add(Ops.createPair_T2P());
            skip = true;
        }

        if (!skip) {
            if (namelist != null) {
                String[] names = namelist.trim().split("[ \\\t]");
                parent.add(Ops.textArray_P(names));
            } else {
                parent.add(Ops.text_P(null));
            }
        }
        parent.add(Ops.sendReturn_T());
        return parent;
    }

    private static void compileMessage(Product parent, String message, String messageExpr, Module module, CompilerElement element) {
        if (message != null) {
            parent.add(Ops.text_P(message));
            return;
        }

        if (messageExpr != null) {
            parent.add(Ops.evaluateECMA_P(messageExpr, module.getDocumentURI(), element.getLine()));
            return;
        }

        parent.add(Ops.text_P(""));
    }
}
