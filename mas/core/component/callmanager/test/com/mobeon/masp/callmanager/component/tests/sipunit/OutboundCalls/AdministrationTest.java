/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.states.ClosedState;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipTransaction;

/**
 * Call Manager component test case to verify that administration such as
 * lock, unlock and shutdown of the Call Manager works as expected with regards
 * to outbound calls.
 * @author Malin Flodin
 */
public class AdministrationTest extends OutboundSipUnitCase {

    public void testOutboundCallInLockedState() throws Exception {
        simulatedSystem.lock();
        callManager.waitForAdminState(ClosedState.class);

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertNoCallCreated();
    }

    public void testOutboundCallAtMaxLoad() throws Exception {
        // This update will cause LWM=0, HWM=1, MAX=2
        simulatedSystem.updateThreshold(0,0,0);

        // Since MAX=2 we have to make two outbound calls before max is reached
        simulatedSystem.createCall(callProperties);
        simulatedPhone.assertRequestReceived(Request.INVITE, true, false);
        simulatedSystem.createCall(callProperties);
        simulatedPhone.assertRequestReceived(Request.INVITE, true, false);

        // A third outbound call is created. It should not be accepted due to MAX=2.
        simulatedSystem.createCall(callProperties);
        assertNoCallCreated();

    }

    /**
     * Verifies that an OPTIONS request received during HWM is rejected
     * with a 503 "Service Unavailable" response.
     * @throws Exception
     */
    public void testOptionsAtHwm() throws Exception {
        // This update will cause LWM=0, HWM=1, MAX=2
        simulatedSystem.updateThreshold(0,0,0);

        // Since HWN=1 we have to make one outbound calls before HWM is reached
        simulatedSystem.createCall(callProperties);
        simulatedPhone.assertRequestReceived(Request.INVITE, true, false);

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
        // This update will cause LWM=0, HWM=1, MAX=2
        simulatedSystem.updateThreshold(0,0,0);

        // Since MAX=2 we have to make two outbound calls before max is reached
        simulatedSystem.createCall(callProperties);
        simulatedPhone.assertRequestReceived(Request.INVITE, true, false);
        simulatedSystem.createCall(callProperties);
        simulatedPhone.assertRequestReceived(Request.INVITE, true, false);

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
    public void testOutboundCallInLockedStateWhenThereIsAlreadyACall() throws Exception {

        // Make an outbound call
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


}
