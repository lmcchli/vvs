/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

import java.util.HashMap;
import java.util.Map;

public abstract class AnalyzeClassRule extends  EventRuleBase {

    protected final Map<Class, EventRule> byClass = new HashMap<Class,EventRule>();

    public static class RulePair {
        Class clazz;
        EventRule rule;

        public RulePair(Class clazz, EventRule rule) {
            this.clazz = clazz;
            this.rule = rule;
        }
    }

    public AnalyzeClassRule(RulePair ... rules) {
        for(RulePair pair:rules) {
            byClass.put(pair.clazz,pair.rule);
        }
    }

    public boolean isValid(Event event) {
        Class c = event.getClass();
        EventRule ea = byClass.get(c);
        if (ea != null) {
            return logIfValid(ea.isValid(event), event);
        } else
            return logIfValid(getDefault(), event);
    }


    public boolean getDefault() {
        return false;
    }

    public String toString() {
        return "isClassWith("+byClass.entrySet()+")";
    }
}
