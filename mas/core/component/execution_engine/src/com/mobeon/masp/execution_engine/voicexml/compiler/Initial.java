/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.InputItemCompiler;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Initial  extends InputItemCompiler {
    static ILogger logger = ILoggerFactory.getILogger(Initial.class);

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Initial");
        //TODO: No semantics yet

        Product product = createInputItem(parent, null, element, true);
        product.add(Ops.logElement(element));

        product.addConstructor(Ops.enterHandlerScope());
        product.addDestructor(Ops.leftHandlerScope());
        // Add a property scope operation, but since the doCreate param is false, it
        // will not actually perform the scope creation. The scope will be created by the
        // FIA, in order for the properties within the form item to be available during
        // prompt queueing.
        product.addConstructor(Ops.addProperyScope(false));
        product.addDestructor(Ops.leaveProperyScope());
        product.addConstructor(Ops.registerCatches());

        product.setTagType(Constants.VoiceXML.INITIAL);
        compileInputItemChildren(module, compilerPass, parent, product, element.content());
        product.add(Ops.enterWaitingState());

        unsupportedAttr(Constants.VoiceXML.NAME, element, module);
        unsupportedAttr(Constants.VoiceXML.EXPR, element, module);
        unsupportedAttr(Constants.VoiceXML.COND, element, module);

        //TODO: when this is getting implemented, tell Kenneth so he can add
        // support for "goto nextitem".

        return product;
    }
}
