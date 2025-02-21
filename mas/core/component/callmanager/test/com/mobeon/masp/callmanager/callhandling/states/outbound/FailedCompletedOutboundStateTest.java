/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * FailedCompletedOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class FailedCompletedOutboundStateTest extends OutboundStateCase {
    FailedCompletedOutboundState failedCompletedState;

    protected void setUp() throws Exception {
        super.setUp();

        failedCompletedState = new FailedCompletedOutboundState(
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
        failedCompletedState.processLockRequest();
    }


    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        failedCompletedState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        failedCompletedState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a disconnect in this state generates a
     * {@link DisconnectedEvent}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        failedCompletedState.disconnect(disconnectEvent);
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
                        "(sub state Completed).");
        failedCompletedState.dial(dialEvent);
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
                        "(sub state Completed).");
        failedCompletedState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        failedCompletedState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockOutboundCall);
        failedCompletedState.processBye(additionalSipRequestEvent);
    }


    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.processCancel(additionalSipRequestEvent);
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
            failedCompletedState.processInvite(initialSipRequestEvent);
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
        failedCompletedState.processReInvite(additionalSipRequestEvent);
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
        failedCompletedState.processOptions(additionalSipRequestEvent);
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
        failedCompletedState.processInfo(additionalSipRequestEvent);
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
        failedCompletedState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfo() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 200);
        failedCompletedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 200);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        failedCompletedState.processSipResponse(sipResponseEvent);
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
        failedCompletedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a Call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.handleCallTimeout(maxCallDurationTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockOutboundCall.expects(never());
        failedCompletedState.handleAbandonedStream();
    }

}
