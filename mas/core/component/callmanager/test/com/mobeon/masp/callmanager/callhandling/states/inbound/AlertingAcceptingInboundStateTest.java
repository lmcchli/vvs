package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;

import javax.sip.message.Response;

/**
 * AlertingAcceptingInboundState Tester.
 *
 * @author Malin Flodin
 */
public class AlertingAcceptingInboundStateTest extends InboundStateCase
{
    AlertingAcceptingInboundState alertingAcceptingState;

    protected void setUp() throws Exception {
        super.setUp();

        alertingAcceptingState = new AlertingAcceptingInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in setting the
     * state to {@link FailedWaitingForAckInboundState} since a BYE cannot be
     * sent until an ACK is received for the INVITE. A {@link FailedEvent} is
     * generated.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The Service is temporarily unavailable due to " +
                "the current administrative state: Locked.", null);
        assertStateFailed(FailedInboundState.FailedSubState.WAITING_FOR_ACK);
        assertStreamsDeleted(mockInboundCall);
        alertingAcceptingState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a play request on the call.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlay(mockInboundCall);
        alertingAcceptingState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingAcceptingState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        assertStopPlay(mockInboundCall);
        alertingAcceptingState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingAcceptingState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingAcceptingState.processVideoFastUpdateRequest();
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
                "Accept is not allowed in Alerting state (sub state Accepting).");
        alertingAcceptingState.accept(acceptEvent);
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
                        "state (sub state Accepting).");
        alertingAcceptingState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when executing a
     * reject in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testReject() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Reject is not allowed in Alerting state (sub state Accepting).");
        alertingAcceptingState.reject(rejectEvent);
    }

    /**
     * Verifies that a disconnect in this state results in setting the
     * state to {@link FailedWaitingForAckInboundState} since a BYE cannot be
     * sent until an ACK is received for the INVITE. A {@link FailedEvent} is
     * generated.
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
        assertStateFailed(FailedInboundState.FailedSubState.WAITING_FOR_ACK);
        assertStreamsDeleted(mockInboundCall);
        alertingAcceptingState.disconnect(disconnectEvent);
    }

    /**
     * Verifies reception of an ACK for an INVITE that contained an SDP offer.
     * A {@link ConnectedEvent} is generated and the next
     * state is set to {@link ConnectedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        assertRemoteSdpOfferExists();
        assertConnectedEvent(mockInboundCall);
        assertStateConnected();
        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if the SDP answer could not be parsed a SIP BYE request is sent.
     * A {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenParsingSdpFails() throws Exception {
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertErrorEvent(mockInboundCall,CallDirection.INBOUND,
                "Could not parse remote SDP answer: Error. " +
                        "Call is disconnected with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if the SDP answer could not be parsed and the BYE request
     * could not be sent an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenParsingSdpFailsAndCreatingByeFails()
            throws Exception {
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertErrorEvent(mockInboundCall,CallDirection.INBOUND,
                "Could not parse remote SDP answer: Error. " +
                        "Call is disconnected with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if the ACK contained no SDP answer a SIP BYE request is sent,
     * a {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWithNoSdpAnswer() throws Exception {
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertRetrievingSdpBody(mockInboundCall, FAIL);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "The SIP ACK contained no SDP answer.", null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if the ACK contained no SDP answer and a SIP BYE request
     * could not be sent, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWithNoSdpAnswerWhenCreatingByeFails()
            throws Exception {
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertRetrievingSdpBody(mockInboundCall, FAIL);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "The SIP ACK contained no SDP answer.", null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if no SDP intersection could be found a SIP BYE request is sent,
     * a {@link FailedEvent} is generated and the
     * state is set to {@link FailedLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenNoSdpIntersectionFound()
            throws Exception {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. Call is disconnected with a SIP BYE request.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if the SDP intersection does not match the call type of the
     * call a SIP BYE request is sent, a {@link FailedEvent} is generated and
     * the state is set to {@link FailedLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenNoSdpIntersectionFoundDueToCallType()
            throws Exception {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        mockInboundCall.stubs().method("getCallType").
                will(returnValue(CallProperties.CallType.VIDEO));
        assertGettingSdpIntersection(mockInboundCall, true);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. The call type (VIDEO) did not " +
                        "match the type of the SDP intersection (VOICE). " +
                        "Call is disconnected with a SIP BYE request.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }



    /**
     * Verifies that when the INVITE contained no SDP offer and an ACK is
     * received, if no SDP intersection could be found and SIP BYE request
     * cannot be sent, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenNoSdpIntersectionFoundAndCreatingByeFails()
            throws Exception {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. Call is disconnected with a SIP BYE request.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP fofer and an ACK is
     * received, if the outbound stream could not be created a SIP BYE request
     * is sent, an {@link ErrorEvent} is generated and the
     * state is set to {@link ErrorLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenCreatingStreamFails()
            throws Exception {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertOutboundStreamCreated(mockInboundCall, FAIL);
        assertErrorEvent(mockInboundCall,
            CallDirection.INBOUND,
            "Could not create outbound stream: Error. Call is disconnected " +
                    "with a SIP BYE request.", false);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when the INVITE contained no SDP fofer and an ACK is
     * received, if the outbound stream could not be created a SIP BYE request
     * could not be sent, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testProcessAckWhenCreatingStreamFailsAndCreatingByeFails()
            throws Exception {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertOutboundStreamCreated(mockInboundCall, FAIL);
        assertErrorEvent(mockInboundCall,
            CallDirection.INBOUND,
            "Could not create outbound stream: Error. Call is disconnected " +
                    "with a SIP BYE request.", false);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);

        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of an ACK for an INVITE that contained no SDP offer.
     * The SDP answer is parsed, and intersection is retrieved and an outbound
     * media stream is created.
     * A {@link ConnectedEvent} is generated and the next
     * state is set to {@link ConnectedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAckWhenInviteContainedNoSdpOffer() throws Exception
    {
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertOutboundStreamCreated(mockInboundCall, SUCCEED);
        assertConnectedEvent(mockInboundCall);
        assertStateConnected();
        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when redirected RTP is activated an ACK for an INVITE 
     * that contained no SDP offer is handled as normal except that no 
     * outbound media stream is created, no {@link ConnectedEvent} is generated,
     * and the state is set to {@link AlertingWaitForCallHoldInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAckWhenInviteContainedNoSdpOfferAndRedirectedRtpActivated() throws Exception
    {
        assertRedirectedRtpActivated();
        assertRetrievingSdpBody(mockInboundCall, SUCCEED);
        assertNoRemoteSdpOfferExists();
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStateAlerting(AlertingInboundState.AlertingSubState.WAIT_FOR_CALL_HOLD);
        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies that when redirected RTP is activated an ACK is handled as normal 
     * except that no {@link ConnectedEvent} is generated,
     * and the state is set to {@link AlertingWaitForCallHoldInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAckWhenRedirectedRtpActivated() throws Exception
    {
        assertRedirectedRtpActivated();
        assertRemoteSdpOfferExists();
        assertStateAlerting(AlertingInboundState.AlertingSubState.WAIT_FOR_CALL_HOLD);
        alertingAcceptingState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent for the BYE request,
     * that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 621);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingAcceptingState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent for the BYE request,
     * that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessByeWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 17, 0);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 614);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingAcceptingState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "OK" response is sent for the CANCEL
     * request and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception
    {
        assertOkResponseSent(mockInboundCall);
        alertingAcceptingState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when processing an
     * INVITE in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInvite() throws Exception
    {
        try {
            alertingAcceptingState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * It is verified that a SIP "Request Pending" response is sent for
     * the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_PENDING);
        alertingAcceptingState.processReInvite(additionalSipRequestEvent);
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
        alertingAcceptingState.processOptions(additionalSipRequestEvent);
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
        alertingAcceptingState.processInfo(additionalSipRequestEvent);
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
        assertErrorResponseSent(mockInboundCall, Response.FORBIDDEN);
        alertingAcceptingState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the status code of the response is retrieved for logging
     * purposes but that the response otherwise is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponse() throws Exception
    {
        assertResponseCodeRetrievedForMethod("INFO", 499);
        alertingAcceptingState.processSipResponse(sipResponseEvent);
    }

    /**
     * A SIP Timeout in this state means that an ACK has timed out.
     * Verifies that a Transaction timeout in this state generates a
     * SIP BYE request. The state is set to ErrorLingeringByeInboundState.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception
    {
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "SIP timeout occurred while waiting for ACK. " +
                        "The call will be ended with a SIP BYE request.", false);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        alertingAcceptingState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a CallNotAccepted call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_CallNotAccepted() throws Exception {
        mockInboundCall.expects(never());
        alertingAcceptingState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that a NoAck call timeout results in an Error event and the
     * state is set to Error Lingering Bye.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NoAck() throws Exception {
        // Called party and dialogID are used in error log.
        mockInboundCall.stubs()
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.stubs()
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "The inbound call has timed out while waiting " +
                    "for an ACK. The call is considered disconnected.",
                false);
        assertStreamsDeleted(mockInboundCall);

        alertingAcceptingState.handleCallTimeout(noAckTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingAcceptingState.handleAbandonedStream();
    }

}
