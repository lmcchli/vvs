package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.message.Response;

/**
 * AlertingNewCallInboundState Tester.
 *
 * @author Malin Nyfeldt
 */
public class AlertingNewCallInboundStateTest extends InboundStateCase
{
    AlertingNewCallInboundState alertingNewCallState;

    protected void setUp() throws Exception {
        super.setUp();
        alertingNewCallState = new AlertingNewCallInboundState((InboundCallInternal) mockInboundCall.proxy());
    }

    /**
     * Verifies that a lock request in this state results in a SIP
     * "Temporarily Unavailable" response sent, a {@link FailedEvent} generated
     * and the state set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testLock() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.SERVICE_UNAVAILABLE);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The Service is temporarily unavailable due to " +
                "the current administrative state: Locked.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.processLockRequest();
    }

    /**
     * Verifies that a requested play results in a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testPlay() throws Exception {
        assertPlayFailedEvent(mockInboundCall);
        alertingNewCallState.play(playEvent);
    }

    /**
     * Verifies that a requested record results in a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} in this state.
     * @throws Exception if test case fails.
     */
    public void testRecord() throws Exception {
        assertRecordFailedEvent(mockInboundCall);
        alertingNewCallState.record(recordEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing play is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testStopPlay() throws Exception {
        mockInboundCall.expects(never());
        alertingNewCallState.stopPlay(stopPlayEvent);
    }

    /**
     * Verifies that a requested stop of an ongoing record is ignored in this
     * state.
     * @throws Exception if test case fails.
     */
    public void testStopRecord() throws Exception {
        mockInboundCall.expects(never());
        alertingNewCallState.stopRecord(stopRecordEvent);
    }

    /**
     * Verifies that a video fast update request in this state is ignored.
     * @throws Exception if test case fails.
     */
    public void testProcessVideoFastUpdateRequest() throws Exception {
        mockInboundCall.expects(never());
        alertingNewCallState.processVideoFastUpdateRequest();
    }

    /**
     * Verifies that for an inbound INVITE without an SDP offer, if the inbound
     * stream could not be created an error is reported and a SIP "Server
     * Internal Error" response is sent.
     * @throws Exception if test case fails.
     */
    public void testAcceptWhenInviteLackedSdpOfferAndInboundStreamCouldNotBeCreated()
            throws Exception {
        assertNoReliableResponses();
        assertResponseSent(SUCCEED, SUCCEED, "Ringing", false);
        assertNoRemoteSdpOfferExists();
        assertCallTypeRetrievedFromConfiguration();
        assertInboundStreamCreated(mockInboundCall, FAIL);
        assertErrorOccurred(
                mockInboundCall,
                "Could not create inbound stream: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that for an inbound INVITE without an SDP offer, if an SDP offer
     * could not be created an error is reported and a SIP "Server
     * Internal Error" response is sent.
     * @throws Exception if test case fails.
     */
    public void testAcceptWhenInviteLackedSdpOfferAndSdpOfferCouldNotBeCreated()
            throws Exception {
        assertNoReliableResponses();
        assertResponseSent(SUCCEED, SUCCEED, "Ringing", false);
        assertNoRemoteSdpOfferExists();
        assertCallTypeRetrievedFromConfiguration();
        assertInboundStreamCreated(mockInboundCall, SUCCEED);
        assertSdpOfferCreated(mockInboundCall, FAIL);
        assertErrorOccurred(
                mockInboundCall,
                "Could not create SDP offer: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "Not Acceptable Here" is sent when no SDP
     * intersection was found. Verifies that a {@link FailedEvent}
     * is generated and that the state is set to
     * {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenNoSdpIntersectionFound() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, false);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_MEDIA_FORMAT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. A SIP 488 response will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "Server Internal Error" is sent when streams
     * could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenCreationOfStreamsFails() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(true, false);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create streams: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingNewCallState.accept(acceptEvent);
    }


    /**
     * Verifies that a SIP "Server Internal Error" is sent when an SDP
     * answer could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenSdpAnswerFailed() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        assertSdpAnswerCreated(true, null);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create SDP answer: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * A SIP "OK" response is sent when accept() executed
     * succesfully. Tbis test case verifies that when an exception is thrown
     * when creating the SIP "OK" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenSendingOkFails() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertResponseSent(true, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Ok\" response: Error", NOT_DISCONNECTED);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "OK" response is sent when accept() executed
     * succesfully. Verifies that the state is set to
     * {@link AlertingAcceptingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testAccept() throws Exception
    {
        assertReliableResponsesForSdp();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertResponseSent(false, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that when redirected RTP is activated the accept() is 
     * handled as normal except that no outbound media stream is created.
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenRedirectedRtpActivated() throws Exception
    {
        assertReliableResponsesForSdp();
        assertRedirectedRtpActivated();
        assertRemoteSdpOfferExists();
        assertResponseSent(false, false, "Ringing", false);
        assertGettingSdpIntersection(mockInboundCall, true);
        assertInboundStreamCreation(false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertResponseSent(false, false, "Ok", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.ACCEPTING);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * A SIP "Ringing" response is sent when accept() executed
     * succesfully. Tbis test case verifies that when an exception is thrown
     * when sending the SIP "Ringing" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenSendingRingingFails() throws Exception
    {
        assertNoReliableResponses();
        assertResponseSent(false, true, "Ringing", false);
        assertErrorOccurred(
                mockInboundCall,
                "Could not send SIP \"Ringing\" response: Error",
                NOT_DISCONNECTED);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that when accept() is executed and provisional responses shall
     * be sent reliably, a SIP "Ringing" response is sent reliably and the state
     * is set to WaitForPrack.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWithReliableRinging() throws Exception {
        assertReliableResponses();
        assertRemoteSdpOfferExists();
        assertReliableResponseSent(false, false, "Ringing", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.WAIT_FOR_PRACK);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that for an inbound INVITE without an SDP offer for which
     * provisional responses shall be sent reliably:
     * if the inbound stream could not be created an error is reported and a
     * SIP "Server Internal Error" response is sent.
     * @throws Exception if test case fails.
     */
    public void testAcceptForReliableRingingWhenInviteLackedSdpOfferAndInboundStreamCouldNotBeCreated()
            throws Exception {
        assertReliableResponses();
        assertNoRemoteSdpOfferExists();
        assertCallTypeRetrievedFromConfiguration();
        assertInboundStreamCreated(mockInboundCall, FAIL);
        assertErrorOccurred(
                mockInboundCall,
                "Could not create inbound stream: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that for an inbound INVITE without an SDP offer, if an SDP offer
     * could not be created an error is reported and a SIP "Server
     * Internal Error" response is sent.
     * @throws Exception if test case fails.
     */
    public void testAcceptForReliableRingingWhenInviteLackedSdpOfferAndSdpOfferCouldNotBeCreated()
            throws Exception {
        assertReliableResponses();
        assertNoRemoteSdpOfferExists();
        assertCallTypeRetrievedFromConfiguration();
        assertInboundStreamCreated(mockInboundCall, SUCCEED);
        assertSdpOfferCreated(mockInboundCall, FAIL);
        assertErrorOccurred(
                mockInboundCall,
                "Could not create SDP offer: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that when accept() is executed and provisional responses shall
     * be sent reliably.
     * Tbis test case verifies that when an exception is thrown
     * when creating the reliable SIP "Ringing" response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testAcceptWhenCreatingReliableRingingFails() throws Exception {
        assertReliableResponses();
        assertRemoteSdpOfferExists();
        assertStateAlerting(AlertingInboundState.AlertingSubState.WAIT_FOR_PRACK);
        assertReliableResponseSent(true, false, "Ringing", false);
        assertErrorOccurred(
                mockInboundCall,
                "Could not send reliable SIP \"Ringing\" response: Error",
                NOT_DISCONNECTED);
        alertingNewCallState.accept(acceptEvent);
    }

    /**
     * Verifies that a SIP "Early Media Failed" event is sent when there was
     * no SDP offer in the initial INVITE. The state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenInviteContainedNoSdpOffer()
            throws Exception
    {
        assertNoRemoteSdpOfferExists();
        assertEarlyMediaFailedEvent(mockInboundCall);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Early Media Failed" event is sent when  
     * support for redirected RTP has been activated. 
     * The state is left unchanged.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenRedirectedRtpActivated()
            throws Exception
    {
        assertRemoteSdpOfferExists();
        assertRedirectedRtpActivated();
        assertEarlyMediaFailedEvent(mockInboundCall);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Not Acceptable Here" is sent when no SDP
     * intersection was found. Verifies that a {@link FailedEvent}
     * is generated and that the state is set to
     * {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenNoSdpIntersectionFound()
            throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertGettingSdpIntersection(mockInboundCall, false);
        assertNotAcceptableResponseSent(
                mockInboundCall, SipWarning.INCOMPATIBLE_MEDIA_FORMAT);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                CallDirection.INBOUND,
                "Media negotiation failed. A SIP 488 response will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Server Internal Error" is sent when streams
     * could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenCreationOfStreamsFails() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(true, false);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create streams: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }


    /**
     * Verifies that a SIP "Server Internal Error" is sent when an SDP
     * answer could not be created. Verifies that an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenSdpAnswerFailed() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        assertSdpAnswerCreated(true, null);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        assertErrorOccurred(mockInboundCall,
                "Could not create SDP answer: Error. A SIP 500 response will be sent.",
                NOT_DISCONNECTED);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * A SIP "Session Progress" response is sent when negotiateEarlyMediaTypes()
     * executed succesfully. Tbis test case verifies that when an exception is
     * thrown when creating the SIP response, an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaWhenSendingSessionProgressFails()
            throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA);
        assertResponseSent(true, false, "SessionProgress", false);
        assertErrorOccurred(mockInboundCall,
                "Could not send SIP \"Session Progress\" response: Error",
                NOT_DISCONNECTED);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Session Progress" response is sent when
     * negotiateEarlyMediaTypes() executed
     * succesfully. Verifies that the state is set to
     * {@link AlertingEarlyMediaInboundState}.
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypes() throws Exception
    {
        assertNoReliableResponses();
        assertRemoteSdpOfferExists();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertResponseSent(false, false, "SessionProgress", false);
        assertEarlyMediaAvailableEvent(mockInboundCall);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA);
        alertingNewCallState.negotiateEarlyMediaTypes(
                negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that when negotiateEarlyMediaTypes() is executed and
     * provisional responses shall be sent reliably, a SIP "Session Progress"
     * response is sent reliably and the state is set to EarlyMediaWaitForPrack.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypesWithReliableProvisionalResponses()
            throws Exception {
        assertRemoteSdpOfferExists();
        assertReliableResponses();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(SUCCEED, SUCCEED);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertReliableResponseSent(SUCCEED, SUCCEED, "SessionProgress", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA_WAIT_FOR_PRACK);
        alertingNewCallState.negotiateEarlyMediaTypes(negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that when negotiateEarlyMediaTypes() is executed and
     * provisional responses shall be sent reliably.
     * Tbis test case verifies that when an exception is thrown
     * when creating the reliable SIP "Session Progress" response,
     * an error is reported.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypesWhenSendingReliableProvisionalResponseFails()
            throws Exception {
        assertRemoteSdpOfferExists();
        assertReliableResponses();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(SUCCEED, SUCCEED);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(SUCCEED, sdpAnswer);
        assertStateAlerting(
                AlertingInboundState.AlertingSubState.EARLY_MEDIA_WAIT_FOR_PRACK);
        assertReliableResponseSent(SUCCEED, FAIL, "SessionProgress", false);
        assertErrorOccurred(
                mockInboundCall,
                "Could not send reliable SIP \"Session Progress\" response: Error",
                NOT_DISCONNECTED);
        alertingNewCallState.negotiateEarlyMediaTypes(negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that when negotiateEarlyMediaTypes() is executed and
     * provisional responses containing SDP shall be sent reliably, a SIP
     * "Session Progress" response is sent reliably and the state is set to
     * EarlyMediaWaitForPrack.
     *
     * @throws Exception if test case failed.
     */
    public void testNegotiateEarlyMediaTypesWithReliableSdpResponse()
            throws Exception {
        assertRemoteSdpOfferExists();
        assertReliableResponsesForSdp();
        assertGettingSdpIntersection(mockInboundCall, true);
        assertStreamCreation(false, false);
        String sdpAnswer = "SDPAnswer";
        assertSdpAnswerCreated(false, sdpAnswer);
        assertReliableResponseSent(SUCCEED, SUCCEED, "SessionProgress", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.EARLY_MEDIA_WAIT_FOR_PRACK);
        alertingNewCallState.negotiateEarlyMediaTypes(negotiateEarlyMediaTypesEvent);
    }

    /**
     * Verifies that a SIP "Forbidden" response is sent for a call reject
     * executed succesfully.
     * Verifies that a {@link FailedEvent} is generated and that the state is
     * set to {@link FailedCompletedInboundState}.
     * @throws Exception if test case failed.
     */
    public void testReject() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.FORBIDDEN);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The call is rejected by service.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.reject(rejectEvent);
    }

    /**
     * Verifies that a disconnect is handled in this state.
     * A SIP "Request Terminated" is sent for the INVITE request,
     * a {@link FailedEvent} is generated and the next state is set to
     * {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testDisconnect() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "A disconnect is requested before the call is connected.", null);
        assertDisconnectedEvent(
                mockInboundCall, DisconnectedEvent.Reason.NEAR_END, true);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.disconnect(disconnectEvent);
    }

    /**
     * Verifies that an ACK is ignored in this state.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessAck() throws Exception
    {
        mockInboundCall.expects(never());
        alertingNewCallState.processAck(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the BYE
     * request.
     * Finally it is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessBye() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", null);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP BYE request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the BYE
     * request.
     * It is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessByeWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 18, 1);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 610);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.processBye(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the CANCEL
     * request.
     * Finally it is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancel() throws Exception
    {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", null);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.processCancel(additionalSipRequestEvent);
    }

    /**
     * Verifies reception of a SIP CANCEL request.
     * It is verified that a SIP "Request Terminated" is sent for the
     * INVITE request and that a SIP "OK" response is sent for the CANCEL
     * request.
     * It is verified that a {@link FailedEvent} is generated and that
     * the next state is set to {@link FailedCompletedInboundState}.
     * It is also verified that the Reason header field in the SIP BYE request
     * is mapped to the correct network status code.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessCancelWithReason() throws Exception
    {
        assertReasonHeader(mockAdditionalRequest, 19, null);
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND,
                "Call disconnected early by far end.", 610);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertOkResponseSent(mockInboundCall);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.processCancel(additionalSipRequestEvent);
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
            alertingNewCallState.processInvite(initialSipRequestEvent);
            fail("Exception not thrown when expected");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * Verifies reception of a SIP re-INVITE.
     * It is verified that a SIP "Request Pending" is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessReInvite() throws Exception
    {
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_PENDING);
        alertingNewCallState.processReInvite(additionalSipRequestEvent);
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
        alertingNewCallState.processOptions(additionalSipRequestEvent);
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
        alertingNewCallState.processInfo(additionalSipRequestEvent);
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
        alertingNewCallState.processPrack(additionalSipRequestEvent);
    }

    /**
     * Verifies that the status code of the response is retrieved for logging
     * purposes but that the response otherwise is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testProcessResponse() throws Exception
    {
        assertResponseCodeRetrievedForMethod("INFO", 499);
        alertingNewCallState.processSipResponse(sipResponseEvent);
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
        alertingNewCallState.processSipTimeout(sipTimeoutEvent);
    }

    /**
     * Verifies that if the configured "Not Accepted" timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Timeout" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleCallNotAcceptedTimeout() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.NEAR_END_ABANDONED, CallDirection.INBOUND,
                "The inbound call was not accepted by the service in time. " +
                        "It is considered abandoned and a SIP 408 response " +
                        "will be sent.",
                null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TIMEOUT);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.handleCallTimeout(callNotAcceptedTimeoutEvent);
    }

    /**
     * Verifies that if the Expires timer expires in this
     * state it results in a {@link FailedEvent} generated, the state is set to
     * {@link FailedCompletedInboundState} and a SIP "Request Terminated" response
     * is sent.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleExpiresTimeout() throws Exception {
        assertFailedEvent(mockInboundCall,
                FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND,
                "The expires timer expired for the INVITE. A SIP 487 " +
                        "response will be sent.", null);
        assertStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        assertErrorResponseSent(mockInboundCall, Response.REQUEST_TERMINATED);
        assertStreamsDeleted(mockInboundCall);
        alertingNewCallState.handleCallTimeout(expiresTimeoutEvent);
    }

    /**
     * Verifies that if the another call timer than the "Not Accepted" or
     * Expires timers expires in this state it is ignored.
     *
     * @throws Exception if test case failed.
     */
    public void testHandleOtherCallTimeout() throws Exception {
        alertingNewCallState.handleCallTimeout(anotherTimeoutEvent);
    }

    /**
     * Verifies that the detection of an abandoned stream is ignored in this state.
     * @throws Exception if test case fails.
     */
    public void testHandleAbandonedStream() throws Exception {
        mockInboundCall.expects(never());
        alertingNewCallState.handleAbandonedStream();
    }

    /**
     * Verifies that the Proxy forwards the SIP "INVITE" to the given UAS.
     * Verifies that the state is set to {@link AlertingProxyingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testProxy() throws Exception
    {
        assertSipRequestSent(false, false, "Sip", false);
        assertStateAlerting(AlertingInboundState.AlertingSubState.PROXYING);
        assertSetUas();
        assertGetUas();
        assertGetInitialSipRequestEvent();
        alertingNewCallState.proxy(proxyEvent);
    }

    /**
     * Verifies that the Proxy returns an error when ProxyEvent is malformed or missing.
     * Verifies that the state is set to {@link AlertingProxyingInboundState}.
     * @throws Exception if test case failed.
     */
    public void testProxyNoUas() throws Exception
    {
        assertErrorOccurred(mockInboundCall, "Could not proxy the SIP INVITE, invalid proxyEvent.", NOT_DISCONNECTED);
        assertErrorResponseSent(mockInboundCall, Response.SERVER_INTERNAL_ERROR);
        alertingNewCallState.proxy(null);
    }
}
