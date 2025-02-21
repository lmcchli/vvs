/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;

/**
 * Call Manager component test case to verify stopping play of media for outbound calls.
 * @author Malin Flodin
 */
public class StopPlayTest extends OutboundSipUnitCase {

    /**
     * Verifies that stop playing media in Connected state results in a
     * stop request on the outbound stream.
     * @throws Exception when the test case fails.
     */
    public void testStopPlayInConnectedState() throws Exception {
        gotoConnectedState();

        // System tries to stop play media
        simulatedSystem.stopPlay();
        simulatedSystem.waitForStopPlay(TIMEOUT_IN_MILLI_SECONDS);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

}

