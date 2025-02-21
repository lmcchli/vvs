/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;


/**
 * Call Manager component test case to verify early media for outbound calls.
 * @author Malin Flodin
 */
public class EarlyMediaTest extends OutboundSipUnitCase {

    /**
     * Verifies that a normal early media for outbound calls.
     * @throws Exception when the test case fails.
     */
    public void testNormalEarlyMedia() throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends early media
        simulatedPhone.indicateEarlyMedia(null);
        assertEarlyMedia();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a when early media is received after ringing, this is
     * handled as early media received before a ringing only that two
     * {@link com.mobeon.masp.callmanager.events.AlertingEvent} are received,
     * one without early media and one with.
     * @throws Exception when the test case fails.
     */
    public void testEarlyMediaAfterRinging() throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends early media
        simulatedPhone.indicateEarlyMedia(null);
        assertEarlyMedia();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a when a ringing response is received after early media
     * an {@link com.mobeon.masp.callmanager.events.AlertingEvent} is generated
     * that indicates NO early media.
     * @throws Exception when the test case fails.
     */
    public void testRingingAfterEarlyMedia() throws Exception {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);
        assertCallCreated(simulatedPhone, false);

        // Phone sends trying
        simulatedPhone.trying();

        // Phone sends early media
        simulatedPhone.indicateEarlyMedia(null);
        assertEarlyMedia();

        // Phone sends ringing
        simulatedPhone.ring();
        assertProgressingEventReceived(NO_EARLY_MEDIA);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

}
