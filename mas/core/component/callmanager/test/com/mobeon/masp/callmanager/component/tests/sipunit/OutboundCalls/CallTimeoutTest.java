/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedWaitingForResponseOutboundState;

import javax.sip.RequestEvent;
import javax.sip.message.Request;

/**
 * Call Manager component test case to verify call timeouts for outbound calls,
 * i.e. timeouts that do not exist in the SIP protocol but are introduced to
 * make sure that no calls are left hanging.
 *
 * @author Malin Flodin
 */
public class CallTimeoutTest extends OutboundSipUnitCase {

    /**
     * Verifies that if the "Max Duration Before Connected" timer expires in
     * Progressing-Calling state (assuming that the "Max Duration Before
     * Connected" timer is set to a lower value than the SIP
     * timer B), a CANCEL request is sent and the state is set to
     * {@link FailedWaitingForResponseOutboundState).
     * @throws Exception when the test case fails.
     */
    public void testCallTimeoutInProgressingCallingState() throws Exception {
        // Set the "Max Duraction Before Connected" timer to a value lower
        // than timer B which is 32s
        callProperties.setMaxDurationBeforeConnected(1000);

        gotoProgressingCallingState();

        Thread.sleep(1000);

        assertFailedEventReceived(FailedEvent.Reason.REJECTED_BY_FAR_END, 622);

        // Wait for the state to be set to Failed
        simulatedSystem.waitForState(FailedWaitingForResponseOutboundState.class);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone sends ok
        simulatedPhone.acceptCall(null);

        // Wait for ACK request
        assertCallAcknowledged(simulatedPhone);

        // Wait for BYE request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.BYE, false, true);
        sendOkForBye(requestEvent, false);
    }

    /**
     * Verifies that if the "Max Duration Before Connected" timer expires in
     * Progressing-Proceeding
     * state, a CANCEL request is sent and the state is set to
     * {@link FailedLingeringCancelOutboundState).
     * @throws Exception when the test case fails.
     */
    public void testCallTimeoutInProgressingProceedingState() throws Exception {
        // Set the "Max Duration Before Connected" timer to a value lower than
        // timer B which is 3s
        callProperties.setMaxDurationBeforeConnected(1000);

        gotoProgressingProceedingState();

        Thread.sleep(1000);

        assertFailedEventReceived(FailedEvent.Reason.REJECTED_BY_FAR_END, 622);

        assertCallCanceled(false, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone sends ok
        simulatedPhone.acceptCall(null);

        // Wait for ACK request
        assertCallAcknowledged(simulatedPhone);

        // Wait for BYE request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.BYE, false, true);
        sendOkForBye(requestEvent, false);
    }

    /**
     * Verifies that if the max duration timer expires in Connected
     * state, a BYE request is sent and the state is set to
     * {@link DisconnectedLingeringByeOutboundState).
     * @throws Exception when the test case fails.
     */
    public void testCallTimeoutInConnectedState() throws Exception {
        // Set the "Max Duration Before Connected" timer
        callProperties.setMaxCallDuration(3000);

        gotoConnectedState();

        Thread.sleep(3000);

        assertCallDisconnect(true, true, false);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertDisconnectedCallStatistics(NEAR_END, 1);
        assertTotalConnectionStatistics(1);
    }
}
