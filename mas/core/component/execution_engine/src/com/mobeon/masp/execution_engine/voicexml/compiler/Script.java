/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.CDATA;
import org.dom4j.Node;
import org.dom4j.Text;
import org.w3c.dom.Entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author David Looberger
 */
public class Script extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product script = createProduct(parent, element);
        script.add(Ops.logElement(element));

        script.setName("SCRIPT");

        // This class is used for <script> in both CCXML
        // and VXML and they have different attributes.
        
        unsupportedAttr(Constants.VoiceXML.CHARSET, element, module);
        unsupportedAttr(Constants.VoiceXML.FETCHHINT, element, module);
        unsupportedAttr(Constants.VoiceXML.FETCHTIMEOUT, element, module);
        unsupportedAttr(Constants.VoiceXML.MAXAGE, element, module);
        unsupportedAttr(Constants.VoiceXML.MAXSTALE, element, module);
        unsupportedAttr(Constants.CCXML.EXPR, element, module);

        // From the spec: Either an "src" attribute or an inline script
        // (but not both) must be specified; otherwise, an error.badfetch
        // event is thrown

        String inlineScript = retrieveInlineScript(module,
                script, element.content());
        String uriStr = element.attributeValue(Constants.VoiceXML.SRC);

        if(uriStr != null && (inlineScript != null && inlineScript.length() > 0)) {
            script.add(Ops.sendEvent(Constants.Event.ERROR_BADFETCH,
                    "script can not be defined both via URI and inline content ", script.getDebugInfo()));
            module.postEvent(Constants.Event.ERROR_BADFETCH, element);
            parent.add(script);
            return script;
        }

        if(uriStr != null){
            try {
                URI uri = new URI(uriStr);
                uri = module.getDocumentURI().resolve(uri);
                script.add(Ops.retrieveEvalueateAndCacheURIFileContent(uri,script.getDebugInfo()));
            } catch (URISyntaxException e) {
                script.add(Ops.sendEvent(Constants.Event.ERROR_BADFETCH, CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.SRC, uriStr), script.getDebugInfo()));
                module.postEvent(Constants.Event.ERROR_BADFETCH, element);
            }
        } else {
            script.add(Ops.compileCacheAndEvaluate(inlineScript,
                    module.getDocumentURI().toString() + "#script_"+script.identity(),
                    module.getDocumentURI(), element.getLine() ));
        }

        // Add script to the parent
        parent.add(script);
        return script;
    }

    private String retrieveInlineScript(Module module, Product script, List<? extends Node> elementlist) {
        StringBuilder scriptText = new StringBuilder("");
        for(Object o:elementlist) {
          Node node = (Node)o;
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                // Should not be any children to this node, issue an error
                compilationError(script,(CompilerElement)node,module, "Unexpected child elements to <script> were found");
            }
            else if(node.getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)node;
                scriptText.append(text.getText());
            }
            else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                CDATA text = (CDATA )node;
                scriptText.append(text.getText());
            } else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                Entity ent = (Entity) node;
                scriptText.append(ent.toString());
            }
        }
        if (scriptText.length() == 0)
            return null;
        else
            return scriptText.toString();
    }
}
