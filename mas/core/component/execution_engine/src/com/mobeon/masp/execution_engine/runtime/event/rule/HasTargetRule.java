/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

public class HasTargetRule extends EventRuleBase {
    public boolean isValid(Event e) {
        if(e instanceof SimpleEvent) {
            SimpleEvent se = (SimpleEvent) e;
            return logIfValid(se.getTargetType() != null && se.getTargetId() != null, e);
        }
        return false;
    }

    public String toString() {
        return "hasTargetRule()";
    }
}
