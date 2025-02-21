/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.message.Response;

/**
 * ConnectedOutboundState Tester.
 *
 * @author Malin Nyfeldt
 */
public class ConnectedOutboundStateTest extends OutboundStateCase {
    ConnectedOutboundState connectedState;

    protected void setUp() throws Exception {
        super.setUp();

        connectedState = new ConnectedOutboundState(
                (OutboundCallInternal) mockOutboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in disconnecting
     * the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is generated and the
     * state is set to {@link DisconnectedLingeringByeOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.processLockRequest();
    }


    /**
     * Verifies that a lock request in this state results in disconnecting
     * the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is generated and the
     * state is set to {@link DisconnectedLingeringByeOutboundState}.
     * <p>
     * If sending the BYE request fails an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testLockWhenSendingByeFails() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(
                mockOutboundCall,
               "SIP BYE request could not be sent. " +
                    "The call is considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a play request on the call.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlay(mockOutboundCall);
        connectedState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a record request on the
     * call.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecord(mockOutboundCall);
        connectedState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        assertStopPlay(mockOutboundCall);
        connectedState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        assertStopRecord(mockOutboundCall);
        connectedState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies reception of a request to send a Video Fast Update request.
     * It is verified that a SIP INFO request is sent.
     * <p>
     * This test case verifies that an exception thrown when the request is
     * sent is handled and results in an error reported.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdateWhenExceptionIsThrown() throws Exception
    {
        // Test when sending SIP INFO request throws exception
        assertInfoSent(true);
        assertErrorOccurred(mockOutboundCall,
                "Could not send SIP INFO request: Error", NOT_DISCONNECTED);
        assertWithoutPictureFastUpdate();
        connectedState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies reception of a request to send a Video Fast Update request.
     * It is verified that a SIP INFO request is sent and that the state is
     * left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdate() throws Exception
    {
        assertInfoSent(false);
        assertWithoutPictureFastUpdate();
        connectedState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies reception of a request to send a Video Fast Update request.
     * It is verified that a SIP INFO request is sent and that the state is
     * left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdateWithRTCP() throws Exception
    {
        assertWithPictureFastUpdate();
        connectedState.processVideoFastUpdateRequest();
    }

   /**
     * Verifies that a disconnect request in this state results in
     * disconnecting the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is generated and the
     * state is set to {@link DisconnectedLingeringByeOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a disconnect request in this state results in
     * disconnecting the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is generated and the
     * state is set to {@link DisconnectedLingeringByeOutboundState}.
     * <p>
     * If sending the BYE request fails an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnectWhenSendingByeFails() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(
                mockOutboundCall,
               "SIP BYE request could not be sent. " +
                    "The call is considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that a NotAllowed event is generated when dialing an
     * outbound call in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testDial() throws Exception {
        assertNotAllowedEvent(mockOutboundCall,
                "Dial is not allowed in Connected state.");
        connectedState.dial(dialEvent);
    }

    /**
     * Verifies that tokens sent in this state are sent to the outbound
     * stream.
     * @throws Exception if test case failed.
     */
    public void testSendToken() throws Exception {
        assertTokenSent();
        connectedState.sendToken(sendTokenEvent);
    }

    /**
     * Verifies that when an ACK is received, the no ack timer is canceled.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        assertNoAckTimerCanceled(mockOutboundCall);
        connectedState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent and that a
     * {@link DisconnectedEvent} is generated.
     * Finally it is verified that the next state is set to
     * {@link DisconnectedCompletedOutboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.FAR_END, false);
        assertOkResponseSent(mockOutboundCall);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.COMPLETED);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies that a CANCEL is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception {
        mockOutboundCall.expects(never());
        connectedState.processCancel(additionalSipRequestEvent);
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
            connectedState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains no SDP, a SIP 488 "Not Acceptable Here"
     * response is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWithNoSdpOffer() throws Exception
    {
        assertSdpInRequest(mockOutboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockOutboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
        connectedState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains an SDP different from the previously
     * received remote SDP, a SIP 488 "Not Acceptable Here"
     * response is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWithDifferentSdpOffer() throws Exception
    {
        assertSdpInRequest(mockOutboundCall, SUCCEED);
        assertParsingSdpBody(mockOutboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockOutboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
        connectedState.processReInvite(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP re-INVITE request.
     * If the re-INVITE contains an SDP identical to the previously
     * received remote SDP, a SIP 200 "OK" response with an SDP answer
     * is sent for the re-INVITE and that the state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInviteWithIdenticalSdpOffer() throws Exception
    {
        assertSdpInRequest(mockOutboundCall, SUCCEED);
        assertParsingSdpBody(mockOutboundCall, SUCCEED);
        assertSdpEquality(mockOutboundCall, SUCCEED);
        assertLocalSdpAnswerRetrieved(mockOutboundCall);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertNoAckTimerStarted(mockOutboundCall);
        connectedState.processReInvite(additionalSipRequestEvent);
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
        connectedState.processOptions(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a non-joined call,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenNotJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        connectedState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call is
     * forwarded if the INFO request contained a VFU.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertVFU(mockOutboundCall, SUCCEED);
        assertInfoRequestForwarded(mockOutboundCall);
        connectedState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call but
     * the other call is null,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoinedButOtherCallIsNull() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        connectedState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call but
     * it does not contain media control,
     * a SIP 405 "Method Not Allowed" response is sent for the INFO request and
     * the state is left unchanged.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoinedButNoMediaControl() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertVFU(mockOutboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockOutboundCall);
        connectedState.processInfo(additionalSipRequestEvent);
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
        connectedState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfo() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 100);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO and the call
     * is not joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenNotJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, FAIL);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is forwarded to the other call if the method
     * is INFO and the call is joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenJoined() throws Exception {
        assertJoinedOtherCall(mockOutboundCall, SUCCEED);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        assertInfoResponseForwarded();
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO and the call
     * is joined but the other call is null.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenJoinedButOtherCallIsNull()
            throws Exception {
        assertJoinedOtherCall(mockOutboundCall, FAIL);
        assertCallJoined(mockOutboundCall, SUCCEED);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockOutboundCall.expects(once()).method("parseMediaControlResponse");
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 481 response to an INFO causes the call to be disconnected.
     * @throws Exception if test case failed.
     */
    public void testProcess481InfoResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 481);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a 408 response to an INFO causes the call to be disconnected.
     * @throws Exception if test case failed.
     */
    public void testProcess408InfoResponse()
            throws Exception {
        assertResponseCodeRetrievedForMethod("INFO", 408);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStreamsDeleted(mockOutboundCall);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 2xx response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess2xxResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.OK);
        mockOutboundCall.expects(never());
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 400 response for a PRACK request is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcess400ResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod("PRACK", Response.BAD_REQUEST);
        mockOutboundCall.expects(never());
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP 481 response for a PRACK request results in a
     * disconnected event, the state set to lingering bye, the streams are
     * deleted and a SIP BYE request is sent.
     * @throws Exception if test case fails.
     */
    public void testProcess481ResponseToPrack() throws Exception {
        assertResponseCodeRetrievedForMethod(
                "PRACK", Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);

        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertStreamsDeleted(mockOutboundCall);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * A SIP 408 response for a PRACK request results in a
     * disconnected event, the state set to lingering bye, the streams are
     * deleted and a SIP BYE request is sent.
     * If sending the BYE fails, an error event is generated, and the
     * state is sent to Error Completed.
     * @throws Exception if test case fails.
     */
    public void testProcess408ResponseToPrackWhenSendingCancelFails()
            throws Exception {
        assertResponseCodeRetrievedForMethod(
                "PRACK", Response.REQUEST_TIMEOUT);
        assertDisconnectedEvent(
                mockOutboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(FAIL);
        assertStreamsDeleted(mockOutboundCall);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that a SIP timeout for a PRACK request results in an
     * error event generated, the state set to error lingering bye and
     * a SIP BYE request sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForPrack() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("PRACK"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for SIP PRACK. The call will " +
                            "be ended with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * A SIP timeout for a PRACK request results in an
     * error event generated, the state set to error lingering bye and
     * a SIP BYE request sent.
     * This test case verifies that if sending the BYE fails, an error
     * event is generated, and the state is sent to Error Completed.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForPrackWhenSendingByeFails()
            throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("PRACK"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for SIP PRACK. The call will " +
                            "be ended with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a SIP timeout for a INVITE request results in an
     * error event generated, the state set to error lingering bye and
     * a SIP BYE request sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForInvite() throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for SIP INVITE. The call will " +
                            "be ended with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * A SIP timeout for a INVITE request results in an
     * error event generated, the state set to error lingering bye and
     * a SIP BYE request sent.
     * This test case verifies that if sending the BYE fails, an error
     * event is generated, and the state is sent to Error Completed.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeoutForInviteWhenSendingByeFails()
            throws Exception {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));

        assertErrorEvent(mockOutboundCall,CallDirection.OUTBOUND,
                "SIP timeout occurred for SIP INVITE. The call will " +
                            "be ended with a SIP BYE request.",
                NOT_DISCONNECTED);
        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(mockOutboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a SIP timeout for a request other than INVITE or PRACK
     * results in an error event generated and the state set to error completed.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessSipTimeout() throws Exception
    {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("OTHER"));
        assertErrorOccurred(mockOutboundCall,
                "SIP timeout expired. The call is considered completed.",
                NOT_DISCONNECTED);
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that if the Max Duration timer specified by the client expires
     * in state Connected results in an {@link ErrorEvent} sent, the state
     * is set to {@link DisconnectedLingeringByeOutboundState} and a SIP BYE
     * request is sent.
     * @throws Exception if test case failed.
     */
    public void testHandleMaxDurationCallTimeout() throws Exception {
        assertDisconnectedEvent(mockOutboundCall,
                DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.handleCallTimeout(maxCallDurationTimeoutEvent);
    }

    /**
     * Verifies that if the Max Duration timer specified by the client expires
     * in state Connected results in an {@link ErrorEvent} sent, the state
     * is set to {@link DisconnectedLingeringByeOutboundState} and a SIP BYE
     * request is sent.
     * If sending the SIP BYE request fails an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleMaxDurationCallTimeoutWhenSendingByeFails()
            throws Exception {
        assertDisconnectedEvent(mockOutboundCall,
                DisconnectedEvent.Reason.NEAR_END, false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(FAIL);
        assertErrorOccurred(
                mockOutboundCall,
               "SIP BYE request could not be sent. " +
                    "The call is considered completed. Error",
                ALREADY_DISCONNECTED);
        connectedState.handleCallTimeout(maxCallDurationTimeoutEvent);
    }

    /**
     * Verifies that a NoAck call timeout results in an Error event, the
     * state is set to Error Lingering Bye and a BYE request is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NoAck() throws Exception {
        // Called party and dialogID are used in error log.
        mockOutboundCall.expects(once())
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockOutboundCall.expects(once())
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockOutboundCall, CallDirection.OUTBOUND,
                "The call has timed out while waiting " +
                        "for an ACK on a re-INVITE. The call will be ended " +
                        "with a SIP BYE request.",
                false);
        assertStreamsDeleted(mockOutboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        connectedState.handleCallTimeout(noAckTimeoutEvent);
    }

    /**
     * Verifies that an other call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_Other() throws Exception {
        mockOutboundCall.expects(never());
        connectedState.handleCallTimeout(maxDurationBeforeConnectedTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream in this state results
     * in disconnecting the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is
     * generated and the state is set to
     * {@link DisconnectedLingeringByeOutboundState}.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        assertDisconnectedEvent(
                mockOutboundCall,
                DisconnectedEvent.Reason.FAR_END_ABANDONED, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(
                DisconnectedOutboundState.DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockOutboundCall);
        connectedState.handleAbandonedStream();
    }

}
