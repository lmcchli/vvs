/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

public class NotRule extends EventCategoryRuleBase {
    private final EventRule rule;

    public NotRule(EventRule rule) {
        this.rule = rule;
    }

    public Category categoryOf(Event e) {
        return logIfNotFalse(categoryNot(rule.categoryOf(e)),e);
    }

    public String toString() {
        return "not("+rule+")";
    }
}
