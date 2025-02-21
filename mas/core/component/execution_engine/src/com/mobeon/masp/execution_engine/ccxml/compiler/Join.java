package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public class Join extends NodeCompilerBase {
    public Product compile(
    Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if(!CompilerMacros.evaluateRequiredStringAttribute_P(Constants.CCXML.ID1,element,parent, module.getDocumentURI(), element.getLine())) {
            return parent;
        }
        if(!CompilerMacros.evaluateRequiredStringAttribute_P(Constants.CCXML.ID2,element,parent, module.getDocumentURI(), element.getLine())) {
            return parent;
        }
        parent.add(Ops.logElement(element));
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.DUPLEX,element,parent, "full", false , module.getDocumentURI(), element.getLine());
        parent.add(Ops.connectionJoin_T3(DebugInfo.getInstance(element)));
        return parent;
    }
}