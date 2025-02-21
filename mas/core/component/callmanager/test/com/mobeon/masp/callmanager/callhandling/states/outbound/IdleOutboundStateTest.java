/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.address.SipURI;

/**
 * IdleOutboundState Tester.
 *
 * @author Malin Flodin
 */
public class IdleOutboundStateTest extends OutboundStateCase {
    IdleOutboundState idleState;

    protected void setUp() throws Exception {
        super.setUp();

        idleState = new IdleOutboundState(
                (OutboundCallInternal) mockOutboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in a
     * {@link FailedEvent} sent and the state is set to
     * {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to lock request.", null);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        idleState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockOutboundCall);
        idleState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockOutboundCall);
        idleState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockOutboundCall.expects(never());
        idleState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockOutboundCall.expects(never());
        idleState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in
     * this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockOutboundCall.expects(never());
        idleState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that a disconnect request in this state results in a
     * {@link FailedEvent} and a {@link DisconnectedEvent} sent, and the state
     * is set to {@link FailedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertFailedEvent(mockOutboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call disconnected by near end.", null);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        idleState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that if inbound stream could not be created for an outbound
     * call, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testDialWhenInboundStreamCouldNotBeCreated() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, FAIL);
        assertErrorOccurred(
                mockOutboundCall, "Could not create inbound stream: Error",
                NOT_DISCONNECTED);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that if SDP offer could not be created for an outbound
     * call, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testDialWhenSdpOfferCouldNotBeCreated() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, FAIL);
        assertErrorOccurred(
                mockOutboundCall, "Could not create SDP offer: Error",
                NOT_DISCONNECTED);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that if no remote party could be found for the outbound call,
     * an error is reported.
     * @throws Exception if test case fails.
     */
    public void testDialWhenNoRemotePartyRetrieved() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, SUCCEED);
        assertNextContact(null, SUCCEED);
        assertErrorOccurred(
                mockOutboundCall,
                "Could not call out since no remote party was found. " +
                        "This only occurs if Call Manager is not " +
                        "registered with any SSP.",
                NOT_DISCONNECTED);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that if exception is thrown when retrieving a remote party
     * for the outbound call, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testDialWhenRetrievingRemotePartyGivesException() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, SUCCEED);
        assertNextContact(null, FAIL);
        assertErrorOccurred(
                mockOutboundCall,
                "Could not call out since no remote party could be created.",
                NOT_DISCONNECTED);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that if the INVITE could not be created for an outbound
     * call, an error is reported.
     * @throws Exception if test case fails.
     */
    public void testDialWhenInviteNotCreated() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, SUCCEED);
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertInviteSent(FAIL, SUCCEED);
        assertErrorOccurred(
                mockOutboundCall,
                "SIP INVITE request could not be created: Error", NOT_DISCONNECTED);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that if the INVITE could not be sent for an outbound
     * call, a {@link com.mobeon.masp.callmanager.events.FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testDialWhenInviteNotSent() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, SUCCEED);
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertInviteSent(SUCCEED, FAIL);
        assertFailedEvent(
                mockOutboundCall, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code 503", 620);
        assertStateFailed(FailedOutboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that a request to dial a new outbound call results in:
     * inbound stream created, SDP offer created, SIP INVITE sent, the "max
     * duration before connected" timer started and the state is set to
     * {@link ProgressingCallingOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testDial() throws Exception {
        assertInboundStreamCreated(mockOutboundCall, SUCCEED);
        assertSdpOfferCreated(mockOutboundCall, SUCCEED);
        assertNextContact((SipURI)mockedSipURI.proxy(), SUCCEED);
        assertInviteSent(SUCCEED, SUCCEED);
        assertMaxDurationBeforeConnectedTimerStarted();
        assertRegisterToReceiveEvents(mockOutboundCall);
        assertStateProgressing(ProgressingOutboundState.ProgressingSubState.CALLING);
        idleState.dial(dialEvent);
    }

    /**
     * Verifies that a NotAllowed event is fired when executing a
     * sending a token in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "SendToken is not allowed in Idle state.");
        idleState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * ACK in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        try {
            mockOutboundCall.expects(never());
            idleState.processAck(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * BYE in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processBye(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * CANCEL in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processCancel(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
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
            idleState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * re-INVITE in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processReInvite(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * OPTIONS in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessOptions() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processOptions(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * INFO in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfo() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processInfo(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving an
     * PRACK in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessPrack() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processPrack(additionalSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * SIP response in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponse() throws Exception {
        try {
            mockOutboundCall.expects(never());
            idleState.processSipResponse(sipResponseEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies that an Illegal State Exception is thrown when receiving a
     * SIP timeout in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeout() throws Exception {
        try {
            mockOutboundCall.expects(never());
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
            mockOutboundCall.expects(never());
            idleState.handleCallTimeout(maxCallDurationTimeoutEvent);
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
            mockOutboundCall.expects(never());
            idleState.handleAbandonedStream();
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

}
