/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.siptesttool.transport.mixed;

import com.mobeon.masp.callmanager.component.tests.siptesttool.transport.TransportCase;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpServer;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;

/**
 * This class contains positive test cases of inbound calls using UDP and TCP
 * mixed within a call.
 * @author Malin Nyfeldt
 */
public class PositiveInboundTest extends TransportCase {

    private TcpServer tcpServer;
    private TcpConnection tcpClient;
    private UdpConnection udpServer;

    public void setUp() throws Exception {
        super.setUp();

        // Create a TCP server that listens for inbound connections
        tcpServer = TcpFactory.createTcpServer(localhost, localport);

        // Create TCP connection to CallManager.
        tcpClient = TcpFactory.createTcpClient(remotehost, remoteport, localhost);

        // Create a UDP server that listens for inbound messages
        udpServer = UdpFactory.createUdpServer(localhost, localport);
    }

    public void tearDown() throws Exception {
        assertTrue(tcpServer.isAlive());
        assertTrue(tcpClient.isAlive());
        assertTrue(udpServer.isAlive());

        // Shutdown TCP server
        tcpServer.shutdown();
        assertFalse(tcpServer.isAlive());

        // Shutdown TCP client
        tcpClient.shutdown();
        assertFalse(tcpClient.isAlive());

        // Shutdown UDP server
        udpServer.shutdown();
        assertFalse(udpServer.isAlive());

        super.tearDown();
    }

    public void testInboundTcpCallWherePhoneDisconnectsOverUdp() throws Exception  {

        // Setup message parameters specific for this test case
        messageParameters.put(RawSipMessageParameter.TRANSPORT, "TCP");
        messageParameters.put(RawSipMessageParameter.CONTACT_TRANSPORT, "tcp");

        // Create and send INVITE request
        RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequest, messageParameters);
        tcpClient.send(inviteRequest.getParsedMessage());

        // Assert call received
        assertCallReceivedBySystem(tcpClient);

        // System accepts the call.
        simulatedSystem.accept();

        // Wait for 200 response and skip other received responses
        RawSipMessage ok = waitForResponse("200", "INVITE", true, tcpClient);

        // Create and send ACK request
        RawSipMessage ack = RawSipMessageFactory.createAckFromOk(
                messageParameters, ok);
        tcpClient.send(ack.getParsedMessage());

        // Assert call connected
        assertCallConnected();

        // Create and send BYE request over UDP
        RawSipMessage bye = RawSipMessageFactory.createByeFromOk(
                messageParameters, ok);
        bye.setParameter(RawSipMessageParameter.CSEQ, "2");
        udpServer.send(remotehost, remoteport, bye.getParsedMessage());

        // Wait for 200 response
        waitForResponse("200", "BYE", false, udpServer);

        // Assert call dicconnected
        assertCallDisconnected(null);
    }

    // NOTE:
    //      The below test case cannot be created, since the SIP stack
    //      always sends a BYE request over TCP if the INVITE was received
    //      over TCP. Therefore it has been left empty.
    public void testInboundTcpCallWhereSystemDisconnectsOverUdp() throws Exception  {
    }

    public void testInboundUdpCallWherePhoneDisconnectsOverTcp() throws Exception  {

        // Setup message parameters specific for this test case
        messageParameters.put(RawSipMessageParameter.TRANSPORT, "UDP");
        messageParameters.put(RawSipMessageParameter.CONTACT_TRANSPORT, "udp");

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

        // Create and send BYE request over TCP
        RawSipMessage bye = RawSipMessageFactory.createByeFromOk(
                messageParameters, ok);
        bye.setParameter(RawSipMessageParameter.CSEQ, "2");
        tcpClient.send(bye.getParsedMessage());

        // Wait for 200 response
        waitForResponse("200", "BYE", false, tcpClient);

        // Assert call dicconnected
        assertCallDisconnected(null);
    }

    // NOTE:
    //      The below test case cannot be created, since the SIP stack
    //      always sends a BYE request over UDP if the INVITE was received
    //      over UDP. Therefore it has been left empty.
    public void testInboundUdpCallWhereSystemDisconnectsOverTcp() throws Exception  {
    }

}
