/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when a call is proxying.
 * <p>
 * This class is thread-safe and immutable.
 */
public class ProxyingEvent implements Event {
    private final Call call;

    public ProxyingEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "ProxyingEvent <Call=" + call + ">";
    }
}
