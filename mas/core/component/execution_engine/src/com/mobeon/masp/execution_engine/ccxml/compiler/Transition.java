/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.compiler.products.TruePredicate;
import com.mobeon.masp.execution_engine.runtime.event.CCXMLSelector;
import com.mobeon.masp.execution_engine.runtime.event.Selector;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * Compiles a &lt;transition&gt; node into it's executable representation
 * <p/>
 * <p/>
 * Equivalent pseudocode:
 * <pre>
 * newScope("transition")
 * receiveEvent_P
 * AssignECMAVar_T($name);
 * </pre>
 * <p/>
 * Destructors:
 * <pre>
 * closeScope
 * </pre>
 *
 * @author Mikael Andersson
 */
public class Transition extends NodeCompilerBase {
    public Product compile(Module app, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {

        String state = element.attributeValue(Constants.CCXML.STATE);

        String event = element.attributeValue(Constants.CCXML.EVENT);

        String cond = element.attributeValue(Constants.CCXML.COND);

        String name = element.attributeValue(Constants.CCXML.NAME);

        Predicate predicate;

        if (cond != null) {
            PredicateImpl impl = new PredicateImpl(null, DebugInfo.getInstance(element));
            impl.addToPredicate(Ops.evaluateECMA_P(cond,app.getDocumentURI(), element.getLine()));
            predicate = impl;
        } else {
            predicate = new TruePredicate( null, DebugInfo.getInstance(element));
        }
        predicate.add(Ops.logElement(element));


        String[] states = null;
        if(state != null)
            states = state.split("[\\s\\t\\n]+");

        if(event == null)
            event = "*";
        Selector sel = CCXMLSelector.parse(event);

        parent.addConstructor(Ops.registerHandler(states, sel, predicate));
        parent.addSource(predicate);

        predicate.addConstructor(Ops.atomic(Ops.setEventsEnabled(false),Ops.newScope("transition")));
        // Disable events to support "Any events that arrive while an event
        // is already being processed are just placed on the queue for later"
        // from the CCXML spec.

        if(CompilerTools.isValidStringAttribute(name)) {
            predicate.add(Ops.setEventVar(name));
        } else {
            predicate.add(Ops.setEventVar(Constants.CCXML_EVENTEVAR));            
        }
        predicate.addDestructor(Ops.atomic(Ops.closeScope(),Ops.setEventsEnabled(true)));

        compileChildren(app, compilerPass, null, predicate, element.content());
        return parent;
    }

}
