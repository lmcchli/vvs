/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.tests.sipunit.SipUnitCase;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingEarlyMediaInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingWaitForPrackInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingEarlyMediaWaitForPrackInboundState;
import com.mobeon.masp.callmanager.events.*;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.RedirectingParty;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;
import javax.sip.header.ContentLengthHeader;

/**
 * Base class used to setup necessary for tests of inbound calls using SipUnit.
 * It extends {@link com.mobeon.masp.callmanager.component.tests.sipunit.SipUnitCase}.
 *
 * @author Malin Flodin
 */
public abstract class InboundSipUnitCase extends SipUnitCase {

    protected Response assertCallAccepting(boolean ringing)
            throws InterruptedException {

        if (ringing) {
            // Wait for Ringing response
            simulatedPhone.assertResponseReceived(Response.RINGING);
        }

        // Wait for OK response
        Response response = simulatedPhone.assertResponseReceived(Response.OK);

        // Wait for the state to be set to Accepting
        simulatedSystem.waitForState(AlertingAcceptingInboundState.class);

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);
        
        return response;
    }

    protected void assertCallCanceled() throws InterruptedException {
        // Wait for the state to be set to Failed
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);

        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(1);
    }

    protected void assertCallConnected() throws Exception {
        // Wait for the state to be set to Connected
        simulatedSystem.waitForState(ConnectedInboundState.class);

        simulatedSystem.assertEventReceived(ConnectedEvent.class, null);
        assertFarEndConnections();

        // Verify that the transition from initiated call to entirely
        // established call has been done in the call dispatcher
        assertDispatchedCalls(1, 1);

        // Verify that there is one current connection and one accepted
        // connection
        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);
        assertConnectedCallStatistics(1);
    }

    protected Response assertEarlyMedia(boolean sessionProgress) throws Exception {

	Response response = null;
        if (sessionProgress) {
            // Wait for Session Progress response
            response = simulatedPhone.assertResponseReceived(Response.SESSION_PROGRESS);
        }

        simulatedSystem.assertEventReceived(EarlyMediaAvailableEvent.class, null);
        simulatedSystem.waitForState(AlertingEarlyMediaInboundState.class);
        assertFarEndConnections();

        // Verify that the call dispatcher is updated with an established call
        assertDispatchedCalls(1, 1);

        assertTotalConnectionStatistics(0);
        
        return response;
    }

    protected void assertEarlyMediaRejected() throws InterruptedException {
        simulatedSystem.assertEventReceived(EarlyMediaFailedEvent.class, null);
    }

    protected void assertCallRejected() throws Exception {
        // Wait for Busy Here response
        simulatedPhone.assertResponseReceived(Response.FORBIDDEN);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);

        // Wait for the state to be set to Disconnected
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(1);
    }

    protected void assertCallTerminated() throws Exception {
        // Wait for Request Terminated response
        simulatedPhone.assertResponseReceived(Response.REQUEST_TERMINATED);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);

        // Wait for the state to be set to Disconnected
        simulatedSystem.waitForState(FailedCompletedInboundState.class);

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(1);
    }

    protected void assertCallIsDisconnected(
            boolean callAlreadyDisconnected, boolean nearEnd, boolean wasConnected)
            throws InterruptedException
    {
        if (!callAlreadyDisconnected) {
            if (wasConnected) {
                // Wait for the state to be set to Disconnected
                simulatedSystem.waitForState(DisconnectedCompletedInboundState.class);
                simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
            } else {
                // Wait for the state to be set to Failed
                simulatedSystem.waitForState(FailedCompletedInboundState.class);
                simulatedSystem.assertEventReceived(FailedEvent.class, null);
            }
        }

        // Verify statistics
        if (wasConnected) {
            assertCurrentConnectionStatistics(0);
            assertTotalConnectionStatistics(1);
            assertDisconnectedCallStatistics(nearEnd, 1);
        }

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);
    }

    protected RequestEvent assertCallDisconnect(
            boolean sendResponse, boolean callWasConnected) throws Exception {

        // Wait for BYE request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.BYE, false, true);


        if (callWasConnected) {
            // Wait for the state to be set to DisconnectedLingeringBye
            simulatedSystem.waitForState(DisconnectedLingeringByeInboundState.class);
            simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
        } else {
            // Wait for the state to be set to FailedLingeringBye
            simulatedSystem.waitForState(FailedLingeringByeInboundState.class);
            simulatedSystem.assertEventReceived(FailedEvent.class, null);
        }

        if (sendResponse) {
            // Send OK response
            Response response =
                    simulatedPhone.createResponse(requestEvent, Response.OK);
            simulatedPhone.sendResponse(requestEvent, response);

            if (callWasConnected) {
                // Wait for the state to be set to DisconnectedCompleted
                simulatedSystem.waitForState(DisconnectedCompletedInboundState.class);
            } else {
                // Wait for the state to be set to FailedCompleted
                simulatedSystem.waitForState(FailedCompletedInboundState.class);
            }
        }

        // Verify that there is none current connection and one disconnected connection
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);

        return requestEvent;
    }

    protected RequestEvent assertCallDisconnectWhenError(boolean sendResponse)
            throws Exception {

        // Wait for BYE request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.BYE, false, true);

        // Wait for the state to be set to ErrorLingeringBye
        simulatedSystem.waitForState(ErrorLingeringByeInboundState.class);
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);

        if (sendResponse) {
            // Send OK response
            Response response =
                    simulatedPhone.createResponse(requestEvent, Response.OK);
            simulatedPhone.sendResponse(requestEvent, response);

            // Wait for the state to be set to ErrorCompleted
            simulatedSystem.waitForState(ErrorCompletedInboundState.class);
        }

        // Verify that there is none current connection and one disconnected connection
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);

        return requestEvent;
    }

    protected Response assertReliableRinging(
            boolean withBody, boolean sendAcknowledgement) throws Exception {
        // Wait for Ringing response
        Response ringingResponse =
                simulatedPhone.assertResponseReceived(Response.RINGING);

        if (withBody) {
            ContentLengthHeader cl = ringingResponse.getContentLength();
            assertTrue(cl.getContentLength() > 0);
        }

        // Wait for the state to be set to Wait For Prack
        simulatedSystem.waitForState(AlertingWaitForPrackInboundState.class);

        if (sendAcknowledgement) {
            // Acknowledge the reliable Ringing
            if (withBody)
                simulatedPhone.acknowledgeReliableResponse(
                        ringingResponse, PhoneSimulator.WITH_BODY, SUCCEED, true);
            else
                simulatedPhone.acknowledgeReliableResponse(
                        ringingResponse, PhoneSimulator.NO_BODY, SUCCEED, true);
        }
        return ringingResponse;
    }

    protected void assertReliableSessionProgress(boolean sendAcknowledgement)
            throws Exception {
        // Wait for Session Progress response
        Response progressResponse =
                simulatedPhone.assertResponseReceived(Response.SESSION_PROGRESS);

        // Wait for the state to be set to Early Media Wait For Prack
        simulatedSystem.waitForState(AlertingEarlyMediaWaitForPrackInboundState.class);

        if (sendAcknowledgement) {
            // Acknowledge the reliable Session Progress
            simulatedPhone.acknowledgeReliableResponse(
                    progressResponse, PhoneSimulator.NO_BODY, SUCCEED, true);
        }
    }

    protected void sendOkForBye(RequestEvent requestEvent, boolean callWasConnected)
            throws Exception {
        // Send OK response
        Response response =
                simulatedPhone.createResponse(requestEvent, Response.OK);
        simulatedPhone.sendResponse(requestEvent, response);

        if (callWasConnected) {
            // Wait for the state to be set to DisconnectedCompleted
            simulatedSystem.waitForState(DisconnectedCompletedInboundState.class);
        } else {
            // Wait for the state to be set to FailedCompleted
            simulatedSystem.waitForState(FailedCompletedInboundState.class);
        }
    }

    protected void assertVideoFastUpdateRequestReceived(
            boolean sendResponse, int responseType)
            throws Exception {

        // Wait for INFO request
        RequestEvent requestEvent =
                simulatedPhone.assertRequestReceived(Request.INFO, false, true);

        if (sendResponse) {
            // Send response
            Response response =
                    simulatedPhone.createResponse(requestEvent, responseType);
            simulatedPhone.sendResponse(requestEvent, response);
        }
    }

    protected void assertCallReceived() throws InterruptedException {
        // Wait for Trying response
        simulatedPhone.assertResponseReceived(Response.TRYING);

        // Wait for an Alerting event
        simulatedSystem.assertEventReceived(AlertingEvent.class, null);

        // Wait for the state to be set to Initiated
        simulatedSystem.waitForState(AlertingNewCallInboundState.class);

        // Verify that the call dispatcher is updated with a new initiated call
        assertDispatchedCalls(1, 1);
    }

    protected void assertDisconnectedState() {
        // Wait for the state to be set to Disconnected
        simulatedSystem.waitForState(DisconnectedCompletedInboundState.class);

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);
    }

    protected void assertErrorState() {
        // Wait for the state to be set to Error
        simulatedSystem.waitForState(ErrorInboundState.class);

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);
    }

    public void assertRedirectingParty() {
        Call call = simulatedSystem.getActiveCall();
        if (call instanceof InboundCall) {
            InboundCall inboundCall = (InboundCall)call;
            assertNotNull(inboundCall.getRedirectingParty().getTelephoneNumber());
            assertEquals(RedirectingParty.PresentationIndicator.RESTRICTED,
                    inboundCall.getRedirectingParty().getPresentationIndicator());
            assertEquals(RedirectingParty.RedirectingReason.USER_BUSY,
                    inboundCall.getRedirectingParty().getRedirectingReason());
        }
    }

    protected void gotoAlertingNewCallState() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,
                PhoneSimulator.WITH_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallReceived();
    }

    protected void gotoAlertingEarlyMediaState() throws Exception {
        gotoAlertingNewCallState();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();
        assertEarlyMedia(SESSION_PROGRESS);
    }

    protected Response gotoAlertingEarlyMediaWaitForPrackState()
        throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY,
                null, false, false);

        // Add Supported header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createRequireHeader("100rel"));

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System negotiates early media for the call.
        simulatedSystem.negotiateEarlyMediaTypes();

        // Wait for Session Progress response
        Response progressResponse =
                simulatedPhone.assertResponseReceived(Response.SESSION_PROGRESS);

        // Wait for the state to be set to Wait For Prack
        simulatedSystem.waitForState(AlertingEarlyMediaWaitForPrackInboundState.class);
        return progressResponse;
    }

    protected void gotoAlertingAcceptingState() throws Exception {
        gotoAlertingNewCallState();

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);
    }

    protected void gotoAlertingWaitForPrackState() throws Exception {
        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY,
                null, false, false);

        // Add Require header with 100rel
        invite.addHeader(simulatedPhone.getHeaderFactory().
                createRequireHeader("100rel"));

        simulatedPhone.sendInvite(invite);
        assertCallReceived();

        // System accepts the call.
        simulatedSystem.accept();

        // Phone receives reliable ringing and acknowledges it using PRACK
        assertReliableRinging(PhoneSimulator.NO_BODY, NO_ACKNOWLEDGE);

        // Wait for the state to be set to Wait For Prack
        simulatedSystem.waitForState(AlertingWaitForPrackInboundState.class);
    }

    protected void gotoConnectedState() throws Exception {
        gotoAlertingAcceptingState();

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();
    }

    protected void gotoTransferState(Call anotherCall) throws Exception {
        gotoConnectedState();

        simulatedSystem.join(anotherCall, false);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);
    }

    protected RequestEvent gotoDisconnectedLingeringByeState() throws Exception {
        gotoConnectedState();

        // System disconnects the call.
        simulatedSystem.disconnect();
        return assertCallDisconnect(false, NEAR_END);
    }

    protected void gotoDisconnectedCompletedState() throws Exception {
        gotoConnectedState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    protected void gotoFailedWaitingForAckState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to disconnect the call
        simulatedSystem.disconnect();
    }
}
