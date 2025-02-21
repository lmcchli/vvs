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
 * Date: May 14, 2006
 * Time: 3:38:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorConnectionFactory implements EventFactory {
    public SimpleEvent create(ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        CCXMLEvent.defineDefault(ec, event, eventName, message, info);
        event.defineConnectionRelated(eventName, conn, (CCXMLExecutionContext) ec);
        if(message != null){
            event.defineReason(message);
        }
        return event;
    }
}
