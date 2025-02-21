/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.compiler.products.TruePredicate;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.masp.execution_engine.runtime.event.CCXMLSelector;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URI;
import java.util.ArrayList;

/**
 * Transition Tester.
 *
 * @author Mikael Andersson
 * @version 1.0
 */
public class TransitionTest extends NodeCompilerCase {
    public static int kka;

    public TransitionTest(String name) {

        super(name, Transition.class, "transition");
    }

    public void testCompile() throws Exception {
        boolean wasCompilerTesting = MASTestSwitches.isCompilerTesting(); // save this so we can restore after the test case

        try {
            MASTestSwitches.initForCompilerTest();
            element.addAttribute(Constants.CCXML.EVENT, "*");
            element.addAttribute(Constants.CCXML.NAME, "ev");
            Product result = compile();
            validateResultAndParent(result, parent);

            Predicate hidden = new TruePredicate(null,DebugInfo.getInstance());
            Executable[] executables = new Executable[0];  // expect no executables
            validateOperations(parent, executables);
            validateConstructors(parent,Ops.registerHandler(null, CCXMLSelector.parse("*"), hidden));
        } finally {
            if(wasCompilerTesting)
                MASTestSwitches.initForCompilerTest();
            else
                MASTestSwitches.reset();
        }
    }

    public void testCompileCond() throws Exception {
        boolean wasCompilerTesting = MASTestSwitches.isCompilerTesting(); // save this so we can restore after the test case

        try {
            MASTestSwitches.initForCompilerTest();
            String eventSelector = "*";
            String eventName = "ev";
            String condition = "anythin != 'test'";
            element.addAttribute(Constants.CCXML.EVENT, eventSelector);
            element.addAttribute(Constants.CCXML.NAME, eventName);
            element.addAttribute(Constants.CCXML.COND, condition);
            Product result = compile();
            validateResultAndParent(result, parent);

            PredicateImpl hidden = new PredicateImpl(null, DebugInfo.getInstance());
            hidden.addToPredicate(Ops.evaluateECMA_P(condition, new URI("a"), 1));
            Executable[] executables = new Executable[0];  // expect no executables
            validateOperations(parent, executables);
            validateConstructors(parent,Ops.registerHandler(null, CCXMLSelector.parse(eventSelector), hidden));
        } finally {
            if(wasCompilerTesting)
                MASTestSwitches.initForCompilerTest();
            else
                MASTestSwitches.reset();
        }
    }

    public void testCompileNoEvent() throws Exception {
        boolean wasCompilerTesting = MASTestSwitches.isCompilerTesting(); // save this so we can restore after the test case

        try {
            MASTestSwitches.initForCompilerTest();
            String eventName = "ev";
            String eventSelector = "*";
            element.addAttribute(Constants.CCXML.STATE, "state1   \t\nstate2");
            element.addAttribute(Constants.CCXML.NAME, eventName);
            Product result = compile();
            validateResultAndParent(result, parent);

            Predicate hidden = new TruePredicate(null, DebugInfo.getInstance());
            Executable[] executables = new Executable[0];  // expect no executables
            validateOperations(parent, executables);
            validateConstructors(parent,Ops.registerHandler(new String[]{"state1", "state2"}, CCXMLSelector.parse(eventSelector), hidden));
        } finally {
            if(wasCompilerTesting)
                MASTestSwitches.initForCompilerTest();
            else
                MASTestSwitches.reset();
        }
    }

    public void testCompileState() throws Exception {
        boolean wasCompilerTesting = MASTestSwitches.isCompilerTesting(); // save this so we can restore after the test case

        try {
            MASTestSwitches.initForCompilerTest();
            String eventName = "ev";
            String eventSelector = "*";
            element.addAttribute(Constants.CCXML.EVENT, eventSelector);
            element.addAttribute(Constants.CCXML.STATE, "state1   \t\nstate2");
            element.addAttribute(Constants.CCXML.NAME, eventName);
            Product result = compile();
            validateResultAndParent(result, parent);

            Predicate predicate = new TruePredicate(null,DebugInfo.getInstance());
            Product expected = addCommonOperations(predicate, eventName);
            if(parent.getSourceProducts().size() != 1) {
                fail("Event handler should add a source element");
            }
            validateConstructors(parent,Ops.registerHandler(new String[]{"state1", "state2"}, CCXMLSelector.parse(eventSelector), predicate));
        } finally {
            if(wasCompilerTesting)
                MASTestSwitches.initForCompilerTest();
            else
                MASTestSwitches.reset();
        }
    }

    private Product addCommonOperations(Predicate product, String eventName) {

        product.addConstructor(Ops.atomic(Ops.setEventsEnabled(false),Ops.newScope("transition")));
        product.add(Ops.setEventVar(eventName));
        product.addDestructor(Ops.atomic(Ops.closeScope(),Ops.setEventsEnabled(true)));
        return product;
    }


    public static Test suite() {

        return new TestSuite(TransitionTest.class);
    }
}
