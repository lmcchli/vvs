/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.Tools;

public class AndRule extends EventCategoryRuleBase {
    private final EventRule[] rules;

    AndRule(EventRule ... rules) {
        this.rules = rules;
    }

    public Category categoryOf(Event e) {
        for(EventRule rule:rules) {
            Category c;
            if((c = rule.categoryOf(e)) != EventRule.Category.TRUE)
                return logIfNotFalse(c,e);
        }
        return logIfNotFalse(Category.TRUE, e);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(BUFFER_CAPACITY);
        sb.append("and(");
        Tools.commaSeparate(sb,rules);
        sb.append(")");
        return sb.toString();
    }

}
