/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import com.mobeon.masp.stream.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.RtspConnectionFactoryMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.RtspServerMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpException;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class MrcpSpeechRecognizerTest extends MockObjectTestCase {
    private MediaTranslationManagerFacade mtm = null;
    String grammarId = "mobeon.com";
    String grammar = "<?xml version=\"1.0\"?>" +
                "<grammar xml:lang=\"en-GB\" version=\"1.0\" root=\"ROOT\">" +
                "<rule id=\"ROOT\" scope=\"public\">" +
                "<item><ruleref uri=\"#words\"/></item>" +
                "</rule>" +
                "<rule id=\"words\">" +
                "<one-of>" +
                "<item> one </item>" +
                "<item> two </item>" +
                "<item> three </item>" +
                "<item> invalid </item>" +
                "<item> nice </item>" +
                "</one-of>" +
                "</rule>" +
                "</grammar>";
    Mock mockStreamFactory;
    Mock mockOutboundStream;
    Mock mockInboundStream;
    Mock mockMediaObject;
    Mock mockEventDispatcher;
    RtspConnectionFactoryMock rtspConnectionFactoryMock;
    RtspServerMock rtspServerMock = new RtspServerMock();
    ISession session;
    int counter = 0;
    final int maxCount = 10000;

    public void setUp() {
        Utility.getSingleton().initialize("test/TestComponentConfig.xml");
        mtm = Utility.getSingleton().getMediaTranslationManager(Utility.getSingleton().getSession());

        RtspRequest.reset();
        MrcpRequest.reset();

        mockStreamFactory = mock(IStreamFactory.class);
        mockOutboundStream = mock(IOutboundMediaStream.class);
        mockInboundStream = mock(IInboundMediaStream.class);
        mockMediaObject = mock(IMediaObject.class);
        mockEventDispatcher = mock(IEventDispatcher.class);

        mtm.setStreamFactory((IStreamFactory)mockStreamFactory.proxy());
        rtspConnectionFactoryMock = new RtspConnectionFactoryMock();
        MediaTranslationFactory.getInstance().setRtspConnectionFactory(rtspConnectionFactoryMock);
        rtspConnectionFactoryMock.setMessageHandler(rtspServerMock);
        session = Utility.getSingleton().getSession();
    }

    public void testGetRecognizerWithGrammar() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put("apa", "srgml");
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        assertEquals(grammars, rec.getGrammars());
        assertEquals(session, rec.getSession());
    }

    public void testGetRecognizerFromTemplate() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put("apa", "srgml");
        SpeechRecognizer template = mtm.getSpeechRecognizer(session, grammars);
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, template);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        assertEquals(grammars, rec.getGrammars());
        assertEquals(session, rec.getSession());
    }

    public void testPrepare() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, "srgml");
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        setupPrepare();
        RtspRequest.reset();
        MrcpRequest.reset();
        speechRecognizer.prepare();
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARING, rec.getServiceState());
        // TODO: fix hang problem
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.PREPARING) sleep(1);
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARED, rec.getServiceState());
    }

    public void testRecognize() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, "srgml");
        setupPrepare();
        setupRecognize();
        setupRecognizeComplete(true);
        RtspRequest.reset();
        MrcpRequest.reset();
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        speechRecognizer.recognize((IInboundMediaStream)mockInboundStream.proxy());

        counter = 0;
        while (rec.getServiceState() != MrcpSpeechRecognizer.ServiceState.RECOGNIZING) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }

        counter = 0;
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.RECOGNIZING) {
            assertTrue(counter++ < maxCount*10);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.RECOGNIZED, rec.getServiceState());
    }

    public void testPreparedCancel() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, "srgml");
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        setupPrepare();
        setupCancel();
        RtspRequest.reset();
        MrcpRequest.reset();
        speechRecognizer.prepare();
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARING, rec.getServiceState());
        counter = 0;
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.PREPARING) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARED, rec.getServiceState());

        speechRecognizer.cancel();
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARED, rec.getServiceState());
        counter = 0;
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.PREPARED) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.IDLE, rec.getServiceState());
        assertNull(rec.getGrammars());
        assertNull(rec.getSession());
    }

    public void testPreparedRecognize() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, "srgml");
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        setupPrepare();
        setupRecognize();
        setupRecognizeComplete(true);
        RtspRequest.reset();
        MrcpRequest.reset();
        speechRecognizer.prepare();
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARING, rec.getServiceState());
        counter = 0;
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.PREPARING) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.PREPARED, rec.getServiceState());
        speechRecognizer.recognize((IInboundMediaStream)mockInboundStream.proxy());

        counter = 0;
        while (rec.getServiceState() != MrcpSpeechRecognizer.ServiceState.RECOGNIZED) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.RECOGNIZED, rec.getServiceState());
    }

    public void testCancelledRecognize() {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, "srgml");
        SpeechRecognizer speechRecognizer = mtm.getSpeechRecognizer(session, grammars);
        assertNotNull("The speechRecognizer must not be null", speechRecognizer);
        assertTrue("The speechRecognizer should be an instance of MrcpSpeechRecognizer",
                speechRecognizer instanceof MrcpSpeechRecognizer);
        MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)speechRecognizer;
        setupPrepare();
        setupRecognize();
        rtspServerMock.setDelay(1000);
//        setupRecognizeComplete(false);
        setupStop();
        setupCancel();
        RtspRequest.reset();
        MrcpRequest.reset();
        speechRecognizer.recognize((IInboundMediaStream)mockInboundStream.proxy());
        assertEquals(MrcpSpeechRecognizer.ServiceState.IDLE, rec.getServiceState());
        counter = 0;
        while (rec.getServiceState() == MrcpSpeechRecognizer.ServiceState.IDLE) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        counter = 0;
        while (rec.getServiceState() != MrcpSpeechRecognizer.ServiceState.RECOGNIZING) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.RECOGNIZING, rec.getServiceState());
        speechRecognizer.cancel();
        counter = 0;
        while (rec.getServiceState() != MrcpSpeechRecognizer.ServiceState.IDLE) {
            assertTrue(counter++ < maxCount);
            sleep(1);
        }
        assertEquals(MrcpSpeechRecognizer.ServiceState.IDLE, rec.getServiceState());
        assertNull(rec.getGrammars());
        assertNull(rec.getSession());
    }

    public void setupPrepare() {
        // Mocking out bound stream creation
        // First get stream
        mockStreamFactory.expects(once()).method("getOutboundMediaStream")
                .withNoArguments()
                .will(returnValue(mockOutboundStream.proxy()));
        // Then create stream
        mockOutboundStream.expects(once()).method("create")
                .with(isA(Collection.class), isA(ConnectionProperties.class));
        // The event dispatcher must be set
        mockOutboundStream.expects(once()).method("setEventDispatcher")
                .with(isA(DummyEventDispatcher.class));

        // Ok, now that the expectations are defined we can perform the translation
        // SET-UP
        RtspRequest request = new SetupRequest(true, 4712);
        request.setUrl("mockingbird.mobeon.com", 4711);
        RtspResponse response = new RtspResponse(200, "OK");
        response.setHeaderField("Session", "ABAB");
        SdpFactory.setPathName("gov.nist");
        SdpFactory sdpFactory;
        SessionDescription sessionDescription = null;
        String content = "m=audio 4714 RTP/AVP 0";
        try {
            sdpFactory = SdpFactory.getInstance();
            sessionDescription
                        = sdpFactory.createSessionDescription(content);
        } catch (SdpException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        response.setSDP(sessionDescription);
        rtspServerMock.setExpectedMessage(request);
        rtspServerMock.setSentMessage(response);

        // DEFINE-GRAMMAR
        MrcpResponse mrcpResponse = new MrcpResponse(1, 200, "COMPLETE");
        request = new DefineGrammarRequest("application/grammar+xml",
                "srgml", grammarId);
        request.setUrl("mockingbird.mobeon.com", 4711);
        request.setHeaderField("Session", "ABAB");
        response = new RtspResponse(200, "OK");
        response.setMrcpMessage(mrcpResponse);
        rtspServerMock.setExpectedMessage(request);
        rtspServerMock.setSentMessage(response);
    }

    public void setupRecognize() {
        int expection = MrcpMessage.SUCCESS;
        // First the streams are joined
        mockInboundStream.expects(once()).method("join")
                .with(eq(mockOutboundStream.proxy()));
        mockInboundStream.expects(once()).method("getEventDispatcher")
                .withNoArguments()
                .will(returnValue(mockEventDispatcher.proxy()));

        // RECOGNIZE
        MrcpMessage mrcpResponse = new MrcpResponse(1, 200, "IN-PROGRESS");
        RtspResponse response = new RtspResponse(200, "OK");
        response.setMrcpMessage(mrcpResponse);
        RtspRequest request = new RecognizeRequest(grammarId);
        request.setHeaderField("Session", "ABAB");
        request.setUrl("mockingbird.mobeon.com", 4711);
        rtspServerMock.setExpectedMessage(request);
        rtspServerMock.setSentMessage(response);
    }

    public void setupRecognizeComplete(boolean completed) {
        int expection = MrcpMessage.SUCCESS;
        if (completed) {
            // Before unjoined this is checked
            mockOutboundStream.expects(once()).method("isJoined")
                    .withNoArguments().will(returnValue(new Boolean(true)));
            // Then they are un-joined
            mockInboundStream.expects(once()).method("unjoin")
                    .with(eq(mockOutboundStream.proxy()));

            // Here is the expected recognize result.
            switch (expection) {
                case MrcpMessage.SUCCESS:
                    mockEventDispatcher.expects(once()).method("fireEvent")
                            .with(isA(RecognitionCompleteEvent.class));
                    break;

                case MrcpMessage.NO_MATCH:
                    mockEventDispatcher.expects(once()).method("fireEvent")
                            .with(isA(RecognitionNoMatchEvent.class));
                    break;
                case MrcpMessage.NO_INPUT_TIMEOUT:
                    mockEventDispatcher.expects(once()).method("fireEvent")
                            .with(isA(RecognitionNoInputEvent.class));
                    break;

                case MrcpMessage.RECOGNITION_TIMEOUT:
                    mockEventDispatcher.expects(once()).method("fireEvent")
                            .with(isA(RecognitionFailedEvent.class));
                    break;
            }
        } else {
            mockEventDispatcher.expects(once()).method("fireEvent")
                    .with(isA(RecognitionFailedEvent.class));
        }

        if (completed) {
            // Issuing recognize COMPLETE event
            MrcpEvent event = new MrcpEvent("RECOGNITION-COMPLETE", 1, "COMPLETE");
            switch (expection) {
                case MrcpMessage.SUCCESS:
                    event.setHeaderField("Completion-Cause", "000 success");
                    event.setContent("application/x-nlsml", "NLSML");
                    break;

                case MrcpMessage.NO_MATCH:
                    event.setHeaderField("Completion-Cause", "001 no-match");
                    break;

                case MrcpMessage.NO_INPUT_TIMEOUT:
                    event.setHeaderField("Completion-Cause", "002 no-input-timeout");
                    break;

                case MrcpMessage.RECOGNITION_TIMEOUT:
                    event.setHeaderField("Completion-Cause", "003 recognition-timeout");
                    break;
            }

            RtspRequest request = new RtspRequest("ANNOUNCE", true);
            request.setUrl("mockingbird.mobeon.com", 4711);
            request.setMrcpMessage(event);
            rtspServerMock.setDelay(1000);
            rtspServerMock.setSentMessage(request);
        }
    }

    public void setupStop() {
        // Before unjoined this is checked
        mockOutboundStream.expects(once()).method("isJoined")
                .withNoArguments().will(returnValue(true));
        // Before unjoined this is checked
        mockInboundStream.expects(once()).method("unjoin")
                .with(eq(mockOutboundStream.proxy()));
        // STOP
        RtspRequest request = new StopRequest(true);
        request.setHeaderField("Session", "ABAB");
        request.setUrl("mockingbird.mobeon.com", 4711);
        rtspServerMock.setExpectedMessage(request);
        RtspResponse response = new RtspResponse(200, "OK");
        rtspServerMock.setSentMessage(response);
    }

    public void setupCancel() {
        // TEARDOWN
        RtspRequest request = new RtspRequest("TEARDOWN", true);
        request.setHeaderField("Session", "ABAB");
        request.setUrl("mockingbird.mobeon.com", 4711);
        rtspServerMock.setExpectedMessage(request);
        RtspResponse response = new RtspResponse(200, "OK");
        rtspServerMock.setSentMessage(response);
    }

    public void setupRecognize(int expection) {
        // Mocking out bound stream creation
        // First get stream
        mockStreamFactory.expects(once()).method("getOutboundMediaStream")
                .withNoArguments()
                .will(returnValue(mockOutboundStream.proxy()));
        // Then create stream
        mockOutboundStream.expects(once()).method("create")
                .with(isA(Collection.class), isA(ConnectionProperties.class));
        // Eventually the stream will be deleted
        mockOutboundStream.expects(once()).method("delete")
                .withNoArguments();
        // The event dispatcher must be set
        mockOutboundStream.expects(once()).method("setEventDispatcher")
                .with(eq(mockEventDispatcher.proxy()));
        // The event dispatcher is retrieved from input
        mockInboundStream.expects(once()).method("getEventDispatcher")
                .withNoArguments()
                .will(returnValue(mockEventDispatcher.proxy()));
         // First the streams are joined
        mockInboundStream.expects(once()).method("join")
                .with(eq(mockOutboundStream.proxy()));
        // Before unjoined this is checked
        mockOutboundStream.expects(once()).method("isJoined")
                .withNoArguments().will(returnValue(true));
        // Then they are un-joined
        mockInboundStream.expects(once()).method("unjoin")
                .with(eq(mockOutboundStream.proxy()));
        // Here is the expected recognize result.
        switch (expection) {
            case MrcpMessage.SUCCESS:
                mockEventDispatcher.expects(once()).method("fireEvent")
                        .with(isA(RecognitionCompleteEvent.class));
                break;

            case MrcpMessage.NO_MATCH:
                mockEventDispatcher.expects(once()).method("fireEvent")
                        .with(isA(RecognitionNoMatchEvent.class));
                break;
            case MrcpMessage.NO_INPUT_TIMEOUT:
                mockEventDispatcher.expects(once()).method("fireEvent")
                        .with(isA(RecognitionNoInputEvent.class));
                break;

            case MrcpMessage.RECOGNITION_TIMEOUT:
                mockEventDispatcher.expects(once()).method("fireEvent")
                        .with(isA(RecognitionFailedEvent.class));
                break;
        }
    }

    void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
