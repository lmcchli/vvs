package com.mobeon.masp.execution_engine.ccxml.runtime.event.rule;

import com.mobeon.masp.callmanager.events.*;
import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;

/**
 * @author Mikael Andersson
 */
public class CallManagerEventRule extends ClassRule {
    public CallManagerEventRule() {
        super(JoinedEvent.class,
                FailedEvent.class,
                ConnectedEvent.class,
                SendTokenErrorEvent.class,
                UnjoinErrorEvent.class,
                EarlyMediaAvailableEvent.class,
                EarlyMediaFailedEvent.class,                
                NotAllowedEvent.class,
                ProgressingEvent.class,
                AlertingEvent.class,
                DisconnectedEvent.class,
                ErrorEvent.class,
                UnjoinedEvent.class,
                JoinErrorEvent.class,
                SipMessageResponseEvent.class,
                ProxiedEvent.class,
                SubscribeEvent.class,
                RedirectedEvent.class
        );
    }
}
