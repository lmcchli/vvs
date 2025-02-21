package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;

/**
 * Selects all play related events sent by stream
 */
public class PlayRule extends EventRuleBase {

    public boolean isValid(Event e) {
        if(e instanceof SimpleEvent && ((SimpleEvent)e).getEvent().startsWith("internal.play."))
            return logIfValid(true, e);
        else
            return false;
    }

    public String toString() {
        return "isPlay()";
    }

}
