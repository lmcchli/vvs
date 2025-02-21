/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * AlertingEarlyMediaWaitForPrackInboundState Tester.
 *
 * @author Malin Nyfeldt
 */
public class AlertingEarlyMediaWaitForPrackInboundStateTest
        extends InboundStateCase {

    AlertingEarlyMediaWaitForPrackInboundState alertingEarlyMediaWaitForPrackState;

    protected void setUp() throws Exception {
        super.setUp();

        alertingEarlyMediaWaitForPrackState =
                new AlertingEarlyMediaWaitForPrackInboundState(
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
        alertingEarlyMediaWaitForPrackState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        alertingEarlyMediaWaitForPrackState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingEarlyMediaWaitForPrackState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a NotAllowed event is generated when executing an
     * accept in this state.
     *
     * @throws Exception if test case failed.
     */
    //TODO re-test this
    /*
    public void testAccept() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Accept is not allowed in Alerting state " +
                        "(sub state EarlyMediaWaitForPrack).");
        alertingEarlyMediaWaitForPrackState.accept(acceptEvent);
    }*/

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in Alerting " +
                        "state (sub state EarlyMediaWaitForPrack).");
        alertingEarlyMediaWaitForPrackState.negotiateEarlyMediaTypes(
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
        alertingEarlyMediaWaitForPrackState.reject(rejectEvent);
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
        alertingEarlyMediaWaitForPrackState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.processAck(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processBye(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processBye(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processCancel(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processCancel(additionalSipRequestEvent);
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
            alertingEarlyMediaWaitForPrackState.processInvite(initialSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processReInvite(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processOptions(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processInfo(additionalSipRequestEvent);
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
        assertSdpInRequest(mockInboundCall, FAIL);
        assertResponseSent(SUCCEED, FAIL, "Ok", false);
        assertErrorOccurred(
                mockInboundCall,
                "Could not send SIP \"Ok\" response: Error",
                NOT_DISCONNECTED);
        alertingEarlyMediaWaitForPrackState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Test case for when a PRACK request is received and processed.
     * Verifies that a SIP "OK" response is sent for the PRACK,
     * that an {@link EarlyMediaAvailableEvent} is generated,
     * and that the state is set to {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    //TODO
/*    public void testProcessPrack() throws Exception
    {
        assertSdpInRequest(mockInboundCall, FAIL);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA);
        assertEarlyMediaAvailableEvent(mockInboundCall);
        alertingEarlyMediaWaitForPrackState.processPrack(additionalSipRequestEvent);
    }*/

    /**
     * Test case for when a PRACK request is received and processed.
     * The PRACK request contains a new SDP offer identical to the previously
     * received SDP offer.
     * Verifies that a SIP "OK" response with an SDP answer is sent for the
     * PRACK, that an {@link EarlyMediaAvailableEvent} is generated,
     * and that the state is set to {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    //TODO
/*    public void testProcessPrackWithNewSdp() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertSdpEquality(mockInboundCall, SUCCEED);
        assertLocalSdpAnswerRetrieved(mockInboundCall);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA);
        assertEarlyMediaAvailableEvent(mockInboundCall);
        alertingEarlyMediaWaitForPrackState.processPrack(additionalSipRequestEvent);
    }*/

    /**
     * Test case for when a PRACK request is received and processed.
     * The PRACK request contains a new SDP offer but the parsing of the
     * SDP offer fails.
     * This results in a SIP 488 "Not Acceptable Here" response sent, a
     * {@link FailedEvent} generated and the state set to
     * {@link FailedCompletedInboundState}.
     * @throws Exception if test case failed.
     */
    public void testProcessPrackWhenParsingNewSdpFails() throws Exception
    {
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "Could not parse remote SDP: Error. A SIP 488 " +
                        "response will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingEarlyMediaWaitForPrackState.processPrack(additionalSipRequestEvent);
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
        alertingEarlyMediaWaitForPrackState.processSipResponse(sipResponseEvent);
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
        alertingEarlyMediaWaitForPrackState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that if the configured "Not Accepted" timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Timeout" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_CallNotAccepted() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.NEAR_END_ABANDONED, CallDirection.INBOUND,
                "The inbound call was not accepted by the service in time. " +
                        "It is considered abandoned and a SIP 408 response " +
                        "will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TIMEOUT);
        alertingEarlyMediaWaitForPrackState.handleCallTimeout(callNotAcceptedTimeoutEvent);
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

        alertingEarlyMediaWaitForPrackState.handleCallTimeout(noAckTimeoutEvent);
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
        alertingEarlyMediaWaitForPrackState.handleCallTimeout(expiresTimeoutEvent);
    }

    /**
     * Verifies that if the configured "Not Accepted" timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Timeout" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NotAccepted() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.NEAR_END_ABANDONED, CallDirection.INBOUND,
                "The inbound call was not accepted by the service in time. " +
                        "It is considered abandoned and a SIP 408 response " +
                        "will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TIMEOUT);
        alertingEarlyMediaWaitForPrackState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that an unknown timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_Other() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.handleCallTimeout(anotherTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingEarlyMediaWaitForPrackState.handleAbandonedStream();
    }
}
