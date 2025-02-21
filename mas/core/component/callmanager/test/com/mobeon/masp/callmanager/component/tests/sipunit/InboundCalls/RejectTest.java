/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify reject of inbound calls.
 * @author Malin Flodin
 */
public class RejectTest extends InboundSipUnitCase {

    /**
     * Verifies that declining an inbound call in
     * {@link AlertingNewCallInboundState} results in a SIP failure response
     * and {@link FailedCompletedInboundState}.
     * @throws Exception when the test case fails.
     */
    public void testRejectCallInAlertingState() throws Exception {
        gotoAlertingNewCallState();

        // System rejects the call
        simulatedSystem.reject(null, null);
        assertCallRejected();
    }

    /**
     * Verifies that declining an inbound call in
     * {@link AlertingAcceptingInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRejectInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to reject the call.
        simulatedSystem.reject(null, null);
        assertNotAllowedEventReceived(
                "Reject is not allowed in Alerting state (sub state Accepting).");
        assertCurrentState(AlertingAcceptingInboundState.class);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that declining an inbound call in Connected state results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRejectInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to reject the call.
        simulatedSystem.reject(null, null);
        assertNotAllowedEventReceived("Reject is not allowed in Connected state.");
        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that declining an inbound call in
     * {@link DisconnectedLingeringByeInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRejectInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System rejects the call.
        simulatedSystem.reject(null, null);
        assertNotAllowedEventReceived("Reject is not allowed in Disconnected " +
                "state (sub state LingeringBye).");
        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that declining an inbound call in
     * {@link DisconnectedCompletedInboundState} results in
     * a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRejectInDisconnectedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System rejects the call.
        simulatedSystem.reject(null, null);
        assertNotAllowedEventReceived("Reject is not allowed in Disconnected " +
                "state (sub state Completed).");
        assertCurrentState(DisconnectedCompletedInboundState.class);
    }

}
