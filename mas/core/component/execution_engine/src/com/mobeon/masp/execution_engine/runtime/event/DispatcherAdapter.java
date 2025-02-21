/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.Event;

/**
 * @author Mikael Andersson
 */
public class DispatcherAdapter implements IEventReceiver {

    IEventDispatcher dispatcher;

    public DispatcherAdapter(IEventDispatcher dispatacher) {
        this.dispatcher = dispatacher;
    }

    public void doEvent(Event event) {
        dispatcher.fireEvent(event);
    }

    public void doGlobalEvent(Event event) {
        dispatcher.fireEvent(event);
    }
}
