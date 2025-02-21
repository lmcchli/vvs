/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.events.UnjoinedEvent;
import com.mobeon.masp.callmanager.events.UnjoinErrorEvent;
import com.mobeon.masp.callmanager.events.JoinedEvent;
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
 * Call Manager component test case to verify unjoin were an outbound call
 * is involved.
 * @author Malin Flodin
 */
public class UnjoinTest extends OutboundSipUnitCase {

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
     * Verifies that unjoining a call in
     * {@link ProgressingCallingOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

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
     * Verifies that unjoining a call in
     * {@link ProgressingProceedingOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingEarlyMediaState() throws Exception {
        gotoProgressingEarlyMediaState();

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link UnjoinedEvent} since the call is previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingEarlyMediaStateWhenJoined() throws Exception {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinedEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link UnjoinErrorEvent} if stream throws exception.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingEarlyMediaStateWhenStreamThrowsException()
            throws Exception {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, FAIL);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoin(C1,C3) when call C1 is joined to C2 with join(C1,C2)
     * in {@link ProgressingEarlyMediaOutboundState} results in a
     * {@link UnjoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInProgressingEarlyMediaStateWhenJoinedToAnotherCall()
            throws Exception {
        gotoProgressingEarlyMediaState();

        // System joins C1 with C2.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins C1 from C3.
        simulatedSystem.unjoin(createAnotherInboundCall(), SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ConnectedOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInConnectedState() throws Exception {
        gotoConnectedState();

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ConnectedOutboundState} results in a
     * {@link UnjoinedEvent} since the call is previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInConnectedStateWhenJoined() throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinedEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link ConnectedOutboundState} results in a
     * {@link UnjoinErrorEvent} if stream throws exception.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInConnectedStateWhenStreamThrowsException()
            throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, FAIL);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoin(C1,C3) when call C1 is joined to C2 with join(C1,C2)
     * in {@link ConnectedOutboundState} results in a {@link UnjoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinInConnectedStateWhenJoinedToAnotherCall()
            throws Exception {
        gotoConnectedState();

        // System joins C1 with C2.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins C1 from C3.
        simulatedSystem.unjoin(createAnotherInboundCall(), SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link DisconnectedLingeringByeOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinTokenInDisconnectedLingeringByeState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link DisconnectedCompletedOutboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinTokenInDisconnectedCompletedState() throws Exception {
        gotoDisconnectedState();

        // System unjoins the call.
        simulatedSystem.unjoin(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);
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
