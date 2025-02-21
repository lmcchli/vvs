/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when the Call Manager client operates on a call in a not
 * allowed mannor, e.g. if the Call Manager client tries to accept an already
 * accepted call.
 * <p>
 * This event is NOT generated if the Call Manager client disconnects an already
 * disconnected call. It that particular situation, a {@link DisconnectedEvent}
 * is generated instead.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class NotAllowedEvent implements Event {

    private final Call call;
    private final String message;

    public NotAllowedEvent(Call call, String message) {
        this.call = call;
        this.message = message;
    }

    public Call getCall() {
        return call;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return "NotAllowedEvent: <Call=" + call + ">, <Message=" + message + ">";
    }
}
