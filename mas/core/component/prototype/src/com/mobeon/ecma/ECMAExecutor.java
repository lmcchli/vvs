/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.ecma;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

/**
 * This class provides a simple interface for execution of ECMA scripts.
 * It uses Rhino from the Mozilla project for the actual execution.
 *
 * @author David Looberger, david@looberger.com
 */
//public class ECMAExecutor extends ImporterTopLevel implements Runnable{
public class ECMAExecutor extends ScriptableObject implements Runnable{
    public static Logger logger = Logger.getLogger("com.mobeon");

    private Context cx;
    private  Scriptable scope;
    private Stack scopeStack;
    private int callDepth;
    private int rootDocumentCallDepth;
    private boolean isInRootDocument;
    protected final Global global = new Global();


    public ECMAExecutor() {
        // super();
        init();
    }
    public ECMAExecutor(Traverser traverser) {
        // super();
        init();
        this.putIntoScope("graphTraverser", traverser);
    }

    private void init() {
        enterContext();
        global.init(cx);

        cx.setLanguageVersion(Context.VERSION_1_6);
        cx.initStandardObjects(this);
        String[] names = { "print" };
        this.defineFunctionProperties(names, ECMAExecutor.class,
                                           ScriptableObject.DONTENUM);
        // this.init(cx,this, false);
        scope = global;
        scopeStack = new Stack();
        callDepth = 0;
        rootDocumentCallDepth = 0;
        isInRootDocument = true;
    }

    public void enterContext() {
        cx = Context.enter();
    }

    public String getClassName() {
        return "ECMAExecutor";
    }

    public boolean isInRootDocument() {
        return isInRootDocument;
    }

    public void setInRootDocument(boolean inRootDocument) {
        this.isInRootDocument = inRootDocument;
    }
    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function.
     * Note that its arguments are of the "varargs" form, which
     * allows it to handle an arbitrary number of arguments
     * supplied to the JavaScript function.
     *
     * Used when the print function is called from the ECMA script
     *
     */
        public static void print(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj)
        {
            for (int i=0; i < args.length; i++) {
                if (i > 0)
                    System.out.print(" ");

                // Convert the arbitrary JavaScript value into a string form.
                String s = Context.toString(args[i]);

                System.out.print(s);
            }
            System.out.println();
        }


    public void exitContext() {
        Context.exit();
    }
    public boolean existInScope(String name) {
       Object ret =  ScriptableObject.getProperty(scope,name);
        if (ret == Scriptable.NOT_FOUND )
            return false;
        else
            return true; 
    }

    public void removeFromScope(String name) {
        scope.delete(name);   
    }

    public Object exec(String ecmascript) {
        enterContext();
        Object ret = cx.evaluateString(scope, ecmascript, "ecmascript", 1, null);
        exitContext();
        return ret;
    }

    public Object execFile(String ecmascriptLocator) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ecmascriptLocator));
            enterContext();
            Object ret = cx.evaluateReader(scope, br, "ecmascript", 1, null);
            exitContext();
            return ret;

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public void newScope() {
        Scriptable newScope = cx.newObject(scope);
        // newScope.setPrototype(scope);
        newScope.setParentScope(scope);
        scopeStack.push(scope);
        scope = newScope;
        if (isInRootDocument)
            rootDocumentCallDepth++;
        else
            callDepth++;
    }

    public void leaveScope() {
        Scriptable newScope = (Scriptable) scopeStack.pop();
        if (newScope != null) {
            if (isInRootDocument)
                rootDocumentCallDepth--;
            else
                callDepth--;
            scope = newScope;
        }
        // Else, we are at the top scope and does nothing
    }


    public int getCallDepth() {
        if (isInRootDocument)
            return rootDocumentCallDepth;
        else
            return callDepth;
    }

    public void putIntoScope(String name , Object obj) {
        enterContext();
        ScriptableObject.putProperty(scope,name,obj);
        exitContext();
        logger.debug("Property " + name + " is put into ECMA space");
    }

    public Object getFromScope(String name) {
        enterContext();
        Object ret =  ScriptableObject.getProperty(scope,name);
        exitContext();
        if (ret == Scriptable.NOT_FOUND )
            return null;
        else
            return ret;
    }

    public void run() {
               ECMAExecutor e = new ECMAExecutor();

        e.exec("var d = 1;");
        e.exec("function myFunc() {print('In original');}");
        e.exec("print(d);");
        e.exec("myFunc();");
        e.newScope();
        e.exec("var d = 4;");
        e.exec("function myFunc() {print('In changed');}");
        e.exec("function myNewFunc() {print('In new');}");
        e.exec("print(d);");
        e.exec("myFunc();");
        e.exec("myNewFunc();");
        System.out.println("Calldepth is " + e.getCallDepth());
        e.leaveScope();
        System.out.println("Calldepth is " + e.getCallDepth());
        e.exec("print(d);");
        e.exec("myFunc();");
        System.out.println("Calling demoscript");
        e.execFile("src/com/mobeon/ecma/test/test2.js");
        NativeObject q = (NativeObject) e.exec("foo();");
        System.err.println("Class = " + q.getClass());
        e.putIntoScope("arne", q);
        e.exec("bar(arne);") ;

        System.out.println("Testing return value (boolean)");
        Object ret =  e.exec("1 != 2;")  ;
        if (((Boolean) ret).booleanValue()) {
            System.out.println("ret == true");
        }
        else {
            System.out.println("ret == false");
        }

        Cond c = new Cond("false");
        if (c.isCond(e)) {
            System.out.println("Cond == true");
        }
        else {
            System.out.println("Cond == false");
        }
    }


    public static void main(String argv[]) {
        PropertyConfigurator.configure("lib/mobeon.properties");

        ECMAExecutor e = new ECMAExecutor();
        e.run();
        /*
        System.out.println("\n\nAs separate threads\n\n");

        Thread t[] = new Thread[3];
        t[0] = new Thread(e);
        t[1] = new Thread(e);
        t[2] = new Thread(e);

        t[0].start();
        t[1].start();
        t[2].start();
        */


    }
}
