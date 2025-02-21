/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * AlertingWaitForPrackInboundState Tester.
 *
 * @author Malin Nyfeldt
 */
public class AlertingWaitForPrackInboundStateTest extends InboundStateCase {

    AlertingWaitForPrackInboundState alertingWaitForPrackState;

    protected void setUp() throws Exception {
        super.setUp();

        alertingWaitForPrackState = new AlertingWaitForPrackInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in a SIP
     * "Temporarily Unavailable" response sent, a {@link FailedEvent} generated
     * and the state set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.SERVICE_UNAVAILABLE);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The Service is temporarily unavailable due to " +
                "the current administrative state: Locked.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        alertingWaitForPrackState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingWaitForPrackState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a NotAllowed event is generated when executing an
     * accept in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testAccept() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Accept is not allowed in Alerting state (sub state WaitForPrack).");
        alertingWaitForPrackState.accept(acceptEvent);
    }

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in Alerting " +
                        "state (sub state WaitForPrack).");
        alertingWaitForPrackState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Forbidden" response is sent for a call reject
     * executed succesfully.
     * Verifies that a {@link FailedEvent} is generated and that the state is
     * set to {@link FailedCompletedInboundState}.
     * @throws Exception if test case failed.
     */
    public void testReject() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.FORBIDDEN);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The call is rejected by service.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.reject(rejectEvent);
    }

    /**
     * Verifies that a disconnect is handled in this state.
     * A SIP "Request Terminated" is sent for the INVITE request,
     * a {@link FailedEvent} is generated and the next state is set to
     * {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "A disconnect is requested before the call is connected.", null);
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the BYE
     * request.
     * Finally it is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", null);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the BYE
     * request.
     * It is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessByeWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 18, 1);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 610);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the CANCEL
     * request.
     * Finally it is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", null);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the CANCEL
     * request.
     * It is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancelWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 19, null);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 610);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * INVITE in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInvite() throws Exception
    {
        try {
            alertingWaitForPrackState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE.
     * It is verified that a SIP "Request Pending" is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_PENDING);
        alertingWaitForPrackState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP OPTIONS request.
     * It is verified that a SIP "Ok" response is sent for
     * the OPTIONS and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessOptions() throws Exception
    {
        assertOkResponseSent(mockInboundCall);
        alertingWaitForPrackState.processOptions(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP INFO request.
     * It is verified that a SIP "Method Not Allowed" response is sent for
     * the INFO and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfo() throws Exception
    {
        assertMethodNotAllowedResponseSent(mockInboundCall);
        alertingWaitForPrackState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * A SIP "Ok" response should be sent for the PRACK. Tbis test case
     * verifies that when an exception is thrown
     * when sending the SIP "Ok" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenSendingOkOnPrackFails() throws Exception
    {
        assertResponseSent(SUCCEED, FAIL, "Ok", false);
        assertErrorOccurred(
                mockInboundCall,
                "Could not send SIP \"Ok\" response: Error",
                NOT_DISCONNECTED);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and a PRACK is
     * received, if the SDP answer could not be parsed a SIP 488 response is
     * sent.
     * A {@link FailedEvent} is generated and the
     * state is set to {@link FailedCompletedInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInviteAndParsingSdpFails()
            throws Exception {
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "Could not parse remote SDP: Error. A SIP 488 " +
                        "response will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and a PRACK is
     * received, if the PRACK contained no SDP answer a SIP 488 response is
     * sent, a {@link FailedEvent} is generated and the
     * state is set to {@link FailedCompletedInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInviteAndNoSdpAnswer()
            throws Exception {
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertParsingSdpBody(mockInboundCall, SUCCEED);

        // NOTE: These lines have been reversed since JMock requires that order
        assertRetrievingSdpBody(mockInboundCall, FAIL);
        assertNoRemoteSdpOfferExists();

        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_MEDIA_FORMAT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "The SIP PRACK contained no SDP answer.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and a PRACK is
     * received, if no SDP intersection could be found a SIP 488 response is sent,
     * a {@link FailedEvent} is generated and the
     * state is set to {@link FailedCompletedInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInviteAndNoSdpIntersectionFound()
            throws Exception {
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);

        // NOTE: These lines have been reversed since JMock requires that order
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();

        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_MEDIA_FORMAT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. A SIP 488 response will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and a PRACK is
     * received, if the outbound stream could not be created a SIP 500 response
     * is sent, an {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorCompletedInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInviteAndCreatingStreamFails()
            throws Exception {
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);

        // NOTE: These lines have been reversed since JMock requires that order
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();

        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertOutboundStreamCreated(mockInboundCall, FAIL);
        assertErrorOccurred(
                mockInboundCall,
                "Could not create outbound stream: Error. A SIP 500 " +
                        "response will be sent.",
                NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and a PRACK is
     * received, an OK response is sent for the PRACK, an SDP answer is
     * retrieved, media negotiation is performed and streams are created.
     * A SIP OK response is sent for the INVITE and the state is set to
     * {@link AlertingAcceptingInboundState}
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInvite() throws Exception
    {
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);

        // NOTE: These lines have been reversed since JMock requires that order
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();

        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertOutboundStreamCreated(mockInboundCall, SUCCEED);

        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingWaitForPrackState.processPrack(initialSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * Verifies that a SIP "Not Acceptable Here" is sent when no SDP
     * intersection was found. Verifies that a {@link FailedEvent}
     * is generated and that the state is set to
     * {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenNoSdpIntersectionFound() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_MEDIA_FORMAT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. A SIP 488 response will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * Verifies that a SIP "Server Internal Error" is sent when streams
     * could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenCreationOfStreamsFails() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(FAIL, SUCCEED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create streams: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * Verifies that a SIP "Server Internal Error" is sent when an SDP
     * answer could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenSdpAnswerFailed() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(SUCCEED, SUCCEED);
        assertSdpAnswerCreated(FAIL, null);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create SDP answer: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * A SIP "OK" response shall be sent for the INVITE.
     * Tbis test case verifies that when an exception is thrown
     * when creating the SIP "OK" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenSendingOkOnInviteFails() throws Exception
    {
        assertRemoteSdpOfferExists();

        // NOTE: The first line below is for the OK(INVITE) response and
        // the second line is for the OK(PRACK) response.
        // This order seems weird but is needed for jmock to work.
        assertResponseSent(FAIL, SUCCEED, "Ok", false);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);

        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(SUCCEED, SUCCEED);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Ok\" response: Error", NOT_DISCONNECTED);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when redirected RTP is activated the PRACK is 
     * handled as normal except that no outbound media stream is created.
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenRedirectedRtpActivated() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertRedirectedRtpActivated();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertInboundStreamCreation(SUCCEED);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that when redirected RTP is activated the PRACK is 
     * handled as normal except that no outbound media stream is created.
     * @throws Exception if test case failed.
     * @throws Exception if test case fails.
     */
    public void testProcessPrackWhenNoSdpInInviteAndRedirectedRtpActivated() throws Exception
    {
        assertRedirectedRtpActivated();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);

        // NOTE: These lines have been reversed since JMock requires that order
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();

        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);

        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingWaitForPrackState.processPrack(initialSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * Verifies that a SIP "OK" response is sent both for the PRACK and the
     * INVITE and that the state is set to
     * {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testProcessPrack() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(SUCCEED, SUCCEED);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the status code of the SIP response is retrieved for
     * logging purposes but that the response otherwise is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipResponse() throws Exception
    {
        assertResponseCodeRetrievedForMethod("INFO", 499);
        alertingWaitForPrackState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP timeout results in an Error event, the
     * state is set to Error Completed and the call is rejected with a SIP
     * 504 "Server Timeout" response.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSIPTimeout() throws Exception
    {
        // Called party and dialogID are used in error log.
        mockInboundCall.stubs()
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.stubs()
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorInboundState.ErrorSubState.COMPLETED);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "The inbound call has timed out while waiting " +
                "for a PRACK. The timer that expired was a SIP timer. " +
                "The call is rejected with a 504 response.",
                false);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_TIMEOUT);
        alertingWaitForPrackState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a CallNotAccepted call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_CallNotAccepted() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.handleCallTimeout(
                callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that a NoAck call timeout results in an Error event, the
     * state is set to Error Completed and the call is rejected with a SIP
     * 504 "Server Timeout" response.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NoAck() throws Exception {
        // Called party and dialogID are used in error log.
        mockInboundCall.stubs()
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.stubs()
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorInboundState.ErrorSubState.COMPLETED);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "The inbound call has timed out while waiting " +
                    "for a PRACK. The timer that expired was an internal " +
                    "safety timer. The call is rejected with a 504 response.",
                false);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_TIMEOUT);

        alertingWaitForPrackState.handleCallTimeout(noAckTimeoutEvent);
    }


    /**
     * Verifies that if the Expires timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Terminated" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_Expires() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The expires timer expired for the INVITE. A SIP 487 response " +
                        "will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        alertingWaitForPrackState.handleCallTimeout(expiresTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForPrackState.handleAbandonedStream();
    }
}
