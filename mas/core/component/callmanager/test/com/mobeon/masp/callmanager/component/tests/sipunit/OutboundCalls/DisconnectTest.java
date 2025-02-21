/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;

import javax.sip.message.Request;

/**
 * Call Manager component test case to verify disconnect of outbound calls.
 * @author Malin Flodin
 */
public class DisconnectTest extends OutboundSipUnitCase {
    /**
     * Verifies that disconnect of an outbound call in Progressing-Calling state
     * results in a pending disconnect which causes a CANCEL request when a
     * provisional response is received.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInProgressingCallingStateWhenProvisionalResponseReceived()
            throws Exception {
        gotoProgressingCallingState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        // Phone sends trying
        simulatedPhone.trying();

        assertCallCanceled(false, false);
    }

    /**
     * Verifies that disconnect of an outbound call in Progressing-Calling state
     * results in a pending disconnect which causes an ACK and a BYE request when a
     * final response is received.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInProgressingCallingStateWhenFinalResponseReceived()
            throws Exception {
        gotoProgressingCallingState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        // Phone sends ok
        simulatedPhone.acceptCall(null);

        // Wait for ACK request
        simulatedPhone.assertRequestReceivedAndIgnoreInviteResends(Request.ACK);

        assertCallDisconnect(true, false, false);
    }

    /**
     * Verifies that disconnect of an outbound call in Progressing-Proceeding state
     * results in a CANCEL request.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        assertCallCanceled(false, false);
    }

    /**
     * Verifies that disconnect of an outbound call in Connected state results in a
     * BYE request.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        assertCallDisconnect(true, true, false);
    }

    /**
     * Verifies that disconnect of an outbound call in Linger state is ignored.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInLingerState() throws Exception {
        gotoLingerState(false);

        // System tries to disconnect.
        simulatedSystem.disconnect();
        simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);

        // Wait for the state to be set to Linger
        simulatedSystem.waitForState(DisconnectedLingeringByeOutboundState.class);
    }

}
