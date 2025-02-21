/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CCXMLSelector extends Selector {

    static Map<String, Selector> selectors = new ConcurrentHashMap<String, Selector>();
    static final Selector factory = new CCXMLSelector("");

    private CCXMLSelector(String event) {
        event = event.trim();
        //Regexp delimiter
        int i;
        if ((i = event.indexOf('*')) >= 0) {
            if (i == 0) {
                prefix = null;
                wildcard = toJavaRegex(event);
                kind = Kind.REGEX;
            } else {
                prefix = event.substring(0, i - 1);
                wildcard = toJavaRegex(event.substring(i));
                kind = Kind.PREFIX_AND_REGEX;
            }
        } else {
            prefix = event;
            wildcard = null;
        }
    }

    public Map<String, Selector> getSelectors() {
        return selectors;
    }

    public Selector create(String event) {
        return new CCXMLSelector(event);
    }

    public static Selector parse(String event) {
        return factory.parseEvent(event);
    }

    public static Selector instance() {
        return factory;
    }
}
