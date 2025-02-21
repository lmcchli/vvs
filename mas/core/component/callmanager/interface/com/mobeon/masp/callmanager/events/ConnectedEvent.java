/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when a call has been connected.
 * After this event has been received, it is possible to play and record media
 * on the call.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class ConnectedEvent implements Event {
    private final Call call;

    public ConnectedEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "ConnectedEvent <Call=" + call + ">";
    }
}
