/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.voicexml.runtime.ShadowVarBase;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mozilla.javascript.ScriptableObject;
public class ScopeImplTest extends TestCase {

    public class Connection extends ScriptableObject {
        public String getClassName() {
            return "Connection";
        }
        public class Redirect extends ScriptableObject {
            public Redirect(){
            }
            public String getClassName() {
                return "Redirect";
            }
            public String number;
        }

        public Connection(){
            redirect = new Redirect();
        }
        public Redirect redirect;
    }



    public void testDeclareVariable() throws Exception {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        scope.evaluateAndDeclareVariable("kalle", "'kaka'");
        assertTrue(scope.isDeclaredInExactlyThisScope("kalle"));
        Scope scopeContext = scopeRegistry.createNewScope("session");
        scopeContext.evaluateAndDeclareVariable("kalle", "'kaka'");
        assertTrue(scopeContext.isDeclaredInExactlyThisScope("kalle"));

        // Test declare without initial value
        scopeContext.evaluateAndDeclareVariable("pelle", null);
        assertTrue(scopeContext.isDeclaredInAnyScope("pelle"));
    }

    public void testEvaluateString(){
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        scope.evaluateAndDeclareVariable("kalle", "7");
        scope.evaluateAndDeclareVariable("olle", "7");
        Object result = scope.evaluate("kalle+olle");
        assertNotNull(result);
        int value = (int) Float.parseFloat(result.toString());
        assertTrue(value == 14);
    }

    public void testGetValue() {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");

    }

    public void testIsDefined() {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        assertFalse(scope.isDeclaredInAnyScope("kalle"));
        scope.evaluateAndDeclareVariable("kalle", null);
        assertTrue(scope.isDeclaredInAnyScope("kalle"));
    }

    public void testSetValue() {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        scope.evaluateAndDeclareVariable("kalle", "8");
        Object o = new Object();
        scope.setValue("kalle", o);
        assertTrue(o == scope.evaluate("kalle"));
    }

    public void testSessionProperties(){
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");

        Connection connection = new Connection();
        connection.redirect.number = "hej micke";

        scope.declareReadOnlyVariable("connection", connection);
        connection.defineProperty("redirect", connection.redirect, ScriptableObject.READONLY);
        connection.redirect.defineProperty("number", connection.redirect.number, ScriptableObject.READONLY);

        scope.evaluateAndDeclareVariable("kalle", "'a'");
        Object o = scope.evaluate("kalle=connection.redirect.number");
        Object o2 = scope.evaluate("kalle=session.connection.redirect.number");

    }


       public void testLastresult(){
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        scopeRegistry.createNewScope("session");
        Scope scope = scopeRegistry.createNewScope("application");

        ShadowVarBase lastresult = new ShadowVarBase();
        scope.declareReadOnlyVariable ("lastresult$", lastresult);
        scope.evaluate("application.lastresult$.marktime = 10");
        Object o2 = scope.evaluate("application.lastresult$.marktime + 20");
        assert(((Integer) o2) == 30);
    }

    public void testLastEvaluationFailed() {
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        scope.evaluateAndDeclareVariable("kalle", "7");

        // Evaluate kalle, shall not fail
        Object result = scope.evaluate("kalle");
        assertFalse(scope.lastEvaluationFailed());

        // Evaluate a non existing var
        result = scope.evaluate("junk");
        assertTrue(scope.lastEvaluationFailed());

        // Evaluate kalle, shall not fail
        result = scope.evaluate("kalle");
        assertFalse(scope.lastEvaluationFailed());
    }

    public void testInterruptEvaluation() {
        ILoggerFactory.configureAndWatch("execution_engine/test/com/mobeon/masp/execution_engine/runapp/test_log.xml");
        ScopeRegistry scopeRegistry = new ScopeRegistryImpl(null);
        Scope scope = scopeRegistry.createNewScope("session");
        String script = "function be_busy(numberMillis) {\n" +
                "                    var now = new Date();\n" +
                "                    var exitTime = now.getTime() + numberMillis;\n" +
                "                    while (true) {\n" +
                "                    now = new Date();\n" +
                "                    if (now.getTime() > exitTime)\n" +
                "                        return;\n" +
                "                    }\n" +
                "                }\n" +
                "               be_busy(5000);";
        String script2 =" Thread.sleep(5000);";
        // Evaluate script, and interrupt it after 2s
        final Thread evalThr = Thread.currentThread();
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1500);
                    System.out.println("Interrupting eval on thread " + evalThr.getName());
                    System.out.println("Eval thread is in state "+ evalThr.getState());
                    evalThr.interrupt();
                    System.out.println("Eval thread is in state "+ evalThr.getState());
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        try {
            System.out.println("Evaluating script on thread " + Thread.currentThread().getName());
            Object result = scope.evaluate(script);
        }
        catch (Throwable e) {
            System.out.println("Caught exception :" + e.getMessage());
        }
        System.out.println("Finished eval");
        assertTrue(scope.lastEvaluationFailed());


    }


    public static Test suite() {
        return new TestSuite(ScopeImplTest.class);
    }
}