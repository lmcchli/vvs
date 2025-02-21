/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

public class TargetRule extends EventRuleBase  {
    private final String id;
    private final String targetType;

    public TargetRule(String id, String targetType) {
        this.id = id;
        this.targetType = targetType;
    }

    public boolean isValid(Event e) {
        boolean valid = false;
        if(e instanceof SimpleEvent) {
            SimpleEvent se = (SimpleEvent) e;
            valid = se.getTargetType() != null && se.getTargetType().equals(targetType);
            if(id != null)
                valid &= se.getTargetId() != null && se.getTargetId().equals(id);
        }
        return logIfValid(valid, e);
    }

    public String toString() {
        return "isTarget(id="+id+" type="+targetType+")";
    }
}
