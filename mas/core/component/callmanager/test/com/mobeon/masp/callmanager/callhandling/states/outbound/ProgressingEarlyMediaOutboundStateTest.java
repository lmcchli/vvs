/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.message.Response;
import javax.sip.address.SipURI;

/**
 * ProgressingEarlyMediaOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class ProgressingEarlyMediaOutboundStateTest extends OutboundStateCase {

    ProgressingEarlyMediaOutboundState progressingEarlyMediaState;

    protected void setUp() throws Exception {
        super.setUp();

        progressingEarlyMediaState = new ProgressingEarlyMediaOutboundState(
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
        progressingEarlyMediaState.processLockRequest();
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
        progressingEarlyMediaState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        progressingEarlyMediaState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a record request on the
     * call.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecord(mockOutboundCall);
        progressingEarlyMediaState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        assertStopRecord(mockOutboundCall);
        progressingEarlyMediaState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies reception of a request to send a Video Fast Update request.
     * It is verified that a SIP INFO request is sent and that the state is
     * left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdate() throws Exception
    {
        assertInfoSent(SUCCEED);
        assertWithoutPictureFastUpdate();
        progressingEarlyMediaState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies reception of a request to send a Video Fast Update request.
     * It is verified that when sending a SIP INFO request fails, it is
     * reported that an error occurred.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdateWhenSendingInfoFails() throws Exception
    {
        // Test when sending SIP INFO request throws exception
        assertInfoSent(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "Could not send SIP INFO request: Error", NOT_DISCONNECTED);
        assertWithoutPictureFastUpdate();
        progressingEarlyMediaState.processVideoFastUpdateRequest();
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
        progressingEarlyMediaState.disconnect(disconnectEvent);
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
        progressingEarlyMediaState.disconnect(disconnectEvent);
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
                        "(sub state EarlyMedia).");
        progressingEarlyMediaState.dial(dialEvent);
    }

    /**
     * Verifies that a NotAllowed event is fired when executing a
     * sending a token in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Progressing state " +
                        "(sub state EarlyMedia).");
        progressingEarlyMediaState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.processAck(additionalSipRequestEvent);
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
        progressingEarlyMediaState.processBye(additionalSipRequestEvent);
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
        progressingEarlyMediaState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.processCancel(additionalSipRequestEvent);
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
            progressingEarlyMediaState.processInvite(initialSipRequestEvent);
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
        progressingEarlyMediaState.processReInvite(additionalSipRequestEvent);
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
        progressingEarlyMediaState.processOptions(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a non-joined call,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenNotJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        progressingEarlyMediaState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call is
     * forwarded if the INFO request contained a VFU.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertVFU(mockOutboundCall, SUCCEED);
        assertInfoRequestForwarded(mockOutboundCall);
        progressingEarlyMediaState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call but
     * the other call is null,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoinedButOtherCallIsNull() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        progressingEarlyMediaState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call but
     * it does not contain media control,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoinedButNoMediaControl() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertVFU(mockOutboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        progressingEarlyMediaState.processInfo(additionalSipRequestEvent);
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
        progressingEarlyMediaState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that a SIP Trying response is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessTryingResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.TRYING);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP "Call Is Being Forwarded" is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessCallIsBeingForwardedResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.CALL_IS_BEING_FORWARDED);
        assertUnreliableResponse();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Queued response is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessQueuedResponse() throws Exception {
        mockOutboundCall.expects(never());
        assertResponseCodeRetrievedForMethod("INVITE", Response.QUEUED);
        assertUnreliableResponse();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a reliable SIP Queued response is acknowledged with a
     * PRACK request.
     * @throws Exception if test case fails.
     */
    public void testProcessReliableQueuedResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.QUEUED);
        assertReliableResponse();
        assertCreatePrack(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * A reliable SIP Queued response is acknowledged with a PRACK request.
     * This test case verifies that if the PRACK request could not be sent,
     * an ErrorEvent is generated, the state is set to Error completed and the
     * streams are deleted.
     * @throws Exception if test case fails.
     */
    public void testProcessReliableQueuedResponseWhenPrackCannotBeSent() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.QUEUED);
        assertReliableResponse();
        assertCreatePrack(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP PRACK request could not be sent. The call is " +
                        "considered completed. Error",
                NOT_DISCONNECTED);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Session Progress response results in an
     * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} generated
     * indicating early media.
     * @throws Exception if test case fails.
     */
    public void testProcessSessionProgressResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.SESSION_PROGRESS);
        assertProgressingEvent(mockOutboundCall, EARLY_MEDIA);
        assertUnreliableResponse();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that an unknown SIP provisional response results in an
     * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} generated
     * indicating early media.
     * @throws Exception if test case fails.
     */
    public void testProcessUnknownProvisionalResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 199);
        assertProgressingEvent(mockOutboundCall, EARLY_MEDIA);
        assertUnreliableResponse();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that when receiving a SIP OK response a SIP ACK is sent, a
     * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} is
     * generated and the state is set to {@link ConnectedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", Response.OK);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertConnectedEvent(mockOutboundCall);
        assertStateConnected();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 300", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 3xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * It is also verified that the Reason header field in the SIP response
     * is mapped to the correct network status code.
     * @throws Exception if test case fails.
     */
    public void testProcess3xxResponseWithReason() throws Exception {
        assertReasonHeader(mockResponse, 25, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 300", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        assertReasonHeader(mockResponse, 25, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 400);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 400", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 500", 621);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 5xx response in this state
     * results in the call considered rejected. A {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * It is also verified that the Reason header field in the SIP response
     * is mapped to the correct network status code.
     * @throws Exception if test case fails.
     */
    public void testProcess5xxResponseWithReason() throws Exception {
        assertReasonHeader(mockResponse, 25, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 500", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        assertReasonHeader(mockResponse, 25, 1);
        assertResponseCodeRetrievedForMethod("INVITE", 600);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 600", 613);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 2xx response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.OK);
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 400 response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess400ResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.BAD_REQUEST);
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO and the call
     * is not joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenNotJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, FAIL);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is forwarded to the other call if the method
     * is INFO and the call is joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        assertInfoResponseForwarded();
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO and the call
     * is joined but the other call is null.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenJoinedButOtherCallIsNull()
            throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 481 response to an INFO causes the call to be canceled.
     * @throws Exception if test case failed.
     */
    public void testProcess481InfoResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 481);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to 481 response.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 408 response to an INFO causes the call to be canceled.
     * @throws Exception if test case failed.
     */
    public void testProcess408InfoResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 408);
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to 408 response.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingEarlyMediaState.processSipResponse(sipResponseEvent);
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
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
        assertRemotePartyBlackListed();
    }

    /**
     * A SIP timeout in this state when retrieving the next
     * remote party gives an valid party results in a SIP INVITE being sent.
     * This test case verifies that if sending the INVITE fails, an error
     * is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutWhenMoreRemotePartiesExistsButSendingInviteFails()
            throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));
        assertNextContact((SipURI)mockedAlternativeSipURI.proxy(), SUCCEED);
        assertSdpOfferRetrieved(mockOutboundCall);
        assertInviteSent(FAIL, SUCCEED);
        assertErrorOccurred(mockOutboundCall,
                "SIP INVITE request could not be created: Error",
                NOT_DISCONNECTED);
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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

        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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
        progressingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
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
                        "expired, i.e. the call was not connected in time. " +
                        "It is handled as if a SIP 408 response was received. " +
                        "Call is disconnected with a SIP CANCEL request.",
                621);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        progressingEarlyMediaState.handleCallTimeout(
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
                        "expired, i.e. the call was not connected in time. " +
                        "It is handled as if a SIP 408 response was received. " +
                        "Call is disconnected with a SIP CANCEL request.",
                621);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        progressingEarlyMediaState.handleCallTimeout(maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that if the Max Duration timer specified by the client expires
     * in this state it is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleOtherCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        progressingEarlyMediaState.handleCallTimeout(maxCallDurationTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream in this state results
     * in disconnecting the call. A SIP CANCEL request is sent, a
     * {@link FailedEvent} is
     * generated and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.FAR_END_ABANDONED,
                CallDirection.OUTBOUND, "Call rejected due to abandoned streams.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        progressingEarlyMediaState.handleAbandonedStream();
    }

}
