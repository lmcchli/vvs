/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedCompletedOutboundState;

import javax.sip.message.Response;

/**
 * Call Manager component test case to verify Sip timeouts for outbound calls.
 * @author Malin Flodin
 */
public class SipTimeoutTest extends OutboundSipUnitCase {

    /**
     * Verifies that an outbound call times out if no
     * provisional response was received in time for the call.
     * A Failed event is generated and the state is set to Failed.
     * @throws Exception when the test case fails.
     */
    public void testSipInviteTimeoutInProgressingCallingState()
            throws Exception {

        gotoProgressingCallingState();

        // Sleep a while to enabler SIP timer B to expire
        Thread.sleep(3000);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedOutboundState.class);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an outbound call that times out if no
     * provisional response was received in time retries a new SSP for the call
     * if one is present.
     * @throws Exception when the test case fails.
     */
    public void testSipInviteTimeoutInProgressingCallingStateRedirectsToOtherSsp()
            throws Exception {
        gotoProgressingCallingState();

        mockRemotePartyController();
        assertRemotePartyBlackListed();
        assertNewRemotePartyRetrieved();

        // Sleep a while to enabler SIP timer B to expire
        Thread.sleep(3000);

        assertCallCreated(simulatedPhone, false);

        resetRemotePartyController();
        
        // Second phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that an outbound call that is redirected to a GW times out if no
     * provisional response was received in time.
     * A Failed event is generated and the state is set to Failed.
     * This test case is only valid if remote party is one or more SSP instances.
     * @throws Exception when the test case fails.
     */
    public void testSipInviteTimeoutInProgressingCallingStateWhenRedirected()
            throws Exception {

        assertConfigurationContainsSsp();

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends redirection
        simulatedPhone.sendRedirect(
                Response.MOVED_TEMPORARILY, simulatedSecondPhone.getLocalContact());
        assertCallCreated(simulatedSecondPhone, true);

        // Sleep a while to enabler SIP timer B to expire
        Thread.sleep(3000);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedOutboundState.class);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an outbound call times out after 64*T1 milliseconds if no
     * provisional response was received for the call.
     * The state is set to Disconnected.
     * @throws Exception when the test case fails.
     */
    public void testSipByeTimeoutInLingerState()
            throws Exception {

        gotoConnectedState();

        simulatedSystem.disconnect();
        assertCallDisconnect(false, true, false);

        // Sleep a while to enabler SIP timer F to expire
        Thread.sleep(3000);

        simulatedSystem.waitForState(ErrorCompletedOutboundState.class);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertDisconnectedCallStatistics(NEAR_END, 1);
        assertErrorCallStatistics(0);
        assertTotalConnectionStatistics(1);
    }
}
