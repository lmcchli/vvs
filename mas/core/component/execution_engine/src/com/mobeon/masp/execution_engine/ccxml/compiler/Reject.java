/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.Msgs;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Reject  extends NodeCompilerBase {

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        
        parent.add(Ops.logElement(element));
        CompilerMacros.stringAttribute_P(Constants.CCXML.HINTS,element,parent);
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.REASON,element,parent, Msgs.message(Msgs.CALL_REJECTED), false, module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateConnectionId_P(element, parent, module.getDocumentURI(), element.getLine());
        parent.add(Ops.connectionReject_T3());
        return parent;

    }

}