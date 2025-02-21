/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;

import javax.sip.message.Response;

/**
 * Call Manager component test case to verify sending video fast update
 * requests for inbound calls.
 * @author Malin Flodin
 */
public class VideoFastUpdateTest extends InboundSipUnitCase {

    /**
     * Verifies that a Video Fast Update request can be sent in connected state.
     * @throws Exception if test case fails.
     */
    public void testVideoFastUpdateRequestInConnectedState() throws Exception {
        gotoConnectedState();

        // System sends a Video Fast Update request
        simulatedSystem.initiateVideoFastUpdate();
        assertVideoFastUpdateRequestReceived(true, Response.OK);

        // Make sure the state is still Connected
        simulatedSystem.waitForState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that a Video Fast Update request responded with a Request Timeout
     * causes the call to be disconnected.
     * @throws Exception if test case fails.
     */
    public void testVideoFastUpdateRequestCausingRequestTimeout() throws Exception {
        gotoConnectedState();

        // System sends a Video Fast Update request
        simulatedSystem.initiateVideoFastUpdate();
        assertVideoFastUpdateRequestReceived(true, Response.REQUEST_TIMEOUT);

        assertCallDisconnect(true, true);
    }

    /**
     * Verifies that a Video Fast Update request responded with a Transaction
     * Does Not Exist causes the call to be disconnected.
     * @throws Exception if test case fails.
     */
    public void testVideoFastUpdateRequestCausingNoTransaction() throws Exception {
        gotoConnectedState();

        // System sends a Video Fast Update request
        simulatedSystem.initiateVideoFastUpdate();
        assertVideoFastUpdateRequestReceived(true, Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);

        assertCallDisconnect(true, true);
    }

    // TODO: A test case is missing:
    //  simPhone makes inbound call.
    //  simSystem makes outdial to simSecondPhone.
    //  join the calls.
    //  Send VFU from 2nd simPhone.
    //  Make sure the VFU is forwarded correctly and that a P-Charging-Vector
    //  is copied as well.

}
