/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedInboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallDirection;

import javax.sip.message.Response;

/**
 * ConnectedInboundState Tester.
 *
 * @author Malin Flodin
 */
public class ConnectedInboundStateTest extends InboundStateCase
{
    ConnectedInboundState connectedState;

    protected void setUp() throws Exception {
        super.setUp();

        connectedState = new ConnectedInboundState(
                (InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in disconnecting
     * the call. A SIP BYE request is sent, a {@link DisconnectedEvent} is
     * generated and the state is set to
     * {@link DisconnectedLingeringByeInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        connectedState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a play request on the call.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlay(mockInboundCall);
        connectedState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a record request on the
     * call.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecord(mockInboundCall);
        connectedState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        assertStopPlay(mockInboundCall);
        connectedState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record results in a stop
     * request on the call.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        assertStopRecord(mockInboundCall);
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
        assertErrorOccurred(mockInboundCall,
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
     *
     * @throws Exception if test case failed.
     */
    public void testProcessVideoFastUpdateWithRTCP() throws Exception
    {
        assertWithPictureFastUpdate();
        connectedState.processVideoFastUpdateRequest();
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
                "Accept is not allowed in Connected state.");
        connectedState.accept(acceptEvent);
    }

    /**
     * Verifies that a {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}
     * is generated when negotiateEarlyMediaTypes() is executed in this state.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNotAllowedEvent(mockInboundCall,
                "Negotiate early media types is not allowed in Connected state.");
        connectedState.negotiateEarlyMediaTypes(
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
                "Reject is not allowed in Connected state.");
        connectedState.reject(rejectEvent);
    }

    /**
     * Verifies that disconnect in this state results in a generated
     * {@link DisconnectedEvent}, a SIP BYE request sent and that the state
     * is set to {@link DisconnectedLingeringByeInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        connectedState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that disconnect in this state results in a generated
     * {@link DisconnectedEvent}, a SIP BYE request sent and that the state is
     * set to {@link DisconnectedLingeringByeInboundState}. If sending the
     * BYE request failed an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnectWhenSendingByeFails() throws Exception {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(false);
        assertSendRequestWithinDialog(true);
        assertErrorOccurred(mockInboundCall,
                "SIP BYE request could not be sent. The call is " +
                        "considered completed. Error", ALREADY_DISCONNECTED);
        connectedState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that when an ACK is received, the no ack timer is canceled.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        assertNoAckTimerCanceled(mockInboundCall);
        connectedState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "OK" response is sent and that a
     * {@link DisconnectedEvent} is generated.
     * Finally it is verified that the next state is set to
     * {@link DisconnectedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.FAR_END, false);
        assertOkResponseSent(mockInboundCall);
        assertStateDisconnected(DisconnectedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        connectedState.processBye(additionalSipRequestEvent);
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
        connectedState.processCancel(additionalSipRequestEvent);
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
        assertSdpInRequest(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.RENEGOTIATION_NOT_SUPPORTED);
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
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, FAIL);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT);
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
        assertSdpInRequest(mockInboundCall, SUCCEED);
        assertParsingSdpBody(mockInboundCall, SUCCEED);
        assertSdpEquality(mockInboundCall, SUCCEED);
        assertLocalSdpAnswerRetrieved(mockInboundCall);
        assertResponseSent(SUCCEED, SUCCEED, "Ok", false);
        assertNoAckTimerStarted(mockInboundCall);
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
        assertOkResponseSent(mockInboundCall);
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
        assertJoinedOtherCall(mockInboundCall, FAIL);
        assertCallJoined(mockInboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockInboundCall);
        connectedState.processInfo(additionalSipRequestEvent);
    }

    /**
     * Verifies that if a SIP INFO request is received for a joined call is
     * forwarded if the INFO request contained a VFU.
     *
     * @throws Exception if the test case failed.
     */
    public void testProcessInfoWhenJoined() throws Exception {
        assertJoinedOtherCall(mockInboundCall, SUCCEED);
        assertCallJoined(mockInboundCall, SUCCEED);
        assertVFU(mockInboundCall, SUCCEED);
        assertInfoRequestForwarded(mockInboundCall);
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
        assertJoinedOtherCall(mockInboundCall, FAIL);
        assertCallJoined(mockInboundCall, SUCCEED);
        assertMethodNotAllowedResponseSent(mockInboundCall);
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
        assertJoinedOtherCall(mockInboundCall, SUCCEED);
        assertCallJoined(mockInboundCall, SUCCEED);
        assertVFU(mockInboundCall, FAIL);
        assertMethodNotAllowedResponseSent(mockInboundCall);
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
        assertErrorResponseSent(mockInboundCall, Response.FORBIDDEN);
        connectedState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the response is ignored if the method differs from INFO.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponseWhenMethodNotInfo() throws Exception {
        assertResponseCodeRetrievedForMethod("NOTIFY", 200);
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is parsed if the method is INFO and the call
     * is not joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenNotJoined() throws Exception {
        assertJoinedOtherCall(mockInboundCall, FAIL);
        assertCallJoined(mockInboundCall, FAIL);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockInboundCall.expects(once()).method("parseMediaControlResponse");
        connectedState.processSipResponse(sipResponseEvent);
    }

    /**
     * Verifies that the response is forwarded to the other call if the method
     * is INFO and the call is joined.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessInfoResponseWhenJoined() throws Exception {
        assertJoinedOtherCall(mockInboundCall, SUCCEED);
        assertCallJoined(mockInboundCall, SUCCEED);
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
        assertJoinedOtherCall(mockInboundCall, FAIL);
        assertCallJoined(mockInboundCall, SUCCEED);
        assertResponseCodeRetrievedForMethod("INFO", 100);
        mockInboundCall.expects(once()).method("parseMediaControlResponse");
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
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
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
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        connectedState.processSipResponse(sipResponseEvent);
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
                NOT_DISCONNECTED);
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a SIP timeout in this state is ignored if received for
     * a SIP INFO and configuration indicates that it should be ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeoutForInfo() throws Exception
    {
        ConfigurationReader.getInstance().getConfig().
                setDisconnectOnSipTimeout(false);
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INFO"));
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a SIP timeout for an INVITE results in an error event,
     * state set to error lingering bye and a SIP BYE request sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessTimeoutForInvite() throws Exception
    {
        mockInitialRequest.stubs().method("getMethod").will(returnValue("INVITE"));
        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "SIP timeout occurred for re-INVITE. The call will " +
                            "be ended with a SIP BYE request.",
                false);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);
        connectedState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that a CallNotAccepted call timeout is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_CallNotAccepted() throws Exception {
        mockInboundCall.expects(never());
        connectedState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that a NoAck call timeout results in an Error event, the
     * state is set to Error Lingering Bye and a BYE request is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallTimeout_NoAck() throws Exception {
        // Called party and dialogID are used in error log.
        mockInboundCall.expects(once())
                .method("getCalledParty").will(returnValue(new CalledParty()));
        mockInboundCall.expects(once())
                .method("getInitialDialogId").will(returnValue("dialogId"));

        assertStateError(ErrorInboundState.ErrorSubState.LINGERING_BYE);
        assertErrorEvent(mockInboundCall, CallDirection.INBOUND,
                "The call has timed out while waiting " +
                        "for an ACK on a re-INVITE. The call will be ended " +
                        "with a SIP BYE request.",
                false);
        assertStreamsDeleted(mockInboundCall);
        assertCreateBye(SUCCEED);
        assertSendRequestWithinDialog(SUCCEED);

        connectedState.handleCallTimeout(noAckTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream in this state results
     * in disconnecting the call. A SIP BYE request is sent, a
     * {@link DisconnectedEvent} is
     * generated and the state is set to
     * {@link DisconnectedLingeringByeInboundState}.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.FAR_END_ABANDONED, false);
        assertCreateBye(false);
        assertSendRequestWithinDialog(false);
        assertStateDisconnected(DisconnectedSubState.LINGERING_BYE);
        assertStreamsDeleted(mockInboundCall);
        connectedState.handleAbandonedStream();
    }

}
