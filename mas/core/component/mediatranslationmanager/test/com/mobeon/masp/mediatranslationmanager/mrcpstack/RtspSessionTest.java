/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import junit.framework.TestCase;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.SetupRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspResponse;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MrcpRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.xml.DOMConfigurator;

public class RtspSessionTest extends TestCase {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspSessionTest.class);
    private RtspSession rtspSession;
    private RtspConnectionMock rtspConnection;
    private RtspServerMock messageHandler;

    public void setUp() {
    	//used to initialize logger.
    }

    public void testSessionSendAndReceive() {
        setUp(new DummyServerMock());
        RtspRequest request = new SetupRequest(false, 4711);
        RtspResponse response = rtspSession.send(request);
        assertNotNull("Check non null response", response);
    }

    public void testSessionTearDown() {
        setUp(new DummyServerMock());
        // Attempt to tear down the session
        RtspRequest request = new RtspRequest("TEARDOWN", false);

        RtspResponse response = rtspSession.send(request);

        // In this case it is actually up to the RTSP Session to generate the response
        // due to the fact that ScanSoft closes the sessaion without issuing any response
        // to the request.
        assertNotNull("Check non null response", response);
        assertEquals("Check error", 200, response.getStatusCode());
        assertEquals("Check error", "OK", response.getStatusText());
    }

    /**
     * In this test we ensure that we receive the request response from the server side
     */
    public void testErrorResponse() {
        messageHandler = new RtspServerMock();

        RtspRequest.reset();
        MrcpRequest.reset();

        RtspRequest request = new RtspRequest("Nisse", false);
        request.setUrl("mockingbird.mobeon.com", 4711);
        RtspResponse expected = new RtspResponse(400, "Failure");
        messageHandler.setExpectedMessage(request);
        messageHandler.setSentMessage(expected);

        request = new RtspRequest("Apa", false);
        request.setUrl("mockingbird.mobeon.com", 4711);
        expected = new RtspResponse(500, "Disaster");
        messageHandler.setExpectedMessage(request);
        messageHandler.setSentMessage(expected);

        RtspRequest.reset();
        MrcpRequest.reset();

        setUp(messageHandler);

        request = new RtspRequest("Nisse", false);
        RtspResponse response = rtspSession.send(request);
        assertNotNull("Check non null response", response);
        assertEquals("Check error", 400, response.getStatusCode());
        assertEquals("Check error", "Failure", response.getStatusText());

        request = new RtspRequest("Apa", false);
        response = rtspSession.send(request);
        assertNotNull("Check non null response", response);
        assertEquals("Check error", 500, response.getStatusCode());
        assertEquals("Check error", "Disaster", response.getStatusText());
    }

    /**
     * This test verifies proper behavior when the server side does
     * not respond to a request.
     * @throws IOException
     */
    public void testSocketTimeout() throws IOException {
        ServerSocket server = null;
        Socket client = null;

        try {
            server = new ServerSocket(4711);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not create server socket.");
        }
        RtspConnection connection = new RtspConnectionImpl("localhost", 4711);
        RtspSession session = new RtspSession(connection);
        connection.open();
        try {
            client = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not get client socket.");
        }

        SetupRequest request = new SetupRequest(false, 23000);
        assertEquals(null, session.send(request));
    }

    private void setUp(ServerMock server) {
        rtspConnection = new RtspConnectionMock();
        rtspConnection.setMessageHandler(server);
        rtspSession = new RtspSession(rtspConnection);
    }

    public void tearDown() {
        if (rtspSession != null) rtspSession.stop();
        if (rtspConnection != null) rtspConnection.close();
    }
}
