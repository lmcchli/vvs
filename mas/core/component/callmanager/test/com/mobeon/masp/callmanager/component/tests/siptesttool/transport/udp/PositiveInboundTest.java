/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.siptesttool.transport.udp;

import com.mobeon.masp.callmanager.component.tests.siptesttool.transport.TransportCase;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;

/**
 * This class contains positive test cases of inbound calls using UDP.
 * @author Malin Nyfeldt
 */
public class PositiveInboundTest extends TransportCase {

    private UdpConnection udpServer;

    public void setUp() throws Exception {
        super.setUp();

        // Setup message parameters specific for this test case
        messageParameters.put(RawSipMessageParameter.TRANSPORT, "UDP");
        messageParameters.put(RawSipMessageParameter.CONTACT_TRANSPORT, "udp");

        // Create a UDP server that listens for inbound messages
        udpServer = UdpFactory.createUdpServer(localhost, localport);
    }

    public void tearDown() throws Exception {
        assertTrue(udpServer.isAlive());

        // Shutdown UDP server
        udpServer.shutdown();
        assertFalse(udpServer.isAlive());

        super.tearDown();
    }

    public void testInboundCallWherePhoneDisconnects() throws Exception  {
        // Create and send INVITE request
        RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequest, messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        // Assert call received
        assertCallReceivedBySystem(udpServer);

        // System accepts the call.
        simulatedSystem.accept();

        // Wait for 200 response and skip other received responses
        RawSipMessage ok = waitForResponse("200", "INVITE", true, udpServer);

        // Create and send ACK request
        RawSipMessage ack = RawSipMessageFactory.createAckFromOk(
                messageParameters, ok);
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());

        // Assert call connected
        assertCallConnected();

        // Create and send BYE request
        RawSipMessage bye = RawSipMessageFactory.createByeFromOk(
                messageParameters, ok);
        bye.setParameter(RawSipMessageParameter.CSEQ, "2");
        udpServer.send(remotehost, remoteport, bye.getParsedMessage());

        // Wait for 200 response
        waitForResponse("200", "BYE", false, udpServer);

        // Assert call dicconnected
        assertCallDisconnected(null);
    }

    public void testInboundCallWhereSystemDisconnects() throws Exception  {
        // Create and send INVITE request
        RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequest, messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        // Assert call received
        assertCallReceivedBySystem(udpServer);

        // System accepts the call.
        simulatedSystem.accept();

        // Wait for 200 response and skip other received responses
        RawSipMessage ok = waitForResponse("200", "INVITE", true, udpServer);

        // Create and send ACK request
        RawSipMessage ack = RawSipMessageFactory.createAckFromOk(
                messageParameters, ok);
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());

        // Assert call connected
        assertCallConnected();

        // System disconnects.
        simulatedSystem.disconnect();

        // Wait for BYE request
        RawSipMessage bye = waitForRequest("BYE", udpServer);

        // Create and send OK response for bye
        RawSipMessage okForBye =
                RawSipMessageFactory.createOkFromBye(messageParameters, bye);
        udpServer.send(remotehost, remoteport, okForBye.getParsedMessage());

        // Assert call dicconnected
        assertCallDisconnected(DisconnectedCompletedInboundState.class);
    }
}
