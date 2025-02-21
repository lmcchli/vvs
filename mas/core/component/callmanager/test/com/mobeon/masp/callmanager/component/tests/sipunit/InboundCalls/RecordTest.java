/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.stream.RecordFailedEvent;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify record of media for inbound calls.
 * @author Malin Flodin
 */
public class RecordTest extends InboundSipUnitCase {

    /**
     * Verifies that recording media in {@link AlertingNewCallInboundState} results in a
     * {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInAlertingState() throws Exception {
        gotoAlertingNewCallState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(AlertingNewCallInboundState.class);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that recording media in {@link AlertingAcceptingInboundState} results in a
     * {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

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
     * Verifies that recording media in Connected state results in a
     * media being recorded on the inbound stream.
     * @throws Exception when the test case fails.
     */
    public void testRecordInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.waitForRecord(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that recording media in {@link DisconnectedLingeringByeInboundState} results in a
     * {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that recording media in {@link DisconnectedCompletedInboundState} results
     * in a {@link RecordFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testRecordInDisconnectedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System tries to record media
        simulatedSystem.record();
        simulatedSystem.assertEventReceived(RecordFailedEvent.class, null);

        assertCurrentState(DisconnectedCompletedInboundState.class);
    }

}
