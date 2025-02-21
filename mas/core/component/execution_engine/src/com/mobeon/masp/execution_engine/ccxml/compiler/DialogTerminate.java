/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class DialogTerminate extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        unsupportedAttr(Constants.CCXML.IMMEDIATE, element, module);
        final String dialogid = element.attributeValue(Constants.CCXML.DIALOG_ID);
        if(CompilerTools.isValidStringAttribute(dialogid)) {
            parent.add(Ops.evaluateECMA_P(dialogid, module.getDocumentURI(), element.getLine()));
            parent.add(Ops.createDialogTerminateByDialogId_T());
        } else {
            compilationError(parent,element,module, CompilerMacros.invalidAttrMessage("dialogterminate", Constants.CCXML.DIALOG_ID, dialogid));
        }
        return parent;
    }
}
