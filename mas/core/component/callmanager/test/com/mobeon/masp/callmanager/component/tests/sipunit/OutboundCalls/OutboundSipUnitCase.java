/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.component.tests.sipunit.SipUnitCase;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorLingeringCancelOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingEarlyMediaOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedLingeringCancelOutboundState;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.RemoteParty;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.RemotePartyController;

import javax.sip.RequestEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.jmock.Mock;


/**
 * Base class used to setup necessary for tests of outbound calls using SipUnit.
 * It extends {@link com.mobeon.masp.callmanager.component.tests.sipunit.SipUnitCase}.
 *
 * @author Malin Flodin
 */
public abstract class OutboundSipUnitCase extends SipUnitCase {

    Mock mockedRemotePartyController = mock(RemotePartyController.class);
    RemotePartyController remotePartyController = null;

    protected void mockRemotePartyController() {
        remotePartyController = CMUtils.getInstance().getRemotePartyController();
        CMUtils.getInstance().setRemotePartyController(
                (RemotePartyController)mockedRemotePartyController.proxy());
    }

    protected void resetRemotePartyController() {
        CMUtils.getInstance().setRemotePartyController(remotePartyController);
    }

    protected void assertRemotePartyBlackListed() {
        mockedRemotePartyController.expects(once()).method("blacklistRemoteParty").
                with(eq(phoneAddress.getHost() + ":" + phoneAddress.getPort()));
    }

    protected void assertNewRemotePartyRetrieved() {
        mockedRemotePartyController.expects(once()).
                method("getRandomRemotePartyAddress").will(returnValue(
                phoneAddress));
    }

    protected void assertConfigurationContainsSsp() {
        // Override configuration and make sure it contains at least one SSP
        RemoteParty remoteParty = new RemoteParty();
        remoteParty.addSsp("SSP1", 1);
        ConfigurationReader.getInstance().getConfig().setRemoteParty(remoteParty);
    }

    protected void assertConfigurationContainsNoSsp() {
        // Override configuration and make sure it contains no SSP
        ConfigurationReader.getInstance().getConfig().removeRemoteParty();
    }

    protected void assertCallAccepted(PhoneSimulator simulatedPhone)
            throws Exception {
        // Wait for ACK request
        simulatedPhone.assertRequestReceivedAndIgnoreInviteResends(Request.ACK);

        // Wait for the state to be set to Connected
        simulatedSystem.waitForState(ConnectedOutboundState.class);

        simulatedSystem.assertEventReceived(
                ConnectedEvent.class, ProgressingEvent.class);

        assertFarEndConnections();

        // Verify that the transition from initiated call to entirely
        // established call has been done in the call dispatcher
        assertDispatchedCalls(0, 1);

        assertCurrentConnectionStatistics(1);
        assertTotalConnectionStatistics(1);
        assertConnectedCallStatistics(1);
    }

    protected void assertEarlyMediaSetup(PhoneSimulator simulatedPhone)
            throws InterruptedException {
        // Wait for the state to be set to Progressing Early Media
        simulatedSystem.waitForState(ProgressingEarlyMediaOutboundState.class);

        assertProgressingEventReceived(true);

        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(0);
        assertConnectedCallStatistics(0);
    }

    protected void assertCallAcknowledged(
            PhoneSimulator simulatedPhone)
            throws InterruptedException {
        // Wait for ACK request
        simulatedPhone.assertRequestReceivedAndIgnoreInviteResends(Request.ACK);
    }

    protected RequestEvent assertCallCanceled(
            boolean sendResponse, boolean errorOccurred)
            throws Exception {

        // Wait for CANCEL request
        RequestEvent requestEvent = simulatedPhone.
                assertRequestReceivedAndIgnoreInviteResends(Request.CANCEL);

        if (errorOccurred) {
            // Wait for the state to be set to Error
            simulatedSystem.waitForState(ErrorLingeringCancelOutboundState.class);
        } else {
            // Wait for the state to be set to Disconnected
            simulatedSystem.waitForState(FailedLingeringCancelOutboundState.class);
        }

        if (sendResponse) {
            // Send OK response
            Response response =
                    simulatedPhone.createResponse(requestEvent, Response.OK);
            simulatedPhone.sendResponse(requestEvent, response);
        }
        return requestEvent;
    }

    protected void assertPrackReceived(boolean sendResponse)
            throws Exception {

        // Wait for PRACK request
        RequestEvent requestEvent = simulatedPhone.
                assertRequestReceivedAndIgnoreInviteResends(Request.PRACK);

        if (sendResponse) {
            // Send OK response
            Response response =
                    simulatedPhone.createResponse(requestEvent, Response.OK);
            simulatedPhone.sendResponse(requestEvent, response);
        }
    }

    protected void assertCallRejected(int sipResponseCode)
            throws InterruptedException
    {
        assertFailedEventReceived(
                FailedEvent.Reason.REJECTED_BY_FAR_END,
                ConfigurationReader.getInstance().getConfig().
                        getReleaseCauseMapping().
                        getNetworkStatusCode(sipResponseCode));

        // Wait for the state to be set to Failed
        simulatedSystem.waitForState(FailedCompletedOutboundState.class);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(1);
    }

    protected void assertCallIsDisconnected(boolean callAlreadyDisconnected,
                                            boolean nearEnd, boolean wasConnected)
            throws InterruptedException
    {

        if (!callAlreadyDisconnected) {
            if (wasConnected) {
                // Wait for the state to be set to Disconnected
                simulatedSystem.waitForState(DisconnectedCompletedOutboundState.class);
                simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
            } else {
                // Wait for the state to be set to Failed
                simulatedSystem.waitForState(FailedCompletedOutboundState.class);
                simulatedSystem.assertEventReceived(FailedEvent.class, null);
            }
        }

        // Verify that there no longer is a call in the call dispatcher
        assertDispatchedCalls(0, 0);

        // Verify statistics
        if (wasConnected) {
            assertCurrentConnectionStatistics(0);
            assertTotalConnectionStatistics(1);
            assertDisconnectedCallStatistics(nearEnd, 1);
        } else {
            assertCurrentConnectionStatistics(0);
            assertTotalConnectionStatistics(1);
            assertFailedCallStatistics(1);
        }
    }

    protected RequestEvent assertCallDisconnect(
            Boolean sendResponse, boolean callWasConnected, boolean errorOccurred)
            throws Exception {

        // Wait for BYE request
        RequestEvent requestEvent = simulatedPhone.
                assertRequestReceivedAndIgnoreInviteResends(Request.BYE);


        if (errorOccurred) {
            // Wait for the state to be set to ErrorLingeringBye
            simulatedSystem.waitForState(ErrorLingeringByeOutboundState.class);
            simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        } else if (callWasConnected) {
            // Wait for the state to be set to DisconnectedLingeringBye
            simulatedSystem.waitForState(DisconnectedLingeringByeOutboundState.class);
            simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
        } else {
            // Wait for the state to be set to FailedLingeringBye
            simulatedSystem.waitForState(FailedLingeringByeOutboundState.class);
            simulatedSystem.assertEventReceived(FailedEvent.class, null);
        }

        if (sendResponse) {
            // Send OK response
            Response response =
                    simulatedPhone.createResponse(requestEvent, Response.OK);
            simulatedPhone.sendResponse(requestEvent, response);

            if (errorOccurred) {
                // Wait for the state to be set to ErrorCompleted
                simulatedSystem.waitForState(ErrorCompletedOutboundState.class);
            } else if (callWasConnected) {
                // Wait for the state to be set to DisconnectedCompleted
                simulatedSystem.waitForState(DisconnectedCompletedOutboundState.class);
            } else {
                // Wait for the state to be set to FailedCompleted
                simulatedSystem.waitForState(FailedCompletedOutboundState.class);
            }
        }

        // Verify that there is none current connection and one disconnected connection
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);

        return requestEvent;
    }

    protected void sendOkForBye(RequestEvent requestEvent, boolean callWasConnected)
            throws Exception {
        // Send OK response
        Response response =
                simulatedPhone.createResponse(requestEvent, Response.OK);
        simulatedPhone.sendResponse(requestEvent, response);

        if (callWasConnected) {
            // Wait for the state to be set to DisconnectedCompleted
            simulatedSystem.waitForState(DisconnectedCompletedOutboundState.class);
        } else {
            // Wait for the state to be set to FailedCompleted
            simulatedSystem.waitForState(FailedCompletedOutboundState.class);
        }
    }

    protected RequestEvent assertCallCreated(
            PhoneSimulator simulatedPhone, boolean redirected)
            throws Exception {

        // Wait for INVITE request
        RequestEvent requestEvent = simulatedPhone.assertRequestReceived(
        	Request.INVITE, true, redirected);

        // Wait for the state to be set to Progressing
        simulatedSystem.waitForState(ProgressingOutboundState.class);

        // Verify that the call dispatcher is updated with a new initiated call
        assertDispatchedCalls(1, 0);
        
        return requestEvent;
    }

    protected void assertNoCallCreated() throws Exception {
        assertFailedEventReceived(FailedEvent.Reason.REJECTED_BY_NEAR_END);

        // Verify statistics
        assertCurrentConnectionStatistics(0);
        assertTotalConnectionStatistics(1);
        assertFailedCallStatistics(1);
        assertErrorCallStatistics(0);
    }

    protected void assertPhoneRinging() throws InterruptedException {
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
        simulatedSystem.waitForState(ProgressingProceedingOutboundState.class);
    }

    protected void assertEarlyMedia() throws Exception {
        assertProgressingEventReceived(EARLY_MEDIA);
        simulatedSystem.waitForState(ProgressingEarlyMediaOutboundState.class);
        assertFarEndConnections();
    }

    protected void assertTokenSent() {
        // Wait for call client to receive an inbound call
        simulatedSystem.waitForOutboundTokens(TIMEOUT_IN_MILLI_SECONDS);
    }

    protected void gotoProgressingCallingState() throws Exception
    {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);
    }

    protected void gotoProgressingEarlyMediaState() throws Exception
    {
        gotoProgressingCallingState();

        // Phone sends early media
        simulatedPhone.indicateEarlyMedia(null);
        assertEarlyMedia();
    }

    protected void gotoProgressingProceedingState() throws Exception
    {
        gotoProgressingCallingState();
        simulatedPhone.trying();
        simulatedPhone.ring();
        assertPhoneRinging();
    }

    protected void gotoConnectedState() throws Exception {
        gotoProgressingProceedingState();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);
    }

    protected RequestEvent gotoLingerState(boolean usingCallCancel) throws Exception {
        RequestEvent requestEvent;
        if (usingCallCancel) {
            gotoProgressingProceedingState();

            // System disconnects the caller.
            simulatedSystem.disconnect();
            requestEvent = assertCallCanceled(false, false);
        } else {
            gotoConnectedState();

            // System disconnects up the caller.
            simulatedSystem.disconnect();
            requestEvent = assertCallDisconnect(false, true, false);
        }
        return requestEvent;
    }

    protected void gotoDisconnectedState() throws Exception {
        gotoConnectedState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }
}
