/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

public class EventRules {

    public static final EventRule TRUE_RULE = new EventRuleBase(){
        public boolean isValid(Event e) {
            return logIfValid(true, e);
        }

        public String toString() {
            return "TRUE";
        }
    };
    public static final EventRule FALSE_RULE = new EventRuleBase(){
        public boolean isValid(Event e) {
            return false;
        }

        public String toString() {
            return "FALSE";
        }
    };


    public static EventRule not(EventRule rule) {
        return new NotRule(rule);
    }

    public static EventRule or(EventRule ... rules) {
        return new OrRule(rules);
    }

    public static EventRule and(EventRule ... rules) {
        return new AndRule(rules);
    }
}
