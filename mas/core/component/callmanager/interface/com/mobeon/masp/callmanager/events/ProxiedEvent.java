/*
 * Copyright (c) 2010 Eircsson.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when the Call Manager has proxied the given call.
 * <p>
 * This class is immutable.
 */
public class ProxiedEvent implements Event {
    private final Call call;
    private final int responseCode;

    public ProxiedEvent(Call call) {
        this.call = call;
        this.responseCode = 0;
    }

    public ProxiedEvent(Call call, int responseCode) {
        this.call = call;
        this.responseCode = responseCode;
    }

    public Call getCall() {
        return call;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String toString() {
        return "ProxiedEvent <Call=" + call + ">" + (responseCode == 0 ? "" : ", <responseCode= " + responseCode + ">");
    }
}
