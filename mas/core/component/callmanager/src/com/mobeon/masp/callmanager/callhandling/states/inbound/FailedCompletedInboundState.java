/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;

/**
 * Represents the sub state "Completed" of {@link FailedInboundState}.
 * This sub state is entered when the call is prematurely disconnected ok,
 * i.e. when the call is rejected before it was connected.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class FailedCompletedInboundState extends FailedInboundState {

    public FailedCompletedInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Failed state (sub state Completed)";
    }
}
