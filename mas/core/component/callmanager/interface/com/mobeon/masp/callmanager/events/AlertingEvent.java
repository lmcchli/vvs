/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when an inbound call has been received from a peer UA.
 *
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class AlertingEvent implements Event {

    private final Call call;

    public AlertingEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "AlertingEvent <Call=" + call + ">";
    }
}
