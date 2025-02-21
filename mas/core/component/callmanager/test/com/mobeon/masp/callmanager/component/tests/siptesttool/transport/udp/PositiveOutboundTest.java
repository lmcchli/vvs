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
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;

/**
 * This class contains positive test cases of outbound calls using UDP.
 * @author Malin Nyfeldt
 */
public class PositiveOutboundTest extends TransportCase {

    private UdpConnection udpServer;

    private static String localTelephone = "1234";
    private static String remoteTelephone = "4321";

    /** Call properties that may be used for an outdial. */
    private final CallProperties callProperties =
            createCallProperties(localTelephone, remoteTelephone);


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

    public void testOutboundCallWherePhoneDisconnects() throws Exception  {

        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        // Wait for INVITE request
        RawSipMessage invite = waitForRequest("INVITE", udpServer);

        // Create and send 100 Trying response for invite
        RawSipMessage trying = RawSipMessageFactory.
                createTryingFromInvite(messageParameters, invite, null);
        udpServer.send(remotehost, remoteport, trying.getParsedMessage());

        String local_tag = trying.getToTag();

        // Create and send 180 Ringing response for invite
        RawSipMessage ringing = RawSipMessageFactory.
                createRingingFromInvite(messageParameters, invite, local_tag);
        udpServer.send(remotehost, remoteport, ringing.getParsedMessage());

        // Verify that the ringing is received by the system
        assertRingingReceivedBySystem();

        // Create and send 200 OK response for invite
        RawSipMessage ok = RawSipMessageFactory.
                createOkFromInvite(messageParameters, invite, local_tag);
        udpServer.send(remotehost, remoteport, ok.getParsedMessage());

        // Wait for ACK request
        RawSipMessage ack = waitForRequest("ACK", udpServer);

        // Assert call connected
        assertCallConnected();

        // Create and send BYE request
        RawSipMessage bye = RawSipMessageFactory.createByeFromAck(
                messageParameters, ack);
        bye.setParameter(RawSipMessageParameter.CSEQ, "2");
        bye.setParameter(RawSipMessageParameter.CALLED, remoteTelephone);
        bye.setParameter(RawSipMessageParameter.CALLING, localTelephone);
        udpServer.send(remotehost, remoteport, bye.getParsedMessage());

        // Wait for 200 response
        waitForResponse("200", "BYE", false, udpServer);

        // Assert call dicconnected
        assertCallDisconnected(null);
    }

    public void testInboundCallWhereSystemDisconnects() throws Exception  {
        // System creates an outbound call.
        simulatedSystem.createCall(callProperties);

        // Wait for INVITE request
        RawSipMessage invite = waitForRequest("INVITE", udpServer);

        // Create and send 100 Trying response for invite
        RawSipMessage trying = RawSipMessageFactory.
                createTryingFromInvite(messageParameters, invite, null);
        udpServer.send(remotehost, remoteport, trying.getParsedMessage());

        String local_tag = trying.getToTag();

        // Create and send 180 Ringing response for invite
        RawSipMessage ringing = RawSipMessageFactory.
                createRingingFromInvite(messageParameters, invite, local_tag);
        udpServer.send(remotehost, remoteport, ringing.getParsedMessage());

        // Verify that the ringing is received by the system
        assertRingingReceivedBySystem();

        // Create and send 200 OK response for invite
        RawSipMessage ok = RawSipMessageFactory.
                createOkFromInvite(messageParameters, invite, local_tag);
        udpServer.send(remotehost, remoteport, ok.getParsedMessage());

        // Wait for ACK request
        waitForRequest("ACK", udpServer);

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
        assertCallDisconnected(DisconnectedCompletedOutboundState.class);
    }


}
