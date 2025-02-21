/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.registration;

import com.mobeon.masp.callmanager.sip.message.SipResponse;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify SSP unregistrations.
 * @author Malin Flodin
 */
public class UnregisterTest extends RegistrationCase {

    /**
     * Verifies by using a lock request that a SIP (UN)REGISTER request contains
     * a zero expire time.
     * A SIP OK is sent in response which results in the Unregistered state.
     * @throws Exception if test case fails.
     */
    public void testUnRegisterRequest() throws Exception {
        gotoRegisteredState();

        // Lock the Call Manager
        simulatedSystem.lock();

        // Wait for (UN)REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertUnregisterReceived();

        // We should now be unregistering
        assertStateUnregistering();

        // Send OK response
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // We should now be unregistered
        assertStateUnregistered();
    }


    /**
     * Verifies that when a SIP Error response is sent as response to a
     * SIP (UN)REGISTER request the state is set to Unregistered immediately.
     * @throws Exception if test case fails.
     */
    public void testUnRegisterRespondedWithError() throws Exception {
        gotoRegisteredState();

        // Lock the Call Manager
        simulatedSystem.lock();

        // Wait for (UN)REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertUnregisterReceived();

        // We should now be unregistering
        assertStateUnregistering();

        // Send Error response
        simulatedSsp.sendResponse(requestEvent, 500);

        // We should now be unregistered
        assertStateUnregistered();
    }

    /**
     * Verifies that when a SIP (UN)REGISTER request times out (Timer B)
     * the state is set to Unregistered immediately.
     * @throws Exception if test case fails.
     */
    public void testUnRegisterThatTimesOut() throws Exception {
        gotoRegisteredState();

        // Lock the Call Manager
        simulatedSystem.lock();

        // Wait for (UN)REGISTER request
        simulatedSsp.assertUnregisterReceived();

        // We should now be unregistering
        assertStateUnregistering();

        // Send no response
        // Sleep a while to enable SIP timer F to expire
        Thread.sleep(3000);

        // We should now be unregistered
        assertStateUnregistered();
    }


}
