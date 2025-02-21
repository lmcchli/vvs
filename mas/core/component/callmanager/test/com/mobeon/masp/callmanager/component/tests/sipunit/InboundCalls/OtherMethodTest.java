/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;

import org.cafesip.sipunit.SipTransaction;

/**
 * Call Manager component test case to verify unknown and known but not
 * supported SIP requests for inbound calls.
 * @author Malin Flodin
 */
public class OtherMethodTest extends InboundSipUnitCase {

    /**
     * Verifies that known but not supported SIP request in Accepting state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethodInAcceptingState()
            throws Exception {
        gotoAlertingAcceptingState();

        SipTransaction transaction = simulatedPhone.sendRegister(true);

        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.REGISTER);

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
     * Verifies that known but not supported SIP request in Connected state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethodInConnectedState()
            throws Exception
    {
        gotoConnectedState();

        SipTransaction transaction = simulatedPhone.sendRegister(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.REGISTER);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that known but not supported SIP request in Linger state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethodInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        SipTransaction transaction = simulatedPhone.sendRegister(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.REGISTER);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that unknown method in Accepting state results in a SIP Not
     * Implemented response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

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
     * Verifies that unknown method in Connected state results in a SIP Not
     * Implemented response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInConnectedState() throws Exception {
        gotoConnectedState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unknown method in Linger state results in a SIP Not
     * Implemented response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
