package com.mobeon.masp.callmanager.callhandling.states.inbound;

import javax.sip.message.Response;

import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

/**
 * AlertingWaitForNewMediaInboundState Tester.
 *
 * @author Malin Nyfeldt
 */
public class AlertingWaitForNewMediaInboundStateTest extends InboundStateCase {

    AlertingWaitForNewMediaInboundState alertingWaitForNewMediaState;

    protected void setUp() throws Exception {
        super.setUp();

        alertingWaitForNewMediaState = new AlertingWaitForNewMediaInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in setting the
     * state to {@link FailedLingeringByeInboundState}. A {@link FailedEvent} is
     * generated and a SIP BYE request is sent..
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The Service is temporarily unavailable due to " +
                "the current administrative state: Locked.", null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        alertingWaitForNewMediaState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a 
     * {@link com.mobeon.masp.stream.PlayFailedEvent}
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        alertingWaitForNewMediaState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingWaitForNewMediaState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForNewMediaState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForNewMediaState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForNewMediaState.processVideoFastUpdateRequest();
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
                "Accept is not allowed in Alerting state (sub state WaitForNewMedia).");
        alertingWaitForNewMediaState.accept(acceptEvent);
    }

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in " +
                "Alerting state (sub state WaitForNewMedia).");
        alertingWaitForNewMediaState.negotiateEarlyMediaTypes(
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
                "Reject is not allowed in Alerting state " +
                "(sub state WaitForNewMedia).");
        alertingWaitForNewMediaState.reject(rejectEvent);
    }

    /**
     * Verifies that a disconnect in this state results in setting the
     * state to {@link FailedLingeringByeInboundState}. 
     * A {@link FailedEvent} is generated and a SIP BYE request is sent.
     * Also, in order to comply with EE (not according to CCXML spec though)
     * a {@link DisconnectedEvent} is generated.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "A disconnect is requested before the call is considered " +
                "completely connected.", null);
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        alertingWaitForNewMediaState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that when an ACK is received, the NO_ACK timer is canceled.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        assertNoAckTimerCanceled(mockInboundCall);
        alertingWaitForNewMediaState.processAck(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processBye(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processCancel(additionalSipRequestEvent);
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
            alertingWaitForNewMediaState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains no SDP, a SIP 488 "Not Acceptable Here"
     * response is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWithNoSdpOffer() throws Exception
    {
        assertSdpInRequest(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
        alertingWaitForNewMediaState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains an SDP that cannot be parsed, 
     * a SIP 488 "Not Acceptable Here"
     * response is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWhenParsingSdpFails() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
        alertingWaitForNewMediaState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains an SDP for which an intersection cannot be found,  
     * a SIP 488 "Not Acceptable Here"
     * response is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWhenNoSdpIntersectionFound() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
        alertingWaitForNewMediaState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains an SDP for which an SDP answer cannot be created,   
     * a SIP 500 response is sent for the re-INVITE, an error event is generated, 
     * a SIP BYE request is sent and the state is set to Error Lingering Bye.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWhenCreatingSdpAnswerFails() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertSdpAnswerCreated(FAIL, null);

        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "Internal error when creating response to re-INVITE. The call will " +
                "be ended with a SIP BYE request.", false);

        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingWaitForNewMediaState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE is ok, a SIP 200 OK response is sent 
     * for the re-INVITE, the No ACK timer is started and the state is set to 
     * AlertingWaitForNewMediaAck.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertGettingSdpIntersection(mockInboundCall, true);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertOutboundStreamCreated(mockInboundCall, SUCCEED);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertNoAckTimerStarted(mockInboundCall);
        assertStateAlerting(AlertingInboundState.AlertingSubState.WAIT_FOR_NEW_MEDIA_ACK);

        alertingWaitForNewMediaState.processReInvite(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processOptions(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processInfo(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processPrack(additionalSipRequestEvent);
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
        alertingWaitForNewMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP Transaction timeout in this state generates a
     * reported error.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception
    {
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "SIP timeout occurred. The call will be ended " +
                "with a SIP BYE request.", false);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        alertingWaitForNewMediaState.processSipTimeout(sipTimeoutEvent);
    }


    /**
     * Verifies that a CallNotAccepted call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_CallNotAccepted() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForNewMediaState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that a NoAck call timeout results in an Error event, the
     * state is set to Error Lingering Bye and a BYE request is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NoAck() throws Exception {
        // Called party and dialogID are used in log.
        mockInboundCall.stubs()
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.stubs()
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "The inbound call has timed out while waiting " +
                        "for an ACK. The call will be ended " +
                        "with a SIP BYE request.",
                false);

        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        alertingWaitForNewMediaState.handleCallTimeout(noAckTimeoutEvent);
    }

    /**
     * Verifies that a Redirected RTP timeout results in a Failed event, the
     * state is set to Failed Lingering Bye and a BYE request is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_RedirectedRtp() throws Exception {
        // Called party and dialogID are used in log.
        mockInboundCall.stubs()
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.stubs()
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The inbound call has timed out while waiting " +
                "for a re-INVITE with new media. The call will be ended " +
                "with a SIP BYE request.", null);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);

        alertingWaitForNewMediaState.handleCallTimeout(redirectedRtpTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingWaitForNewMediaState.handleAbandonedStream();
    }
}
