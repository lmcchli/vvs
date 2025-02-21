/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;

import javax.sip.message.Request;

/**
 * Call Manager component test case to verify that abandoned inbound calls are
 * discovered and disconnected.
 * @author Malin Flodin
 */
public class AbandonedCallTest extends InboundSipUnitCase {


    public void testCallNotAcceptedInTime() throws Exception {
        ConfigurationReader.getInstance().getConfig().setCallNotAcceptedTimer(5000);

        Request invite = simulatedPhone.createRequest(Request.INVITE,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);
        simulatedPhone.sendInvite(invite);

        assertCallReceived();

        // Sleep instead of accepting call. Sleep enough time to let the
        // "Not Accepted" call timer expire.
        Thread.sleep(5000);

        assertCallIsDisconnected(false, true, false);

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
     * {@link ConnectedInboundState} results in the call being disconnected.
     * @throws Exception when the test case fails.
     */
    public void testAbandonedStreamInConnectedState() throws Exception {
        gotoConnectedState();

        // System detects abandoned stream
        simulatedSystem.fireStreamAbandonedEvent();

        assertCallDisconnect(true, true);

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
