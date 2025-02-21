/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * Call Manager component test case to verify re-INVITE to outbound calls.
 * @author Malin Nyfeldt
 */
public class ReInviteTest extends OutboundSipUnitCase {

    /**
     * Verifies that re-INVITE to an outbound call in Progressing-Calling state
     * results in a SIP Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);

        // Wait for Request Pending response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.INVITE);

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
     * Verifies that re-INVITE to an outbound call in Progressing-Proceeding
     * state results in a SIP Request Pending response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteInProgressingProceedingState() throws Exception {
        // TODO: Correct error in SIP stack and uncomment this test case
//        gotoProgressingProceedingState();
//
//        SipTransaction transaction =
//                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);
//
//        // Wait for Request Pending response
//        simulatedPhone.
//                assertResponseReceived(transaction,
//                        Response.REQUEST_PENDING, Request.INVITE);
//
//        assertCurrentState(ProgressingProceedingOutboundState.class);
//
//        // Phone sends ok
//        simulatedPhone.acceptCall(null);
//        assertCallAccepted(simulatedPhone);
//
//        // Phone disconnects the call
//        simulatedPhone.disconnect(false);
//        assertCallIsDisconnected(false, FAR_END, true);
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

        assertCurrentState(ConnectedOutboundState.class);

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

        assertCurrentState(ConnectedOutboundState.class);

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

        assertCurrentState(ConnectedOutboundState.class);

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

        assertCurrentState(ConnectedOutboundState.class);

        Thread.sleep(4000);

        assertCallDisconnect(true, true, true);
    }

    /**
     * A re-INVITE with an SDP offer identical to the previously
     * received remote SDP to an inbound call in Connected state results in
     * a SIP OK response. This test case verifies that if a SIP ACK is never
     * sent for the OK response to the re-INVITE and if a SIP timeout is not
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

        assertCurrentState(ConnectedOutboundState.class);

        Thread.sleep(1200);

        assertCallDisconnect(true, true, true);
    }

    /**
     * Verifies that re-INVITE to an outbound call in Linger state results in
     * a SIP Not Acceptable Here response.
     * @throws Exception when the test case fails.
     */
    public void testReInviteInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        SipTransaction transaction =
                simulatedPhone.sendReInvite(PhoneSimulator.NO_BODY, SUCCEED);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(
                transaction, Response.NOT_ACCEPTABLE_HERE, Request.INVITE);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

}
