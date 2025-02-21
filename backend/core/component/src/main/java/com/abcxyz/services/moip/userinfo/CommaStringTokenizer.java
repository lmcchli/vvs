/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.abcxyz.services.moip.userinfo;

import java.util.*;

/**
 * CommaStringTokenizer is like a StringTokenizer with the delimiter ",",
 * but it considers the empty string between two consecutive commas to be a
 * token, so it does not eat a sequence of commas in one big bite.
 */
public class CommaStringTokenizer {
    public CommaStringTokenizer(String s) {
        st= new StringTokenizer(s, ",", true);
    }
    
    public boolean hasMoreTokens() {
        return st.hasMoreTokens();
    }
    
    public String nextToken() {
        String token;
        
        if (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.equals(",")) {
                return "";
            } else {
                if (st.hasMoreTokens()) {
                    st.nextToken(); //Skip the next token, which must be a delimiter
                }
                return token;
            }
        } else {
            return "";
        }
    }
    
    private StringTokenizer st;

    /**
     * Extracts Properties from a list of keys and a corresponding list of
     * values. The lists are Strings with words separated by commas.
     */
    public static Properties getPropertiesFromLists(String keys, String values) {
        Properties props = new Properties();
        String key;
        String val;

        CommaStringTokenizer stKeys = new CommaStringTokenizer(keys); 
        CommaStringTokenizer stValues = new CommaStringTokenizer(values); 
        while(stKeys.hasMoreTokens()){
            key = stKeys.nextToken();
            val = stValues.nextToken();
            props.setProperty(key, val);
        }        
        return props;
    }
}
