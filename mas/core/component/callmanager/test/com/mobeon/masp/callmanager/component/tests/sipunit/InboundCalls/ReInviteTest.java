/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * Call Manager component test case to verify re-INVITE to inbound calls.
 * @author Malin Flodin
 */
public class ReInviteTest extends InboundSipUnitCase {

    /**
     * Verifies that re-INVITE to an inbound call in Accepting state results in
     * a SIP Request Pending response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);

        // Wait for Request Pending response
        simulatedPhone.assertResponseReceived(
                transaction, Response.REQUEST_PENDING, Request.INVITE);

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
     * Verifies that re-INVITE with no SDP offer
     * to an inbound call in Connected state results in
     * a SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteWithoutSdpOfferInConnectedState() throws Exception {
        gotoConnectedState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(
                transaction, Response.NOT_ACCEPTABLE_HERE, Request.INVITE);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that re-INVITE with an SDP offer different from the previously
     * received remote SDP to an inbound call in Connected state results in
     * a SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteWithDifferentSdpOfferInConnectedState()
            throws Exception {
        gotoConnectedState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.WITH_BODY, FAIL);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(
                transaction, Response.NOT_ACCEPTABLE_HERE, Request.INVITE);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that re-INVITE with an SDP offer identical to the previously
     * received remote SDP to an inbound call in Connected state results in
     * a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteWithIdenticalSdpOfferInConnectedState()
            throws Exception {
        gotoConnectedState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.WITH_BODY, SUCCEED);

        // Wait for OK response
        simulatedPhone.assertResponseReceived(
                transaction, Response.OK, Request.INVITE);

        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, true);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * A re-INVITE with an SDP offer identical to the previously
     * received remote SDP to an inbound call in Connected state results in
     * a SIP OK response. This test case verifies that if a SIP ACK is never
     * send for the OK response to the re-INVITE, the call is disconnected
     * with a SIP BYE request.
     *
     * @throws Exception when the test case fails.
     */
    public void testReInviteInConnectedStateWhenNoAckIsSentForReInvite()
            throws Exception {
        gotoConnectedState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.WITH_BODY, SUCCEED);

        // Wait for OK response
        simulatedPhone.assertResponseReceived(
                transaction, Response.OK, Request.INVITE);

        assertCurrentState(ConnectedInboundState.class);

        Thread.sleep(4000);

        assertCallDisconnectWhenError(true);
    }

    /**
     * A re-INVITE with an SDP offer identical to the previously
     * received remote SDP to an inbound call in Connected state results in
     * a SIP OK response. This test case verifies that if a SIP ACK is never
     * sent for the OK response to the re-INVITE and a SIP timeout is never
     * received from the SIP stack, the call is disconnected
     * with a SIP BYE request.
     *
     * @throws Exception when the test case fails.
     */
    public void testReInviteInConnectedStateWhenNoAckOrSipTimeoutIsReceivedForReInvite()
            throws Exception {
        ConfigurationReader.getInstance().getConfig().setCallNotAcceptedTimer(1000);

        gotoConnectedState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.WITH_BODY, SUCCEED);

        // Wait for OK response
        simulatedPhone.assertResponseReceived(
                transaction, Response.OK, Request.INVITE);

        assertCurrentState(ConnectedInboundState.class);

        Thread.sleep(1200);

        assertCallDisconnectWhenError(true);
    }

    /**
     * Verifies that re-INVITE to an inbound call in Linger state results in
     * a SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(
                transaction, Response.NOT_ACCEPTABLE_HERE, Request.INVITE);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

}
