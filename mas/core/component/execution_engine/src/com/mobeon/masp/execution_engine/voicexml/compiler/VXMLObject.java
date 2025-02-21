/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.List;

public class VXMLObject extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List content) {
        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));

        product.add(Ops.sendEvent(Constants.Event.UNSUPPORTED_OBJECTNAME, "object is not supported", product.getDebugInfo() ));

        //TODO: when this is getting implemented, tell Kenneth so he can add
        // support for "goto nextitem".

        return product;
    }
}
