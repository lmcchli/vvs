/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import javax.sip.RequestEvent;

/**
 * Call Manager component test case to verify BYE to inbound calls.
 * @author Malin Flodin
 */
public class ByeTest extends InboundSipUnitCase {

    /**
     * Verifies that BYE to an inbound call in Accepting state results in a
     * SIP OK response, SIP Request Terminated response and a FailedEvent.
     * @throws Exception when the test case fails.
     */
    public void testByeInAcceptingState() throws Exception {
        gotoAlertingAcceptingState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that BYE to an inbound call in Connected state results in a
     * SIP OK response and a
     * {@link com.mobeon.masp.callmanager.events.DisconnectedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testByeInConnectedState() throws Exception {
        gotoConnectedState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that BYE to an inbound call in Linger state results in a
     * SIP OK response.
     * @throws Exception when the test case fails.
     */
    public void testByeInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
