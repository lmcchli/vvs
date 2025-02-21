/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

/**
 * This event contains a request to negotiate call media types before early
 * media can be played on an inbound call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 */
public class NegotiateEarlyMediaTypesEvent extends CallCommandEvent {

    public String toString() {
        return "NegotiateEarlyMediaTypesEvent";
    }
}
