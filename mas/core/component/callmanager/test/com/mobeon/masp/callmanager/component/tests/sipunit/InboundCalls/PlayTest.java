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
import com.mobeon.masp.stream.PlayFailedEvent;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify play of media for inbound calls.
 * @author Malin Flodin
 */
public class PlayTest extends InboundSipUnitCase {

    /**
     * Verifies that playing media in {@link AlertingNewCallInboundState} results in a
     * {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInAlertingState() throws Exception {
        gotoAlertingNewCallState();

        // System tries to play media
        simulatedSystem.play(true);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

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
     * Verifies that playing media in {@link AlertingAcceptingInboundState} results in a
     * {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.waitForPlay(TIMEOUT_IN_MILLI_SECONDS);

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
     * Verifies that playing media in {@link ConnectedInboundState} results in a
     * media being played on the outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testPlayInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.waitForPlay(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that playing media in {@link DisconnectedLingeringByeInboundState} results in a
     * {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that playing media in {@link DisconnectedCompletedInboundState} results
     * in a {@link PlayFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testPlayInDisconnectedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System tries to play media
        simulatedSystem.play(false);
        simulatedSystem.assertEventReceived(PlayFailedEvent.class, null);

        assertCurrentState(DisconnectedCompletedInboundState.class);
    }

}
