/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;

/**
 * Represents the inbound call state Disconnected.
 * This class is a base class for the substates
 * {@link DisconnectedCompletedOutboundState},
 * @link DisconnectedLingeringByeOutboundState}.
 * <p>
 * The Disconnected state is entered when a call has been disconnected and the
 * SIP BYE procedure is performed.
 *
 * @author Malin Flodin
 */
public abstract class DisconnectedOutboundState extends CallCompletedOutboundState {

    public enum DisconnectedSubState {
        COMPLETED, LINGERING_BYE
    }

    public DisconnectedOutboundState(OutboundCallInternal call) {
        super(call);
    }
}
