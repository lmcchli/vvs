/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * ErrorLingeringCancelOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class ErrorLingeringCancelOutboundStateTest
        extends OutboundStateCase {

    ErrorLingeringCancelOutboundState errorLingeringCancelState;

    protected void setUp() throws Exception {
        super.setUp();

        errorLingeringCancelState =
                new ErrorLingeringCancelOutboundState(
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
        errorLingeringCancelState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        errorLingeringCancelState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        errorLingeringCancelState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a disconnect in this state generates a
     * {@link com.mobeon.masp.callmanager.events.DisconnectedEvent}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        errorLingeringCancelState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when dialing an
     * outbound call in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDial() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "Dial is not allowed in Error state " +
                        "(sub state LingeringCancel).");
        errorLingeringCancelState.dial(dialEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when sending tokens in
     * this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Error state " +
                        "(sub state LingeringCancel).");
        errorLingeringCancelState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent. State is set to
     * {@link ErrorCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockOutboundCall);
        assertStateError(
                ErrorOutboundState.ErrorSubState.COMPLETED);
        errorLingeringCancelState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.processCancel(additionalSipRequestEvent);
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
            errorLingeringCancelState.processInvite(initialSipRequestEvent);
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
        errorLingeringCancelState.processReInvite(additionalSipRequestEvent);
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
        errorLingeringCancelState.processOptions(additionalSipRequestEvent);
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
        errorLingeringCancelState.processInfo(additionalSipRequestEvent);
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
        errorLingeringCancelState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO
     * and INVITE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfoOrInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 100);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a response to the CANCEL is "ignored" in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseForCancel() throws Exception {
        assertResponseCodeRetrievedForMethod("CANCEL", 200);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 1xx response to the INVITE is "ignored" in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess1xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 100);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 3xx response to the INVITE results in
     * {@link ErrorCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess3xxesponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 300);
        assertStateError(ErrorOutboundState.ErrorSubState.COMPLETED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 4xx response to the INVITE results in
     * {@link ErrorCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess4xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 400);
        assertStateError(ErrorOutboundState.ErrorSubState.COMPLETED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 5xx response to the INVITE results in
     * {@link ErrorCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess5xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 500);
        assertStateError(ErrorOutboundState.ErrorSubState.COMPLETED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 6xx response to the INVITE results in
     * {@link ErrorCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess6xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 600);
        assertStateError(ErrorOutboundState.ErrorSubState.COMPLETED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE results in a SIP ACK and a
     * SIP BYE request. The state is set to
     * {@link ErrorLingeringByeOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInvite() throws Exception {
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
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
        assertErrorOccurred(mockOutboundCall,
                "SIP ACK request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
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
        assertErrorOccurred(mockOutboundCall,
                "SIP ACK request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 2xx response to the INVITE that results in a SIP ACK and
     * SIP BYE generates a reported error if there
     * was an error when sending the BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcess2xxResponseForInviteWhenSendingByeFails() throws Exception {
        // Create BYE fails
        assertResponseCodeRetrievedForMethod("INVITE", 200);
        assertCreateAck(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertCreateBye(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call " +
                        "is considered completed. Error",
                ALREADY_DISCONNECTED);
        errorLingeringCancelState.processSipResponse(sipResponseEvent);
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
        errorLingeringCancelState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a Call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.handleCallTimeout(
                maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockOutboundCall.expects(never());
        errorLingeringCancelState.handleAbandonedStream();
    }

}
