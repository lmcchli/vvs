package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 3, 2006
 * Time: 2:30:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class EarlyMediaAvailableFactory implements EventFactory{
    public SimpleEvent create(ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineMessage(message);
        event.defineConnectionId(conn);
        event.defineSourceRelated(ec);
        event.defineSessionId(ec.getSessionId());
        event.defineConnectionRelated(eventName, conn, (CCXMLExecutionContext) ec);
        return event;
    }
}
