package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-15
 * Time: 10:28:54
 * To change this template use File | Settings | File Templates.
 */
public class MutatableEventRule extends EventRuleBase {
    private EventRule rule;
    private final boolean sync;

    public MutatableEventRule(boolean isSynchronized, EventRule rule) {
        this.sync = isSynchronized;
        if (this.sync) {
            synchronized (this) {
                this.rule = rule;
            }
        } else
            this.rule = rule;
    }

    public void setRule(EventRule rule) {
        if (rule == null)
            return;
        if (this.sync) {
            synchronized (this) {
                this.rule = rule;
            }
        } else
            this.rule = rule;
    }

    public boolean isValid(Event e) {
        EventRule active;
        if (this.sync) {
            synchronized (this) {
                active = rule;
            }
        } else
            active = rule;
        return active.isValid(e);
    }
}
