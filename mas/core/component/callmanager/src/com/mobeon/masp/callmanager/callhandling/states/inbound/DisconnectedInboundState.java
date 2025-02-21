/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the inbound call state Disconnected.
 * This class is a base class for the substates
 * {@link DisconnectedCompletedInboundState} and
 * @link DisconnectedLingeringByeInboundState}.
 * <p>
 * The Disconnected state is entered when a call has been disconnected and the
 * SIP BYE procedure is performed.
 *
 * @author Malin Flodin
 */
public abstract class DisconnectedInboundState extends CallCompletedInboundState {

    public enum DisconnectedSubState {
        COMPLETED, LINGERING_BYE
    }

    public DisconnectedInboundState(InboundCallInternal call) {
        super(call);
    }
}
