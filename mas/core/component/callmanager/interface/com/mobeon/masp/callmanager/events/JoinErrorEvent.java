/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * This event is generated when a join request failed.
 * It contains the two calls that should have been joined together and an error
 * message describing the reason for the failure.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class JoinErrorEvent implements Event {
    private final Call firstCall;
    private final Call secondCall;
    private final String errorMessage;

    public JoinErrorEvent(Call firstCall, Call secondCall, String errorMessage) {
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
        return "JoinErrorEvent: <FirstCall=" + firstCall + 
                ">, <SecondCall=" + secondCall +
                ">, <ErrorMessage=" + errorMessage + ">";
    }
}
