/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.stream.RecordFailedEvent;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify record of media for outbound calls.
 * @author Malin Flodin
 */
public class RecordTest extends OutboundSipUnitCase {

    /**
     * Verifies that recording media in {@link ProgressingCallingOutboundState}
     * results in a {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInProgressingCallingState()
            throws Exception {
        gotoProgressingCallingState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(ProgressingCallingOutboundState.class);

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that recording media in {@link ProgressingProceedingOutboundState}
     * results in a {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that recording media in {@link ConnectedOutboundState} results in
     * media being recorded on the outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testRecordInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.waitForRecord(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that recording media in {@link DisconnectedLingeringByeOutboundState} results in a
     * {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

}

