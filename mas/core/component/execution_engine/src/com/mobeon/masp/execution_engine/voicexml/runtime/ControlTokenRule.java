/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.stream.ControlTokenEvent;

/**
 * @author David Looberger
 */
public class ControlTokenRule extends EventRuleBase {
    public boolean isValid(Event e) {
        return logIfValid(e instanceof ControlTokenEvent, e);
    }

    public String toString() {
        return "isControlToken()";
    }
}
