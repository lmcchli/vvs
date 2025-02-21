/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * This event is generated when an unjoin request failed.
 * It contains the two calls that should have been unjoined and an error
 * message describing the reason for the failure.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class UnjoinErrorEvent implements Event {
    private final Call firstCall;
    private final Call secondCall;
    private final String errorMessage;

    public UnjoinErrorEvent(Call firstCall, Call secondCall, String errorMessage) {
        this.firstCall = firstCall;
        this.secondCall = secondCall;
        this.errorMessage = errorMessage;
    }

    public Call getFirstCall() {
        return firstCall;
    }

    public Call getSecondCall() {
        return secondCall;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String toString() {
        return "UnjoinErrorEvent: <FirstCall=" + firstCall +
                ">, <SecondCall=" + secondCall +
                ">, <ErrorMessage=" + errorMessage + ">";
    }
}
