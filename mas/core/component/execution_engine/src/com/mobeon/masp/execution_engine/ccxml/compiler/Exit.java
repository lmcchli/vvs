/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Exit extends NodeCompilerBase {

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        unsupportedAttr(Constants.CCXML.EXPR, element, module);
        unsupportedAttr(Constants.CCXML.NAMELIST, element, module);
        parent.add(Ops.logElement(element));
        parent.add(Ops.atomic(createExitOps(parent.getDebugInfo())));
        return parent;
    }

    public static Operation[] createExitOps(DebugInfo debugInfo) {
        return new Operation[]{
                //It looks weird to shutdown first and _then_ send CCXML_EXIT
                //but it's necessary !
                Ops.engineShutdown(true),
                Ops.sendCCXMXLEvent(Constants.Event.CCXML_EXIT, "CCXML Exiting", debugInfo),
        };
    }

}
