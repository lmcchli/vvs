/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp.applications.vxml.value;

/**
 * @author David Looberger
 */
public class ValueGenerator {
    private String str;
    private int iVal;
    private float fVal;
    private boolean bVal;

    public ValueGenerator(String str, int intValue, float floatValue, boolean boolVal) {
        this.str = str;
        this.iVal = intValue;
        this.fVal = floatValue;
        this.bVal = boolVal;
    }

    public String getString() {
        return str;
    }

    public int getInt(){
        return iVal;
    }

    public float getFloat() {
        return fVal;
    }

    public boolean getBool() {
        return this.bVal;
    }

    public String[] getStringArray() {
        String[] arr = new String[3];
        arr[0] = str;
        arr[1] = str;
        arr[2] = str;
        return arr;
    }
}
