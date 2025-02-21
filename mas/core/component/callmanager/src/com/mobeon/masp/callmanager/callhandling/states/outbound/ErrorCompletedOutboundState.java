/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;

/**
 * Represents the sub state "Completed" of {@link ErrorOutboundState}.
 * This sub state is entered when the call has experienced an unrecoverable error
 * and no BYE or CANCEL procedure can be performed or are already completed.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class ErrorCompletedOutboundState extends ErrorOutboundState {

    public ErrorCompletedOutboundState(OutboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Error state (sub state Completed)";
    }
}
