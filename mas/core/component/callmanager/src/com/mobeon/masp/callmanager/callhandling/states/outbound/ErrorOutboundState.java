/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;

/**
 * Represents the outbound call state Error.
 * This state is entered an serious error occurs causing the call to be
 * considered disconnected. This can occur if a SIP request or response could
 * not be sent or if a SIP timeout event occurs.
 * <p>
 * This class is a base class for the substates
 * {@link ErrorCompletedOutboundState},
 * @link ErrorLingeringByeOutboundState} and
 * @link ErrorLingeringCancelOutboundState}.
 *
 * @author Malin Flodin
 */
public abstract class ErrorOutboundState extends CallCompletedOutboundState {

    public enum ErrorSubState {
        COMPLETED, LINGERING_BYE, LINGERING_CANCEL
    }

    public ErrorOutboundState(OutboundCallInternal call) {
        super(call);
    }
}
