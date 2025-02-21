/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;

/**
 * Represents the outbound call state Failed.
 * This class is a base class for the substates
 * {@link FailedCompletedOutboundState}, {@link FailedLingeringByeOutboundState}
 * and {@link FailedLingeringCancelOutboundState}.
 * and {@link FailedWaitingForResponseOutboundState}.
 * <p>
 * The Failed state is entered when a call has been rejected prior to connect.
 *
 * @author Malin Flodin
 */
public abstract class FailedOutboundState extends CallCompletedOutboundState {

    public enum FailedSubState {
        COMPLETED, LINGERING_BYE, LINGERING_CANCEL, WAITING_FOR_RESPONSE
    }

    public FailedOutboundState(OutboundCallInternal call) {
        super(call);
    }

}
