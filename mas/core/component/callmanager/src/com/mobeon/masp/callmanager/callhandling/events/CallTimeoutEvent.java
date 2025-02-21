/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.callmanager.callhandling.CallTimerTask;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * A call timeout event occurs when a timer scheduled for call management
 * expires.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 *
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class CallTimeoutEvent implements EventObject {
    private final CallTimerTask.Type type;

    public CallTimeoutEvent(CallTimerTask.Type type) {
        this.type = type;
    }

    public CallTimerTask.Type getType() {
        return type;
    }

    public String toString() {
        return "CallTimeoutEvent (type = " + type + ")";
    }
}
