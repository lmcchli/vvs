/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.Tools;

public class OrRule extends EventCategoryRuleBase {
    private final EventRule[] rules;

    public OrRule(EventRule ... rules) {
        this.rules = rules;
    }


    public Category categoryOf(Event e) {
        for(EventRule rule:rules) {
            Category c = rule.categoryOf(e);
            if(c != Category.FALSE)
                return logIfNotFalse(c, e);
        }
        return Category.FALSE;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(BUFFER_CAPACITY);
        sb.append("or(");
        Tools.commaSeparate(sb,rules);
        sb.append(")");
        return sb.toString();
    }
}
