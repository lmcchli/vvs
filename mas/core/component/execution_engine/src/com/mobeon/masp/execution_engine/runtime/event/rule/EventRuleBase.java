package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

/**
 * @author Mikael Andersson
 */
public abstract class EventRuleBase extends EventRuleUtil implements EventRule {

    public Category categoryOf(Event e) {
        return isValid(e)? EventRule.Category.TRUE: EventRule.Category.FALSE;
    }

}
