/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.siptesttool.transport.tcp;

import com.mobeon.masp.callmanager.component.tests.siptesttool.transport.TransportCase;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpServer;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp.TcpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;

/**
 * This class contains test cases where multiple SIP messages are concatenated
 * in one single TCP packet.
 * @author Malin Nyfeldt
 */
public class ConcatenateMessagesTest extends TransportCase {

    private TcpServer tcpServer;
    private TcpConnection tcpClient;

    public void setUp() throws Exception {
        super.setUp();

        // Setup message parameters specific for this test case
        messageParameters.put(RawSipMessageParameter.TRANSPORT, "TCP");
        messageParameters.put(RawSipMessageParameter.CONTACT_TRANSPORT, "tcp");

        // Create a TCP server that listens for inbound connections
        tcpServer = TcpFactory.createTcpServer(localhost, localport);

        // Create TCP connection to CallManager.
        tcpClient = TcpFactory.createTcpClient(remotehost, remoteport, localhost);
    }

    public void tearDown() throws Exception {
        assertTrue(tcpServer.isAlive());
        assertTrue(tcpClient.isAlive());

        // Shutdown TCP server
        tcpServer.shutdown();
        assertFalse(tcpServer.isAlive());

        // Shutdown TCP client
        tcpClient.shutdown();
        assertFalse(tcpClient.isAlive());

        super.tearDown();
    }

    public void testInboundCallWhereTwoOptionsRequestsAreConcatenated()
            throws Exception  {
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

        // Create and send two OPTIONS request in one TCP packet
        RawSipMessage options = RawSipMessageFactory.createOptionsFromOk(
                messageParameters, ok);
        options.setParameter(RawSipMessageParameter.CSEQ, "2");
        String options1 = options.getParsedMessage();
        options = RawSipMessageFactory.createOptionsFromOk(
                messageParameters, ok);
        options.setParameter(RawSipMessageParameter.CSEQ, "3");
        String options2 = options.getParsedMessage();
        tcpClient.send(options1 + options2);

        // Wait for two 200 response for OPTIONS
        RawSipMessage okForOptions =
                waitForResponse("200", "OPTIONS", false, tcpClient);
        assertEquals("2", okForOptions.getCSeq());
        okForOptions = waitForResponse("200", "OPTIONS", false, tcpClient);
        assertEquals("3", okForOptions.getCSeq());

        // Create and send BYE request
        RawSipMessage bye = RawSipMessageFactory.createByeFromOk(
                messageParameters, ok);
        bye.setParameter(RawSipMessageParameter.CSEQ, "4");
        tcpClient.send(bye.getParsedMessage());

        // Wait for 200 response
        waitForResponse("200", "BYE", false, tcpClient);

        // Assert call dicconnected
        assertCallDisconnected(null);
    }

}
