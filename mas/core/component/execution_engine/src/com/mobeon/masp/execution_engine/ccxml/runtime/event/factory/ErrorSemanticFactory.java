/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.common.eventnotifier.Event;
import org.mozilla.javascript.ScriptableObject;

public class ErrorSemanticFactory implements EventFactory {

    public SimpleEvent create(
            ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo debugInfo , Event related) {
        CCXMLEvent event = new CCXMLEvent(debugInfo);
        event.defineName(eventName);
        event.defineMessage(message);
        event.defineConnectionId(conn);
        event.defineSessionId(ec.getSessionId());
        event.defineSourceRelated(ec);
        event.defineProperty(Constants.VoiceXML.TAGNAME, debugInfo.getTagName(), ScriptableObject.READONLY);
        event.defineProperty(Constants.VoiceXML.REASON, message, ScriptableObject.READONLY);
        event.defineProperty(Constants.VoiceXML.LOCATION, debugInfo.getLocation(), ScriptableObject.READONLY);
        return event;
    }
}
