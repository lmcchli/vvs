/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.ntf.util.PersistentObject;

import java.util.*;

public class PhoneStateChangedInfo implements PersistentObject {
    private static final String type = "PSC";
    private String key;
    private String telephonenumber;
    private String state;
    private String[] par;
    private int paramCount;

    public PhoneStateChangedInfo(String[] p) {
        telephonenumber = p[0];
        state = p[2];
        par = p;
        paramCount = p.length - 3;
    }

    /**
     * Get the telephonenumber.
     *@return the telephonenumber
     */
    public String getTelephonenumber() { return telephonenumber; }

    /**
     * Get the state.
     *@return the state
     */
    public String getState() { return state; }

    /**
     * Get the Nth parameter.
     *@return the Nth parameter value.
     */
    public String getParam(int i) { return par[i + 3]; }

    /**
     * Get the number of params
     * @return the number of params
     */

    public int getParamCount() { return paramCount; }

    public void parseStringData(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, ";");
        if( tokenizer.countTokens() == 3 ) {
            telephonenumber = tokenizer.nextToken();
            state = tokenizer.nextToken();
            String paramStr = tokenizer.nextToken();
            StringTokenizer paramTokenizer = new StringTokenizer(paramStr, ",");
            String[] p = new String[paramTokenizer.countTokens()];
            int index = 0;
            while(paramTokenizer.hasMoreTokens()) {
                p[index++] = paramTokenizer.nextToken();
            }
            par = p;

        }
    }

    public String getStringData() {
        String paramStr = "";
        for( int i=0;i<par.length;i++ ) {
            paramStr += par[i];
            if( i+1 !=  par.length) {
                paramStr += ",";
            }
        }
        return telephonenumber + ";" + state + ";" + paramStr;
    }

    public String getType() {
        return type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

