package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-21
 * Time: 11:50:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class EventCategoryRuleBase extends EventRuleUtil implements EventRule {
    public boolean isValid(Event e) {
        return collapse(categoryOf(e));
    }
}
