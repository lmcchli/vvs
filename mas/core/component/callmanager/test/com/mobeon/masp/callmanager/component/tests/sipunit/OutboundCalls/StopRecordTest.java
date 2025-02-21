/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;

/**
 * Call Manager component test case to verify stopping recording of media for
 * outbound calls.
 * @author Malin Flodin
 */
public class StopRecordTest extends OutboundSipUnitCase {

    /**
     * Verifies that stop recording media in Connected state results in a
     * stop request on the outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testStopRecordInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to stop record media
        simulatedSystem.stopRecord();
        simulatedSystem.waitForStopRecord(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

}

