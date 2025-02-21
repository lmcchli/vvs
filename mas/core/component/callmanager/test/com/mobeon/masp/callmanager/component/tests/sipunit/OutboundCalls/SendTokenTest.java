/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify sending tokens over outbound calls.
 * @author Malin Flodin
 */
public class SendTokenTest extends OutboundSipUnitCase {

    /**
     * Verifies that sending tokens over an outbound call in Progressing-Calling
     * state results in a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testSendTokenInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        // System tries to send token.
        simulatedSystem.sendToken();
        assertNotAllowedEventReceived(
                "SendToken is not allowed in Progressing state (sub state Calling).");

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
     * Verifies that sending tokens over an outbound call in
     * Progressing-Proceeding state results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testSendTokenInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System tries to send token.
        simulatedSystem.sendToken();
        assertNotAllowedEventReceived(
                "SendToken is not allowed in Progressing state (sub state Proceeding).");

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that sending tokens over an outbound call in Connected state
     * results in tokens sent over outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testSendTokenInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to send token.
        simulatedSystem.sendToken();

        assertTokenSent();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that sending tokens over an outbound call in Linger state
     * results in a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testSendTokenInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // System tries to send token.
        simulatedSystem.sendToken();

        assertNotAllowedEventReceived(
                "SendToken is not allowed in Disconnected state (sub state LingeringBye).");

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
    
}
