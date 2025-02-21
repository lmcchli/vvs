/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.EventFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.common.eventnotifier.Event;

public class ConnectionEventFactory implements EventFactory {

    public SimpleEvent create(
    ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineConnectionRelated(eventName, conn, (CCXMLExecutionContext) ec);
        return event;
    }
}
