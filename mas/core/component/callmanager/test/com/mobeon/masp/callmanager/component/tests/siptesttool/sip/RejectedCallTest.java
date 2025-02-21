package com.mobeon.masp.callmanager.component.tests.siptesttool.sip;

import com.mobeon.masp.callmanager.component.tests.siptesttool.transport.TransportCase;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpConnection;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp.UdpFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessage;
import com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.utilities.SipToolConstants;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent;

/**
 * These test cases verifies that a call can be rejected by MAS 
 * before the call is setup. One test with early media and one without.
 * 
 * These tests was created to debug a problem with the SIP stack where a call
 * rejected during the early media phase caused a leak of a dialog in the stack.
 * Refer to TR 31843.
 * 
 */
public class RejectedCallTest extends TransportCase {

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

        // DEBUG: Wait 4 minutes to manually verify that the the dialog
        // is removed in the SIP stack:
        //Thread.sleep(4*60*1000);

        super.tearDown();
        
    }

    /**
     * This TC verifies that a call can be rejected by the MAS before the call
     * has been accepted.
     * 
     */
    public void testRejectedInboundCall() throws Exception  {
        // Create and send INVITE request

        RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequest, messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        log.info("*** Invite sent: \n" + inviteRequest.getParsedMessage());
        
        // Assert call received
        assertCallReceivedBySystem(udpServer);

        log.info("*** Rejecting call");
        // System rejects the call.
        simulatedSystem.reject(null, null);

        log.info("*** Waiting for 403");
        // Wait for 403 response and skip other received responses
        RawSipMessage forbidden = waitForResponse("403", "INVITE", true, udpServer);

        log.info("*** Receieved 403: \n" + forbidden.getParsedMessage());

        // Create and send ACK request for INVITE
        RawSipMessage ack = RawSipMessageFactory.createAckFromNonOk(
                messageParameters, forbidden);
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());
        log.info("*** ACK sent: \n" +ack.getParsedMessage());

        log.info("*** Wait for call completed");
        // Assert call completed
        simulatedSystem.waitForState(FailedCompletedInboundState.class);
        log.info("*** TC finished");

    }

    /**
     * This TC verifies that a call can be rejected during early media.
     * 
     * This test recreates the scenario where the problem described in TR 31843
     * occurs. It does not verify the fault in the SIP stack by itself 
     * but it can be manually tested by performing the following steps:
     * 1. Uncomment the delay in the end of the tearDown method in this file.
     * 2. Run this test case.
     * 3. When the TC has finished verify in the logs that the SIP stack auditor
     * did not need to remove any leaked dialogs. (INFO log level).
     * 
     */
    public void testRejectedInboundCallEarlyMedia() throws Exception  {

    	// Create and send INVITE request

    	RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequestIndicatingEarlyMedia,
                messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        log.info("*** Invite sent: \n" + inviteRequest.getParsedMessage());
        
        // Assert call received
        assertCallReceivedBySystem(udpServer);

        log.info("*** Negotiate early media");
        simulatedSystem.negotiateEarlyMediaTypes();

        log.info("*** Waiting for 183");
        RawSipMessage provisionalResponse = waitForResponse("183", "INVITE", true, udpServer);
        log.info("*** 183 received: \n"+provisionalResponse.getParsedMessage());
        
        
    	RawSipMessage prackRequest = RawSipMessageFactory.
    		createPrackFromProvisionalResponse(messageParameters, provisionalResponse);
    	// Add 1 to CSeq since this will be the second transaction for this session
        prackRequest.setParameter(RawSipMessageParameter.CSEQ, "2");
        log.info("*** Sending PRACK:\n" + prackRequest.getParsedMessage());
        udpServer.send(remotehost, remoteport, prackRequest.getParsedMessage());
        
        simulatedSystem.assertEventReceived(EarlyMediaAvailableEvent.class, null);
        
        RawSipMessage prackResponse = waitForResponse("200", "PRACK", true, udpServer);
        log.info("*** 200 for PRACK received: \n"+prackResponse.getParsedMessage());
        
        log.info("*** Rejecting call");
        // System rejects the call.
        simulatedSystem.reject(null, null);

        log.info("*** Waiting for 403");
        // Wait for 403 response and skip other received responses
        RawSipMessage forbidden = waitForResponse("403", "INVITE", true, udpServer);

        log.info("*** Receieved 403: \n" + forbidden.getParsedMessage());

        // Create and send ACK request for INVITE
        RawSipMessage ack = RawSipMessageFactory.createAckFromNonOk(
                messageParameters, forbidden);
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());
        log.info("*** ACK sent: \n" +ack.getParsedMessage());

        log.info("*** Wait for call completed");
        // Assert call completed
        simulatedSystem.waitForState(FailedCompletedInboundState.class);
        log.info("*** TC finished");

        


    }

    /**
     * This TC performs a normal disconnect using BYE after the call is accepted.
     * @throws Exception
     */
    public void testDisconnectedInboundCallEarlyMedia() throws Exception  {

    	// Create and send INVITE request

    	RawSipMessage inviteRequest = new RawSipMessage(
                SipToolConstants.inviteRequestIndicatingEarlyMedia,
                messageParameters);
        udpServer.send(remotehost, remoteport, inviteRequest.getParsedMessage());

        log.info("*** Invite sent: \n" + inviteRequest.getParsedMessage());
        
        // Assert call received
        assertCallReceivedBySystem(udpServer);

        log.info("*** Negotiate early media");
        simulatedSystem.negotiateEarlyMediaTypes();

        log.info("*** Waiting for 183");
        RawSipMessage provisionalResponse = waitForResponse("183", "INVITE", true, udpServer);
        log.info("*** 183 received: \n"+provisionalResponse.getParsedMessage());
        
        
    	RawSipMessage prackRequest = RawSipMessageFactory.
    		createPrackFromProvisionalResponse(messageParameters, provisionalResponse);
    	// Add 1 to CSeq since this will be the second transaction for this session
        prackRequest.setParameter(RawSipMessageParameter.CSEQ, "2");
        log.info("*** Sending PRACK:\n" + prackRequest.getParsedMessage());
        udpServer.send(remotehost, remoteport, prackRequest.getParsedMessage());
        
        simulatedSystem.assertEventReceived(EarlyMediaAvailableEvent.class, null);
        
        RawSipMessage prackResponse = waitForResponse("200", "PRACK", true, udpServer);
        log.info("*** 200 for PRACK received: \n"+prackResponse.getParsedMessage());
        
        log.info("*** Accepting call");
        simulatedSystem.accept();

        log.info("*** Waiting for 200");
        RawSipMessage ok = waitForResponse("200", "INVITE", true, udpServer);

        log.info("*** Receieved 200: \n" + ok.getParsedMessage());

        // Create and send ACK request for INVITE (CSeq=1 since we are ack'ing the OK for INVITE (not PRACK)
        RawSipMessage ack = RawSipMessageFactory.createAckFromOk(
                messageParameters, ok);
        ack.setParameter(RawSipMessageParameter.CSEQ, "1");
        
        udpServer.send(remotehost, remoteport, ack.getParsedMessage());
        log.info("*** ACK sent: \n" +ack.getParsedMessage());

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

        // Assert call disconnected
        assertCallDisconnected(DisconnectedCompletedInboundState.class);

        log.info("*** TC finished");

    }

}
