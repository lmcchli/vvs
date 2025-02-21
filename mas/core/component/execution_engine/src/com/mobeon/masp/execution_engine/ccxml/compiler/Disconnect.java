/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Disconnect extends NodeCompilerBase {
    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product product = createProduct(parent,element);

        String connectionId = element.attributeValue(Constants.CCXML.CONNECTION_ID);
        if(CompilerTools.isValidStringAttribute(connectionId)) {
            parent.add(Ops.evaluateECMA_P(connectionId,module.getDocumentURI(), element.getLine()));
        } else {
            parent.add(Ops.eventVar_P(Constants.CCXML.CONNECTION_ID));
        }
        product.add(Ops.logElement(element));
        product.add(Ops.disconnect_T());
        parent.add(product);
        return product;
    }
}
