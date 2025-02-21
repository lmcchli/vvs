/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import java.io.FileInputStream;

import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;

public class MessageParserTest extends TestCase {
    private final String path = "test/data/mrcpstack/messages/";
    FileInputStream input;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testRtspRequest() throws Exception {
        input = new FileInputStream(path + "testRtspRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        assertTrue(result != null);
        assertTrue(result.getMessageType() == MessageType.RTSP_REQUEST);
        assertTrue(result.getMrcpMessage() == null);
        RtspRequest req = new RtspRequest("OPTIONS", "*");
        assertTrue(req.getMessage().equals(result.getMessage()));
    }
    public void testRtspResponse() throws Exception {
        input = new FileInputStream(path + "testRtspResponse.txt");
        RtspMessage result = MessageParser.parse(input);
        assertTrue(result != null);
        assertTrue(result.getMessageType() == MessageType.RTSP_RESPONSE);
        assertTrue(result.getMrcpMessage() == null);
        RtspResponse resp = new RtspResponse(200, "OK");
        assertTrue(resp.getMessage().equals(result.getMessage()));
    }
    public void testMrcpRequest() throws Exception {
        input = new FileInputStream(path + "testMrcpRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        assertTrue(result != null);
        assertTrue(result.getMessageType() == MessageType.RTSP_REQUEST);
        assertTrue(result.getMrcpMessage() != null);
        assertTrue(result.getMrcpMessage().getMessageType() == MessageType.MRCP_REQUEST);
        MrcpRequest req = new MrcpRequest("RECOGNIZE", 4711);
        assertTrue(req.getMessage().equals(result.getMrcpMessage().getMessage()));
    }
    public void testMrcpResponse() throws Exception {
        input = new FileInputStream(path + "testMrcpResponse.txt");
        RtspMessage result = MessageParser.parse(input);
        assertNotNull(result);
        assertEquals(result.getMessageType(), MessageType.RTSP_REQUEST);
        assertNotNull(result.getMrcpMessage());
        assertEquals(result.getMrcpMessage().getMessageType(), MessageType.MRCP_RESPONSE);
        MrcpResponse resp = new MrcpResponse(4711, 200, "COMPLETE");
        assertEquals(resp.getRequestState(), ((MrcpResponse)result.getMrcpMessage()).getRequestState());
        assertEquals(resp.getRequestId(), ((MrcpResponse)result.getMrcpMessage()).getRequestId());
        assertEquals(resp.getStatusCode(), ((MrcpResponse)result.getMrcpMessage()).getStatusCode());
        assertEquals(resp.getMessage(), result.getMrcpMessage().getMessage());
    }
    public void testMrcpEvent() throws Exception {
        input = new FileInputStream(path + "testMrcpEvent.txt");
        RtspMessage result = MessageParser.parse(input);
        assertNotNull(result);
        assertEquals(result.getMessageType(), MessageType.RTSP_REQUEST);
        assertNotNull(result.getMrcpMessage());
        assertEquals(result.getMrcpMessage().getMessageType(), MessageType.MRCP_EVENT);
        MrcpEvent event = new MrcpEvent("START-OF-SPEECH", 4711, "IN-PROGRESS");
        assertEquals(event.getRequestState(), ((MrcpEvent)result.getMrcpMessage()).getRequestState());
        assertEquals(event.getMessage(), result.getMrcpMessage().getMessage());
    }
}
