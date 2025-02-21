/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;

/**
 * Represents the sub state "Completed" of {@link DisconnectedOutboundState}.
 * This sub state is entered when the call is disconnected ok, for example
 * when a SIP BYE request is received from peer or when the Call Manager client
 * has disconnected the call and a SIP OK response has been received.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class DisconnectedCompletedOutboundState extends DisconnectedOutboundState {

    public DisconnectedCompletedOutboundState(OutboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Disconnected state (sub state Completed)";
    }
}
