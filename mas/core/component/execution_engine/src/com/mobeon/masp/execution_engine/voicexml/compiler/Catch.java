/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author David Looberger
 */
public class Catch extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(Catch.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Catch");
        Predicate eventHandler = createPredicate(null, null, element);
        List<Operation> atomicOps = new ArrayList<Operation>();
        Operation logElement = Ops.logElement(element);
        atomicOps.add(logElement);

        parent.addSource(eventHandler);

        String event = element.attributeValue(Constants.VoiceXML.EVENT);

        atomicOps.add(Ops.enterFinalProcessingState(false));

        CompilerMacros.addScope(eventHandler, null);

        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            eventHandler.addToPredicate(Ops.evaluateECMA_P(cond, module.getDocumentURI(), element.getLine()));
        }

        unsupportedAttr(Constants.VoiceXML.COUNT, element, module);

        if (event == null) {
            event = ".";
        }
        // TODO: this is a way to make sure all events are executed in transitioning state, in case
        // the input item implementation is unable to ensure this in case of an event (e.g. noinput).
        // A clean solution for transitioning state needs to be implemented.
        atomicOps.add(Ops.enterTransitioningState());
        atomicOps.add(Ops.retrieveCurrentEvent());
        AtomicExecutable atomic = new AtomicExecutable(atomicOps.toArray(new Operation[atomicOps.size()]));

        eventHandler.add(atomic);

        // The children of the Catch node is the actual event handler
        compileCatchChildren(module, compilerPass, eventHandler, element.content());

        // Add an operation registering the eventHandler in the parent
        // for each event in the catch claues
        StringTokenizer tokenizer = new StringTokenizer(event, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() > 0) {
                // TODO: Add functionality for setting the _event and _message variables

                // TODO: is it good to enable events? doing it for backwards compatibility
                parent.addConstructor(Ops.addEventHandler(token, eventHandler, true));
            }
        }

        // A catch lives in a form, and if we reach the end we unwind
        // the engine stack such that execution of the form may continue.

        eventHandler.add(Ops.catchUnwind());
        return parent;
    }

    public static void compileCatchChildren(Module module, Compiler.CompilerPass compilerPass, Product parent, List<Node> containingContent) {
        // The compiled children are the event handler for the catched event.
        compilerPass.compile(module, parent, containingContent);
    }
}
