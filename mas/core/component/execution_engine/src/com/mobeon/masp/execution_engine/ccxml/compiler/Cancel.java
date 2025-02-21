/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Cancel extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        parent.add(Ops.logElement(element));
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.SENDID,element,parent, "<undefined sendid>", false, module.getDocumentURI(), element.getLine());
        parent.add(Ops.cancelEvent_T());
        return parent;
    }
}
