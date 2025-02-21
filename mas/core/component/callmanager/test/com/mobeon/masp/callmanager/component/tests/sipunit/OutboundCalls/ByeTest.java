/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import org.cafesip.sipunit.SipTransaction;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify BYE to outbound calls.
 * @author Malin Flodin
 */
public class ByeTest extends OutboundSipUnitCase {

    /**
     * Verifies that BYE to an outbound call in Progressing-Calling state
     * results in a SIP Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testByeInProgressingCallingState() throws Exception {
        gotoProgressingCallingState();

        SipTransaction transaction =
                simulatedPhone.sendBye(PhoneSimulator.WITHIN_DIALOG);
        // Wait for Call/Transaction Does Not Exist response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.BYE);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that BYE to an outbound call in Progressing-Proceeding state
     * results in a SIP OK response, SIP Request Terminated response and a
     * FailedEvent.
     * @throws Exception when the test case fails.
     */
    public void testByeInProgressingProceedingState() throws Exception {
        gotoProgressingProceedingState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that BYE to an outbound call in Connected state results in a
     * SIP OK response and a DisconnectedEvent.
     * @throws Exception when the test case fails.
     */
    public void testByeInConnectedState() throws Exception {
        gotoConnectedState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that BYE to an outbound call in Linger state results in a
     * SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testByeInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
