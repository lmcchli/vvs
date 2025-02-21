/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * FailedWaitingForResponseOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class FailedWaitingForResponseOutboundStateTest
        extends OutboundStateCase {

    FailedWaitingForResponseOutboundState failedWaitingForResponseState;

    protected void setUp() throws Exception {
        super.setUp();

        failedWaitingForResponseState =
                new FailedWaitingForResponseOutboundState(
                        (OutboundCallInternal) mockOutboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        failedWaitingForResponseState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        failedWaitingForResponseState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a disconnect in this state generates a
     * {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        failedWaitingForResponseState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when dialing an
     * outbound call in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDial() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "Dial is not allowed in Failed state " +
                        "(sub state WaitingForResponse).");
        failedWaitingForResponseState.dial(dialEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when sending tokens in
     * this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Failed state " +
                        "(sub state WaitingForResponse).");
        failedWaitingForResponseState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent. State is set to
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockOutboundCall);
        assertStateFailed(
                FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.processCancel(additionalSipRequestEvent);
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
            failedWaitingForResponseState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * It is verified that a SIP "Not Acceptable Here" response is sent for
     * the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception {
        assertNotAcceptableResponseSent(
                mockOutboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
        failedWaitingForResponseState.processReInvite(additionalSipRequestEvent);
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
        failedWaitingForResponseState.processOptions(additionalSipRequestEvent);
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
        failedWaitingForResponseState.processInfo(additionalSipRequestEvent);
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
        failedWaitingForResponseState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO
     * and INVITE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfoOrInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 200);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 200);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * A 1xx response to the INVITE in this state results in a SIP CANCEL
     * request sent and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess1xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 100);
        assertCreateCancel(SUCCEED);
        assertSendRequest(SUCCEED);
        assertCancelNoResponseTimer();
        assertStateFailed(
                FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * A 1xx response to the INVITE in this state results in a SIP CANCEL
     * request sent and the state is set to
     * {@link FailedLingeringCancelOutboundState}.
     * This test case verifies that if the SIP CANCEL request could not be sent,
     * an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess1xxResponseForInviteWhenCancelCouldNotBeSent()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 100);
        assertCreateCancel(SUCCEED);
        assertSendRequest(FAIL);
        assertCancelNoResponseTimer();
        assertStateFailed(
                FailedOutboundState.FailedSubState.LINGERING_CANCEL);
        assertErrorOccurred(mockOutboundCall,
                "SIP CANCEL request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 3xx response to the INVITE results in
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess3xxResponseForInvite() throws Exception {
        // Verify for 3xx response
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 4xx response to the INVITE results in
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess4xxResponseForInvite() throws Exception {
        // Verify for 4xx response
        assertResponseCodeRetrievedForMethod("INVITE", 400);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 5xx response to the INVITE results in
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess5xxResponseForInvite() throws Exception {
        // Verify for 5xx response
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 6xx response to the INVITE results in
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess6xxResponseForInvite() throws Exception {
        // Verify for 6xx response
        assertResponseCodeRetrievedForMethod("INVITE", 600);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE results in a SIP ACK and a
     * SIP BYE request. The state is set to
     * {@link FailedLingeringByeOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE that results in a SIP ACK and
     * SIP BYE generates a reported error if there
     * was an error when creating the ACK.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInviteWhenCreatingAckFails() throws Exception {
        // Create ACK fails
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(FAIL);
        assertCancelNoResponseTimer();
        assertErrorOccurred(mockOutboundCall,
                "SIP ACK request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE that results in a SIP ACK and
     * SIP BYE generates a reported error if there
     * was an error when sending the ACK.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInviteWhenSendingAckFails() throws Exception {
        // Send ACK fails
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertCancelNoResponseTimer();
        assertErrorOccurred(mockOutboundCall,
                "SIP ACK request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE that results in a SIP ACK and
     * SIP BYE generates a reported error if there
     * was an error when creating BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInviteWhenCreatingByeFails() throws Exception {
        // Create BYE fails
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertCancelNoResponseTimer();
        assertStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        failedWaitingForResponseState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a Transaction timeout in this state generates a
     * reported error.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception
    {
        assertErrorOccurred(mockOutboundCall,
                "SIP timeout expired. The call is considered completed.",
                ALREADY_DISCONNECTED);
        failedWaitingForResponseState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a CALL_NOT_CONNECTED Call timeout is ignored in this state.
     * Verifies that a NO_RESPONSE Call timeout is handled in this state. The
     * state is changed to Failed completed.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.handleCallTimeout(
                maxDurationBeforeConnectedTimeoutEvent);

        mockOutboundCall.stubs().method("getInitialDialogId").will(returnValue("dialogid"));
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        failedWaitingForResponseState.handleCallTimeout(
                noResponseTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockOutboundCall.expects(never());
        failedWaitingForResponseState.handleAbandonedStream();
    }

    private void assertCancelNoResponseTimer() throws Exception {
        mockOutboundCall.expects(once()).method("cancelNoResponseTimer");
    }
}
