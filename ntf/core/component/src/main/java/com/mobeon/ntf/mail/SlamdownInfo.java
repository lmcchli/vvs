/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.ntf.util.PersistentObject;
import java.util.*;

public class SlamdownInfo implements PersistentObject {
    private static final String type = "SLAM";
    private String _message;
    private String _mail;
    private int _callType;
    private String key;
    
    public SlamdownInfo(String mail, String message, int callType) {
        _mail = mail;
        _message = message;
        _callType = callType;
    }
    
    /** 
     * Empty constructor used by persistentQueue 
     */
    public SlamdownInfo() {
        _message = new String();
        _mail = new String();
    }
    
    public String getMessage() {
        return _message;
    }
    
    public String getMail() {
        return _mail;
    }
    
    public int getCallType() {
        return _callType;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getStringData() {
        return _mail + ";" + _message + ";" + _callType;
    }
    
    public String getType() {
        return type;
    }
    
    public void parseStringData(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, ";");
        if( tokenizer.countTokens() == 3 ) {
            _mail = tokenizer.nextToken();
            _message = tokenizer.nextToken();
            String ct = tokenizer.nextToken();
            try {
                _callType = Integer.parseInt(ct);
            } catch (NumberFormatException ne) {}
        }
        
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
}
