/**
 * Copyright (c) 2005 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;


public interface PersistentObject {
    
    /**
     * Creates object variables from the string used in getStringData.
     */
    public void parseStringData(String data);
    /**
     * Return a string representation of the object
     */
    public String getStringData();
    /**
     *Return a String that tells the type od object. 
     */
    public String getType();
    
    /**
     *Sets a key for this object. The key is set from persistentQueue.
     *the key should not be set otherwise!
     */
    public void setKey(String key);
    /**
     *returns the key.
     */
    public String getKey();
    
    
}
