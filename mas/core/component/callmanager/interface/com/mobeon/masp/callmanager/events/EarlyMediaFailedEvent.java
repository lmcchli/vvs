/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * This event is sent when an inbound call is NOT available for early media play.
 * <p>
 * This event is sent as response when a request to
 * {@link com.mobeon.masp.callmanager.InboundCall#negotiateEarlyMediaTypes()}
 * is rejected.
 * While waiting for this event, the inbound call can not be accepted, and no
 * play can be performed on the call.
 * <p>
 * This event is sent when requesting early media for inbound calls
 * that did not contain an SDP offer (which is currently not supported). When
 * the PRACK extension has been implemented in Call Manager, this scenario will
 * be supported and this event will never be generated. 
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class EarlyMediaFailedEvent implements Event {

    private final Call call;

    public EarlyMediaFailedEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "EarlyMediaFailedEvent <Call=" + call + ">";
    }
}
