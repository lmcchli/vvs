/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;

import org.cafesip.sipunit.SipTransaction;

/**
 * Call Manager component test case to verify OPTIONS of inbound calls.
 * @author Malin Flodin
 */
public class OptionsTest extends InboundSipUnitCase {

    /**
     * Verifies that OPTIONS in Accepting state results in a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);

        assertCurrentState(AlertingAcceptingInboundState.class);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that OPTIONS in Connected state results in a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInConnectedState() throws Exception {
        gotoConnectedState();

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that OPTIONS in Linger state results in a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
