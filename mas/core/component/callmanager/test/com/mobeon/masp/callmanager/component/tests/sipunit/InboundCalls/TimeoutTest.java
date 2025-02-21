/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorCompletedInboundState;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;


/**
 * Call Manager component test case to verify timeouts during inbound calls.
 * @author Malin Nyfeldt
 */
public class TimeoutTest extends InboundSipUnitCase {
    /**
     * Verifies that a call times out after Timer H *T1 milliseconds if the call
     * was not acknowledged. Timer H is set to 4 in the configuration.
     * @throws Exception when the test case fails.
     */
    public void testTimeoutWhenWaitingForAck() throws Exception {
        gotoAlertingAcceptingState();

        Thread.sleep(4000);

        assertCallDisconnectWhenError(true);
    }

    public void testTimeoutWhenWaitingForAckFailedState() throws Exception {
        gotoFailedWaitingForAckState();

        Thread.sleep(4000);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        assertCurrentState(ErrorCompletedInboundState.class);
    }

    public void testTimeoutLingeringBye() throws Exception {
        gotoFailedWaitingForAckState();

        // This prevents the phone to send response to the BYE request.
        simulatedPhone.disableResponses();

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);

        Thread.sleep(4000);

        simulatedSystem.assertEventReceived(FailedEvent.class, null);
        simulatedSystem.assertEventReceived(DisconnectedEvent.class, null);
        simulatedSystem.assertEventReceived(ErrorEvent.class, null);
        assertCurrentState(ErrorCompletedInboundState.class);
    }
}
