/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the sub state "Completed" of {@link DisconnectedInboundState}.
 * This sub state is entered when the call is disconnected ok, for example
 * when a SIP BYE request is received from peer or when the Call Manager client
 * has disconnected the call and a SIP OK response has been received.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class DisconnectedCompletedInboundState extends DisconnectedInboundState {

    public DisconnectedCompletedInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Disconnected state (sub state Completed)";
    }
}

