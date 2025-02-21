/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.registration;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.events.ErrorEvent;

import javax.sip.RequestEvent;
import javax.sip.message.Response;
import javax.sip.message.Request;

import org.cafesip.sipunit.SipTransaction;

/**
 * Call Manager component test case to verify SSP registrations.
 * @author Malin Flodin
 */
public class RegisterTest extends RegistrationCase {

    /**
     * Verifies that a SIP REGISTER request responded to with an OK response
     * containing the expires parameter in the contact header will result in
     * the Registered state.
     * @throws Exception if test case fails.
     */
    public void testRegisterResponseWithContactExpire() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send OK response with contact header with expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        sipResponse.addContactHeaderExpiration(60);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // We should now be registered
        assertStateRegistered();
    }

    /**
     * Verifies that a SIP REGISTER request responded to with an OK response
     * containing the expires header will result in the Registered state.
     * @throws Exception if test case fails.
     */
    public void testRegisterResponseWithExpiresHeader() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send OK response with contact header with expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        sipResponse.addContactHeaderExpiration(60);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // We should now be registered
        assertStateRegistered();
    }

    /**
     * Verifies that a SIP REGISTER request responded to with an OK response
     * containing no expires time will result in the backoff timer scheduled.
     * The state is left unchanged, i.e. in Registering state.
     * @throws Exception if test case fails.
     */
    public void testRegisterResponseWithNoExpiresTime() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send OK response with no expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // Wait for retransmission of REGISTER request
        simulatedSsp.assertRegisterReceived();

        // We should still be registering
        assertStateRegistering();
    }

    /**
     * Verifies that a SIP REGISTER request responded to with an OK response
     * containing zero expires time will result in the backoff timer scheduled.
     * The state is left unchanged, i.e. in Registering state.
     * @throws Exception if test case fails.
     */
    public void testRegisterResponseWithZeroExpiresTime() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send OK response with zero expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        sipResponse.addExpiresHeader(0);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // Wait for retransmission of REGISTER request
        simulatedSsp.assertRegisterReceived();

        // We should still be registering
        assertStateRegistering();
    }

    /**
     * Verifies that a SIP REGISTER request that times out (timer B expires)
     * will result in the backoff timer scheduled.
     * The state is left unchanged, i.e. in Registering state.
     * @throws Exception if test case fails.
     */
    public void testRegisterThatTimesOut() throws Exception {
        // Wait for REGISTER request
        simulatedSsp.assertRegisterReceived();

        // Send no response
        // Sleep a while to enable SIP timer F to expire
        Thread.sleep(3000);

        // Wait for retransmission of REGISTER request
        simulatedSsp.assertRegisterReceived();

        // We should still be registering
        assertStateRegistering();
    }

    /**
     * Verifies that a SIP REGISTER request that is responded with a SIP
     * error response will result in the backoff timer scheduled.
     * The state is left unchanged, i.e. in Registering state.
     * @throws Exception if test case fails.
     */
    public void testRegisterRespondedWithError() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send Error response
        simulatedSsp.sendResponse(requestEvent, 500);

        // We should still be registering
        assertStateRegistering();
    }

    /**
     * Verifies that a SIP provisional response to a SIP REGISTER request is
     * ignored.
     * The state is left unchanged, i.e. in Registering state.
     * @throws Exception if test case fails.
     */
    public void testRegisterRespondedWithProvisional() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send Error response
        simulatedSsp.sendResponse(requestEvent, 100);

        // We should still be registering
        assertStateRegistering();
    }

    /**
     * Verifies that the retry timer is scheduled when the SIP REGISTER request
     * succeeds.
     * The state is set to Registered state.
     * @throws Exception if test case fails.
     */
    public void testRegisterRetry() throws Exception {
        // Wait for REGISTER request
        RequestEvent requestEvent = simulatedSsp.assertRegisterReceived();

        // Send OK response with contact header with expires time
        SipResponse sipResponse = simulatedSsp.createOkResponse(requestEvent);
        sipResponse.addContactHeaderExpiration(3);
        simulatedSsp.sendResponse(requestEvent, sipResponse.getResponse());

        // We should now be registered
        assertStateRegistered();

        // Sleep a while to enable the retry timer to expire
        Thread.sleep(3000);

        // Wait for a new REGISTER request
        simulatedSsp.assertRegisterReceived();
    }

    /**
     * Verifies that when creating an outbound call while not registered in any
     * SSP's, an {@link ErrorEvent} is generated and the state is set to
     * {@link com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorCompletedOutboundState}.
     * The state is set to Registered state.
     * @throws Exception if test case fails.
     */
    public void testCreateOutboundCallWhenNotRegisteredInAnySsp() throws Exception {
        CallProperties callProperties = new CallProperties();
        CalledParty calledParty = new CalledParty();
        calledParty.setSipUser("sipPhone@localhost");
        CallingParty callingParty = new CallingParty();
        callingParty.setTelephoneNumber("4321");
        callProperties.setCalledParty(calledParty);
        callProperties.setCallingParty(callingParty);
        callProperties.setMaxDurationBeforeConnected(5*TIMEOUT_IN_MILLI_SECONDS);

        // At startup CallManager is not registered in any SSP.
        // Try creating an outbound call
        simulatedSystem.createCall(callProperties);

        assertEventReceived(ErrorEvent.class);
        assertCallStateError();
    }

    /**
     * Verifies that an OPTIONS request received while not registered to an SSP
     * will be responded to with a SIP OK response containing the experienced
     * operational status "up".
     * @throws Exception if test case fails.
     */
    public void testOptionsWhenNotRegistered() throws Exception {
        // Wait for REGISTER request
        simulatedSsp.assertRegisterReceived();

        Request options = simulatedSsp.createOptionsRequest();

        SipTransaction transaction =
                simulatedSsp.sendOutOfDialogRequest(options);

        // Wait for response
        Response response = simulatedSsp.assertResponseReceived(
                transaction, Response.OK, Request.OPTIONS);

        // Verify that the response contains an Accept header
        simulatedSsp.assertExperiencedOperationalStatus(response, "up");
    }
}
