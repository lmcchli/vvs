/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * DisconnectedLingeringByeOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class DisconnectedLingeringByeOutboundStateTest extends OutboundStateCase {
    DisconnectedLingeringByeOutboundState disconnectedLingeringByeState;

    protected void setUp() throws Exception {
        super.setUp();

        disconnectedLingeringByeState = new DisconnectedLingeringByeOutboundState(
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
        disconnectedLingeringByeState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        disconnectedLingeringByeState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        disconnectedLingeringByeState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.processVideoFastUpdateRequest();
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
        disconnectedLingeringByeState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when dialing an
     * outbound call in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDial() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "Dial is not allowed in Disconnected state " +
                        "(sub state LingeringBye).");
        disconnectedLingeringByeState.dial(dialEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when sending tokens in
     * this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Disconnected state " +
                        "(sub state LingeringBye).");
        disconnectedLingeringByeState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent. State is set to
     * {@link DisconnectedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockOutboundCall);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.COMPLETED);
        disconnectedLingeringByeState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.processCancel(additionalSipRequestEvent);
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
            disconnectedLingeringByeState.processInvite(initialSipRequestEvent);
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
        disconnectedLingeringByeState.processReInvite(additionalSipRequestEvent);
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
        disconnectedLingeringByeState.processOptions(additionalSipRequestEvent);
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
        disconnectedLingeringByeState.processInfo(additionalSipRequestEvent);
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
        disconnectedLingeringByeState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO
     * and BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfoOrBye() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 100);
        disconnectedLingeringByeState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        disconnectedLingeringByeState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the state is set to
     * {@link DisconnectedCompletedOutboundState} if the response method is BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseForBye() throws Exception {
        assertResponseCodeRetrievedForMethod("BYE", 481);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.COMPLETED);
        disconnectedLingeringByeState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a Transaction timeout in this state sets
     * the state to {@link ErrorOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception
    {
        assertStateError(ErrorOutboundState.ErrorSubState.COMPLETED);
        disconnectedLingeringByeState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a Call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.handleCallTimeout(
                maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockOutboundCall.expects(never());
        disconnectedLingeringByeState.handleAbandonedStream();
    }

}
