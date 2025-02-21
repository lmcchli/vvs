/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * IdleInboundState Tester.
 *
 * @author Malin Flodin
 */
public class IdleInboundStateTest extends InboundStateCase
{
    IdleInboundState idleState;

    protected void setUp() throws Exception {
        super.setUp();

        idleState = new IdleInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * lock in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        try {
            idleState.processLockRequest();
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        idleState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        idleState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        idleState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        idleState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * video fast update request in {@link IdleInboundState}.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        try {
            idleState.processVideoFastUpdateRequest();
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing an
     * accept in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testAccept() throws Exception
    {
        try {
            idleState.accept(acceptEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * negotiateEarlyMediaTypes in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        try {
            idleState.negotiateEarlyMediaTypes(negotiateEarlyMediaTypesEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * reject in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testReject() throws Exception
    {
        try {
            idleState.reject(rejectEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when executing a
     * disconnect in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception
    {
        try {
            idleState.disconnect(disconnectEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * ACK in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        try {
            idleState.processAck(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * BYE in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception
    {
        try {
            idleState.processBye(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * CANCEL in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception
    {
        try {
            idleState.processCancel(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that a SIP "Bad Request" response is sent when the SDP
     * offer could not be parsed. A {@link FailedEvent} is generated and the
     * state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInviteWhenParsingSdpFails() throws Exception
    {
        assertResponseSent(false, false, "Trying", true);
        assertParsingSdpBody(mockInboundCall, true);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "Could not parse remote SDP: Error. A SIP 488 " +
                        "response will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        idleState.processInvite(initialSipRequestEvent);
    }

    /**
     * Verify that a SIP INVITE with no SDP offer is handled in exactly the same
     * way as an INVITE with SDP offer.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInviteWhenNoSdpOffer() throws Exception
    {
        assertParsingSdpBody(mockInboundCall, false);
        assertRetrievingSdpBody(mockInboundCall, true);
        assertGetContactHeaders(mockInitialRequest);
        assertFarEndConnection(mockInboundCall);
        assertServiceLoaded(false);
        assertResponseSent(false, false, "Trying", true);
        assertRegisterToReceiveEvents(mockInboundCall);
        assertNotAcceptedTimerStarted();
        assertExpiresTimerStarted();
        assertAlertingEvent(mockInboundCall);
        assertStateAlerting(AlertingInboundState.AlertingSubState.NEW_CALL);
        idleState.processInvite(initialSipRequestEvent);

    }

    /**
     * Verifies that when an exception is thrown when loading the service,
     * a SIP "Server Internal Error" response is sent and an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInviteLoadServiceFailed() throws Exception
    {
        // Test when creating SIP Trying throws an exception
        assertParsingSdpBody(mockInboundCall, false);
        assertRetrievingSdpBody(mockInboundCall, false);
        assertResponseSent(false, false, "Trying", true);
        assertGetContactHeaders(mockInitialRequest);
        assertFarEndConnection(mockInboundCall);
        assertServiceLoaded(true);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Service could not be loaded: Error", NOT_DISCONNECTED);
        idleState.processInvite(initialSipRequestEvent);
    }

    /**
     * Verifies that when an exception is thrown when sending the SIP
     * "Trying" response. An error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInviteSendTryingResponseFailed() throws Exception
    {
        // Test when creating SIP Trying throws an exception
        assertResponseSent(true, false, "Trying", true);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Trying\" response: Error", NOT_DISCONNECTED);
        idleState.processInvite(initialSipRequestEvent);

        // Test when sending SIP Trying throws an exception
        assertResponseSent(false, true, "Trying", true);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Trying\" response: Error", NOT_DISCONNECTED);
        idleState.processInvite(initialSipRequestEvent);
    }

    /**
     * Verify that all headers are correct, the SDP body exists and can be
     * parsed.
     * Also verifies that the application is loaded, that a SIP
     * "Trying" response is sent, that the "Not Accepted" and Expires timers
     * are started and that the state is set to {@link AlertingNewCallInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInvite() throws Exception
    {
        assertParsingSdpBody(mockInboundCall, false);
        assertRetrievingSdpBody(mockInboundCall, false);
        assertGetContactHeaders(mockInitialRequest);
        assertFarEndConnection(mockInboundCall);
        assertServiceLoaded(false);
        assertResponseSent(false, false, "Trying", true);
        assertRegisterToReceiveEvents(mockInboundCall);
        assertNotAcceptedTimerStarted();
        assertExpiresTimerStarted();
        assertAlertingEvent(mockInboundCall);
        assertStateAlerting(AlertingInboundState.AlertingSubState.NEW_CALL);
        idleState.processInvite(initialSipRequestEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * re-INVITE in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception
    {
        try {
            idleState.processReInvite(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * OPTIONS in Idle state.
     *
     * @throws Exception if test case fails.
     */
    public void testProcessOptions() throws Exception
    {
        try {
            idleState.processOptions(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * INFO in Idle state.
     *
     * @throws Exception if test case fails.
     */
    public void testProcessInfo() throws Exception
    {
        try {
            idleState.processInfo(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * PRACK in Idle state.
     *
     * @throws Exception if test case fails.
     */
    public void testProcessPrack() throws Exception
    {
        try {
            idleState.processPrack(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * response in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipResponse() throws Exception
    {
        try {
            idleState.processSipResponse(sipResponseEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * timeout in Idle state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeout() throws Exception
    {
        try {
            idleState.processSipTimeout(sipTimeoutEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * Call timeout in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout() throws Exception {
        try {
            mockInboundCall.expects(never());
            idleState.handleCallTimeout(callNotAcceptedTimeoutEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when detecting an
     * abandoned stream in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        try {
            mockInboundCall.expects(never());
            idleState.handleAbandonedStream();
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

}
