/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.OutboundCalls;

import org.cafesip.sipunit.SipTransaction;
import org.jmock.Mock;

import javax.sip.message.Response;
import javax.sip.message.Request;
import javax.sip.RequestEvent;
import javax.sip.Dialog;

import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingEarlyMediaOutboundState;
import com.mobeon.masp.callmanager.callhandling.InboundCallImpl;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.events.JoinedEvent;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;

/**
 * Call Manager component test case to verify INFO requests for outbound calls.
 *
 * @author Malin Nyfeldt
 */
public class InfoTest extends OutboundSipUnitCase {

    InboundCallImpl anInboundCall;
    Mock dialogMock = mock(Dialog.class);
    Request request = null;

    String infoMsg =
            "INFO sip:invalid@10.16.2.97;transport=udp SIP/2.0\r\n" +
            "Call-ID: c4c505b37dbe6d0d52c9e5e09720e270@10.16.2.97\r\n" +
            "From: <sip:sipPhone@localhost>;tag=1843543946\r\n" +
            "To: \"Anonymous\" <sip:invalid@10.16.2.97>;tag=525651165\r\n" +
            "CSeq: 1 INFO\r\n" +
            "Contact: <sip:mas@10.16.2.97:5060>\r\n" +
            "Max-Forwards: 70\r\n" + 
            "Route: <sip:localhost:5090>\r\n" +
            "Content-Type: application/media_control+xml\r\n" +
            "Content-Length: 204\r\n" +
            "\r\n" +
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
            "<media_control xmlns=\"urn:ietf:params:xml:ns:media_control\">\r\n" +
            "<vc_primitive>\r\n" +
            "<to_encoder>\r\n" +
            "<picture_fast_update/>\r\n" +
            "</to_encoder>\r\n" +
            "</vc_primitive>\r\n" +
            "</media_control>";

    // The StringMsgParser comes from the NIST SIP implementation and is not
    // part of the JAIN SIP interface. It is used for testing purposes only.
    private static final StringMsgParser stringMsgParser = new StringMsgParser();

    public void setUp() throws Exception {
        super.setUp();
        anInboundCall = simulatedSystem.createInboundCall();
        anInboundCall.setStateConnected();
        anInboundCall.setInboundStream(simulatedSystem.getInboundStream());
        anInboundCall.setOutboundStream(simulatedSystem.getOutboundStream());
        dialogMock.stubs().method("getDialogId").will(returnValue("dialogid"));
        anInboundCall.setDialog((Dialog)dialogMock.proxy());
        CMUtils.getInstance().getCallDispatcher().removeCall(
                anInboundCall.getInitialDialogId(),
                anInboundCall.getEstablishedDialogId());

        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(infoMsg);
       request = (Request)sipMessage;
    }

    /**
     * Verifies that a SIP INFO request in Progressing-Calling
     * state results in a SIP Call/Transaction Does Not Exist response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInProgressingCallingState()
            throws Exception
    {
        gotoProgressingCallingState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Call/Transaction does not exist response
        simulatedPhone.assertResponseReceived(transaction,
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, Request.INFO);

        assertCurrentState(ProgressingCallingOutboundState.class);

        // Phone sends ringing
        simulatedPhone.ring();
        assertPhoneRinging();

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request in Progressing-Proceeding
     * state results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInProgressingProceedingState()
            throws Exception
    {
        gotoProgressingProceedingState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ProgressingProceedingOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request in Progressing-Early Media
     * state results in a SIP Method Not Allowed response when the call is
     * not joined.
     * @throws Exception when the test case fails.
     */
    public void testInfoInProgressingEarlyMediaStateWhenNotJoined()
            throws Exception
    {
        gotoProgressingEarlyMediaState();

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ProgressingEarlyMediaOutboundState.class);

        // Phone sends ok
        simulatedPhone.acceptCall(null);
        assertCallAccepted(simulatedPhone);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request containing VFU in Progressing-Early Media
     * state when the call is joined results in the SIP INFO being forwarded.
     * @throws Exception when the test case fails.
     */
    public void testVFUInfoInProgressingEarlyMediaStateWhenJoined()
            throws Exception
    {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        dialogMock.expects(once()).method("createRequest").
                with(eq(Request.INFO)).will(returnValue(request));
        dialogMock.expects(once()).method("sendRequest");
        simulatedPhone.sendInfo(true, true);

        // Wait for the SIP INFO request to be received
        Thread.sleep(500);

        assertCurrentState(ProgressingEarlyMediaOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that SIP INFO request containing no VFU in
     * Progressing-Early Media state when the call is joined results in the
     * SIP INFO being rejected with a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testOtherInfoInProgressingEarlyMediaStateWhenJoined()
            throws Exception
    {
        gotoProgressingEarlyMediaState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        SipTransaction transaction = simulatedPhone.sendInfo(true, false);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ProgressingEarlyMediaOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, false);
    }

    /**
     * Verifies that SIP INFO request in Connected state
     * results in a SIP Method Not Allowed response when the call is not joined.
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

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request containing VFU in Connected
     * state when the call is joined results in the SIP INFO being forwarded.
     * @throws Exception when the test case fails.
     */
    public void testVFUInfoInConnectedStateWhenJoined() throws Exception
    {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        dialogMock.expects(once()).method("createRequest").
                with(eq(Request.INFO)).will(returnValue(request));
        dialogMock.expects(once()).method("sendRequest");
        simulatedPhone.sendInfo(true, true);

        // Wait for the SIP INFO request to be received
        Thread.sleep(500);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP INFO request containing no VFU in
     * Connected state when the call is joined results in the
     * SIP INFO being rejected with a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testOtherInfoInConnectedStateWhenJoined()
            throws Exception
    {
        gotoConnectedState();

        // System joins the call.
        simulatedSystem.join(anInboundCall, SUCCEED);
        simulatedSystem.assertEventReceived(JoinedEvent.class, null);

        SipTransaction transaction = simulatedPhone.sendInfo(true, false);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(ConnectedOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(false, FAR_END, true);
    }

    /**
     * Verifies that SIP IFO request in Linger state
     * results in a SIP Method Not Allowed response.
     * @throws Exception when the test case fails.
     */
    public void testInfoInLingerState() throws Exception {
        RequestEvent byeRequestEvent = gotoLingerState(false);

        SipTransaction transaction = simulatedPhone.sendInfo(true, true);
        // Wait for Method Not Allowed response
        simulatedPhone.assertResponseReceived(transaction,
                Response.METHOD_NOT_ALLOWED, Request.INFO);

        assertCurrentState(DisconnectedLingeringByeOutboundState.class);

        // Phone disconnects the call
        simulatedPhone.disconnect(false);
        assertCallIsDisconnected(true, NEAR_END, true);

        sendOkForBye(byeRequestEvent, true);
    }
}
