/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.mock.MockCallSession;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import jakarta.activation.MimeType;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Testclass join/unjoin of Inbound and Outbound Media Streams.
 *
 */
public class JoinUnjoinStreamTest extends MockObjectTestCase {
    static {
        System.loadLibrary("ccrtpadapter");
    }

    private static final String HOST_BRAGE = "150.132.5.213";
    private static final String HOST_PC = "10.16.2.26";
    private static final String REMOTE_HOST = "150.132.5.158";
    private static final int REMOTE_AUDIO_PORT = 23000;

    private MimeType VIDEO_QUICKTIME;
    private MimeType AUDIO_WAV;
    protected static final String LONG_AUDIOFILE_NAME =
        "nativestreamhandling/medialibrary/mtest/test_pcmu.wav";

    protected String remoteHostAddress = "150.132.5.158";
    protected StreamFactoryImpl mFactory;
    protected Mock mockEventDispatcher = mock(IEventDispatcher.class);
    protected Mock mockRTPSessionFactory = mock(RTPSessionFactory.class);
    protected Mock mockRTPSession = mock(RTPSession.class);

    /**
     * Setting up vital stuff for the tests.
     */
    public void setUp() {
        mockRTPSessionFactory.expects(atLeastOnce()).
                method("createInboundRTPSession").
                will(returnValue(mockRTPSession.proxy()));
        mockRTPSessionFactory.expects(atLeastOnce()).
                method("createOutboundRTPSession").
                will(returnValue(mockRTPSession.proxy()));
        mockRTPSession.expects(atLeastOnce()).method("init");
        try {
            remoteHostAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            fail("Failed to get adress of local host");
        }
        mFactory = new StreamFactoryImpl();
        try {
            ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
            mFactory.setContentTypeMapper(ctm);
            cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
            mFactory.setSessionFactory((RTPSessionFactory)mockRTPSessionFactory.proxy());

            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            AUDIO_WAV = new MimeType("audio/wav");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the stream factory: " + e);
        }
    }

    /**
     * Verifying that joining unitialized streams throws exception.
     *
     * NullPointerExceptions are not allowed.
     */
    public void testFailingJoin() {
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();

        try {
            inStream.join(outStream);
            fail("Got no exception.");
        } catch (NullPointerException e) {
            fail("Got exception: '" + e + "' :\n[" + e.getStackTrace() + "]");
        } catch (Exception e) {}

    }

    /**
     * Verifying that unjoining unitialized streams throws exception.
     *
     * NullPointerExceptions are not allowed.
     */
    public void testFailingUnjoin() {
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();

//        mockRTPSession.expects(once()).method("unjoin").with(same(outStream));

        try {
            inStream.unjoin(outStream);
            fail("Got no exception.");
        } catch (NullPointerException e) {
            fail("Got exception: '" + e + "' :\n[" + e.getStackTrace() + "]");
        } catch (Exception e) {}
    }

    /**
     * Verifying that incompatible streams are not joinable.
     */
    public void testIncompatibleStreams()
    {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();
        mockRTPSession.expects(atLeastOnce()).method("create");

        // Initializing an inbound PCMU stream
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing an outbound AMR stream
        try {
            outStream.setEventDispatcher(eventDispatcher);
            outStream.setCallSession(MockCallSession.getSession());
            Collection<RTPPayload> payloads = getAmrMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Verifying that join fails
        try {
            inStream.join(outStream);
            fail("Got no exception.");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("Got exception: '" + e + "' :\n[" + e.getStackTrace() + "]");
        }
    }

    /**
     * Verifying that two compatible streams are join/unjoinable.
     */
    public void testValidAudioJoinUnjoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();
        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the inbound PCMU/AMR stream
        try {
            outStream.setEventDispatcher(eventDispatcher);
            outStream.setCallSession(MockCallSession.getSession());
            Collection<RTPPayload> payloads = getAudioMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        try {
            inStream.unjoin(outStream);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        inStream.delete();
        outStream.delete();
    }

    /**
     * Verifying that two compatible streams are join/unjoinable.
     */
    public void testValidVideoJoinUnjoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();
        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getVideoMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the inbound PCMU/AMR stream
        try {
            outStream.setEventDispatcher(eventDispatcher);
            Collection<RTPPayload> payloads = getVideoMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
            cProp.setVideoHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        try {
            inStream.unjoin(outStream);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        inStream.delete();
        outStream.delete();
    }

    /**
     * Verifying that video and audio streams are join/unjoinable.
     * Typically scenario when utilizing TTS in a video call.
     */
    public void testAssymetricJoinUnjoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = null;
        IOutboundMediaStream outStream = null;
        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Verifying that audio can be joined to video

        // Initializing the inbound audio stream
        try {
            inStream = mFactory.getInboundMediaStream();
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound video stream
        try {
            outStream = mFactory.getOutboundMediaStream();
            outStream.setEventDispatcher(eventDispatcher);
            Collection<RTPPayload> payloads = getVideoMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
            cProp.setVideoHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
//        try {
//            Thread.sleep(2000);
//        }
//        catch (InterruptedException e) {
//            fail("Interrupted: " + e);
//        }

        try {
            inStream.unjoin(outStream);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

//        try {
//            Thread.sleep(100);
//        }
//        catch (InterruptedException e) {
//            fail("Interrupted: " + e);
//        }

        inStream.delete();
        outStream.delete();

        // Verifying that video can be joined to audio

        // Initializing the inbound video stream
        try {
            inStream = mFactory.getInboundMediaStream();
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getVideoMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound audio stream
        try {
            outStream = mFactory.getOutboundMediaStream();
            outStream.setEventDispatcher(eventDispatcher);
            Collection<RTPPayload> payloads = getAudioMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

//        try {
//            Thread.sleep(2000);
//        }
//        catch (InterruptedException e) {
//            fail("Interrupted: " + e);
//        }

        try {
            inStream.unjoin(outStream);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

//        try {
//            Thread.sleep(100);
//        }
//        catch (InterruptedException e) {
//            fail("Interrupted: " + e);
//        }

        inStream.delete();
        outStream.delete();
    }
    /**
     * Verifying that two compatible streams are join/unjoinable.
     */
    public void testMultipleValidVideoJoinUnjoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream1 = mFactory.getOutboundMediaStream();
        IOutboundMediaStream outStream2 = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getVideoMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        try {
            outStream1.setEventDispatcher(eventDispatcher);
            outStream2.setEventDispatcher(eventDispatcher);
            Collection<RTPPayload> payloads = getVideoMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(REMOTE_AUDIO_PORT);
            cProp.setAudioHost(HOST_PC);
            cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
            cProp.setVideoHost(HOST_PC);
            cProp.setPTime(20);
            cProp.setMaxPTime(20);
            outStream1.setCallSession(MockCallSession.getSession());
            outStream1.create(payloads, cProp, null,inStream); //should probably set inStream=null here
            cProp.setAudioPort(REMOTE_AUDIO_PORT + 4);
            cProp.setVideoPort(REMOTE_AUDIO_PORT + 6);
            outStream2.setCallSession(MockCallSession.getSession());
            outStream2.create(payloads, cProp, null,inStream); //should probably set inStream=null here
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream1);
            inStream.join(outStream2);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        try {
            inStream.unjoin(outStream2);
            inStream.unjoin(outStream1);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        inStream.delete();
        outStream1.delete();
        outStream2.delete();
    }

    /**
     * Tests the behaviour when a stream is joined before a
     * play-operation has finished.
     */
    public void testJoinDuringPlay() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("play");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        inStream.setEventDispatcher(eventDispatcher);
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        outStream.setEventDispatcher(eventDispatcher);
        Object callId = new Object();
        PlayFinishedEvent finEvent = new PlayFinishedEvent(callId,
                PlayFinishedEvent.CAUSE.STREAM_JOINED, 0);
//        mockEventDispatcher.expects(once()).method("fireEvent").with(eq(finEvent));
        try {
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            outStream.play(callId, createAudioMediaObject(LONG_AUDIOFILE_NAME),
                    PlayOption.WAIT_FOR_AUDIO, 0);
            inStream.join(outStream);
            inStream.unjoin(outStream);
            inStream.delete();
            outStream.delete();
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * Tests the behaviour when play is called while a stream is joined.
     */
    public void testPlayDuringJoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        inStream.setEventDispatcher(eventDispatcher);
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        outStream.setEventDispatcher(eventDispatcher);
        Object callId = new Object();
        try {
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
            outStream.play(callId, createAudioMediaObject(LONG_AUDIOFILE_NAME),
                    PlayOption.WAIT_FOR_AUDIO, 0);
            fail("Play while joined should case an exception.");
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        inStream.delete();
        outStream.delete();
    }

    /**
     * Tests the behaviour when an already joined stream is joined.
     */
    public void testMultipleJoin() {
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        inStream.setEventDispatcher(eventDispatcher);
        try {
            inStream.setCallSession(MockCallSession.getSession());
            inStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        outStream.setEventDispatcher(eventDispatcher);
        try {
            outStream.setCallSession(MockCallSession.getSession());
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(outStream);
            inStream.join(outStream);
            fail("Multiple calls to join should cause an exception.");
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        inStream.delete();
        outStream.delete();
    }

    public void testAudioTransfer() {
        remoteHostAddress = HOST_PC;
        IEventDispatcher eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream callOneInStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream callOneOutStream = mFactory.getOutboundMediaStream();
        IInboundMediaStream callTwoInStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream callTwoOutStream = mFactory.getOutboundMediaStream();
        IInboundMediaStream callThreeInStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream callThreeOutStream = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        callOneInStream.setEventDispatcher(eventDispatcher);
        callTwoInStream.setEventDispatcher(eventDispatcher);
        callThreeInStream.setEventDispatcher(eventDispatcher);
        callOneInStream.setCallSession(MockCallSession.getSession());
        callTwoInStream.setCallSession(MockCallSession.getSession());
        callThreeInStream.setCallSession(MockCallSession.getSession());
        try {
            callOneInStream.create(getAudioMediaMimeTypes());
            callTwoInStream.create(getAudioMediaMimeTypes());
            callThreeInStream.create(getAudioMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT+40);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        callOneOutStream.setEventDispatcher(eventDispatcher);
        callTwoOutStream.setEventDispatcher(eventDispatcher);
        callThreeOutStream.setEventDispatcher(eventDispatcher);
        callOneOutStream.setCallSession(MockCallSession.getSession());
        callTwoOutStream.setCallSession(MockCallSession.getSession());
        callThreeOutStream.setCallSession(MockCallSession.getSession());
        try {
            callOneOutStream.create(payloads, cProp, null,callOneInStream);
            cProp.setAudioPort(REMOTE_AUDIO_PORT+42);
            callTwoOutStream.create(payloads, cProp, null,callTwoInStream);
            cProp.setAudioPort(REMOTE_AUDIO_PORT+44);
            callThreeOutStream.create(payloads, cProp, null,callThreeInStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        System.out.println("Call One Inbound port:        " + callOneInStream.getAudioPort());
        System.out.println("Call One Outbound port:       " + callOneOutStream.getAudioPort());
        System.out.println("Call Two Inbound port:        " + callTwoInStream.getAudioPort());
        System.out.println("Call Two Outbound port:       " + callTwoOutStream.getAudioPort());
        System.out.println("Call Three Inbound port:      " + callThreeInStream.getAudioPort());
        System.out.println("Call Three Outbound port:     " + callThreeOutStream.getAudioPort());

        System.out.println("Simulate join of calls ...");
        try {
            callOneInStream.join(callTwoOutStream);
            callTwoInStream.join(callOneOutStream);
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }

        System.out.println("Unjoin the calls ...");
        try {
            callOneInStream.unjoin(callTwoOutStream);
            callTwoInStream.unjoin(callOneOutStream);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        //
        System.out.println("Multiple join ...");
        try {
            callOneInStream.join(callThreeOutStream);
            callOneInStream.join(callTwoOutStream);
            callThreeInStream.join(callOneOutStream);
            sleep(10000);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        //
        System.out.println("Implicit unjoin ...");
        try {
            callThreeInStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        System.out.println("Implicit unjoin ...");
        try {
            callThreeOutStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        System.out.println("Implicit unjoin ...");
        try {
            callOneInStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        System.out.println("Deleting streams ...");
//        callOneInStream.delete();
        callOneOutStream.delete();
        callTwoInStream.delete();
        callTwoOutStream.delete();
//        callThreeInStream.delete();
//        callThreeOutStream.delete();
    }

    public void testVideoTransfer() {
        remoteHostAddress = "10.16.2.67";
        IEventDispatcher 
            eventDispatcher = (IEventDispatcher)mockEventDispatcher.proxy();
        IInboundMediaStream 
            callOneInStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream 
            callOneOutStream = mFactory.getOutboundMediaStream();

        mockRTPSession.expects(atLeastOnce()).method("create");
        mockRTPSession.expects(atLeastOnce()).method("join");
        mockRTPSession.expects(atLeastOnce()).method("unjoin");
        mockRTPSession.expects(atLeastOnce()).method("delete");

        // Initializing the inbound PCMU stream
        callOneInStream.setEventDispatcher(eventDispatcher);

        try {
            callOneInStream.setCallSession(MockCallSession.getSession());
            callOneInStream.create(getVideoMediaMimeTypes());
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the outbound PCMU/AMR stream
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        int portOffset = 10;
        cProp.setAudioPort(REMOTE_AUDIO_PORT+portOffset); portOffset += 2;
        cProp.setAudioHost(remoteHostAddress);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+portOffset); portOffset += 2;
        cProp.setVideoHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        callOneOutStream.setEventDispatcher(eventDispatcher);
        callOneOutStream.setCallSession(MockCallSession.getSession());
        try {
            callOneOutStream.create(payloads, cProp, null,callOneInStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        System.out.println("Call One Inbound audio port:        " 
                           + callOneInStream.getAudioPort());
        System.out.println("Call One Inbound video port:        " 
                           + callOneInStream.getVideoPort());
        System.out.println("Call One Outbound audio port:       " 
                           + callOneOutStream.getAudioPort());
        System.out.println("Call One Outbound video port:       " 
                           + callOneOutStream.getVideoPort());

        System.out.println("Simulate join of calls ...");
        try {
            callOneInStream.join(callOneOutStream);
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }

        System.out.println("Unjoin the calls ...");
        try {
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        //
        System.out.println("Multiple join ...");
        try {
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        /*
        System.out.println("Implicit unjoin ...");
        try {
            callThreeInStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        System.out.println("Implicit unjoin ...");
        try {
            callThreeOutStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        System.out.println("Implicit unjoin ...");
        try {
            callOneInStream.delete();
            sleep(100);
        } catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception.",
                    IllegalStateException.class, e);
        }
        */
        System.out.println("Deleting streams ...");
        callOneInStream.delete();
        callOneOutStream.delete();
    }

    protected MediaMimeTypes getAudioMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        return mediaMimeTypes;
    }

    protected MediaMimeTypes getVideoMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
        return mediaMimeTypes;
    }

    protected Collection<RTPPayload> getAudioMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        return list;
    }

    protected Collection<RTPPayload> getVideoMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
        return list;
    }

    protected Collection<RTPPayload> getAmrMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
        return list;
    }

    /**
     * Creates a MediaObject instance from the audio test media file.
     *
     * @param fileName Filename.
     *
     * @return A MediaObject instance.
     */
    protected IMediaObject createAudioMediaObject(String fileName) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            File f = new File(fileName);
            result = factory.create(f);
            result.getMediaProperties().setContentType(AUDIO_WAV);
        }
        catch (Exception e) {
            fail("Unexpected error in createMediaObject: " + e);
        }
        return result;
    }


    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        JoinUnjoinStreamTest test = new JoinUnjoinStreamTest();
        test.setUp();
	//        test.testFailingJoin();
	//        test.testFailingUnjoin();
	//        test.testIncompatibleStreams();
//        test.testMultipleValidVideoJoinUnjoin();
        test.testVideoTransfer();
        test.tearDown();
        //test.setUp();
        //test.testJoinDuringPlay();
        //test.tearDown();
    }
}
