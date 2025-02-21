/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.InboundCalls;

import org.cafesip.sipunit.SipTransaction;
import org.jmock.Mock;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;
import javax.sip.Dialog;

import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState;
import com.mobeon.masp.callmanager.callhandling.OutboundCallImpl;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.events.JoinedEvent;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * Call Manager component test case to verify INFO requests for inbound calls.
 *
 * @author Malin Nyfeldt
 */
public class InfoTest extends InboundSipUnitCase {

    OutboundCallImpl anOutboundCall;
    Mock dialogMock = mock(Dialog.class);

    public void setUp() throws Exception {
        super.setUp();
        anOutboundCall =
                new OutboundCallImpl(callProperties, null, null, null, null,
                        ConfigurationReader.getInstance().getConfig());

        anOutboundCall.setStateProgressing(ProgressingOutboundState.ProgressingSubState.EARLY_MEDIA);
        anOutboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anOutboundCall.setOutboundStream(simulatedSystem.getOutboundStream());

        dialogMock.stubs().method("getDialogId").will(returnValue("dialogid"));
        anOutboundCall.setDialog((Dialog)dialogMock.proxy());
        anOutboundCall.setInitialDialogId("initialDialogId");
    }

    /**
     * Verifies that SIP INFO request in Accepting state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInAcceptingState()
            throws Exception {
        gotoAlertingAcceptingState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);

        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(AlertingAcceptingInboundState.class);

        // Phone sends ACK
        simulatedPhone.acknowledge(
                PhoneSimulator.WITHIN_DIALOG, PhoneSimulator.NO_BODY, false);
        assertCallConnected();

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request in Connected state when the call is NOT
     * joined results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInConnectedStateWhenNotJoined()
            throws Exception
    {
        gotoConnectedState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }


    /**
     * Verifies that SIP INFO request containing VFU in Connected state when
     * the call is joined results in the SIP INFO being forwarded.
     * @throws Exception when the test case fails.
     */
    public void testVFUInfoInConnectedStateWhenJoined()
            throws Exception
    {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        dialogMock.expects(once()).method("createRequest").with(eq(Request.INFO));
        simulatedPhone.sendInfo(true, true);

        // Wait for the SIP INFO request to be received
        Thread.sleep(500);

        // System disconnects the call
        simulatedSystem.disconnect();
        assertCallDisconnect(true, true);
    }


    /**
     * Verifies that SIP INFO request containing no VFU in Connected state when
     * the call is joined results in the SIP INFO being rejected with a
     * SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testOtherInfoInConnectedStateWhenJoined()
            throws Exception
    {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anOutboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        dialogMock.expects(never()).method("createRequest");
        SipTransaction transaction = simulatedPhone.sendInfo(true, false);

        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ConnectedInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }


    /**
     * Verifies that SIP INFO request in Linger state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoDisconnectedLingeringByeState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(DisconnectedLingeringByeInboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
