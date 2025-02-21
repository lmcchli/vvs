/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * FailedLingeringByeInboundState Tester.
 *
 * @author Malin Flodin
 */
public class FailedLingeringByeInboundStateTest extends InboundStateCase
{
    FailedLingeringByeInboundState failedLingeringByeState;

    protected void setUp() throws Exception {
        super.setUp();

        failedLingeringByeState = new FailedLingeringByeInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        mockInboundCall.expects(never());
        failedLingeringByeState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        failedLingeringByeState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        failedLingeringByeState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.processVideoFastUpdateRequest();
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
                "Accept is not allowed in Failed state " +
                        "(sub state LingeringBye).");
        failedLingeringByeState.accept(acceptEvent);
    }

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in Failed " +
                        "state (sub state LingeringBye).");
        failedLingeringByeState.negotiateEarlyMediaTypes(
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
                "Reject is not allowed in Failed state " +
                        "(sub state LingeringBye).");
        failedLingeringByeState.reject(rejectEvent);
    }


    /**
     * Verifies that a disconnect generates a {@link DisconnectedEvent} but is
     * otherwise ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        failedLingeringByeState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent and that the next state
     * is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(
                FailedInboundState.FailedSubState.COMPLETED);
        failedLingeringByeState.processBye(additionalSipRequestEvent);
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
        failedLingeringByeState.processCancel(additionalSipRequestEvent);
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
            failedLingeringByeState.processInvite(initialSipRequestEvent);
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
                mockInboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
        failedLingeringByeState.processReInvite(additionalSipRequestEvent);
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
        failedLingeringByeState.processOptions(additionalSipRequestEvent);
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
        failedLingeringByeState.processInfo(additionalSipRequestEvent);
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
        failedLingeringByeState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO
     * and BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfoOrBye() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 100);
        failedLingeringByeState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockInboundCall.expects(once()).method("parseMediaControlResponse");

        failedLingeringByeState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the state is set to
     * {@link FailedCompletedInboundState} if the response method is BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseForBye() throws Exception {
        assertResponseCodeRetrievedForMethod("BYE", 200);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        failedLingeringByeState.processSipResponse(sipResponseEvent);
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
                ALREADY_DISCONNECTED);
        failedLingeringByeState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a Call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        failedLingeringByeState.handleAbandonedStream();
    }

}
