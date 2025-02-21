/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;

/**
 * Select record event to be received by VoiceXML
 */
public class RecordRule extends EventRuleBase {
    public boolean isValid(Event e) {
        return logIfValid(e instanceof SimpleEvent && ((SimpleEvent) e).getEvent().startsWith("internal.record."), e);
    }

    public String toString() {
        return "isRecord()";
    }
}
