/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

/**
 * Call Manager component test case to verify CANCEL to inbound calls.
 * @author Malin Flodin
 */
public class CancelTest extends InboundSipUnitCase {

    /**
     * Verifies that CANCEL to an inbound call in Alerting state results in a
     * SIP OK response, SIP Request Terminated response and a
     * {@link com.mobeon.masp.callmanager.events.FailedEvent}.
     * @throws Exception when the test case fails.
     */
    public void testCancelInAlertingNewCallState() throws Exception {
        gotoAlertingNewCallState();

        // Phone disconnects the call
        simulatedPhone.cancel(PhoneSimulator.WITHIN_DIALOG, true, true);
        assertCallCanceled();
    }
}
