/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.runtime.Msgs;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class CreateCall extends NodeCompilerBase {

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        unsupportedAttr(Constants.CCXML.AAI, element, module);
        unsupportedAttr(Constants.CCXML.USE, element, module);

        parent.add(Ops.logElement(element));
        CompilerMacros.stringAttribute_P(Constants.CCXML.CONNECTION_ID,element,parent);
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.CALLER_ID,element,parent, "''", true, module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.DESTINATION,element,parent, Msgs.message(Msgs.CALL_REJECTED), false, module.getDocumentURI(), element.getLine());
        CompilerMacros.stringAttribute_P(Constants.CCXML.HINTS,element,parent);
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.TIMEOUT,element,parent, null, false, module.getDocumentURI(), element.getLine());
        parent.add(Ops.connectionCreateCall_T4());
        return parent;
    }
}
