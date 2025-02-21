package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;

import java.util.Collection;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-mar-13
 * Time: 11:11:05
 * To change this template use File | Settings | File Templates.
 */
public class SipMessageResponseFactory implements EventFactory {
    public SimpleEvent create(ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineMessage(message);
        event.defineSourceRelated(ec);
        event.defineSessionId(ec.getSessionId());
        if(related != null && related instanceof SipMessageResponseEvent){
            SipMessageResponseEvent e = (SipMessageResponseEvent) related;
            Collection<NamedValue<String,String>> params = e.getParams();
            for (NamedValue<String,String> param : params) {
                event.defineProperty(param.getName(), param.getValue(), ScriptableObject.READONLY);
            }
        }
        return event;
    }
}
