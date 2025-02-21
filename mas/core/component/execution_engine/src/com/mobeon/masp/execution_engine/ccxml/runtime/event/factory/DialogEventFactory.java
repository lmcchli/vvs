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
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.eventnotifier.Event;
import org.mozilla.javascript.ScriptableObject;

public class DialogEventFactory implements EventFactory {


    public CCXMLEvent create(
            VXMLExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineMessage(message);
        event.defineReason(message);
        event.defineProperty(Constants.CCXML.DIALOG_ID, dialog.getDialogId().toString(), ScriptableObject.READONLY);
        if (conn != null)
            event.defineConnectionId(conn);
        event.defineSourceRelated(ec);
        return event;
    }

    public SimpleEvent create(
            ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineMessage(message);
        if (dialog != null) {
            event.defineProperty(Constants.CCXML.DIALOG_ID, dialog.getDialogId().toString(), ScriptableObject.READONLY);
        }
        if (conn != null)
            event.defineConnectionId(conn);
        event.defineSourceRelated(ec);
        return event;
    }
}
