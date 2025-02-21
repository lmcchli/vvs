/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.stream.PlayFailedEvent;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify play of media for outbound calls.
 * @author Malin Flodin
 */
public class PlayTest extends OutboundSipUnitCase {

    /**
     * Verifies that playing media in {@link ProgressingCallingOutboundState}
     * results in a {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInProgressingCallingState()
            throws Exception {
        gotoProgressingCallingState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

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
     * Verifies that playing media in {@link ProgressingProceedingOutboundState}
     * results in a {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that playing media in {@link ConnectedOutboundState} results in
     * media being played on the outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testPlayInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.waitForPlay(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that playing media in {@link DisconnectedLingeringByeOutboundState} results in a
     * {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

}

