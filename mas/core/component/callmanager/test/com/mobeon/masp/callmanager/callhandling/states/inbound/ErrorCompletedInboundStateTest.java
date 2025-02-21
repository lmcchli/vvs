/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * ErrorInboundState Tester.
 *
 * @author Malin Flodin
 */
public class ErrorCompletedInboundStateTest extends InboundStateCase
{
    ErrorCompletedInboundState errorCompletedState;

    protected void setUp() throws Exception {
        super.setUp();

        errorCompletedState = new ErrorCompletedInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        mockInboundCall.expects(never());
        errorCompletedState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        errorCompletedState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        errorCompletedState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.processVideoFastUpdateRequest();
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
                "Accept is not allowed in Error state (sub state Completed).");
        errorCompletedState.accept(acceptEvent);
    }

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in Error state " +
                        "(sub state Completed).");
        errorCompletedState.negotiateEarlyMediaTypes(
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
                "Reject is not allowed in Error state (sub state Completed).");
        errorCompletedState.reject(rejectEvent);
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
        errorCompletedState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a SIP ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertOkResponseSent(mockInboundCall);
        errorCompletedState.processBye(additionalSipRequestEvent);
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
        errorCompletedState.processCancel(additionalSipRequestEvent);
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
            errorCompletedState.processInvite(initialSipRequestEvent);
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
        errorCompletedState.processReInvite(additionalSipRequestEvent);
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
        errorCompletedState.processOptions(additionalSipRequestEvent);
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
        errorCompletedState.processInfo(additionalSipRequestEvent);
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
        errorCompletedState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfo() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 200);

        errorCompletedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponse() throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockInboundCall.expects(once()).method("parseMediaControlResponse");

        errorCompletedState.processSipResponse(sipResponseEvent);
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
        errorCompletedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a Call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        errorCompletedState.handleAbandonedStream();
    }

}
