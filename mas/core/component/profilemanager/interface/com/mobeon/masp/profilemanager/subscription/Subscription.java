/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.subscription;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for setting subscriber parameters for subscriber creation
 *
 * @author mande
 */
public class Subscription {
    private Map<String, String[]> attributes = new HashMap<String, String[]>();

    public void addAttribute(String name, String value) {
        addAttribute(name, new String[]{value});
    }

    public void addAttribute(String name, String[] value) {
        attributes.put(name, value);
    }

    public Map<String, String[]> getAttributes() {
        return attributes;
    }
}
