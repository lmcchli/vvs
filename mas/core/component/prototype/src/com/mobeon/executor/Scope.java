/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor;

import java.util.HashMap;
import java.util.Stack;
import java.util.Iterator;


/**
 * Base class for managing scoping for various objects. Can be used to manage named VXML nodes,
 * named Exceptions, variable etc. One Scope object per type of object to manage should be created,
 * e.g. one Node manager, one Exception manager etc.
 */
public class Scope {

    private Stack scope;
    private HashMap values;
    private int callDepth;

    public Scope() {
        this.values = new HashMap();
        this.scope = new Stack();
        this.callDepth = 0;
    }

    public HashMap getValues() {
         return values;
     }

     public void setValues(HashMap values) {
         this.values = values;
     }

    public Stack getScope() {
        return scope;
    }

    public void setScope(Stack scope) {
        this.scope = scope;
    }

    public void newScope() {
        HashMap newValues = new HashMap(this.values);
        scope.push(values);
        values = newValues;
        callDepth++;
    }

    public void leaveScope() {
        if (callDepth >= 1) {
            HashMap oldValues = (HashMap) scope.pop();
            if (oldValues != null) {
                callDepth--;
                values = oldValues;
            }
        }
    }

    public void addSymbol(Object key, Object val) {
        values.put(key, val);
    }


    public Object getSymbol(Object key) {
        if (values.containsKey(key)) {
            return values.get(key);
        }
        else {
            return null;
        }
    }

    public Iterator iterator() {
        return values.keySet().iterator();
    }

    public static void main(String argv[]) {
        Scope s = new Scope();
        s.addSymbol("A", new String("A_VAL"));
        s.addSymbol("B", new String("B_VAL"));

        for (Iterator it = s.iterator(); it.hasNext();) {
            String key = (String) it.next();
            System.out.println("Symbol :" + key + " value : " + s.getSymbol(key));
        }
        System.out.println("New scope");
        s.newScope();
        s.addSymbol("A", new String("A_VAL_2"));

        for (Iterator it = s.iterator(); it.hasNext();) {
            String key = (String) it.next();
            System.out.println("Symbol :" + key + " value : " + s.getSymbol(key));
        }
        System.out.println("Leaving scope");
        s.leaveScope();
        for (Iterator it = s.iterator(); it.hasNext();) {
            String key = (String) it.next();
            System.out.println("Symbol :" + key + " value : " + s.getSymbol(key));
        }
        System.out.println("Leaving scope");
        s.leaveScope();
        for (Iterator it = s.iterator(); it.hasNext();) {
            String key = (String) it.next();
            System.out.println("Symbol :" + key + " value : " + s.getSymbol(key));
        }
    }
}
