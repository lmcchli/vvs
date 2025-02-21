/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.ntf.util.PersistentObject;
import java.util.*;

public class MwiOffInfo implements PersistentObject {
    private static final String type = "MWI";
    private String telephoneNumber;
   
    private String key;
    
    public MwiOffInfo(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
    
    /** 
     * Empty constructor used by persistentQueue 
     */
    public MwiOffInfo() {
        telephoneNumber = "";
    }
    
    public String getTelephoneNumber() {
        return telephoneNumber;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getStringData() {
        return telephoneNumber;
    }
    
    public String getType() {
        return type;
    }
    
    public void parseStringData(String data) {
        if( data != null ) {
            telephoneNumber = data.trim();
        }
        
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
}
