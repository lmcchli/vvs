package com.mobeon.masp.execution_engine.ccxml.compiler;


import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.*;
import org.dom4j.Text;

import java.util.List;

/**
 * Compiles a &lt;ccxml&gt; node into it's executable representation
 * <p/>
 * Equivalent pseudocode:
 * <pre>
 * newScope("ccxml")
 * </pre>
 * Destructors:
 * <pre>
 * closeScope
 * </pre>
 *
 * @author Mikael Andersson
 */
public class CCXML extends NodeCompilerBase {
    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product ccxml = createProduct(parent,element);
        ccxml.add(Ops.logElement(element));
        ccxml.add(Ops.setExecutingModule(module));

        module.setProduct(ccxml);
        ccxml.addConstructor(Ops.newScope(Constants.CCXML.CCXML));
        // No destructor since CCXML document always runs to then end, and then waits
        // for events

        Product ecmaScriptElements = createProduct(ccxml, element);
        Product eventProcessor = createProduct(ccxml, element);
        compile(module, compilerPass, element.content(), ecmaScriptElements, eventProcessor);
        ccxml.add(ecmaScriptElements);
        ccxml.add(Ops.sendCCXMXLEvent(Constants.Event.CCXML_LOADED, "", DebugInfo.getInstance()));                
        ccxml.add(eventProcessor);

        return ccxml;
    }

    private void compile(Module module,
                         com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass,
                         List<Node> containingContent,
                         Product ecmaScriptElements,
                         Product eventProcessor) {

        for(Object o:containingContent) {
            Node node = (Node)o;
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement element = (CompilerElement)node;
                if (element.getName().equals("eventprocessor")) {
                    compilerPass.compile(module,eventProcessor,element,containingContent);
                }
                else {
                    compilerPass.compile(module,ecmaScriptElements,element,containingContent);
                }
            } else if(node.getNodeType() == Node.TEXT_NODE) {
                org.dom4j.Text text = (Text)node;
                compilerPass.compile(module, ecmaScriptElements, text,containingContent);
            }
        }
    }

}
