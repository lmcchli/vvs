/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

public class Subdialog extends InputItemCompiler {
    static ILogger logger = ILoggerFactory.getILogger(Field.class);

    public Product compile(
            Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        if (logger.isDebugEnabled()) logger.debug("Compiling subdialog");
        String name = element.attributeValue(Constants.VoiceXML.NAME);
        Predicate subdialog = createInputItem(parent, name, element);
        subdialog.add(Ops.logElement(element));

        subdialog.addConstructor(Ops.enterHandlerScope());
        subdialog.addDestructor(Ops.leftHandlerScope());
        // Add a property scope operation, but since the doCreate param is false, it
        // will not actually perform the scope creation. The scope will be created by the
        // FIA, in order for the properties within the form item to be available during
        // prompt queueing.
        subdialog.addConstructor(Ops.addProperyScope(false));
        subdialog.addDestructor(Ops.leaveProperyScope());
        subdialog.addConstructor(Ops.registerCatches());

        subdialog.setTagType(Constants.VoiceXML.SUBDIALOG);
        /*
        subdialog.addConstructor(Ops.addProperyScope());
        subdialog.addDestructor(Ops.leaveProperyScope());*/
        /*DTMFGrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if(grammar != null) {
            field.registerDTMFGrammar(grammar);
        }*/

        // If name is NULL, retrieve the internally generated name
        if (name == null) {
            name = subdialog.getName();
        }

        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            subdialog.setCond(cond);
        }
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        if (expr != null) {
            subdialog.setExpr(expr);
        }

        unsupportedAttr(Constants.VoiceXML.ENCTYPE, element, module);
        unsupportedAttr(Constants.VoiceXML.METHOD, element, module);

        CompilerMacros.stringAttribute_P(Constants.VoiceXML.MAXAGE, element, subdialog);
        CompilerMacros.stringAttribute_P(Constants.VoiceXML.FETCHTIMEOUT, element, subdialog);

        // We need the <filled> last in the field product.
        Product filledProduct = createProduct(subdialog, element);
        filledProduct.setName(subdialog.getName());

        // Compile children and push params onto the stack
        compileInputItemChildren(
                module, compilerPass, filledProduct, subdialog, subdialog, element, element.content());

        DebugInfo debugInfo = DebugInfo.getInstance(element);

        //Prepare src parameter
        String src = element.attributeValue(Constants.VoiceXML.SRC);
        if (CompilerTools.isValidStringAttribute(src)) {
            subdialog.add(Ops.text_P(src));
        } else {
            src = element.attributeValue(Constants.VoiceXML.SRCEXPR);
            subdialog.add(Ops.evaluateECMA_P(src, module.getDocumentURI(), element.getLine()));
        }

        //Construct a waitset waiting on return, and terminating on badfetch
        WaitSetProduct wsp = new WaitSetProduct(subdialog, debugInfo);
        wsp.add(Ops.returnSubdialog(name, debugInfo));
        wsp.addWaitFor("com.mobeon.return");
        wsp.addTerminateOn("error.badfetch");
        subdialog.add(wsp);

        //Execute subdialog
        subdialog.add(Ops.executeSubdialog_TTM(debugInfo));
        subdialog.add(Ops.log("Returned to document " + module.getDocumentURI()));
        subdialog.add(filledProduct);

        if (isCompilingFormPredicate(module, name, subdialog)) {
            parent.add(subdialog);
            registerContentsInContainingForm(module, name, subdialog);
            return subdialog;
        } else {
            compilationError(parent, element, module, "<subdialog> should be child of a <form>");
            return parent;
        }
    }

}