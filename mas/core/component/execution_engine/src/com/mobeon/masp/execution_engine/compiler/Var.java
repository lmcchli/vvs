/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.products.BinaryExecutable;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * Compile VoiceXML <var> element which may look like:
 * <var name="phone" expr="'6305551212'"/>
 * or
 * <var name="phone"/>
 */
public class Var extends NodeCompilerBase {

    public Product compile(
    Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));

        String namePart = element.attributeValue("name");
        String exprPart = element.attributeValue("expr");
        product.add(Ops.introduceECMAVariable(namePart, product.getDebugInfo()));
        // "expr" part of <var> is optional.
        if (element.getTagType().isLeafTypeOf("form")) {

            BinaryExecutable hasParam = new BinaryExecutable(null,DebugInfo.getInstance(element));
            hasParam.addToPredicate(Ops.hasParam_P(namePart));

            Product trueProduct = createProduct(parent,element);
            trueProduct.add(Ops.getParam_P(namePart));
            trueProduct.add(Ops.assignECMAVar_T(namePart));
            hasParam.setTrueCallable(trueProduct);


            if (exprPart != null) {
                Product falseProduct = createProduct(parent,element);
                falseProduct.add(Ops.evaluateECMA_P(element, exprPart, app.getDocumentURI(), element.getLine()));
                falseProduct.add(Ops.assignECMAVar_T(namePart));
                hasParam.setFalseCallable(falseProduct);
            }
            product.add(hasParam);
        } else {

            if (exprPart != null) {
                product.add(Ops.evaluateECMA_P(element, exprPart, app.getDocumentURI(), element.getLine()));
                product.add(Ops.assignECMAVar_T(namePart));
            }
        }
        parent.add(product);
        return product;
    }
}
