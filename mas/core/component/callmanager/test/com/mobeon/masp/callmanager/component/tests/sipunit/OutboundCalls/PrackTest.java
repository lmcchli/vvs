/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.events.ProgressingEvent;

/**
 * Call Manager component test case for PRACK usage when reliable 1xx
 * responses are received for outbound calls.
 *
 * @author Malin Nyfeldt
 */
public class PrackTest extends OutboundSipUnitCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Verifies that if sending a reliable provisional response to Call Manager
     * in the Progressing Early Media state, a PRACK request is received.
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testReliableResponseInProgressingEarlyMediaState()
            throws Exception {
        gotoProgressingEarlyMediaState();

        simulatedPhone.sendReliableRinging();
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
        assertPrackReceived(true);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);

    }

    /**
     * Verifies that if sending a reliable provisional response to Call Manager
     * in the Progressing Early Media state, a PRACK request is received.
     * If no 200 OK response is sent for the PRACK request, a SIP timeout will
     * cause Call Manager to cancel the call.
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testReliableResponseInProgressingEarlyMediaStateWhenNoOkForPrack()
            throws Exception {
        gotoProgressingEarlyMediaState();

        simulatedPhone.sendReliableRinging();
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
        assertPrackReceived(false);

        Thread.sleep(2000);
        assertCallCanceled(true, true);
    }

    /**
     * Verifies that if sending a reliable provisional response to Call Manager
     * in the Progressing Proceeding state, a PRACK request is received.
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testReliableResponseInProgressingProceedingState()
            throws Exception {
        gotoProgressingProceedingState();

        simulatedPhone.sendReliableRinging();
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
        assertPrackReceived(true);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that if sending a reliable provisional response to Call Manager
     * in the Progressing Proceeding state, a PRACK request is received.
     * If no 200 OK response is sent for the PRACK request, a SIP timeout will
     * cause Call Manager to cancel the call.
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testReliableResponseInProgressingProceedingStateWhenNoOkForPrack()
            throws Exception {
        gotoProgressingProceedingState();

        simulatedPhone.sendReliableRinging();
        simulatedSystem.assertEventReceived(ProgressingEvent.class, null);
        assertPrackReceived(false);

        Thread.sleep(2000);
        assertCallCanceled(true, true);
    }

}
