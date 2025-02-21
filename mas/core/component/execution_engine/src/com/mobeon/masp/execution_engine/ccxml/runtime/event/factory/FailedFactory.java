package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.events.FailedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 4, 2006
 * Time: 5:20:14 PM
 * To change this template use File | Settings | File Templates.
 */

public class FailedFactory implements EventFactory {
    public SimpleEvent create(
            ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineMessage(message);
        event.defineConnectionId(conn);
        event.defineSourceRelated(ec);
        event.defineSessionId(ec.getSessionId());
        if(related instanceof FailedEvent){
            FailedEvent failedEvent = (FailedEvent) related;
            int networkStatusCode = failedEvent.getNetworkStatusCode();
            event.defineReason("" + networkStatusCode);
        }
        return event;
    }
}
