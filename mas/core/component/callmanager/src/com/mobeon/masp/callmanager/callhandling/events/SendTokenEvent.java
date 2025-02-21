/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.stream.ControlToken;

/**
 * A send token event contains all information regarding sending tokens. 
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 *
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class SendTokenEvent extends CallCommandEvent {

    private final ControlToken[] tokens;

    public SendTokenEvent(ControlToken[] tokens) {
        this.tokens = tokens;
    }

    public ControlToken[] getTokens() {
        return tokens;
    }

    public String toString() {
        return "SendTokenEvent (tokens = " + tokens + ")";
    }
}
