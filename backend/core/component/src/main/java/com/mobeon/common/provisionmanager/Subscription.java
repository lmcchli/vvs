/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a subcriber. Contains a list of attribute-value pairs. The format of the attribute name and value is as used
 * in the CAI interface.
 *
 * @author ermmaha
 */
public class Subscription {
    private Map<String, String[]> attributes = new HashMap<String, String[]>();

    /**
     * Adds an attribute (single value).
     *
     * @param name
     * @param value
     */
    public void addAttribute(String name, String value) {
        attributes.put(name, new String[]{value});
    }

    /**
     * Adds an attribute (multi value).
     *
     * @param name
     * @param value
     */
    public void addAttribute(String name, String[] value) {
        attributes.put(name, value);
    }

    /**
     * Retrives the attribute map
     *
     * @return attribute map
     */
    public Map<String, String[]> getAttributes() {
        return attributes;
    }
}
