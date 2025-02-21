/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when sending a token fails. The call state is left
 * unchanged.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class SendTokenErrorEvent implements Event {
    private final Call call;

    public SendTokenErrorEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "SendTokenErrorEvent: <Call=" + call + ">";
    }
}