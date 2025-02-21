/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.EventFactory;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.eventnotifier.Event;

public class SimpleEventFactory implements EventFactory {
    public SimpleEvent create(
    ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo info, Event related) {
        return new SimpleEventImpl(eventName,message,info);
    }
}
