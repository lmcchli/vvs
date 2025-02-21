/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Throw extends NodeCompilerBase {

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        parent.add(Ops.logElement(element));

        if (element.attribute(Constants.VoiceXML.EVENTEXPR) != null || element.attribute(Constants.VoiceXML.EVENT) != null)
        {
            CompilerMacros.evaluateStringAttribute_P(Constants.VoiceXML.EVENTEXPR, element, parent, element.attributeValue(Constants.VoiceXML.EVENT), false, module.getDocumentURI(), element.getLine());
        } else {
            parent.add(Ops.sendEvent(Constants.Event.ERROR_SEMANTIC,
                    CompilerMacros.bothAreDefinedMessage(element, Constants.VoiceXML.EVENTEXPR, Constants.VoiceXML.EVENT),
                    DebugInfo.getInstance(element)));
            return parent;
        }

        if (element.attribute(Constants.VoiceXML.MESSAGEEXPR) != null || element.attribute(Constants.VoiceXML.MESSAGE) != null)
        {
            CompilerMacros.evaluateStringAttribute_P(Constants.VoiceXML.MESSAGEEXPR, element, parent, element.attributeValue(Constants.VoiceXML.MESSAGE), false, module.getDocumentURI(), element.getLine());
        } else {
            // We need to explicitly tell later running code that message had no value
            parent.add(Ops.text_P(null));
        }
        parent.add(Ops.sendEvent_T2(DebugInfo.getInstance(element)));
        parent.add(Ops.waitForEvents());
        return parent;
    }
}
