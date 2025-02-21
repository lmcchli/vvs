/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.masp.callmanager.callhandling.CallImpl;

/**
 * This event contains all information regarding removing a current call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public final class RemoveCallEvent implements EventObject {

    private final CallImpl call;

    public RemoveCallEvent(CallImpl call) {
        this.call = call;
    }

    public CallImpl getCall() {
        return call;
    }

    public String toString() {
        return "RemoveCallEvent";
    }
}
