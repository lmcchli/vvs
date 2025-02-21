/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;

/**
 * Call Manager component test case to verify stopping record of media for
 * inbound calls.
 * @author Malin Flodin
 */
public class StopRecordTest extends InboundSipUnitCase {

    /**
     * Verifies that stop recording media in Connected state results in a
     * stop request on the inbound stream.
     * @throws Exception when the test case fails.
     */
    public void testStopRecordInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to stop record of media
        simulatedSystem.stopRecord();
        simulatedSystem.waitForStopRecord(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

}
