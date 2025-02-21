/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

public class SimpleEventRule extends EventRuleBase {
    private final boolean prefixMatch;
    private final String[] eventNames;

    public SimpleEventRule(boolean prefixMatch,String ... eventNames) {
        this.prefixMatch = prefixMatch;
        this.eventNames = eventNames;
    }

    public boolean isValid(Event e) {
        if(e instanceof SimpleEvent) {
            SimpleEvent se = (SimpleEvent) e;
            for(String eventName:eventNames) {
                if(!prefixMatch) {
                    if(eventName.equals(se.getEvent())){
                        return logIfValid(true, e);
                    }
                } else {
                    if(se.getEvent().startsWith(eventName) || se.getEvent().equals(eventName)){
                        return logIfValid(true, e);
                    }
                }
            }
        }
        return false;
    }

    public String toString(){
        String eventNamesStr = "";

        for (String s : eventNames) {
            eventNamesStr = eventNamesStr+","+s;
        }
        return super.toString()+",eventNames"+eventNamesStr+"prefixMatch:"+prefixMatch;
    }
}
