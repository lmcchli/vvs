/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CalledParty;

import javax.sip.message.Response;

/**
 * FailedWaitingForAckInboundState Tester.
 *
 * @author Malin Flodin
 */
public class FailedWaitingForAckInboundStateTest extends InboundStateCase {

    FailedWaitingForAckInboundState failedWaitingForAckState;

    protected void setUp() throws Exception {
        super.setUp();

        failedWaitingForAckState = new FailedWaitingForAckInboundState(
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
        failedWaitingForAckState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        failedWaitingForAckState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        failedWaitingForAckState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        failedWaitingForAckState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        failedWaitingForAckState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        failedWaitingForAckState.processVideoFastUpdateRequest();
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
                        "(sub state WaitingForAck).");
        failedWaitingForAckState.accept(acceptEvent);
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
                        "state (sub state WaitingForAck).");
        failedWaitingForAckState.negotiateEarlyMediaTypes(
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
                        "(sub state WaitingForAck).");
        failedWaitingForAckState.reject(rejectEvent);
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
        failedWaitingForAckState.disconnect(disconnectEvent);
    }

    /**
     * Verifies reception of an ACK.
     * <p>
     * It is verified that a SIP BYE request is sent and that the next state
     * is set to {@link FailedLingeringByeInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAckWhenPendingDisconnectExists() throws Exception
    {
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateFailed(FailedInboundState.FailedSubState.LINGERING_BYE);
        // The noAck timer is cancelled when an ack is received.
        mockInboundCall.expects(once()).method("cancelNoAckTimer");

        failedWaitingForAckState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of an ACK.
     * <p>
     * It is verified that if sending the SIP BYE request fails, an
     * error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAckWhenSendingByeFailes() throws Exception
    {
        assertCreateBye(false);
        assertSendRequestWithinDialog(true);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error", ALREADY_DISCONNECTED);
        failedWaitingForAckState.processAck(additionalSipRequestEvent);
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
        failedWaitingForAckState.processBye(additionalSipRequestEvent);
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
        failedWaitingForAckState.processCancel(additionalSipRequestEvent);
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
            failedWaitingForAckState.processInvite(initialSipRequestEvent);
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
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_PENDING);
        failedWaitingForAckState.processReInvite(additionalSipRequestEvent);
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
        failedWaitingForAckState.processOptions(additionalSipRequestEvent);
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
        failedWaitingForAckState.processInfo(additionalSipRequestEvent);
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
        failedWaitingForAckState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO
     * and BYE.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfo() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 100);
        failedWaitingForAckState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockInboundCall.expects(once()).method("parseMediaControlResponse");
        failedWaitingForAckState.processSipResponse(sipResponseEvent);
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
        failedWaitingForAckState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a CallNotAccepted call timeout is ignored in this state.
     * Verifies that a NoAck call timeout changes the state to Failed completed.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockInboundCall.expects(never());
        failedWaitingForAckState.handleCallTimeout(callNotAcceptedTimeoutEvent);

        // Called party and dialogID are used in error log.
        mockInboundCall.expects(once())
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.expects(once())
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        failedWaitingForAckState.handleCallTimeout(noAckTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        failedWaitingForAckState.handleAbandonedStream();
    }

}
