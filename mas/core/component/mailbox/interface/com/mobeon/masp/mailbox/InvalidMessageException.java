/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Thrown from the Mailbox when trying to store a message with invalid property values.
 * The InvalidMessageException holds a Map of invalid property values.
 * @author qhast
 */
public class InvalidMessageException extends MailboxException {

    private Map<String,Object> invalidProperties = new HashMap<String,Object>();

    public InvalidMessageException() {
        super("Storable message is invalid!");
    }

    /**
     * Adds an invalid property value to the Exception. Null is permitted for property value.
     * Property name cannot be null or empty.
     * @param propertyName the name of the invalid property.
     * @param propertyValue the invalid property value.
     */
    public void addInvalidPropertyValue(String propertyName, Object propertyValue) {
        if(propertyName==null || propertyName.length()==0) {
            throw new IllegalArgumentException("propertyName cannot be null or empty!");            
        }
        invalidProperties.put(propertyName,propertyValue);
    }

    /**
     * This method returns a read-only reference to the internal map of invalid property values.
     * The name of the property is used as key.
     * @return the Map of invalid property values
     */
    public Map<String, Object> getInvalidProperties() {
        return Collections.unmodifiableMap(invalidProperties);
    }

    /**
     * Returns a message including the list of invalid properties as a key-value-pair list.
     *
     * @return the detail message string.
     */
    @Override
    public String getMessage() {
        return super.getMessage()+": invalidProperties="+invalidProperties;
    }


}
