package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.message.Response;

/**
 * AlertingEarlyMediaInboundState Tester.
 *
 * @author Malin Flodin
 */
public class AlertingEarlyMediaInboundStateTest extends InboundStateCase
{
    AlertingEarlyMediaInboundState alertingEarlyMediaState;

    protected void setUp() throws Exception {
        super.setUp();

        alertingEarlyMediaState = new AlertingEarlyMediaInboundState(
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
        alertingEarlyMediaState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a play request on the call.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlay(mockInboundCall);
        alertingEarlyMediaState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingEarlyMediaState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        assertStopPlay(mockInboundCall);
        alertingEarlyMediaState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaState.processVideoFastUpdateRequest();
    }

    /**
     * A SIP "OK" response is sent when accept() executed
     * succesfully. Tbis test case verifies that when an exception is thrown
     * when creating the SIP "OK" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenSendingOkFails() throws Exception
    {
        assertNoReliableResponses();
        assertLocalSdpAnswerRetrieved(mockInboundCall);
        assertResponseSent(true, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Ok\" response: Error", NOT_DISCONNECTED);
        alertingEarlyMediaState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "OK" response is sent when accept() executed
     * succesfully. Verifies that the state is set to
     * {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testAccept() throws Exception
    {
        assertNoReliableResponses();
        assertLocalSdpAnswerRetrieved(mockInboundCall);
        assertResponseSent(false, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingEarlyMediaState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "OK" response is sent when accept() executed
     * succesfully. Verifies that the Ok response contains no SDP if the
     * previous 183 was sent reliably
     * {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenOkContainsNoSdp() throws Exception
    {
        assertReliableResponses();
        assertResponseSent(false, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingEarlyMediaState.accept(acceptEvent);
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
                        "state (sub state EarlyMedia).");
        alertingEarlyMediaState.negotiateEarlyMediaTypes(
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
        alertingEarlyMediaState.reject(rejectEvent);
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
        alertingEarlyMediaState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockInboundCall.expects(never());
        alertingEarlyMediaState.processAck(additionalSipRequestEvent);
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
                "Call disconnected early by far end.", 621);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingEarlyMediaState.processBye(additionalSipRequestEvent);
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
        assertReasonHeader(mockAdditionalRequest, 17, 1);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 603);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingEarlyMediaState.processBye(additionalSipRequestEvent);
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
                "Call disconnected early by far end.", 621);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingEarlyMediaState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the CANCEL
     * request.
     * It is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP CANCEL
     * request is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancelWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 17, null);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 603);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingEarlyMediaState.processCancel(additionalSipRequestEvent);
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
            alertingEarlyMediaState.processInvite(initialSipRequestEvent);
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
        alertingEarlyMediaState.processReInvite(additionalSipRequestEvent);
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
        alertingEarlyMediaState.processOptions(additionalSipRequestEvent);
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
        alertingEarlyMediaState.processInfo(additionalSipRequestEvent);
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
        alertingEarlyMediaState.processPrack(additionalSipRequestEvent);
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
        alertingEarlyMediaState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a Transaction timeout in this state generates a
     * reported error.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception
    {
        assertErrorOccurred(mockInboundCall,
                "SIP timeout expired. The call is considered completed.",
                NOT_DISCONNECTED);
        alertingEarlyMediaState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that if the configured "Not Accepted" timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Timeout" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallNotAcceptedTimeout() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.NEAR_END_ABANDONED, CallDirection.INBOUND,
                "The inbound call was not accepted by the service in time. " +
                        "It is considered abandoned and a SIP 408 response " +
                        "will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TIMEOUT);
        alertingEarlyMediaState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that if the Expires timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Terminated" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleExpiresTimeout() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The expires timer expired for the INVITE. A SIP 487 response " +
                        "will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        alertingEarlyMediaState.handleCallTimeout(expiresTimeoutEvent);
    }

    /**
     * Verifies that if the another call timer than the "Not Accepted" or
     * Expires timers expires in this state it is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleOtherCallTimeout() throws Exception {
        alertingEarlyMediaState.handleCallTimeout(anotherTimeoutEvent);
    }


    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaState.handleAbandonedStream();
    }

}

