/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

/**
 * Call Manager component test case to verify disconnect of inbound calls.
 * @author Malin Flodin
 */
public class DisconnectTest extends InboundSipUnitCase {

    /**
     * Verifies that disconnect of an inbound call in
     * {@link AlertingNewCallInboundState} results in a
     * {@link com.mobeon.masp.callmanager.events.NotAllowedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInAlertingNewCallState() throws Exception {
        gotoAlertingNewCallState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        assertCallTerminated();
    }

    /**
     * Verifies that disconnect of an inbound call in
     * {@link AlertingAcceptingInboundState} results in a
     * BYE request send after the ACK has been received.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInAlertingAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        // Phone sends ACK
        simulatedPhone.acknowledge(PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);

        assertCallDisconnect(true, false);
    }

    /**
     * Verifies that disconnect of an inbound call in Connected state results in a
     * BYE request and DisconnectedEvent.
     * @throws Exception when the test case fails.
     */
    public void testDisconnectInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to disconnect.
        simulatedSystem.disconnect();

        assertCallDisconnect(true, true);
    }
}
