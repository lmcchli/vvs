/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.events.UnjoinErrorEvent;
import com.mobeon.masp.callmanager.events.UnjoinedEvent;
import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.callhandling.OutboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.states.inbound.*;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState.ProgressingSubState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify unjoin were an inbound call
 * is involved.
 * @author Malin Flodin
 */
public class UnjoinTest extends InboundSipUnitCase {

    OutboundCallImpl anOutboundCall;
    public void setUp() throws Exception {
        super.setUp();
        anOutboundCall = new OutboundCallImpl(
                callProperties, null, null, null, null,
                ConfigurationReader.getInstance().getConfig());
        anOutboundCall.setStateProgressing(ProgressingSubState.EARLY_MEDIA);
        anOutboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anOutboundCall.setOutboundStream(simulatedSystem.getOutboundStream());
    }

    /**
     * Verifies that unjoining a call in
     * {@link AlertingNewCallInboundState} results in a {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInAlertingNewCallState() throws Exception {
        gotoAlertingNewCallState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

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
     * Verifies that unjoining a call in
     * {@link AlertingAcceptingInboundState} results in a {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInAlertingAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link AlertingEarlyMediaInboundState} results in a {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInAlertingEarlyMediaState() throws Exception {
        gotoAlertingEarlyMediaState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

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
     * Verifies that unjoining a call in
     * {@link ConnectedInboundState} results in a {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInConnectedState() throws Exception {
        gotoConnectedState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a joined call in
     * {@link ConnectedInboundState} results in a {@link UnjoinedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallWhenJoined() throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinedEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a joined call in
     * {@link ConnectedInboundState} results in a {@link UnjoinErrorEvent} if
     * the stream throws an exception.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallWhenStreamThrowsException()
            throws Exception {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, FAIL);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoin(C1,C3) when call C1 is joined to C2 with join(C1,C2)
     * results in a {@link UnjoinErrorEvent}.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallWhenJoinedToAnotherCall() throws Exception {
        gotoConnectedState();

        // System joins C1 with C2.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        // System unjoins C1 from C3.
        simulatedSystem.unjoin(createAnotherOutboundCall(), SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link DisconnectedLingeringByeInboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInDisconnectedLingeringByeState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that unjoining a call in
     * {@link DisconnectedCompletedInboundState} results in a
     * {@link UnjoinErrorEvent}
     * since the call is not previously joined.
     * @throws Exception when the test case fails.
     */
    public void testUnjoinCallInDisconnectedCompletedState() throws Exception {
        gotoDisconnectedCompletedState();

        // System unjoins the call.
        simulatedSystem.unjoin(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(UnjoinErrorEvent.class, null);
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
