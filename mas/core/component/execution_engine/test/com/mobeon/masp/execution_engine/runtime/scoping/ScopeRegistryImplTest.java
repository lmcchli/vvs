/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.runtime.ecma.ECMAExecutorException;

public class ScopeRegistryImplTest extends Case {

    public ScopeRegistryImplTest(String name) {
        super(name);
    }

    public void testExistenceOfVariablesWithSameNameInDifferentScopes()
    {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope sessionScope = scopeRegistry.createNewScope("session");
        Scope applicationScope = scopeRegistry.createNewScope("application");
        Scope ccxmlScope = scopeRegistry.createNewScope("ccxml");
        Scope transitionScope = scopeRegistry.createNewScope("transition");

        // Declare a variable in the session scope and test that it is
        // accessible in all "inner" scopes.

        sessionScope.evaluateAndDeclareVariable("kalle", null);
        assertTrue(sessionScope.isDeclaredInAnyScope("kalle"));
        assertTrue(applicationScope.isDeclaredInAnyScope("kalle"));
        assertTrue(ccxmlScope.isDeclaredInAnyScope("kalle"));
        assertTrue(transitionScope.isDeclaredInAnyScope("kalle"));

        // Declare a variable in the application scope an check that it is
        // accessible in all inner but not outer scopes.

        applicationScope.evaluateAndDeclareVariable("olle", null);
        assertFalse(sessionScope.isDeclaredInAnyScope("olle"));
        assertTrue(applicationScope.isDeclaredInAnyScope("olle"));
        assertTrue(ccxmlScope.isDeclaredInAnyScope("olle"));
        assertTrue(transitionScope.isDeclaredInAnyScope("olle"));

        // Declare a variable in the transition scope an check that it is
        // accessible only there.

        transitionScope.evaluateAndDeclareVariable("pelle", null);
        assertFalse(sessionScope.isDeclaredInAnyScope("pelle"));
        assertFalse(applicationScope.isDeclaredInAnyScope("pelle"));
        assertFalse(ccxmlScope.isDeclaredInAnyScope("pelle"));
        assertTrue(transitionScope.isDeclaredInAnyScope("pelle"));
    }

    /**
     * Test that assigment always goes to the variable in the most inner
     * scope.
     */
    public void testAssignmentOfVariablesWithSameNameInDifferentScopes()
    {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope sessionScope = scopeRegistry.createNewScope("session");
        Scope applicationScope = scopeRegistry.createNewScope("application");
        Scope ccxmlScope = scopeRegistry.createNewScope("ccxml");
        Scope transitionScope = scopeRegistry.createNewScope("transition");

        // Declare a variable in the session and transition scope. Assign in
        // transition scope.
        sessionScope.evaluateAndDeclareVariable("olle", "7");
        transitionScope.evaluateAndDeclareVariable("olle", null);
        transitionScope.evaluate("olle=8");
        assertTrue(integerValue(sessionScope, "olle", 7));
        assertTrue(integerValue(applicationScope, "olle", 7));
        assertTrue(integerValue(ccxmlScope, "olle", 7));
        assertTrue(integerValue(transitionScope, "olle", 8));

        // Declare a variable in the application and ccxml scope. Assign in
        // ccxml scope.
        applicationScope.evaluateAndDeclareVariable("kalle", "7");
        ccxmlScope.evaluateAndDeclareVariable("kalle", null);
        ccxmlScope.evaluate("kalle=8");
        assertFalse(sessionScope.isDeclaredInAnyScope("kalle"));
        assertTrue(integerValue(applicationScope, "kalle", 7));
        assertTrue(integerValue(ccxmlScope, "kalle", 8));
        assertTrue(integerValue(transitionScope, "kalle", 8));
    }


    /**
     * Test that using scope prefix ("session.varName") is a way to
     * assign variables in outer scopes when variables with same name is
     * declared in an inner scope.
     */
    public void testAssignmentOfVariablesUsingScopePrefix(){

        // DOIT remove ScopeContextImpl.testIt();

        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope sessionScope = scopeRegistry.createNewScope("session");
        Scope applicationScope = scopeRegistry.createNewScope("application");
        Scope ccxmlScope = scopeRegistry.createNewScope("ccxml");
        Scope transitionScope = scopeRegistry.createNewScope("transition");

        // Declare a variable in two scopes
        sessionScope.evaluateAndDeclareVariable("olle", "7");
        transitionScope.evaluateAndDeclareVariable("olle", null);
        transitionScope.evaluate("olle=8");
        transitionScope.evaluate("session.olle=9");
        assertTrue(integerValue(sessionScope, "olle", 9));
        assertTrue(integerValue(transitionScope, "olle", 8));

        // Declare a variable in all scope; change it from the innermost

        sessionScope.evaluateAndDeclareVariable("nisse", "7");
        applicationScope.evaluateAndDeclareVariable("nisse", "8");
        ccxmlScope.evaluateAndDeclareVariable("nisse", "9");
        transitionScope.evaluateAndDeclareVariable("nisse", "10");

        transitionScope.evaluate("nisse=20");
        transitionScope.evaluate("ccxml.nisse=21");
        transitionScope.evaluate("application.nisse=22");
        transitionScope.evaluate("session.nisse=23");

        assertTrue(integerValue(transitionScope, "nisse", 20));
        assertTrue(integerValue(transitionScope, "ccxml.nisse", 21));
        assertTrue(integerValue(transitionScope, "application.nisse", 22));
        assertTrue(integerValue(transitionScope, "session.nisse", 23));

        assertTrue(integerValue(ccxmlScope, "nisse", 21));
        assertTrue(integerValue(applicationScope, "nisse", 22));
        assertTrue(integerValue(sessionScope, "nisse", 23));
    }

    /**
     * Test that it is possible to check exactly which scope a
     * variable was defined in.
     */

    public void testIsDeclaredInExactlyThisScope() {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope sessionScope = scopeRegistry.createNewScope("session");
        Scope applicationScope = scopeRegistry.createNewScope("application");
        Scope ccxmlScope = scopeRegistry.createNewScope("ccxml");
        Scope transitionScope = scopeRegistry.createNewScope("transition");

        ccxmlScope.evaluateAndDeclareVariable("nisse", "9");

        assertFalse(sessionScope.isDeclaredInExactlyThisScope("nisse"));
        assertFalse(applicationScope.isDeclaredInExactlyThisScope("nisse"));
        assertTrue(ccxmlScope.isDeclaredInExactlyThisScope("nisse"));
        assertFalse(transitionScope.isDeclaredInExactlyThisScope("nisse"));
    }

    public void testEvaluateInDifferentScopes() throws ECMAExecutorException {
        ScopeRegistry ecmaExecutor = new ScopeRegistryImpl(null);
        Scope sessionScope = ecmaExecutor.createNewScope("session");
        Scope applicationScope = ecmaExecutor.createNewScope("application");

        sessionScope.evaluateAndDeclareVariable("kalle", "8");
        applicationScope.evaluateAndDeclareVariable("kalle", "9");
        applicationScope.evaluate("kalle=10");

        assertTrue(integerValue(applicationScope, "kalle", 10));
        assertTrue(integerValue(sessionScope, "kalle", 8));

        applicationScope.evaluate("session.kalle=20");

        assertTrue(integerValue(applicationScope, "kalle", 10));
        assertTrue(integerValue(applicationScope, "session.kalle", 20));
        assertTrue(integerValue(sessionScope, "kalle", 20));

        ecmaExecutor.deleteMostRecentScope();
        assertTrue(integerValue(sessionScope, "kalle", 20));
    }

    private boolean integerValue(Scope scope, String variableName, int expectedValue) {
        Object o = scope.evaluate(variableName);
        int value = (int) Float.parseFloat(o.toString());
        return (expectedValue == value);
    }
}
