/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.RtspConnectionFactoryMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.DummyServerMock;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.stream.StackException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.VoidStub;

import java.io.ByteArrayInputStream;

/**
 * This class performs the unit testing of Text to Speech over MRCP/RTSP.
 */
public class MrcpTextToSpeechTest extends MockObjectTestCase {
    private MediaTranslationManagerFacade mtm = null;
    Mock mockStreamFactory;
    Mock mockOutboundStream;
    Mock mockInboundStream;
    Mock mockMediaObject;
    Mock mockEventDispatcher;

    public void setUp() {
        Utility.getSingleton().initialize("test/TestComponentConfig.xml");
        mtm = Utility.getSingleton().getMediaTranslationManager(Utility.getSingleton().getSession());
        // Setting up the mocked objects.
        mockStreamFactory = mock(IStreamFactory.class);
        mockOutboundStream = mock(IOutboundMediaStream.class);
        mockInboundStream = mock(IInboundMediaStream.class);
        mockMediaObject = mock(IMediaObject.class);
        mockEventDispatcher = mock(IEventDispatcher.class);

        // Injecting objects into MTM
        mtm.setStreamFactory((IStreamFactory)mockStreamFactory.proxy());
        RtspConnectionFactoryMock connectionFactory = new RtspConnectionFactoryMock();
        connectionFactory.setMessageHandler(new DummyServerMock());
        MediaTranslationFactory.getInstance().setRtspConnectionFactory(connectionFactory);
    }

    /**
     * Testing the open method ...
     */
    public void testOpen() {
        TextToSpeech textToSpeech =
                mtm.getTextToSpeech(Utility.getSingleton().getSession());
        assertNotNull("The translator must not be null", textToSpeech);
        assertTrue("The translator should be an instance of MrcpTextToSpeech",
                textToSpeech instanceof MrcpTextToSpeech);

        setupOpenStubs();
        textToSpeech.open((IOutboundMediaStream)mockOutboundStream.proxy());
        textToSpeech.open((IOutboundMediaStream)mockOutboundStream.proxy());
    }

    /**
     * Setting up the open() stubs ..
     */
    public void setupOpenStubs() {
        // Here are the stubs:
        // StreamFactory::getInboundMediaStream()
        mockStreamFactory.expects(atLeastOnce()).method("getInboundMediaStream")
                .withNoArguments()
                .will(returnValue(mockInboundStream.proxy()));

        // OutboundStream::getEventDispatcher()
        mockOutboundStream.expects(atLeastOnce()).method("getEventDispatcher")
                .withNoArguments()
                .will(returnValue(mockEventDispatcher.proxy()));

        // InboundStream::setEventDispatcher()
        mockInboundStream.expects(atLeastOnce()).method("setEventDispatcher")
                .with(eq(mockEventDispatcher.proxy()));

        // InboundStream::create() with throw
        StackException stackException = new StackException("Mocked exception ...");
        mockInboundStream.expects(atLeastOnce()).method("create")
                .with(isA(MediaMimeTypes.class))
                .will(onConsecutiveCalls(
                        throwException(stackException),
                        VoidStub.INSTANCE));


        mockInboundStream.expects(once()).method("join")
                .with(isA(IOutboundMediaStream.class))
                .will(throwException(stackException));

        // OutboundStream::translationFailed()
        mockOutboundStream.expects(once()).method("translationFailed")
                .with(eq("Failed to create inbound media stream: " + stackException));
        mockOutboundStream.expects(once()).method("translationFailed")
                .with(eq("Failed to join inbound media stream: " + stackException));
    }

    /**
     * Testing the close method ...
     */
    public void testClose() {
        TextToSpeech textToSpeech =
                mtm.getTextToSpeech(Utility.getSingleton().getSession());
        assertNotNull("The translator must not be null", textToSpeech);
        assertTrue("The translator should be an instance of MrcpTextToSpeech",
                textToSpeech instanceof MrcpTextToSpeech);
        // Verifying that close will not fail when called on an unproperly opened
        // TTS session.
        try {
            textToSpeech.close();
        } catch(Exception e) {
            fail("Caught exception!");
        }
    }

    /**
     * Testing the translation of text to speech
     *
     * @throws Exception
     */
    public void testTranslateSsml() throws Exception {
        // Get a translator and ensure that it is an MrcpTextToSpeech
        TextToSpeech textToSpeech =
                mtm.getTextToSpeech(Utility.getSingleton().getSession());
        assertNotNull("The translator must not be null", textToSpeech);
        assertTrue("The translator should be an instance of MrcpTextToSpeech",
                textToSpeech instanceof MrcpTextToSpeech);

        // Casting to MrcpTextToSpeech in order to access implementation specific
        // methods.
        MrcpTextToSpeech translator = (MrcpTextToSpeech)textToSpeech;

        // Setting up the stubs ...
        setupTranslateSsmlStubs();

        // Ok, now that the expectations are defined we can perform the translation
        translator.translate((IMediaObject)mockMediaObject.proxy(),
                (IOutboundMediaStream)mockOutboundStream.proxy());

        translator.speakComplete(null);
        translator.control("teardown", "");
    }

    /**
     * Setting up the stubbed expectations.
     */
    public void setupTranslateSsmlStubs() {
        // This is the contents of the translated Media Object.
        String ssml = "<?xml version=\"1.0\"?>" +
                "<speak>" +
                "<s>Hello world!</s>" +
                "</speak>";
        ByteArrayInputStream input = new ByteArrayInputStream(ssml.getBytes());

        // Media object stubs
        // 1) Set up getMediaProperties()
        MediaProperties mediaPropeties =
                new MediaProperties(MediaTranslationFactory.getInstance().getSsmlMimeType());
        mockMediaObject.expects(once()).method("getMediaProperties")
                .withNoArguments()
                .will(returnValue(mediaPropeties));
        // 2) Set up get media object size
        mockMediaObject.expects(once()).method("getSize")
                .withNoArguments()
                .will(returnValue((long)ssml.length()));
        // 3) Set up getInputStream()
        mockMediaObject.expects(once()).method("getInputStream")
                .withNoArguments()
                .will(returnValue(input));

        // Stream Factory stubs
        // 1) Set up getInboundMediaStream()
        mockStreamFactory.expects(once()).method("getInboundMediaStream")
                .withNoArguments()
                .will(returnValue(mockInboundStream.proxy()));

        // Inbound Media Stream stubs
        // 1) Set up create()
        mockInboundStream.expects(once()).method("create")
                .with(isA(MediaMimeTypes.class));
        mockInboundStream.expects(once()).method("delete")
                .withNoArguments();
        // 2) Set up setEventDispatcher()
        mockInboundStream.expects(once()).method("setEventDispatcher")
                .with(eq(mockEventDispatcher.proxy()));
        // 3) Set up getAudioPort()
        mockInboundStream.expects(once()).method("getAudioPort")
                .withNoArguments()
                .will(returnValue(23000));
        // 4) Set up join
        mockInboundStream.expects(once()).method("join")
                .with(isA(IOutboundMediaStream.class));
        // 5) Set up unjoin
        mockInboundStream.expects(once()).method("unjoin")
                .with(isA(IOutboundMediaStream.class));

        // Outbound Media Stream stubs
        // 1) Set up translationDone()
        mockOutboundStream.expects(once()).method("translationDone")
                .withNoArguments();

        // ...
        mockOutboundStream.expects(once()).method("getEventDispatcher")
                .withNoArguments()
                .will(returnValue(mockEventDispatcher.proxy()));
    }

    /**
     * Testing the translation of text to speech
     *
     * @throws Exception
     */
    public void testTranslatePlain() throws Exception {
        // Get a translator and ensure that it is an MrcpTextToSpeech
        TextToSpeech textToSpeech =
                mtm.getTextToSpeech(Utility.getSingleton().getSession());
        assertNotNull("The translator must not be null", textToSpeech);
        assertTrue("The translator should be an instance of MrcpTextToSpeech",
                textToSpeech instanceof MrcpTextToSpeech);

        // Casting to MrcpTextToSpeech in order to access implementation specific
        // methods.
        MrcpTextToSpeech translator = (MrcpTextToSpeech)textToSpeech;

        // Setting up the stubs ...
        setupTranslatePlainStubs();

        // Ok, now that the expectations are defined we can perform the translation
        translator.translate((IMediaObject)mockMediaObject.proxy(),
                (IOutboundMediaStream)mockOutboundStream.proxy());

        translator.speakComplete(null);
        translator.control("teardown", "");
    }

    /**
     * Setting up the stubbed expectations.
     */
    public void setupTranslatePlainStubs() {
        // This is the contents of the translated Media Object.
        String plain = "Hello world!";

        ByteArrayInputStream input = new ByteArrayInputStream(plain.getBytes());

        // Media object stubs
        // 1) Set up getMediaProperties()
        MediaProperties mediaPropeties =
                new MediaProperties(MediaTranslationFactory.getInstance().getPlainMimeType());
        mockMediaObject.expects(once()).method("getMediaProperties")
                .withNoArguments()
                .will(returnValue(mediaPropeties));
        // 2) Set up get media object size
        mockMediaObject.expects(once()).method("getSize")
                .withNoArguments()
                .will(returnValue((long)plain.length()));
        // 3) Set up getInputStream()
        mockMediaObject.expects(once()).method("getInputStream")
                .withNoArguments()
                .will(returnValue(input));

        // Stream Factory stubs
        // 1) Set up getInboundMediaStream()
        mockStreamFactory.expects(once()).method("getInboundMediaStream")
                .withNoArguments()
                .will(returnValue(mockInboundStream.proxy()));

        // Inbound Media Stream stubs
        // 1) Set up create()
        mockInboundStream.expects(once()).method("create")
                .with(isA(MediaMimeTypes.class));
        mockInboundStream.expects(once()).method("delete")
                .withNoArguments();
        // 2) Set up setEventDispatcher()
        mockInboundStream.expects(once()).method("setEventDispatcher")
                .with(eq(mockEventDispatcher.proxy()));
        // 3) Set up getAudioPort()
        mockInboundStream.expects(once()).method("getAudioPort")
                .withNoArguments()
                .will(returnValue(23000));
        // 4) Set up join
        mockInboundStream.expects(once()).method("join")
                .with(isA(IOutboundMediaStream.class));
        // 5) Set up unjoin
        mockInboundStream.expects(once()).method("unjoin")
                .with(isA(IOutboundMediaStream.class));

        // Outbound Media Stream stubs
        // 1) Set up translationDone()
        mockOutboundStream.expects(once()).method("translationDone")
                .withNoArguments();

        // ...
        mockOutboundStream.expects(once()).method("getEventDispatcher")
                .withNoArguments()
                .will(returnValue(mockEventDispatcher.proxy()));
    }
}
