/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

import com.mobeon.common.trafficeventsender.email.EmailEvent;

import java.util.HashMap;

/**
 * This class is used to create a Traffic Event. Each Traffic Event is identified by its name.
 * Contains a properties map that has the attributes that should be sent in the event.
 *
 * @author ermmaha
 */
public class TrafficEvent implements EmailEvent {

    private String name;
    private HashMap<String, String> properties;
    
    /** The name of the property for a payload. */
    public static final String PAYLOAD_PROPERTY_NAME = "payload";
    
    /** The payload, in case of a payload event. */
    private String payload;

    /**
     * Constructor
     */
    public TrafficEvent() {
        payload = null;
        properties = new HashMap<String, String>();
    }

    /**
     * Constructor
     *
     * @param name
     */
    public TrafficEvent(String name) {
        this.name = name;
        payload = null;
        properties = new HashMap<String, String>();
    }

    /**
     * Retrieves the name on the event
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name on the event
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the properties map
     *
     * @return HashMap
     */
    public HashMap<String, String> getProperties() {
        return properties;
    }

    /**
     * Adds a property to the properties map
     *
     * @param propertyName
     * @param value
     */
    public void setProperty(String propertyName, String value) {
        if (propertyName.equals(PAYLOAD_PROPERTY_NAME)) {
            // If payload, set payload to value
            this.payload = value;
        }
        else {
            properties.put(propertyName, value);
        }
    }
    
    /**
     * Returns whether or not this TrafficEvent has a payload or not.
     */
    public boolean hasPayload()
    {
        return (payload != null);
    }
    
    /**
     * Returns the payload of this TrafficEvent, if applicable. If not, returns null.
     * The payload can be verified through the <code>hasPayload()</code> method.
     */
    public String getPayload()
    {
        return payload;
    }
}