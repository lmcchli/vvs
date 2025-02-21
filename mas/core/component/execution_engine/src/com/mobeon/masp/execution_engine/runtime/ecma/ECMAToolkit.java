/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.ecma;

import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import static com.mobeon.masp.util.Tools.println;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.*;

import java.lang.reflect.Method;

public class ECMAToolkit {
    private static ILogger log = ILoggerFactory.getILogger(ECMAToolkit.class);

    //--------     Sample code below this line ------------------



    public static class OurObject extends ScriptableObject {
        private String className;

        private static Method longCallMethod;
        private static Method printMethod;
        private static ILogger log = ILoggerFactory.getILogger(OurObject.class);
        public ResumableCall continuation;

        private static Method returnString;

        static {
            try {
                longCallMethod = OurObject.class.getMethod("longCall", Scriptable.class);
                returnString = OurObject.class.getMethod("returnString");
                printMethod = OurObject.class.getMethod("print",Context.class, Scriptable.class,
                                                        Object[].class, Function.class);
            } catch (NoSuchMethodException e) {
                Ignore.exception(e);
            }
        }

        public OurObject(String className) {
            this.className = className;
            FunctionObject longCall = new FunctionObject("longCall", longCallMethod, this);

            FunctionObject ret = new FunctionObject("ret", returnString, this);
            defineProperty("ret",ret,ScriptableObject.READONLY);

            defineProperty("longCall", longCall, ScriptableObject.READONLY);
            defineProperty("print",new FunctionObject("print",printMethod,this),ScriptableObject.READONLY);
        }

        public Object returnString() {
            return "hello";
        }

        public void longCall(final Scriptable value) {
            println("Entering longcall !");
            if (value instanceof Function) {
                final Function f = (Function) value;
                if ("Continuation".equals(f.getClassName())) {
                    final Context cx = Context.getCurrentContext();
                    throw new ContinuationAbortedException("mobeon.continuation.aborted",new ResumableCall() {
                        public Object resume(Object resumedResult) {
                            return f.call(cx, OurObject.this, null, new Object[]{new AsyncResult(resumedResult)});
                        }
                    });
                }
            }
        }

        public String getClassName() {
            return className;
        }

        public void put(String string, Scriptable scriptable, Object object) {
            super.put(string, scriptable, object);
            println(className + ": Property " + string + " set to " + object.toString());
        }

        public static void print(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0)
                    Tools.print(" ");

                // Convert the arbitrary JavaScript value into a string form.
                String s = Context.toString(args[i]);

                Tools.print(s);
            }
            Tools.println();
        }

    }

    public static void main(String[] args) {
        ILoggerFactory.configureAndWatch("execution_engine/test_log.xml");
        ContextFactory.initGlobal(new WatchdogContextFactory());
        Context cx = Context.enter();
        OurObject session = new OurObject("Session");
        session = (OurObject) cx.initStandardObjects(session, false);
        if(args.length != 0 && args[0].equals("--continuation"))
            testContinuation(cx, session);
        else if(args.length != 0 && args[0].equals("--string"))
            testString(cx,session);
        else
            testScoping(cx, session);
        Context.exit();
    }

    private static void testString(Context cx, OurObject session) {
        Object o = cx.evaluateString(session,"ret()","ecma",1,null);
        String s = (String)o;
        Tools.println(s);
    }

    private static void testScoping(Context cx, OurObject session) {
        cx.evaluateString(session, "var session='wrong'", "ecma", 1, null);
        ScriptableObject dialog = new OurObject("Dialog");
        dialog.defineProperty("both", "dialog", 0);
        session.defineProperty("both", "session", 0);
        dialog.setParentScope(session);
        Object result = cx.evaluateString(dialog, "session='ok';dialog='ok';session+','+dialog+','+both", "ecma", 1, null);
        if (log.isDebugEnabled()) log.debug(result);
        result = cx.evaluateString(dialog, "both='ok'", "ecma", 1, null);
        result = cx.evaluateString(dialog, "topScope='session'", "ecma", 1, null);
        if (log.isDebugEnabled()) log.debug("session in session is: " + session.get("session", session));
        if (log.isDebugEnabled()) log.debug("both in session is: " + session.get("both", session));
        if (log.isDebugEnabled()) log.debug("both in dialog is: " + dialog.get("both", dialog));
    }

    private static void testContinuation(Context cx, OurObject session) {
        try {
            Object result = cx.evaluateString(session,
                                              "function continuation() {" +
                                              "   return new Continuation();" +
                                              "}" +
                                              "var call = continuation();" +
                                              "print('Before');" +
                                              "if(call.isDone != true)" +
                                              "   longCall(call);" +
                                              "print('Result after: '+call.result);" +
                                              "'result string'"
            , "ecma", 1, null);
        } catch (ContinuationAbortedException ee) {
            if (ee.getMessage().startsWith("mobeon.continuation.aborted")) {
                try {
                    Object resultString = ee.getResumable().resume("Continuation resumed !");
                    println("The continuation returned " + resultString);
                } catch (Exception e) {
                    Ignore.exception(e);
                }
            }
        }
    }
}
