/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;

/**
 * Call Manager component test case to verify unknown and known but not
 * supported SIP requests for outbound calls.
 * @author Malin Flodin
 */
public class OtherMethodTest extends OutboundSipUnitCase {

    /**
     * Verifies that known but not supported SIP request in Progressing-Calling
     * state results in a SIP Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethodInProgressingCallingState()
            throws Exception
    {
        gotoProgressingCallingState();

        SipTransaction transaction = simulatedPhone.sendRegister(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.REGISTER);

        assertCurrentState(ProgressingCallingOutboundState.class);

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
     * Verifies that known but not supported SIP request in Progressing-Proceeding
     * state results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethodInProgressingProceedingState()
            throws Exception
    {
        gotoProgressingProceedingState();

        SipTransaction transaction = simulatedPhone.sendRegister(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.REGISTER);

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

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

        assertCurrentState(ConnectedOutboundState.class);

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
        RequestEvent byeRequestEvent = gotoLingerState(false);

        SipTransaction transaction = simulatedPhone.sendRegister(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.REGISTER);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }

    /**
     * Verifies that unknown method in Progressing-Calling
     * state results in a SIP Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInProgressingCallingState()
            throws Exception
    {
        gotoProgressingCallingState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "UNKNOWN");

        assertCurrentState(ProgressingCallingOutboundState.class);

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
     * Verifies that unknown method in Progressing-Proceeding
     * state results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInProgressingProceedingState()
            throws Exception
    {
        gotoProgressingProceedingState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unknown method in Connected state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInConnectedState() throws Exception
    {
        gotoConnectedState();

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that unknown method in Linger state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethodInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        SipTransaction transaction = simulatedPhone.sendUnknownMethod(true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.NOT_IMPLEMENTED, "UNKNOWN");

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
