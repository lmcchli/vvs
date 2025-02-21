/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Response;
import javax.sip.message.Request;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

/**
 * Call Manager component test case to verify SIP requests/responses and
 * timeout events generated out-of-dialog.
 * @author Malin Flodin
 */
public class OutOfDialogTest extends SipUnitCase {

    /**
     * Verifies that OPTIONS sent out-of-dialog results in a SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testOptions() throws Exception {
        SipTransaction transaction = simulatedPhone.sendOptions(false);
        // Wait for OK response
        simulatedPhone.assertResponseReceived(transaction,
                Response.OK, Request.OPTIONS);
    }

    /**
     * Verifies that known but not supported SIP request sent out-of-dialog
     * results in a SIP Call/Transaction does not exist response.
     * @throws Exception when the test case fails.
     */
    public void testKnownButNotSupportedMethod() throws Exception
    {
        SipTransaction transaction = simulatedPhone.sendRegister(false);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.REGISTER);
    }

    /**
     * Verifies that unknown method sent out-of-dialog results in a SIP
     * Call/Transaction does not exist response.
     * @throws Exception when the test case fails.
     */
    public void testUnknownMethod() throws Exception {
        SipTransaction transaction = simulatedPhone.sendUnknownMethod(false);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "UNKNOWN");
    }

    /**
     * Verifies that BYE sent out-of-dialog results in a SIP Call or Transaction
     * Does Not Exist.
     * @throws Exception when the test case fails.
     */
    public void testBye() throws Exception {
        SipTransaction transaction =
                simulatedPhone.sendBye(PhoneSimulator.OUT_OF_DIALOG);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.BYE);
    }

    /**
     * Verifies that it is possible to send an out-of-dialog ACK.
     * @throws Exception when the test case fails.
     */
    public void testAck() throws Exception {
        simulatedPhone.acknowledge(PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, false);
        Thread.sleep(3000);

    }

}
