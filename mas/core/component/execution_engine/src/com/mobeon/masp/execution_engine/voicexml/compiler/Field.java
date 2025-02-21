/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * Compiler for the Field VoiceXML tag.
 *
 * @author David Looberger
 */
public class Field extends InputItemCompiler {
    static ILogger logger = ILoggerFactory.getILogger(Field.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Field");
        String name = element.attributeValue(Constants.VoiceXML.NAME);
        Predicate field = createInputItem(parent, name, element);
        field.add(Ops.logElement(element));

        field.setTagType(Constants.VoiceXML.FIELD);

        field.addConstructor(Ops.enterHandlerScope());
        field.addDestructor(Ops.leftHandlerScope());
        // Add a property scope operation, but since the doCreate param is false, it
        // will not actually perform the scope creation. The scope will be created by the
        // FIA, in order for the properties within the form item to be available during
        // prompt queueing.
        field.addConstructor(Ops.addProperyScope(false));
        field.addDestructor(Ops.leaveProperyScope());
        field.addConstructor(Ops.registerCatches());

        /* field.addConstructor(Ops.addProperyScope());
      field.addDestructor(Ops.leaveProperyScope());  */
        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            field.add(Ops.registerDTMFGrammar(grammar));
        }
        grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            field.add(Ops.registerASRGrammar(grammar));
        }
        // If name is NULL, retrieve the internally generated name
        if (name == null) {
            name = field.getName();
        }

        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            field.setCond(cond);
        }
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        if (expr != null) {
            field.setExpr(expr);
        }

        String type = element.attributeValue(Constants.VoiceXML.TYPE);
        if (type != null) {
            if (!Constants.VoiceXML.isValidFieldType(type)) {
                compilationError(field, element, module, CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.TYPE, type));
            }
        }
        unsupportedAttr(Constants.VoiceXML.TYPE, element, module);
        unsupportedAttr(Constants.VoiceXML.SLOT, element, module);
        unsupportedAttr(Constants.VoiceXML.MODAL, element, module);

        // Add handler for DTMF utterance
        Executable ehDTMF = addEventHandlerForDTFMUtterance(name, element, module);
        Executable ehASR = addEventHandlerForASRUtterance(name, element, module);
        Predicate ehDTMFWakeup = addEventHandlerDTMFWakeup(name, element);

        // We need the <filled> last in the field product.
        Product filledProduct = createProduct(field, element);
        filledProduct.setName(field.getName());
        compileInputItemChildren(module, compilerPass, filledProduct,
                field, field, element, element.content());


        // General idea:
        // Since DTMF can arrive at any time and can also already be buffered before we start running,
        // we want to run in a controlled manner, and start by disabling events until we are ready.
        // We poll for buffered DTMF, and if there is buffered DTMF giving match, the form item variable
        // will be set, and the <field> is ready. The event handlers for DTMF/ASR are aware of this. The
        // event handlers are predicates being registered only if the form item variable is still undefined,
        // and the waitIfUndefined operation will also only wait if we are not done already.

        field.add(Ops.addBargeinHandler());
        field.add(Ops.enterWaitingState());  // Start by playing queued prompts etc
        Product p = createProduct(field, element);
        p.add(Ops.setEventsEnabled(false));
        p.add(Ops.addEventHandler(Constants.Event.DTMF_WAKEUP_EVENT, ehDTMFWakeup, false));
        p.add(Ops.setValueToASRInterpretation());
        p.add(Ops.assignIfBufferedDTMF());
        p.add(ehDTMF);
        p.add(ehASR);
        p.add(Ops.waitIfUndefined(field.getName())); // events are enabled by this op
        p.addConstructor(Ops.enterHandlerScope());  // register our own scope to make sure we get all DTMF to our handlers
        p.addDestructor(Ops.leftHandlerScope());

        field.add(p);
        field.add(Ops.enterTransitioningState());
        field.add(filledProduct);


        if (isCompilingFormPredicate(module, name, field)) {
            parent.add(field);
            registerContentsInContainingForm(module, name, field);
            return field;
        } else {
            compilationError(parent, element, module, "<field> should be child of a <form>");
            return parent;
        }
    }

    private Predicate addEventHandlerDTMFWakeup(String name, CompilerElement element) {
        Predicate eventHandler = createPredicate(null, null, element);

        eventHandler.add(new AtomicExecutable(
                Ops.onDTMFWakeup(),
                Ops.setValueToDTMFInterpretation(),
                Ops.changeExecutionResult_T()));

        return eventHandler;

    }

    private Executable addEventHandlerForASRUtterance(String formItemName, CompilerElement element, Module module) {

        // We want the event handler to be registered only if the form item is still undefined

        Predicate eventHandler = createPredicate(null, null, element);

        eventHandler.add(new AtomicExecutable(Ops.bargeinHandler_P(),
                Ops.setValueToASRInterpretation(),
                Ops.changeExecutionResult_T()));

        Predicate registerTheEventHandler = createPredicate(null, null, element);
        registerTheEventHandler.addToPredicate(Ops.evaluateECMA_P(formItemName + "==undefined",module.getDocumentURI(), element.getLine()));
        registerTheEventHandler.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT, eventHandler, false));

        return registerTheEventHandler;
    }

    private static Executable addEventHandlerForDTFMUtterance(String formItemName, CompilerElement element, Module module) {

        // We want the event handler to be registered only if the form item is still undefined

        Predicate eventHandler = createPredicate(null, null, element);

        eventHandler.add(new AtomicExecutable(
                Ops.bargeinHandler_P(),   // We want the functionality of this op but dont care about what it puts on value stack...
                Ops.collectDTMFUtterance(false, true),
                Ops.setValueToDTMFInterpretation(),
                Ops.popValueStack(),   // ...so pop it
                Ops.waitForEvents()));

        Predicate registerTheEventHandler = createPredicate(null, null, element);
        registerTheEventHandler.addToPredicate(Ops.evaluateECMA_P(formItemName + "==undefined", module.getDocumentURI(), element.getLine()));
        registerTheEventHandler.add(Ops.addEventHandler(Constants.VoiceXML.DTMFUTTERANCE_EVENT, eventHandler, false));
        return registerTheEventHandler;
    }
}
