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
import java.util.StringTokenizer;

public class Send extends NodeCompilerBase {
    public Product compile(
    Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        parent.add(Ops.logElement(element));
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.TARGET,element,parent, "<undefined target id>", false,module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.TARGETTYPE,element,parent, Constants.CCXML.CCXML, false, module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.DATA,element,parent, "error.fetch", false, module.getDocumentURI(), element.getLine());
        CompilerMacros.evaluateStringAttribute_P(Constants.CCXML.DELAY,element,parent, "0s", false, module.getDocumentURI(), element.getLine());

        parent.add(Ops.mark_P());
        String nameList = element.attributeValue(Constants.CCXML.NAMELIST);
        if(nameList != null){
            StringTokenizer st = new StringTokenizer(nameList);
             while (st.hasMoreTokens()) {
                 parent.add(Ops.evaluateECMA_P(st.nextToken(), module.getDocumentURI(), element.getLine()));
             }
        }

        parent.add(Ops.sendEvent_TMT4(DebugInfo.getInstance(element)));
        return parent;
    }
}
