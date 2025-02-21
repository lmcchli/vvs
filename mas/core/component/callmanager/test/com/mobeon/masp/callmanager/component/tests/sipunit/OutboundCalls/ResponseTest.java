/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;

/**
 * Call Manager component test case to verify various SIP responses to outbound
 * calls.
 * @author Malin Flodin
 */
public class ResponseTest extends OutboundSipUnitCase {

    /**
     * Verifies that an unknown provisional response in state
     * {@link com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState}
     * is handled as a session progress response.
     */
    public void testUnknownProvisionalResponseInStateProgressingCalling()
            throws Exception {
        gotoProgressingCallingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(199);

        assertProgressingEventReceived(false);
        simulatedSystem.waitForState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that an unknown provisional response in state
     * {@link com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState}
     * is handled as a session progress response.
     */
    public void testUnknownProvisionalResponseInStateProgressingProceeding()
            throws Exception {
        gotoProgressingProceedingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(199);

        assertProgressingEventReceived(false);
        simulatedSystem.waitForState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a 4xx response in state
     * {@link ProgressingCallingOutboundState}
     * results in a {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     */
    public void test4xxResponseInStateProgressingCalling()
            throws Exception {
        gotoProgressingCallingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(499);

        assertCallRejected(499);
    }

    /**
     * Verifies that a 4xx response in state
     * {@link ProgressingProceedingOutboundState}
     * results in a {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     */
    public void test4xxResponseInStateProgressingProceeding()
            throws Exception {
        gotoProgressingProceedingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(401);

        assertCallRejected(401);
    }

    /**
     * Verifies that a 6xx response in state
     * {@link ProgressingCallingOutboundState}
     * results in a {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     */
    public void test6xxResponseInStateProgressingCalling()
            throws Exception {
        gotoProgressingCallingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(699);

        assertCallRejected(699);
    }

    /**
     * Verifies that a 6xx response in state
     * {@link ProgressingProceedingOutboundState}
     * results in a {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     */
    public void test6xxResponseInStateProgressingProceeding()
            throws Exception {
        gotoProgressingProceedingState();

        // Phone sends unknown provisional response
        simulatedPhone.sendResponse(601);

        assertCallRejected(601);
    }

}
