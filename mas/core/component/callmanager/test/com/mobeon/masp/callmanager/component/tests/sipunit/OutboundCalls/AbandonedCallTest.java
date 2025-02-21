/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingEarlyMediaOutboundState;

import javax.sip.message.Response;

/**
 * Call Manager component test case to verify that abandoned outbound calls are
 * discovered and disconnected.
 * @author Malin Flodin
 */
public class AbandonedCallTest extends OutboundSipUnitCase {

    /**
     * Verifies that detecting an abandoned stream in
     * {@link ProgressingEarlyMediaOutboundState} results in the call being
     * disconnected.
     * @throws Exception when the test case fails.
     */
    public void testAbandonedStreamInProgressingEarlyMediaState()
            throws Exception {
        gotoProgressingEarlyMediaState();

        // System detects abandoned stream
        simulatedSystem.fireStreamAbandonedEvent();

        assertCallCanceled(true, false);
        simulatedPhone.sendResponse(Response.REQUEST_TERMINATED);

        assertCurrentConnectionStatistics(0);
        assertConnectedCallStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(0);
        assertDisconnectedCallStatistics(false, 0);
        assertDisconnectedCallStatistics(true, 0);
        assertAbandonedDisconnectedCallStatistics(0);
        assertAbandonedRejectedCallStatistics(1);
        assertTotalConnectionStatistics(1);
    }

    /**
     * Verifies that detecting an abandoned stream in
     * {@link ConnectedOutboundState} results in the call being disconnected.
     * @throws Exception when the test case fails.
     */
    public void testAbandonedStreamInConnectedState() throws Exception {
        gotoConnectedState();

        // System detects abandoned stream
        simulatedSystem.fireStreamAbandonedEvent();

        assertCallDisconnect(true, true, false);

        assertCurrentConnectionStatistics(0);
        assertConnectedCallStatistics(0);
        assertErrorCallStatistics(0);
        assertFailedCallStatistics(0);
        assertDisconnectedCallStatistics(false, 0);
        assertDisconnectedCallStatistics(true, 0);
        assertAbandonedDisconnectedCallStatistics(1);
        assertAbandonedRejectedCallStatistics(0);
        assertTotalConnectionStatistics(1);
    }
}
