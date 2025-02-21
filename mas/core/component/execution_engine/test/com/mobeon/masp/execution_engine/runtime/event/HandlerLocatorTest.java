/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.products.TruePredicate;
import junit.framework.Test;
import junit.framework.TestSuite;

public class HandlerLocatorTest extends Case {

    public static Test suite() {
        return new TestSuite(HandlerLocatorTest.class);
    }

    public HandlerLocatorTest(String event) {
        super(event);
    }

    /**
     * Validates that handler with, and without a state specification
     * ends up in the right places.
     * 
     * @throws Exception
     */
    public void testAddEventHandler() throws Exception {
        Predicate predicate = new TruePredicate(null, DebugInfo.getInstance());
        String handlerRe = "event.*";
        String state = "any";

        HandlerLocator locator = new HandlerLocator(CCXMLSelector.instance());

        locator.addEventHandler(predicate,handlerRe,state);
        HandlerTable byState = locator.getHandlerByState().get(state);

        if(!byState.getIterator().hasNext())
            die("Handler wasn't found in the correct handler table for the specified state");

        locator.addEventHandler(predicate,handlerRe);
        if(!locator.getGlobal().getIterator().hasNext())
            die("Global handler not found in global handler table");
    }

    /**
     * Validate precedence rules for event handlers, these are the
     * same precedence rules as for CCXML and VoiceXML events.
     *
     * @throws Exception
     */
    public void testLocateEventHandler() throws Exception {
        Predicate predicate1 = new TruePredicate(null,"predicate1", DebugInfo.getInstance());
        Predicate predicate2 = new TruePredicate(null,"predicate2", DebugInfo.getInstance());
        Predicate predicate3 = new TruePredicate(null,"predicate3", DebugInfo.getInstance());
        Predicate predicate4 = new TruePredicate(null,"predicate4", DebugInfo.getInstance());
        Predicate predicate5 = new TruePredicate(null,"predicate5", DebugInfo.getInstance());
        Predicate predicate6 = new TruePredicate(null,"predicate6", DebugInfo.getInstance());
        Predicate predicate7 = new TruePredicate(null,"predicate7", DebugInfo.getInstance());

        HandlerLocator locator = new HandlerLocator(CCXMLSelector.instance());
        locator.addEventHandler(predicate1,"a.b","any");
        locator.addEventHandler(predicate2,"a.*","any");
        locator.addEventHandler(predicate3,"a.*");
        locator.addEventHandler(predicate4,"b.*");
        locator.enteredScope(null);
            locator.addEventHandler(predicate5,"b.*");
            validateLocator(locator, "any",  "a.b", predicate1, true);
            validateLocator(locator, "any",  "a.långt", predicate2, true);
            validateLocator(locator, "other",  "a.långt", predicate3, true);
            validateLocator(locator, "any",  "b.tjohej", predicate5, true);
        locator.leftScope(null);

        validateLocator(locator, "any",  "b.tjohej", predicate4, true);
        locator.enteredScope(null);
            //b.* matches pred6
            locator.addEventHandler(predicate6,"b.*");
            validateLocator(locator, "any",  "b.tjohej", predicate6, true);
            validateLocator(locator, "any",  "b.tjohej", predicate6, true);
            locator.enteredScope(null);
                locator.addEventHandler(predicate7,"b.*");
                locator.enteredScope(null);
                    validateLocator(locator, "any",  "b.tjohej", predicate7, true);
                locator.leftScope(null);
                validateLocator(locator, "any",  "b.tjohej", predicate7, true);
            locator.leftScope(null);
            validateLocator(locator, "any",  "b.tjohej", predicate6, true);
        locator.leftScope(null);
        validateLocator(locator, "any",  "b.slafs", predicate4, true);
        locator.enteredScope(null);
        locator.enteredScope(null);
        locator.enteredScope(null);
        validateLocator(locator, "any",  "b.tjohej", predicate7, false);
        validateLocator(locator, "other",  "a.långt", predicate3, true);
        validateLocator(locator, "any",  "b.tjohej", predicate5, false);
        validateLocator(locator, "any",  "b.tjohej", predicate4, true);
        locator.enteredScope(null);
        locator.addEventHandler(predicate5,"b.*");
        locator.leftScope(null);
        locator.enteredScope(null);
        validateLocator(locator, "any",  "b.tjohej", predicate5, false);
        locator.leftScope(null);
        locator.enteredScope(null);
        locator.enteredScope(null);
        locator.leftScope(null);
        locator.leftScope(null);
        locator.enteredScope(null);
        locator.leftScope(null);
        locator.enteredScope(null);
        validateLocator(locator, "any",  "b.tjohej", predicate5, false);


    }

    private void validateLocator(HandlerLocator locator, String state, String event, Predicate expectedPredicate, boolean expectedResult) {
        EventHandler result = locator.locateEventHandler(state, event, null);
        if(result == null)
            die("Locator didn't find a matching handler, even though one exists");

        if(expectedResult) {
            if((result.getPredicate() != expectedPredicate))
                die("Locator located wrong predicate "+result.getPredicate()+", expected "+expectedPredicate);
        } else {
            if((result.getPredicate() == expectedPredicate))
                die("Locator located wrong predicate "+result.getPredicate()+", it wasn't expected");
        }
    }
}