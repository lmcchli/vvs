package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.common.eventnotifier.Event;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 31, 2006
 * Time: 2:11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionRelatedRule extends EventRuleBase {
    public boolean isValid(Event e) {
        return logIfValid(e instanceof SimpleEvent && (((SimpleEvent) e).getEvent().startsWith("error.semantic")
                                            || ((SimpleEvent) e).getEvent().startsWith("error.com.mobeon.platform")), e);
    }

     public String toString() {
        return "isExecutionRelated()";
    }
}
