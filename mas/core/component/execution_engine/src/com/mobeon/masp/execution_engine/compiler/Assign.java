/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */

package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

public class Assign extends NodeCompilerBase {
    public Product compile(Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));
        product.add(Ops.evaluateECMA_P(element.attributeValue("expr"),app.getDocumentURI(), element.getLine()));
        product.add(Ops.assignECMAVar_T(element.attributeValue("name")));
        parent.add(product);
        return product;
    }
}
