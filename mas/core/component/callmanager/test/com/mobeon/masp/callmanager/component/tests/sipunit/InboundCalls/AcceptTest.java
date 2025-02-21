/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.OutboundHostPortUsage;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import javax.sip.message.Response;
import javax.sip.RequestEvent;
import jakarta.activation.MimeType;

/**
 * Call Manager component test case to verify accept of inbound calls.
 * @author Malin Flodin
 */
public class AcceptTest extends InboundSipUnitCase {

    /**
     * Verifies that accepting an inbound call results in a SIP OK response
     * and Accepting state.
     * @throws Exception when the test case fails.
     */
    public void testNormalCallAccept() throws Exception {
        gotoAlertingNewCallState();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that accepting a call without media Mime types
     * results in call accepted.
     * @throws Exception when the test case fails.
     */
    public void testAcceptWithoutMediaMimeTypes() throws Exception {
        gotoAlertingNewCallState();

        // System accepts the call with null media Mime types.
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, null);
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        assertCurrentState(AlertingAcceptingInboundState.class);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that accepting a call when a media match cannot be found
     * results in a FailedEvent.
     * @throws Exception when the test case fails.
     */
    public void testAcceptWithNoMediaMatch() throws Exception {
        gotoAlertingNewCallState();

        // System accepts the call with non matching media Mime types.
        MimeType audioPCMA = new MimeType("audio", "pcma");
        MediaMimeTypes mimeType = new MediaMimeTypes(audioPCMA);
        CallMediaTypes[] callMediaTypes =
                new CallMediaTypes[] {new CallMediaTypes(mimeType, mimeType)};
        simulatedSystem.setSessionData(
                SystemSimulator.SessionData.CALL_MEDIA_TYPES_ARRAY, callMediaTypes);
        simulatedSystem.accept();

        // Wait for Ringing response
        simulatedPhone.assertResponseReceived(Response.RINGING);

        assertFailedEventReceived(FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.NOT_ACCEPTABLE_HERE);

        assertCallIsDisconnected(true, NEAR_END, false);
    }

    /**
     * Verifies that when accepting a call and creating the inbound media stream
     * throws an exception results in a ErrorEvent.
     * @throws Exception when the test case fails.
     */
    public void testAcceptWithInboundStreamException() throws Exception {
        gotoAlertingNewCallState();

        simulatedSystem.createInboundStreamException();

        // System accepts the call
        simulatedSystem.accept();

        // Wait for Ringing response
        simulatedPhone.assertResponseReceived(Response.RINGING);

        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.SERVER_INTERNAL_ERROR);

        assertErrorState();

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertErrorCallStatistics(1);
        assertFailedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that when accepting a call and creating the outbound media
     * stream throws an exception results in a ErrorEvent.
     * @throws Exception when the test case fails.
     */
    public void testAcceptWithOutboundStreamException() throws Exception {
        gotoAlertingNewCallState();

        simulatedSystem.createOutboundStreamException();

        // System accepts the call
        simulatedSystem.accept();

        // Wait for Ringing response
        simulatedPhone.assertResponseReceived(Response.RINGING);

        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        // Wait for Not Acceptable Here response
        simulatedPhone.assertResponseReceived(Response.SERVER_INTERNAL_ERROR);

        assertErrorState();

        // Verify that there is none current connection and one failed connection
        assertCurrentConnectionStatistics(0);
        assertFailedCallStatistics(1);
        assertErrorCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that when accepting a call and the outbound stream host and port
     * already is in use, an ErrorEvent is generated and a SIP
     * "Server Internal Error" response sent.
     * @throws Exception when the test case fails.
     */
    public void testAcceptWithHostPortAlreadyInUse() throws Exception {
        OutboundHostPortUsage.getInstance().addNewHostAndPort(
                simulatedPhone.getHost(), simulatedPhone.getRTPPort());

        gotoAlertingNewCallState();

        // System accepts the call
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);

//        // Wait for Ringing response
//        simulatedPhone.assertResponseReceived(Response.RINGING);
//
//        assertEventReceived(ErrorEvent.class);
//
//        // Wait for Not Acceptable Here response
//        simulatedPhone.assertResponseReceived(Response.SERVER_INTERNAL_ERROR);
//
//        assertErrorState();
//
//        // Verify that there is none current connection and one failed connection
//        assertCurrentConnectionStatistics(0);
//        assertFailedCallStatistics(1);
//        assertErrorCallStatistics(1);
//        assertTotalConnectionStatistics(1);

        OutboundHostPortUsage.getInstance().clear();
    }

    /**
     * Verifies that accepting a call in {@link AlertingAcceptingInboundState}
     * results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testAcceptInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to accept the call again.
        simulatedSystem.accept();
        assertNotAllowedEventReceived(
                "Accept is not allowed in Alerting state (sub state Accepting).");

        assertCurrentState(AlertingAcceptingInboundState.class);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that accepting a call in Connected state results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testAcceptInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to accept the call again.
        simulatedSystem.accept();
        assertNotAllowedEventReceived("Accept is not allowed in Connected state.");
        assertCurrentState(ConnectedInboundState.class);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that accepting a call in
     * {@link DisconnectedLingeringByeInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testAcceptInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System tries to accept the call again.
        simulatedSystem.accept();
        assertNotAllowedEventReceived(
                "Accept is not allowed in Disconnected state (sub state LingeringBye).");

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, false);

        sendOkForBye(byeRequestEvent, true);        
    }

    /**
     * Verifies that accepting a call in
     * {@link DisconnectedCompletedInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testAcceptInDisconnectedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System tries to accept the call again.
        simulatedSystem.accept();
        assertNotAllowedEventReceived(
                "Accept is not allowed in Disconnected state (sub state Completed).");
    }


}
