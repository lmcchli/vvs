/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.ntf.util.PersistentObject;
import java.util.*;

public class IvrToSmsInfo implements PersistentObject {
    private static final String type = "IVR";
    private String key = "";
    private String message;
    private String language;
    private String telephonenumber;
    
    public IvrToSmsInfo(String m, String l, String t) {
        message = m;
        language = l;
        telephonenumber = t;
        
    }
    
    public IvrToSmsInfo() {
        message = "";
        language = "";
        telephonenumber = "";
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getLanguage() {
        return language;
    }

    public String getTelephonenumber() {
        return telephonenumber;
    }

    public String getKey() {
        return key;
    }
    
    public String getStringData() {
        return message + ";" + language + ";" + telephonenumber;
    }
    
    public String getType() {
        return type;
    }
    
    public void parseStringData(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, ";");
        if( tokenizer.countTokens() == 3 ) {
            message = tokenizer.nextToken();
            language = tokenizer.nextToken();
            telephonenumber = tokenizer.nextToken();
        }
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
}
