/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;

/**
 * Rule that selects events passed from a dialog
 * to a CCXML context.
 */
public class DialogEventRule extends EventRuleBase {

    public boolean isValid(Event e) {
        return logIfValid(e instanceof SimpleEvent && (((SimpleEvent) e).getEvent().startsWith("dialog")
                                            || ((SimpleEvent) e).getEvent().startsWith("error.dialog")), e);
    }

    public String toString() {
        return "isDialogEvent()";
    }
}
