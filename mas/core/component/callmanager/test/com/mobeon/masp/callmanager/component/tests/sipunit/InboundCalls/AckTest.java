/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedWaitingForAckInboundState;

/**
 * Call Manager component test case to verify ACK of inbound calls.
 * @author Malin Flodin
 */
public class AckTest extends InboundSipUnitCase {

    /**
     * Verifies that ACK to an inbound call in Accepting state results in a
     * ConnectedEvent and state Connected.
     * @throws Exception when the test case fails.
     */
    public void testNormalAcknowledge() throws Exception {
        gotoAlertingAcceptingState();

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that ACK to an inbound call in
     * {@link FailedWaitingForAckInboundState} (when a disconnect is
     * pending) results in a SIP BYE request being send and state set to
     * {@link FailedLingeringByeInboundState}.
     * @throws Exception when the test case fails.
     */
    public void testAckInFailedWaitingForAckState() throws Exception {
        gotoFailedWaitingForAckState();

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);

        assertCallDisconnect(true, false);
    }

}
