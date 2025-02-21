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

/**
 * Compiles a &lt;log&gt; node into it's executable representation
 * <p/>
 * Equivalent pseudocode:
 * <pre>
 * mark_P
 * (if $label)
 *     evaluateECMA_P($label)
 * (if $expr)
 *     evaluateECMA_P($expr)
 * log_TM
 * </pre>
 *
 * @author Mikael Andersson
 */
public class Log extends NodeCompilerBase {
    public Product compile(Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));

        product.add(Ops.mark_P());
        String label = element.attributeValue(Constants.CCXML.LABEL);
        if (label != null) {
            product.add(Ops.evaluateECMA_P(label, app.getDocumentURI(), element.getLine()));
            product.add(Ops.text_P(" "));
        }
        String expr = element.attributeValue(Constants.CCXML.EXPR);
        if (expr != null) {
            product.add(Ops.evaluateECMA_P(expr, app.getDocumentURI(), element.getLine()));
        }
        product.add(Ops.log_TM());
        addProduct(parent, product);
        return product;
    }

    private void addProduct(Product parent, Product product) {
        if (parent != null) {
            parent.add(product);
        }
    }
}
