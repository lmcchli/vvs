/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * This event is generated when an unjoin request has been performed
 * successfully. It contains the two calls that were unjoined.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class UnjoinedEvent implements Event {
    private final Call firstCall;
    private final Call secondCall;

    public UnjoinedEvent(Call firstCall, Call secondCall) {
        this.firstCall = firstCall;
        this.secondCall = secondCall;
    }

    public Call getFirstCall() {
        return firstCall;
    }

    public Call getSecondCall() {
        return secondCall;
    }

    public String toString() {
        return "UnjoinedEvent: <FirstCall=" + firstCall +
                ">, <SecondCall=" + secondCall + ">";
    }
}
