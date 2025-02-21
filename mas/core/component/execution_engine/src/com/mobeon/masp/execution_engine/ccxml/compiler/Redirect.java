package com.mobeon.masp.execution_engine.ccxml.compiler;

import java.util.List;

import org.dom4j.Node;

import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.ccxml.compiler.operations.ConnectionRedirect_T4;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompiler;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

/**
 * Compiles the CCXML 1.0 <redirect> tag.
 * <redirect connectionid="evt.connectionid" dest="MoipSystem@localhost" reason="" hints=""/>
 * Puts on stack optional reason and hints and then mandatory destination
 * Finally puts the connectionId onto the stack last 
 * @author lmcraby
 * @version MIO 2.0.1
 */
public class Redirect implements NodeCompiler {

    @Override
    public Product compile(Module module, CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        parent.add(Ops.logElement(element));
        CompilerMacros.stringAttribute_P(Constants.CCXML.HINTS,element,parent);
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.REASON,element,parent, RedirectStatusCode._302_MOVED_TEMPORARILY.toString(), false, module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateRequiredStringAttribute_P(Constants.CCXML.DESTINATION,element,parent,  module.getDocumentURI(), element.getLine());
        
        CompilerMacros.evaluateConnectionId_P(element, parent, module.getDocumentURI(), element.getLine());
        parent.add( new ConnectionRedirect_T4());
        return parent;
    }

}
