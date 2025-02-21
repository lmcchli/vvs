/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.FileInputStream;

import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpFactory;

public class MessageTest extends TestCase {
    private final String path = "test/data/mrcpstack/messages/";
    FileInputStream input;

    public void setUp() throws Exception {
        super.setUp();
        MrcpRequest.reset();
        RtspRequest.reset();
    }

    public void testDefineGrammarRequest() throws Exception {
        input = new FileInputStream(path + "testDefineGrammarRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        String mimeType = "application/grammar+xml";
        String grammar = "<?xml version=\"1.0\"?>" + RtspMessage.nl +
                         "<grammar/>";
        String grammarId = "test:mobeon.com";
        DefineGrammarRequest m = new DefineGrammarRequest(mimeType, grammar, grammarId);
        m.setUrl("localhost.mobeon.com", 4712);
        assertNotNull(result);
        assertNotNull(result.getMrcpMessage());
        assertEquals(m.getMrcpMessage().getContent(), result.getMrcpMessage().getContent());
        assertEquals(m.getMrcpMessage().getMessage(), result.getMrcpMessage().getMessage());
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testPauseRequest() throws Exception {
        input = new FileInputStream(path + "testPauseRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        PauseRequest m = new PauseRequest();
        m.setUrl("localhost.mobeon.com", 4712);
        assertTrue(result != null);
        assertTrue(m.getMrcpMessage().getContent().equals(result.getMrcpMessage().getContent()));
        assertTrue(m.getMrcpMessage().getMessage().equals(result.getMrcpMessage().getMessage()));
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testRecognizeRequest() throws Exception {
        input = new FileInputStream(path + "testRecognizeRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        String mimeType = "application/grammar+xml";
        String grammar = "<?xml version=\"1.0\"?>" + RtspMessage.nl +
                         "<grammar/>";
        String grammarId = "test:mobeon.com";
        RecognizeRequest m = new RecognizeRequest(mimeType, grammar, grammarId);
        m.setUrl("localhost.mobeon.com", 4712);
        assertTrue(result != null);
        assertTrue(m.getMrcpMessage().getContent().equals(result.getMrcpMessage().getContent()));
        assertTrue(m.getMrcpMessage().getMessage().equals(result.getMrcpMessage().getMessage()));
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testResumeRequest() throws Exception {
        input = new FileInputStream(path + "testResumeRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        ResumeRequest m = new ResumeRequest();
        m.setUrl("localhost.mobeon.com", 4712);
        assertNotNull(result);
        assertEquals(m.getMrcpMessage().getContent(), result.getMrcpMessage().getContent());
        assertEquals(m.getMrcpMessage().getMessage(), result.getMrcpMessage().getMessage());
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testSetParamsRequest() throws Exception {
        input = new FileInputStream(path + "testSetParamsRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        SetParamsRequest m = new SetParamsRequest();
        m.setUrl("localhost.mobeon.com", 4712);
        assertNotNull(result);
        assertNotNull(m.getMrcpMessage().getContent());
        assertNotNull(m.getMrcpMessage().getMessage());
        assertEquals(m.getMrcpMessage().getContent(), result.getMrcpMessage().getContent());
        assertEquals(m.getMrcpMessage().getMessage(), result.getMrcpMessage().getMessage());
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testSetupRequest() throws Exception {
        input = new FileInputStream(path + "testSetupRequest.txt");
        // Read first expected result
        RtspMessage result1 = MessageParser.parse(input);
        // Build first message
        SetupRequest message1 = new SetupRequest(false, 4714);
        // Verify that the messages are the same
        message1.setUrl("localhost.mobeon.com", 4712);
        assertNotNull(result1);
        assertNull(message1.getMrcpMessage());
        assertEquals(message1.getMessage(), result1.getMessage());

        // Set up expected result 2
        RtspMessage result2 = MessageParser.parse(input);
        SetupRequest message2 = new SetupRequest(true, 4714);
        message2.setUrl("localhost.mobeon.com", 4712);
        assertNotNull(result2);
        assertNull(message2.getMrcpMessage());
        assertEquals(message2.getMessage(), result2.getMessage());

        // Setting up expected result 3
        RtspMessage result3 = MessageParser.parse(input);
        SetupRequest message3 = new SetupRequest(false, 4714);
        message3.setUrl("localhost.mobeon.com", 4712);
        String content = "" +
                "v=0" + RtspMessage.nl +
                "s=Nisses SDP" + RtspMessage.nl +
                "m=audio 4712 RTP/AVP 0" + RtspMessage.nl +
                "m=video 4714 RTP/AVP 31" + RtspMessage.nl
                + RtspMessage.nl;
        SdpFactory.setPathName("gov.nist");
        SdpFactory sdpFactory = SdpFactory.getInstance();
        SessionDescription sessionDescription =
                    sdpFactory.createSessionDescription(content);
        message3.setSDP(sessionDescription);
        assertNotNull(result3);
        assertNull(message3.getMrcpMessage());
        assertEquals(message3.getMessage(), result3.getMessage());
    }

    public void testSpeakRequest() throws Exception {
        input = new FileInputStream(path + "testSpeakRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        String mimeType = "application/text+ssml";
        String text = "<speak>Hello World!</speak>";
        SpeakRequest m = new SpeakRequest(mimeType, text);
        m.setUrl("localhost.mobeon.com", 4712);
        assertTrue(result != null);
        assertTrue(m.getMrcpMessage().getContent().equals(result.getMrcpMessage().getContent()));
        assertTrue(m.getMrcpMessage().getMessage().equals(result.getMrcpMessage().getMessage()));
        assertEquals(m.getMessage(), result.getMessage());
    }

    public void testStopRequest() throws Exception {
        input = new FileInputStream(path + "testStopRequest.txt");
        RtspMessage result = MessageParser.parse(input);
        StopRequest m = new StopRequest(false);
        m.setUrl("localhost.mobeon.com", 4712);
        assertTrue(result != null);
        assertTrue(m.getMrcpMessage().getContent().equals(result.getMrcpMessage().getContent()));
        assertTrue(m.getMrcpMessage().getMessage().equals(result.getMrcpMessage().getMessage()));
        assertTrue(m.getMessage().equals(result.getMessage()));
        result = MessageParser.parse(input);
        m = new StopRequest(true);
        m.setUrl("localhost.mobeon.com", 4712);
        assertTrue(result != null);
        assertTrue(m.getMrcpMessage().getContent().equals(result.getMrcpMessage().getContent()));
        assertTrue(m.getMrcpMessage().getMessage().equals(result.getMrcpMessage().getMessage()));
        assertEquals(m.getMessage(), result.getMessage());
    }
}
