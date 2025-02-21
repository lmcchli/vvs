/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.common.eventnotifier.Event;

/**
 * An event generated when a failure occurs that forces the call into an
 * error state in which it no longer can be used.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class ErrorEvent implements Event {
    private final Call call;
    private final CallDirection direction;
    private final String message;
    private final boolean alreadyDisconnected;

    public ErrorEvent(Call call, CallDirection direction, String message,
                      boolean alreadyDisconnected) {
        this.call = call;
        this.direction = direction;
        this.message = message;
        this.alreadyDisconnected = alreadyDisconnected;
    }

    public Call getCall() {
        return call;
    }

    public CallDirection getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAlreadyDisconnected() {
        return alreadyDisconnected;
    }

    public String toString() {
        return "ErrorEvent: <Call=" + call + ">, <Direction="+ direction +
                ">, <AlreadyDisconnected=" + alreadyDisconnected +
                ">, <Message=" + message + ">";
    }
}
