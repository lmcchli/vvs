/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the inbound call state Error.
 * This state is entered an serious error occurs causing the call to be
 * considered disconnected. This can occur if a SIP request or response could
 * not be sent or if a SIP timeout event occurs.
 * <p>
 * This class is a base class for the substates
 * {@link ErrorCompletedInboundState}
 * @link ErrorLingeringByeInboundState}.
 *
 * @author Malin Flodin
 */
public abstract class ErrorInboundState extends CallCompletedInboundState {

    public enum ErrorSubState {
        COMPLETED, LINGERING_BYE
    }

    public ErrorInboundState(InboundCallInternal call) {
        super(call);
    }
}
