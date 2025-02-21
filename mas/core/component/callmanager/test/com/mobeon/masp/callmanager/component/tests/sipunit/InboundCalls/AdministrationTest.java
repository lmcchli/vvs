/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.CMUtils;

import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipTransaction;

/**
 * Call Manager component test case to verify that administration such as
 * lock, unlock and shutdown of the Call Manager works as expected with regards
 * to inbound calls.
 * @author Malin Flodin
 */
public class AdministrationTest extends InboundSipUnitCase {

    /**
     * Verifies that an inbound call received in locked state is rejected with
     * a SIP "Temporarily Unavailable" response.
     * @throws Exception
     */
    public void testInboundCallInLockedState() throws Exception {
        simulatedSystem.lock();

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallRejected(Response.SERVICE_UNAVAILABLE, simulatedPhone, 0, 1, 1, 0);
    }

    /**
     * Verifies that an inbound call received during max load is rejected with
     * a 503 "Service Unavailable" response.
     * @throws Exception
     */
    public void testInboundCallAtMaxLoad() throws Exception {
        simulatedSystem.updateThreshold(1, 0, 2);

        addCalls(2);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallRejected(503, simulatedPhone, 0, 1, 1, 0);
    }

    /**
     * Verifies that an OPTIONS request received during HWM is rejected
     * with a 503 "Service Unavailable" response.
     * @throws Exception
     */
    public void testOptionsAtHwm() throws Exception {
        simulatedSystem.updateThreshold(1, 0, 2);

        addCalls(1);

        // Send options request
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);
        SipTransaction transaction = simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for 503 response.
        simulatedPhone.assertResponseReceived(transaction, 503, Request.OPTIONS);
    }

    /**
     * Verifies that an OPTIONS request received during max load is rejected
     * with a 503 "Service Unavailable" response.
     * @throws Exception
     */
    public void testOptionsAtMaxLoad() throws Exception {
        simulatedSystem.updateThreshold(1, 0, 2);

        addCalls(2);

        // Send options request
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.NO_BODY, null, false, false);
        SipTransaction transaction = simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for 503 response.
        simulatedPhone.assertResponseReceived(transaction, 503, Request.OPTIONS);
    }

    /**
     * Verifies that an inbound call received in locked state, when there is already a call,
     *  is rejected with a SIP "Temporarily Unavailable" response.
     * @throws Exception
     */
    public void testInboundCallInLockedStateWhenThereIsAlreadyACall() throws Exception {

        // Make an inbound call
        gotoConnectedState();

        // close unforced
        boolean forced = false;
        CMUtils.getInstance().getCmController().close(forced);

        // Make another inbound call and make sure the call is not accepted
        Request secondInvite = simulatedSecondPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG,  PhoneSimulator.NO_BODY, null, false, false);
        simulatedSecondPhone.sendInvite(secondInvite);
        assertCallRejected(Response.SERVICE_UNAVAILABLE, simulatedSecondPhone, null, null, null, null);
    }


    private void addCalls(int nrOfCalls) {
        for(int i = 0; i<nrOfCalls; ++i) {
            CMUtils.getInstance().getCmController().getLoadRegulator().addCall(null);
        }
    }

}
