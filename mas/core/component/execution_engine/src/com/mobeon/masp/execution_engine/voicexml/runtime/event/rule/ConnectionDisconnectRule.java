package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * @author David Looberger
 */
public class ConnectionDisconnectRule extends EventRuleBase {
    private VXMLExecutionContext context;
    private String targetType;
    private String id;

    public ConnectionDisconnectRule(VXMLExecutionContext context, String id, String targetType) {
        this.context = context;
        this.id = id;
        this.targetType = targetType;
    }

    public boolean isValid(Event e) {
        boolean valid = false;
        if (e instanceof SimpleEvent) {

            final String event = ((SimpleEvent) e).getEvent();
            if (
                    event.startsWith("connection.disconnect.")
                            || event.equals("connection.disconnect")) {
                SimpleEvent se = (SimpleEvent) e;
                valid = se.getTargetType() != null && se.getTargetType().equals(targetType);
                if (id != null)
                    valid &= se.getTargetId() != null && se.getTargetId().equals(id);
            }
        }
        return logIfValid(valid, e);
    }

    public String toString() {
        return "isConnectionDisconnect()";
    }
}
