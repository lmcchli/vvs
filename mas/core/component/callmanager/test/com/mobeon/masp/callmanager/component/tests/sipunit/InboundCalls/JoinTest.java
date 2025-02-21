/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.events.JoinErrorEvent;
import com.mobeon.masp.callmanager.callhandling.OutboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingEarlyMediaInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState.ProgressingSubState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import javax.sip.RequestEvent;
import javax.sip.Dialog;

import org.jmock.Mock;

/**
 * Call Manager component test case to verify join were an inbound call
 * is involved.
 * @author Malin Flodin
 */
public class JoinTest extends InboundSipUnitCase {

    OutboundCallImpl anOutboundCall;
    Mock dialogMock = mock(Dialog.class);

    public void setUp() throws Exception {
        super.setUp();
        anOutboundCall = new OutboundCallImpl(
                callProperties, null, null, null, null,
                ConfigurationReader.getInstance().getConfig());
        anOutboundCall.setStateProgressing(ProgressingSubState.EARLY_MEDIA);
        anOutboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anOutboundCall.setOutboundStream(simulatedSystem.getOutboundStream());

        dialogMock.stubs().method("getDialogId").will(returnValue("dialogid"));
        anOutboundCall.setDialog((Dialog)dialogMock.proxy());
        anOutboundCall.setInitialDialogId("initialDialogId");
    }

    /**
     * Verifies that joining a call in
     * {@link AlertingNewCallInboundState} results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInAlertingNewCallState() throws Exception {
        gotoAlertingNewCallState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // System accepts the call.
        simulatedSystem.accept();
        assertCallAccepting(RINGING);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link AlertingAcceptingInboundState} results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInAlertingAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link AlertingEarlyMediaInboundState} results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInAlertingEarlyMediaState() throws Exception {
        gotoAlertingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);


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
     * Verifies that joining a call in
     * {@link ConnectedInboundState} results in a {@link JoinedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedState() throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);
        
        assertTrue(anOutboundCall.isCallJoined());

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertFalse(anOutboundCall.isCallJoined());
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedInboundState} results in a {@link JoinErrorEvent} if the
     * stream throws an exception.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedStateWhenStreamThrowsException()
            throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, FAIL);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedInboundState} when the outbound call inbound stream is
     * null results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedStateWhenOutboundCallInboundStreamIsNull()
            throws Exception {
        gotoConnectedState();

        // Make sure the outbound call inbound stream is null.
        anOutboundCall.setInboundStream(null);

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedInboundState} when the outbound call outbound stream is
     * null results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedStateWhenOutboundCallOutboundStreamIsNull()
            throws Exception {
        gotoConnectedState();

        // Make sure the outbound call outbound stream is null.
        anOutboundCall.setOutboundStream(null);

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call that is already joined
     * results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinWhenAlreadyJoined() throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System joins the call AGAIN.
        OutboundCallImpl anotherOutboundCall = createAnotherOutboundCall();
        simulatedSystem.join(anotherOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);

        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link DisconnectedLingeringByeInboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInDisconnectedLingeringByeState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that joining a call in
     * {@link DisconnectedCompletedInboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInDisconnectedCompletedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);
    }

    // ========================= Private methods ===========================

    /**
     * Creates an outbound call that can be used during testing.
     * @return A new outbound call.
     * @throws Exception if creating the outbound call fails.
     */
    private OutboundCallImpl createAnotherOutboundCall() throws Exception {
        OutboundCallImpl anOutboundCall;

        anOutboundCall = new OutboundCallImpl(
                callProperties, null, null, null, null,
                ConfigurationReader.getInstance().getConfig());
        anOutboundCall.setStateProgressing(ProgressingSubState.EARLY_MEDIA);
        anOutboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anOutboundCall.setOutboundStream(simulatedSystem.getOutboundStream());
        return anOutboundCall;
    }


}
