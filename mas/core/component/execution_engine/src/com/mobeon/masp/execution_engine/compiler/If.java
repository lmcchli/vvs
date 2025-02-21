/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.products.BinaryExecutable;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;

import java.util.List;
import java.net.URI;

/*
* Copyright (c) 2005 Mobeon AB All Rights Reserved.
*/

/**
 * This class compiles statements line:
 * <if cond="flavor == 'vanilla'">
 * <assign name="flavor_code" expr="'v'"/>
 * <elseif cond="flavor == 'chocolate'"/>
 * <assign name="flavor_code" expr="'h'"/>
 * <elseif cond="flavor == 'strawberry'"/>
 * <assign name="flavor_code" expr="'b'"/>
 * <else/>
 * <assign name="flavor_code" expr="'?'"/>
 * </if>
 */
public class If extends NodeCompilerBase {

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        BinaryExecutable currentCondition = createConditionCallable(null, element, module.getDocumentURI(), element.getLine());
        Product trueProduct = createTrueProduct(parent, element);

        currentCondition.setTrueCallable(trueProduct);
        BinaryExecutable firstCondition = currentCondition;
        for (Node node : element.content()) {
            if (isCondition(node)) {
                if (!validCondition(node)) {
                    compilationError(parent, element, module, "Condition for " + node.getName() + " is invalid");
                    return parent;
                }
                currentCondition = createConditionCallable(currentCondition, node, module.getDocumentURI(), element.getLine());
                trueProduct = createTrueProduct(parent, (CompilerElement) node);
                currentCondition.setTrueCallable(trueProduct);
            } else {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    compilerPass.compile(module, trueProduct, (CompilerElement) node, content);
                } else if (node.getNodeType() == Node.TEXT_NODE) {
                    compilerPass.compile(module, trueProduct, (Text) node, content);
                }
            }
        }
        parent.add(Ops.logElement(element));
        parent.add(firstCondition);
        return parent;
    }

    private static Product createTrueProduct(Product parent, CompilerElement element) {
        Product trueProduct = createProduct(parent, element);
        trueProduct.add(Ops.logElement(element));
        return trueProduct;
    }

    private static boolean isCondition(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE
               && node.getName().startsWith("else");
    }

    private static boolean validCondition(Node node) {
        return ((Element) node).attribute(Constants.VoiceXML.COND) == null
               || !node.getName().equals("else");
    }

    private static BinaryExecutable createConditionCallable(BinaryExecutable currentCondition, Node node, URI uri, int lineNumber) {
        CompilerElement element = (CompilerElement) node;
        BinaryExecutable newCondition = new BinaryExecutable(null, DebugInfo.getInstance(element));
        newCondition.setCondition(element.attributeValue(Constants.VoiceXML.COND), uri, lineNumber);
        if (currentCondition != null)
            currentCondition.setFalseCallable(newCondition);
        return newCondition;
    }

}