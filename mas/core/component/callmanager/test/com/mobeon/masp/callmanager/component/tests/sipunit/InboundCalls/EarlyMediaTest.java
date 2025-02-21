/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingEarlyMediaInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify early media for inbound calls.
 * @author Malin Flodin
 */
public class EarlyMediaTest extends InboundSipUnitCase {

    /**
     * Verifies that negotiating early media for an inbound call before it is
     * accepted results in a
     * {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent} and
     * a SIP "Session Progress" response.
     * @throws Exception when the test case fails.
     */
    public void testNormalNegotiationOfEarlyMedia() throws Exception {
        gotoAlertingNewCallState();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertEarlyMedia(SESSION_PROGRESS);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(NO_RINGING);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that an INVITE with an Expires time that is not accepted within
     * the expires time is rejected with a SIP Request Terminated response.
     * @throws Exception when the test case fails.
     */
    public void testInviteWithExpiresTimerThatExpiresForEarlyMedia() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.WITH_BODY, null, false, false);
        simulatedPhone.addExpiresHeader(invite, 2);
        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertEarlyMedia(SESSION_PROGRESS);

        // Instead of accepting the call, wait until the expires timer expired.
        Thread.sleep(2500);

        // Wait for Request Terminated response
        simulatedPhone.assertResponseReceived(Response.REQUEST_TERMINATED);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }


    /**
     * Verifies that negotiating early media for an inbound call that contained
     * not SDP offer in INVITE is rejected with an
     * {@link com.mobeon.masp.callmanager.events.EarlyMediaFailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaWhenInboundInviteContainedNoSdpOffer()
            throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertEarlyMediaRejected();


        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.WITH_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that negotiating early media for an inbound call before it is
     * accepted results in a
     * {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent} and
     * a SIP "Session Progress" response even though media types were not given
     * by the Call Manager client.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaWithoutMediaTypes() throws Exception {
        gotoAlertingNewCallState();

        // System accepts the call with null media Mime types.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, null);

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertEarlyMedia(SESSION_PROGRESS);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.WITH_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that if media has already been selected for the session and
     * contains both audio and video, and a new inbound call contains audio
     * only, the call is rejected.
     * @throws Exception when the test case fails.
     */
    public void testInboundCallWhenMediaHasAlreadyBeenDeterminedButDoesNotMatch()
            throws Exception {
        gotoAlertingNewCallState();

        // Set the already determined media types.
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(new MimeType("audio/pcmu"));
        mediaMimeTypes.addMimeType(new MimeType("video/h263"));
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.SELECTED_CALL_MEDIA_TYPES,
                new CallMediaTypes(mediaMimeTypes, null));

        // System negotiates early media for the call. Since media will not
        // match, the call will be rejected/cancelled
        simulatedSystem.negotiateEarlyMediaTypes();
        assertCallCanceled();
    }

    /**
     * Verifies that negotiating early media for an inbound call in
     * {@link AlertingEarlyMediaInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaInAlertingEarlyMediaState()
            throws Exception {
        gotoAlertingEarlyMediaState();

        // System tries to negotiate early media again.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertNotAllowedEventReceived(
                "Negotiate early media types is not allowed in Alerting state " +
                        "(sub state EarlyMedia).");

        assertCurrentState(AlertingEarlyMediaInboundState.class);


        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(NO_RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.WITH_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that negotiating early media for an inbound call in
     * {@link AlertingAcceptingInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaInAlertingAcceptingState()
            throws Exception {
        gotoAlertingAcceptingState();

        // System tries to negotiate early media again.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertNotAllowedEventReceived(
                "Negotiate early media types is not allowed in Alerting state " +
                        "(sub state Accepting).");

        assertCurrentState(AlertingAcceptingInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that negotiating early media for an inbound call in
     * {@link ConnectedInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to negotiate early media again.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertNotAllowedEventReceived(
                "Negotiate early media types is not allowed in Connected state.");

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that negotiating early media for an inbound call in
     * {@link DisconnectedLingeringByeInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaInDisconnectedLingeringByeState()
            throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System tries to negotiate early media again.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertNotAllowedEventReceived(
                "Negotiate early media types is not allowed in Disconnected " +
                        "state (sub state LingeringBye).");

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that negotiating early media for an inbound call in
     * {@link DisconnectedCompletedInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testNegotiationOfEarlyMediaInDisconnectedCompletedState()
            throws Exception {
        gotoDisconnectedCompletedState();

        // System tries to negotiate early media again.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertNotAllowedEventReceived(
                "Negotiate early media types is not allowed in Disconnected " +
                        "state (sub state Completed).");

        assertCurrentState(DisconnectedCompletedInboundState.class);
    }
}
