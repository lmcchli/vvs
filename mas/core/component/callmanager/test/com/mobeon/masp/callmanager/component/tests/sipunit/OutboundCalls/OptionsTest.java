/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;

/**
 * Call Manager component test case to verify OPTIONS of outbound calls.
 * @author Malin Flodin
 */
public class OptionsTest extends OutboundSipUnitCase {
    /**
     * Verifies that OPTIONS in Progressing-Calling state results in a SIP
     * Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for Call/Transaction Does Not Exist response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.OPTIONS);

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
     * Verifies that OPTIONS in Progressing-Proceeding state results in a SIP
     * OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

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

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that OPTIONS in Linger state results in a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        SipTransaction transaction = simulatedPhone.sendOptions(true);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
