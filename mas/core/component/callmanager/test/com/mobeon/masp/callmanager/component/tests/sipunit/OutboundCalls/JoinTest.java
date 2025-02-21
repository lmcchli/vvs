/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.events.JoinErrorEvent;
import com.mobeon.masp.callmanager.callhandling.InboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingEarlyMediaOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.CMUtils;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify join were an outbound call
 * is involved.
 * @author Malin Flodin
 */
public class JoinTest extends OutboundSipUnitCase {

    InboundCallImpl anInboundCall;

    public void setUp() throws Exception {
        super.setUp();
        anInboundCall = simulatedSystem.createInboundCall();
        anInboundCall.setStateConnected();
        anInboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anInboundCall.setOutboundStream(simulatedSystem.getOutboundStream());
        CMUtils.getInstance().getCallDispatcher().removeCall(
                anInboundCall.getInitialDialogId(), anInboundCall.getEstablishedDialogId());
    }

    /**
     * Verifies that joining a call in
     * {@link ProgressingCallingOutboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ProgressingProceedingOutboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link JoinedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinInProgressingEarlyMediaState() throws Exception {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        assertTrue(anInboundCall.isCallJoined());

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        assertTrue(anInboundCall.isCallJoined());

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertFalse(anInboundCall.isCallJoined());
    }

    /**
     * Verifies that joining a call in
     * {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link JoinErrorEvent} if stream throws exception.
     * @throws Exception when the test case fails.
     */
    public void testJoinInProgressingEarlyMediaStateWhenStreamThrowsException()
            throws Exception {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, FAIL);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedOutboundState} results in a
     * {@link JoinedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinInConnectedState() throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        assertTrue(anInboundCall.isCallJoined());

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

        assertFalse(anInboundCall.isCallJoined());
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedOutboundState} when the inbound call inbound stream is
     * null results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedStateWhenInboundCallInboundStreamIsNull()
            throws Exception {
        gotoConnectedState();

        // Make sure the outbound call inbound stream is null.
        anInboundCall.setInboundStream(null);

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link ConnectedOutboundState} when the inbound call outbound stream is
     * null results in a {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinCallInConnectedStateWhenInboundCallOutboundStreamIsNull()
            throws Exception {
        gotoConnectedState();

        // Make sure the outbound call outbound stream is null.
        anInboundCall.setOutboundStream(null);

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
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
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System joins the call AGAIN.
        InboundCallImpl anotherInboundCall = createAnotherInboundCall();
        simulatedSystem.join(anotherInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that joining a call in
     * {@link DisconnectedLingeringByeOutboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinTokenInDisconnectedLingeringByeState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that joining a call in
     * {@link DisconnectedCompletedOutboundState} results in a
     * {@link JoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testJoinTokenInDisconnectedCompletedState() throws Exception {
        gotoDisconnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinErrorEvent.class, null);
    }


    // ========================= Private methods ===========================

    /**
     * Creates an inbound call that can be used during testing.
     * @return A new inbound call.
     * @throws Exception if creating the inbound call fails.
     */
    private InboundCallImpl createAnotherInboundCall() throws Exception {
        InboundCallImpl anInboundCall;
        anInboundCall = simulatedSystem.createInboundCall();
        anInboundCall.setStateConnected();
        anInboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anInboundCall.setOutboundStream(simulatedSystem.getOutboundStream());
        CMUtils.getInstance().getCallDispatcher().removeCall(
                anInboundCall.getInitialDialogId(), anInboundCall.getEstablishedDialogId());
        return anInboundCall;
    }

}
