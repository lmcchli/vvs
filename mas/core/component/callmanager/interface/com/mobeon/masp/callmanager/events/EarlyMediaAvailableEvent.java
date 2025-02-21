/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * This event is sent when an inbound call is available for early media play.
 * <p>
 * This event is sent as response when a request to
 * {@link com.mobeon.masp.callmanager.InboundCall#negotiateEarlyMediaTypes()}
 * is completed.
 * While waiting for this event, the inbound call can not be accepted, and no
 * play can be performed on the call.
 * <P>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class EarlyMediaAvailableEvent implements Event {

    private final Call call;

    public EarlyMediaAvailableEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "EarlyMediaAvailableEvent <Call=" + call + ">";
    }
}
