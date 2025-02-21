/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import static com.mobeon.masp.execution_engine.runtime.event.Selector.Kind.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceXMLSelector extends Selector {

    static Map<String, Selector> selectors = new ConcurrentHashMap<String, Selector>();
    static final Selector factory = new VoiceXMLSelector("");

    private VoiceXMLSelector(String event) {
        event = event.trim();
        //Regexp delimiter
        int i;
        if("".equals(event) || ".".equals(event)) {
            prefix = null;
            wildcard = "(?!internal\\.)"+toJavaRegex("*");
            kind = REGEX;
        } else {
            prefix = event;
            wildcard = toJavaRegex(".*");
            kind = PREFIX_MAYBE_REGEX;
        }
    }

    public Selector create(String event) {
        return new VoiceXMLSelector(event);
    }

    public Map<String, Selector> getSelectors() {
        return selectors;
    }

    public static Selector parse(String event) {
        return factory.parseEvent(event);
    }
    public static Selector instance() {
        return factory;
    }
}
