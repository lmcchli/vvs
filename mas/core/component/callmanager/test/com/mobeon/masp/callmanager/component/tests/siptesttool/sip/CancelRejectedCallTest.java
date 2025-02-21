package com.mobeon.masp.callmanager.component.tests.siptesttool.sip;

import com.mobeon.masp.callmanager.component.tests.siptesttool.transport.TransportCase;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;

/**
 * This test case verifies that a SIP CANCEL request received to cancel an
 * INVITE that already has failed with a final response will receive a SIP 481
 * response. The test is built on top of a UDP transport.
 * @author Malin Nyfeldt
 */
public class CancelRejectedCallTest extends TransportCase {

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

    public static String cancelRequest =
            "CANCEL sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] CANCEL\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public void testCancelAlreadyRejectedInboundCall() throws Exception  {
        // Create and send INVITE request
        RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequest, messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        // Assert call received
        assertCallReceivedBySystem(udpServer);

        // System rejects the call.
        simulatedSystem.reject(null, null);

        // Wait for 200 response and skip other received responses
        RawSipMessage forbidden = waitForResponse("403", "INVITE", true, udpServer);

        // Create and send CANCEL request
        RawSipMessage cancel = new RawSipMessage(
                cancelRequest, messageParameters);
        cancel.setParameter(RawSipMessageParameter.BRANCH, forbidden.getBranchId());
        cancel.setParameter(RawSipMessageParameter.LOCAL_TAG, forbidden.getFromTag());
        cancel.setParameter(RawSipMessageParameter.CALL_ID, forbidden.getCallId());
        udpServer.send(remotehost, remoteport, cancel.getParsedMessage());

        // Wait for 481 response for CANCEL
        waitForResponse("481", "CANCEL", true, udpServer);

        // Create and send ACK request for INVITE
        RawSipMessage ack = RawSipMessageFactory.createAckFromNonOk(
                messageParameters, forbidden);
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());

        // Assert call completed
        simulatedSystem.waitForState(FailedCompletedInboundState.class);
        
    }


}
