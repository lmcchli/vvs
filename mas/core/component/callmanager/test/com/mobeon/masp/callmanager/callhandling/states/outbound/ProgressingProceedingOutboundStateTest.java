/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.message.Response;
import javax.sip.address.SipURI;

/**
 * ProgressingProceedingOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class ProgressingProceedingOutboundStateTest extends OutboundStateCase {

    ProgressingProceedingOutboundState progressingProceedingState;

    protected void setUp() throws Exception {
        super.setUp();

        progressingProceedingState = new ProgressingProceedingOutboundState(
                (OutboundCallInternal) mockOutboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in a
     * {@link FailedEvent} sent, a SIP CANCEL request
     * sent, and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to lock request.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingProceedingState.processLockRequest();
    }

    /**
     * Verifies that a lock request in this state results in a
     * {@link FailedEvent} sent, a SIP CANCEL request
     * sent, and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     * <p>
     * If an error occurs while sending the SIP CANCEL request, an
     * error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testLockWhenSendingCancelFails() throws Exception
    {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to lock request.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        progressingProceedingState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        progressingProceedingState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in
     * {@link ProgressingProceedingOutboundState} is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a disconnect request in this state results in a
     * {@link FailedEvent} sent, a SIP CANCEL request
     * sent, and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call disconnected by near end.", null);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingProceedingState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a disconnect request in this state results in a
     * {@link FailedEvent} sent, a SIP CANCEL request
     * sent, and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     * <p>
     * If an error occurs while sending the SIP CANCEL request, an
     * error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnectWhenSendingCancelFails() throws Exception
    {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call disconnected by near end.", null);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when dialing an
     * outbound call in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDial() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "Dial is not allowed in Progressing state " +
                        "(sub state Proceeding).");
        progressingProceedingState.dial(dialEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * sending a token in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Progressing state " +
                        "(sub state Proceeding).");
        progressingProceedingState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        progressingProceedingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent and that a
     * {@link FailedEvent} is generated.
     * Finally it is verified that the next state is set to
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 480", 610);
        assertOkResponseSent(mockOutboundCall);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent and that a
     * {@link FailedEvent} is generated.
     * It is verified that the next state is set to
     * {@link FailedCompletedOutboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessByeWithReason() throws Exception {
        assertReasonHeader(mockAdditionalRequest, 17, 0);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 480", 614);
        assertOkResponseSent(mockOutboundCall);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * INVITE in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInvite() throws Exception {
        try {
            mockOutboundCall.expects(never());
            progressingProceedingState.processInvite(initialSipRequestEvent);
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
        assertErrorResponseSent(mockOutboundCall, Response.REQUEST_PENDING);
        progressingProceedingState.processReInvite(additionalSipRequestEvent);
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
        assertOkResponseSent(mockOutboundCall);
        progressingProceedingState.processOptions(additionalSipRequestEvent);
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
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        progressingProceedingState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP PRACK request.
     * It is verified that a SIP "Forbidden" response is sent for
     * the PRACK and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrack() throws Exception
    {
        assertErrorResponseSent(mockOutboundCall, Response.FORBIDDEN);
        progressingProceedingState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that a SIP Trying response is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessTryingResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.TRYING);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP "Call Is Being Forwarded" is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessCallIsBeingForwardedResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.CALL_IS_BEING_FORWARDED);
        assertUnreliableResponse();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Queued response is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessQueuedResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.QUEUED);
        assertUnreliableResponse();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Ringing response results in a
     * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} generated
     * indicating NO early media.
     * @throws Exception if test case fails.
     */
    public void testProcessRingingResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.RINGING);
        assertProgressingEvent(mockOutboundCall, NO_EARLY_MEDIA);
        assertUnreliableResponse();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a media
     * negotiation, creation of streams and an
     * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} generated
     * indicating NO early media.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponseWithNoSdp() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, FAIL);
        assertProgressingEvent(mockOutboundCall, NO_EARLY_MEDIA);
        assertUnreliableResponse();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP Session Progress response a SIP
     * CANCEL request is sent when the SDP offer could not be parsed.
     * A {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringCancelOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSessionProgressResponseWhenParsingSdpFails()
            throws Exception
    {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, FAIL);
        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "Could not parse remote SDP answer: Error", NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP Session Progress response a SIP
     * CANCEL request is sent when the SDP offer could not be parsed.
     * A {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringCancelOutboundState}.
     * If the SIP CANCEL request could not be sent, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSessionProgressResponseWhenParsingSdpFailsAndSendingCancelFails()
            throws Exception
    {
        assertResponseCodeRetrievedForMethod("INVITE", 199);
        assertParsingSdpBody(mockOutboundCall, FAIL);
        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "Could not parse remote SDP answer: Error", NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a SIP CANCEL
     * request, a {@link com.mobeon.masp.callmanager.events.FailedEvent}
     * and the state set to {@link FailedLingeringCancelOutboundState} if no
     * SDP intersection was found.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponseWithNoFoundSdpIntersection()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, false);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Media negotiation failed.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a SIP CANCEL
     * request, a {@link com.mobeon.masp.callmanager.events.FailedEvent}
     * and the state set to {@link FailedLingeringCancelOutboundState} if no
     * SDP intersection was found.
     * If the SIP CANCEL request cannot be sent, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponseWithNoFoundSdpIntersectionWhenSendingCancelFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, false);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Media negotiation failed.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a SIP CANCEL
     * request, a {@link com.mobeon.masp.callmanager.events.ErrorEvent}
     * and the state set to {@link ErrorLingeringCancelOutboundState} if the
     * outbound stream could not be created.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponseWhenCreatingStreamFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, FAIL);

        assertErrorEvent(mockOutboundCall,
                CallDirection.OUTBOUND,
                "Could not create outbound stream: Error", false);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a SIP CANCEL
     * request, a {@link com.mobeon.masp.callmanager.events.ErrorEvent}
     * and the state set to {@link ErrorLingeringCancelOutboundState} if the
     * outbound stream could not be created.
     * If the SIP CANCEL request cannot be created, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponseWhenCreatingStreamFailsAndCreatingCancelFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, FAIL);

        assertErrorEvent(mockOutboundCall,
                CallDirection.OUTBOUND,
                "Could not create outbound stream: Error", false);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in a progressing
     * event, creation of outbound stream and the state set to progressing
     * early media.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 199);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, SUCCEED);
        assertProgressingEvent(mockOutboundCall, EARLY_MEDIA);
        assertStateProgressing(
                ProgressingOutboundState.ProgressingSubState.EARLY_MEDIA);
        assertUnreliableResponse();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * A SIP Session Progress response results in a progressing
     * event, creation of outbound stream and the state set to progressing
     * early media. If the response is sent reliably a PRACK request is sent
     * as acknowledgement.
     * @throws Exception if test case fails.
     */
    public void testProcessReliableSessionProgressResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 199);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, SUCCEED);
        assertProgressingEvent(mockOutboundCall, EARLY_MEDIA);
        assertStateProgressing(
                ProgressingOutboundState.ProgressingSubState.EARLY_MEDIA);
        assertReliableResponse();
        assertCreatePrack(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * A SIP Session Progress response results in a progressing
     * event, creation of outbound stream and the state set to progressing
     * early media. If the response is sent reliably a PRACK request is sent
     * as acknowledgement.
     * This test case verifies that if the PRACK request could not be sent,
     * an {@link com.mobeon.masp.callmanager.events.ErrorEvent} is generated,
     * the state is set to {@link ErrorCompletedOutboundState} and the streams
     * are deleted.
     * @throws Exception if test case fails.
     */
    public void testProcessReliableSessionProgressResponseWhenSendingPrackFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 199);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, SUCCEED);
        assertProgressingEvent(mockOutboundCall, EARLY_MEDIA);
        assertStateProgressing(
                ProgressingOutboundState.ProgressingSubState.EARLY_MEDIA);
        assertReliableResponse();
        assertCreatePrack(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP PRACK request could not be sent. The call is " +
                        "considered completed. Error",
                NOT_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }


    /**
     * Verifies that a SIP 2xx response results in an ACK request. If the ACK
     * could not be sent, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenSendingAckFails() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP ACK request could not be sent. The call is " +
                        "considered completed. Error",
                NOT_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the SDP answer could not be parsed.
     * A {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenParsingSdpFails() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, FAIL);
        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "Could not parse remote SDP answer: Error", NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the SDP answer could not be parsed.
     * A {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeOutboundState}.
     * If creating BYE fails, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenParsingSdpFailsAndCreatingByeFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 211);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED); // For the ACK
        assertParsingSdpBody(mockOutboundCall, FAIL);
        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "Could not parse remote SDP answer: Error", NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the response contained no SDP answer.
     * A {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWithNoSdpAnswer() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, FAIL);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Response to INVITE contained no SDP answer.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the response contained no SDP answer.
     * A {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeOutboundState}.
     * If creating BYE fails, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWithNoSdpAnswerWhenCreatingByeFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 211);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, FAIL);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Response to INVITE contained no SDP answer.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the no SDP intersection could be found.
     * A {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenNoSdpIntersectionFound()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, false);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Media negotiation failed.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent when the no SDP intersection could be found.
     * A {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeOutboundState}.
     * If creating BYE fails, en error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenNoSdpIntersectionFoundAndCreatingByeFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 211);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED); // ACK Request
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, false);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.OUTBOUND,
                "Media negotiation failed.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent if the outbound stream could not be created.
     * An {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenCreatingStreamFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertReNegotiatedSdpOnInboundStream(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, FAIL);

        assertErrorEvent(mockOutboundCall,
            CallDirection.OUTBOUND,
            "Could not create outbound stream: Error", false);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP
     * BYE request is sent if the outbound stream could not be created.
     * An {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeOutboundState}.
     * If creating the BYE request fails, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseWhenCreatingStreamFailsAndCreatingByeFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertReNegotiatedSdpOnInboundStream(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, FAIL);

        assertErrorEvent(mockOutboundCall,
            CallDirection.OUTBOUND,
            "Could not create outbound stream: Error", false);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP ACK is sent, media
     * negotiation is performed, the outbound stream is created, a
     * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} is
     * generated and the state is set to {@link ConnectedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertRetrievingSdpBody(mockOutboundCall, SUCCEED);
        assertGettingSdpIntersection(mockOutboundCall, true);
        assertGetContactHeaders(mockResponse);
        assertFarEndConnection(mockOutboundCall);
        assertReNegotiatedSdpOnInboundStream(mockOutboundCall);
        assertOutboundStreamCreated(mockOutboundCall, SUCCEED);
        assertConnectedEvent(mockOutboundCall);
        assertStateConnected();
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when redirection is not allowed to be
     * handled results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWhenCallRedirectionIsNotAllowed()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionNotAllowed();
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 300", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when the call already has been redirected
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWhenCallAlreadyRedirected()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionAllowed();
        assertCallAlreadyRedirected();
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 300", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when there is no next contact
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWhenNoNextContact()
            throws Exception {
        assertReasonHeader(mockResponse, 40, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionAllowed();
        assertCallNotAlreadyRedirected();
        assertContactsRetrieved();
        assertNextContact(null, SUCCEED);

        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 300", 620);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when creating a new INVITE fails,
     * an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWhenCreatingNewInviteFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionAllowed();
        assertCallNotAlreadyRedirected();
        assertContactsRetrieved();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(FAIL, SUCCEED);
        assertErrorOccurred(mockOutboundCall,
                "SIP INVITE request could not be created. Error", NOT_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when sending a new INVITE fails,
     * a {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWhenSendingNewInviteFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionAllowed();
        assertCallNotAlreadyRedirected();
        assertContactsRetrieved();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(SUCCEED, FAIL);
        assertFailedEvent(
                mockOutboundCall, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 503", 620);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response when the call is not redirected since
     * before results in retrieving the contacts to try, sending a new INVITE
     * and setting the state to {@link ProgressingCallingOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertRedirectionAllowed();
        assertCallNotAlreadyRedirected();
        assertContactsRetrieved();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(SUCCEED, SUCCEED);
        assertStateProgressing(
                ProgressingOutboundState.ProgressingSubState.CALLING);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 4xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess4xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 400);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 400", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 4xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * It is also verified that the Reason header field in the SIP response
     * is mapped to the correct network status code.
     * @throws Exception if test case fails.
     */
    public void testProcess4xxResponseWithReason() throws Exception {
        assertReasonHeader(mockResponse, 8, null);
        assertResponseCodeRetrievedForMethod("INVITE", 400);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 400", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response when the call has not been redirected
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponseWhenCallNotRedirected()
            throws Exception {
        assertReasonHeader(mockResponse, 31, 15);
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertCallNotAlreadyRedirected();
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 500", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response when there is no next contact
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponseWhenNoNextContact()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 501);
        assertCallAlreadyRedirected();
        assertNextContact(null, SUCCEED);

        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 501", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response when creating a new INVITE fails,
     * an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponseWhenCreatingNewInviteFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertCallAlreadyRedirected();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(FAIL, SUCCEED);
        assertErrorOccurred(mockOutboundCall,
                "SIP INVITE request could not be created. Error", NOT_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response when sending a new INVITE fails,
     * a {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponseWhenSendingNewInviteFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertCallAlreadyRedirected();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(SUCCEED, FAIL);
        assertFailedEvent(
                mockOutboundCall, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 503", 620);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response when the call is redirected results in
     * retrieving the next contact to try, sending a new INVITE
     * and setting the state to {@link ProgressingCallingOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertCallAlreadyRedirected();
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertNewInviteSent(SUCCEED, SUCCEED);
        assertStateProgressing(
                ProgressingOutboundState.ProgressingSubState.CALLING);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 6xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess6xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 600);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 600", 614);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 6xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * It is also verified that the Reason header field in the SIP response
     * is mapped to the correct network status code.
     * @throws Exception if test case fails.
     */
    public void testProcess6xxResponseWithReason() throws Exception {
        assertReasonHeader(mockResponse, 46, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 600);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 600", 620);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 2xx response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.OK);
        mockOutboundCall.expects(never());
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 400 response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess400ResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.BAD_REQUEST);
        mockOutboundCall.expects(never());
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 481 response for a PRACK request results in a
     * failed event, the state set to lingering cancel, the streams are
     * deleted and a SIP CANCEL request is sent.
     * @throws Exception if test case fails.
     */
    public void testProcess481ResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod(
                "PRACK", Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.OUTBOUND,
                "Call rejected due to 481 response.", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * A SIP 408 response for a PRACK request results in a
     * failed event, the state set to lingering cancel, the streams are
     * deleted and a SIP CANCEL request is sent.
     * If sending the CANCEL fails, an error event is generated, and the
     * state is sent to Error Completed.
     * @throws Exception if test case fails.
     */
    public void testProcess408ResponseToPrackWhenSendingCancelFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod(
                "PRACK", Response.REQUEST_TIMEOUT);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.OUTBOUND,
                "Call rejected due to 408 response.", 610);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP timeout in this state when there is no new remote
     * party to try results in a {@link FailedEvent}
     * and that the state is set to {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutWhenNoNewRemoteParty() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));
        assertNextContact(null, SUCCEED);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 408", 610);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
        assertRemotePartyBlackListed();
    }

    /**
     * Verifies that a SIP timeout in this state when retrieving the next
     * remote party gives an exception results in a {@link FailedEvent}
     * and that the state is set to {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutWhenRemotePartyException() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));
        assertNextContact(null, FAIL);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 408", 610);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
        assertRemotePartyBlackListed();
    }

    /**
     * Verifies that a SIP timeout in this state when retrieving the next
     * remote party gives an valid party results in a SIP INVITE being sent
     * and that the state is set to {@link ProgressingCallingOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutWhenMoreRemotePartiesExists() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));
        assertNextContact((SipURI)mockedAlternativeSipURI.proxy(), SUCCEED);
        assertSdpOfferRetrieved(mockOutboundCall);
        assertInviteSent(SUCCEED, SUCCEED);
        assertStateProgressing(ProgressingOutboundState.ProgressingSubState.CALLING);
        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
        assertRemotePartyBlackListed();
    }

    /**
     * Verifies that a SIP timeout for a PRACK request results in an
     * error event generated, the state set to error lingering cancel and
     * a SIP CANCEL request sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForPrack() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("PRACK"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for PRACK. The call will " +
                            "be ended with a SIP CANCEL request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);

        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * A SIP timeout for a PRACK request results in an
     * error event generated, the state set to error lingering cancel and
     * a SIP CANCEL request sent.
     * This test case verifies that if sending the CANCEL fails, an error
     * event is generated, and the state is sent to Error Completed.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForPrackWhenSendingCancelFails()
            throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("PRACK"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for PRACK. The call will " +
                            "be ended with a SIP CANCEL request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a SIP timeout for a request other than INVITE or PRACK
     * results in an error event generated and the state set to error completed.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeout() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("OTHER"));
        assertErrorOccurred(mockOutboundCall,
                "SIP timeout expired. The call is considered completed.",
                NOT_DISCONNECTED);
        progressingProceedingState.processSipTimeout(sipTimeoutEvent);
    }
    
    /**
     * Verifies that if the "Max Duration Before Connected" timer specified by
     * the client expires in this state results in an {@link FailedEvent} sent,
     * a SIP CANCEL request sent, and setting the state to
     * {@link FailedLingeringByeOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleMaxDurationBeforeConnectedCallTimeout() throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "The \"max duration before connected\" timer has " +
                        "expired, i.e. the call was not connected in time." +
                        " It is handled as if a SIP 408 response was received." +
                        " Call is disconnected with a SIP CANCEL request.",
                621);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        progressingProceedingState.handleCallTimeout(
                maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that if the "Max Duration Before Connected" timer specified by
     * the client expires in this state results in an {@link FailedEvent} sent,
     * a SIP CANCEL request sent, and setting the state to
     * {@link FailedLingeringByeOutboundState}.
     * <p>
     * If sending the SIP CANCEL request fails, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleMaxDurationBeforeConnectedCallTimeoutWhenSendingCancelFails()
            throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "The \"max duration before connected\" timer has " +
                        "expired, i.e. the call was not connected in time." +
                        " It is handled as if a SIP 408 response was received." +
                        " Call is disconnected with a SIP CANCEL request.",
                621);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingProceedingState.handleCallTimeout(maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that if the Max Duration timer specified by the client expires
     * in this state it is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleOtherCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.handleCallTimeout(maxCallDurationTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockOutboundCall.expects(never());
        progressingProceedingState.handleAbandonedStream();
    }

}
