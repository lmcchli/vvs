/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the inbound call state Failed.
 * This class is a base class for the substates
 * {@link FailedCompletedInboundState}, {@link FailedLingeringByeInboundState}, 
 * {@link FailedRedirectedInboundState } and {@link FailedWaitingForAckInboundState}.
 * <p>
 * The Failed state is entered when a call has been rejected prior to connect.
 *
 * @author Malin Flodin
 */
public abstract class FailedInboundState extends CallCompletedInboundState {

    public enum FailedSubState {
        COMPLETED, LINGERING_BYE, WAITING_FOR_ACK, REDIRECTED
    }

    public FailedInboundState(InboundCallInternal call) {
        super(call);
    }
}
