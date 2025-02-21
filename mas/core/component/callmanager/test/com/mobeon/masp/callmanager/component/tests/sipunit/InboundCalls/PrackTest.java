/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Call Manager component test case to reliable 1xx transmission (using PRACK)
 * for inbound calls.
 *
 * @author Malin Nyfeldt
 */
public class PrackTest extends InboundSipUnitCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Verifies that an INVITE requiring the extension 100rel is accepted and
     * treated according to RFC 3262.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWhen100relRequired() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY,
                null, false, false);

        // Add Require header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createRequireHeader("100rel"));

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Phone receives reliable ringing and acknowledges it using PRACK
        assertReliableRinging(PhoneSimulator.NO_BODY, ACKNOWLEDGE);

        // Call is accepted with an OK response
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that an INVITE supporting the extension 100rel is accepted and
     * treated according to RFC 3262. The default behavior for Call Manager
     * when this occurs is to send provisional responses carrying SDP reliably.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWhen100relSupported() throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY,
                null, false, false);

        // Add Supported header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createSupportedHeader("100rel"));

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

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
     * Verifies that an INVITE supporting the extension 100rel is accepted and
     * treated according to RFC 3262. This test case verifies the reliable
     * sending of SIP 183 "Session Progress" for the early media scenario.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWithEarlyMediaWhen100relSupported()
            throws Exception {

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY,
                null, false, false);

        // Add Supported header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createSupportedHeader("100rel"));

        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();

        // Phone receives reliable session progress and acknowledges it using PRACK
        assertReliableSessionProgress(ACKNOWLEDGE);

        // Early media is now setup correctly
        assertEarlyMedia(NO_SESSION_PROGRESS);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that when 100rel is required, an INVITE without SDP offer
     * results in a 180 Ringing response being sent with an SDP offer.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithoutSdpOfferWhen100relRequired()
            throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY,
                null, false, false);

        // Add Require header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createRequireHeader("100rel"));

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Phone receives reliable ringing with SDP and acknowledges it using
        // PRACK
        assertReliableRinging(PhoneSimulator.WITH_BODY, ACKNOWLEDGE);

        // Call is accepted with an OK response
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that when 100rel is required, an INVITE without SDP offer
     * results in a 180 Ringing response being sent with an SDP offer.
     * If the corresponding PRACK does not contain an SDP answer, the call will
     * be rejected with a SIP 488 "Not Acceptable Here" response.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWithoutSdpOfferWhen100relRequiredAndNoSdpAnswerInPrack()
            throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY,
                null, false, false);

        // Add Require header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createRequireHeader("100rel"));

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Phone receives reliable ringing with SDP,
        // no acknowledgement is sent here
        Response ringing =
                assertReliableRinging(PhoneSimulator.WITH_BODY, NO_ACKNOWLEDGE);

        // Send a PRACK request without SDP answer for the 180 Ringing
        simulatedPhone.acknowledgeReliableResponse(
                ringing, PhoneSimulator.NO_BODY, SUCCEED, true);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that an INVITE supporting the extension 100rel is accepted and
     * treated according to RFC 3262. This test case verifies the reliable
     * sending of SIP 183 "Session Progress" for the early media scenario when
     * the PRACK request contains a new SDP offer identical to the previously
     * received remote SDP.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWithReliableEarlyMediaAndEqualReNegotiation()
            throws Exception {

        Response progressResponse = gotoAlertingEarlyMediaWaitForPrackState();

        // Acknowledge the reliable Session Progress
        // The acknowledgement contains a new SDP offer identical to the
        // previous remote SDP.
        simulatedPhone.acknowledgeReliableResponse(
                progressResponse, PhoneSimulator.WITH_BODY, SUCCEED, true);

        // Early media is now setup correctly
        assertEarlyMedia(NO_SESSION_PROGRESS);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that an INVITE supporting the extension 100rel is accepted and
     * treated according to RFC 3262. This test case verifies the reliable
     * sending of SIP 183 "Session Progress" for the early media scenario when
     * the PRACK request contains a new SDP offer different from the previously
     * received remote SDP.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWithReliableEarlyMediaAndDifferentReNegotiation()
            throws Exception {

        Response progressResponse = gotoAlertingEarlyMediaWaitForPrackState();

        // Acknowledge the reliable Session Progress
        // The acknowledgement contains a new SDP offer different from the
        // previous remote SDP.
        simulatedPhone.acknowledgeReliableResponse(
                progressResponse, PhoneSimulator.WITH_BODY, FAIL, false);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that when a PRACK is not received in the within a certain configured
     * time for a non-early media scenario the call is rejected with a
     * SIP 504 "Server Timeout" response.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWhenNoPrack() throws Exception {
        ConfigurationReader.getInstance().getConfig().
                setCallNotAcceptedTimer(32000);

        gotoAlertingWaitForPrackState();

        Thread.sleep(2500);

        // Wait for Bad Extension response
        simulatedPhone.assertResponseReceived(Response.SERVER_TIMEOUT);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertErrorCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that when a PRACK is not received in the within a certain
     * configured time for an early media scenario the call is rejected with a
     * SIP 504 "Server Timeout" response.
     * @throws Exception    An exception is thrown if the test case fails.
     */
    public void testInboundCallSetupWhenNoPrackInEarlyMedia() throws Exception {
        ConfigurationReader.getInstance().getConfig().setCallNotAcceptedTimer(32000);

        gotoAlertingEarlyMediaWaitForPrackState();

        Thread.sleep(2000);

        // Wait for Bad Extension response
        simulatedPhone.assertResponseReceived(Response.SERVER_TIMEOUT);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertErrorCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

}
