/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public abstract class NodeCompilerBase implements NodeCompiler {
    private ILogger log;

    public static Predicate createInputItem(Product parent, String localName, CompilerElement element) {
        return new InputItemImpl(parent, localName, DebugInfo.getInstance(element));
    }

    public static Predicate createInputItem(Product parent, String localName, CompilerElement element, boolean isRepromptPoint) {
        InputItemImpl item = new InputItemImpl(parent, localName, DebugInfo.getInstance(element));
        item.setIsRepromptPoint(isRepromptPoint);
        return item;
    }

    public static Product createProduct(Product parent, CompilerElement element) {
        return new ProductImpl(parent, DebugInfo.getInstance(element));
    }

    public static Predicate createPredicate(Product parent, String localname, CompilerElement element) {
        return new PredicateImpl(parent, localname, DebugInfo.getInstance(element));
    }

    public static Predicate createFilledPredicate(Product parent, String localname, CompilerElement element) {
        return new FilledPredicateImpl(parent, localname, DebugInfo.getInstance(element));
    }

    private ILogger getLog() {
        if (log == null)
            log = ILoggerFactory.getILogger(getClass());
        return log;
    }

    /**
     * Call this function for all non-supported attributes, to get a warning
     * log if the attribute is defined. You dont have to check if
     * the attribute is defined before calling the function.
     *
     * @param attrName The name of the attrivute
     * @param element  the element in which the attribute may be defined.
     * @param module   the module being compiled
     * @logs.warn "<uri> line <line>: Attribute <attrName> is not supported and will be ignored." - In the document pointed out by <uri>, an element had the tag <tag> specified at line <line>. The attribute is not supported and will be ignored. 
     */
    protected void unsupportedAttr(String attrName, CompilerElement element, Module module) {
        String attrValue = element.attributeValue(attrName);
        if (attrValue != null) {
            String msg = module.getDocumentURI() + " line " +
                         element.getLine() + ": Attribute '" + attrName +
                         "' is not supported and will be ignored.";
            getLog().warn(msg);
        }

    }

    public static void compileChildren(Module module, Compiler.CompilerPass compilerPass, Product parent, Product product, List<Node> containingContent) {
        if (parent != null && product != null)
            parent.add(product);
        Compiler.compile(module, compilerPass, product, containingContent);
    }

    protected static void compilationError(String event,Product product, CompilerElement element, Module module, String message) {
        module.postEvent(event, element);
        product.add(Ops.sendEvent(event, message, product.getDebugInfo()));
    }
    protected static void compilationError(Product product, CompilerElement element, Module module, String message) {
        module.postEvent(Constants.Event.ERROR_SEMANTIC, element);
        product.add(Ops.sendEvent(Constants.Event.ERROR_SEMANTIC, message, product.getDebugInfo()));
    }

    protected static void compilationError(Product product, CompilerElement element, Module module, String error, String message) {
        module.postEvent(error, element);
        product.add(Ops.sendEvent(error, message, product.getDebugInfo()));
    }

}
