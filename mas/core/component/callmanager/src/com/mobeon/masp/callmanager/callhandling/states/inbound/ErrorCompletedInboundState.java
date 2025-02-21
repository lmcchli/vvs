/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the sub state "Completed" of {@link ErrorInboundState}.
 * This sub state is entered when the call has experienced an unrecoverable error
 * and no BYE procedure can be performed or is already completed.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class ErrorCompletedInboundState extends ErrorInboundState {

    public ErrorCompletedInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Error state (sub state Completed)";
    }
}
