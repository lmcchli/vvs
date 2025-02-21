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
import com.mobeon.masp.stream.mock.MockRTPSessionFactory;
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
import java.util.List;

/**
 * Support test class for MediaStreams.
 * 
 * @author Jörgen Terner
 */
public class MediaStreamSupportTest extends MockObjectTestCase {
    
    protected static final String FILENAME = "nativestreamhandling/medialibrary/mtest/beep_pcmu.wav";
    protected static final String LONG_AUDIOFILE_NAME = 
        "nativestreamhandling/medialibrary/mtest/test_pcmu.wav";
    protected static final String VIDEO_FILENAME =
        "nativestreamhandling/medialibrary/mtest/UM_0604.mov";
        
    // (Brage=150.132.5.213");
    protected String remoteHostAddress = "0.0.0.0"; 

    protected MimeType VIDEO_QUICKTIME;
    protected MimeType AUDIO_WAV;

    private static final String HOST_BRAGE = "150.132.5.213";
    private static final String HOST_PC = "10.16.2.133";
    protected static final String REMOTE_HOST = HOST_BRAGE; // "0.0.0.0";
    protected static final int REMOTE_AUDIO_PORT = 4712; 
        
    protected Mock mEventDispatcher;

    protected StreamFactoryImpl mFactory;

    /* JavaDoc in base class. */
    public void setUp() {
        mEventDispatcher = mock(IEventDispatcher.class);
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
            // This overrides/replaces the default CCRTPSessionFactory
            mFactory.setSessionFactory(new MockRTPSessionFactory());
            mFactory.setContentTypeMapper(ctm);
            cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
            
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            AUDIO_WAV = new MimeType("audio/wav");
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory: " + e);
        }
    }

    /* JavaDoc in base class. */
    public void tearDown() {
    }

    /**
     * Gets a new stream instance.
     * 
     * @return A new stream instance.
     */
    protected IMediaStream getStreamInstance() {
        return null;
    }
    
    /**
     * Gets a new stream instance on which the method create
     * has been called.
     * 
     * @return A newly created stream instance.
     */
    protected IMediaStream getCreatedStream() {
        return null;
    }
    
    /**
     * <pre>
     * Constructor test
     * 
     * Call constructor
     * ----------------
     * Action: Create a new stream instance.
     * Result: No exceptions are thrown.
     * </pre>
     */
    public void testConstructor() {
        // Call constructor
        try {
            getStreamInstance();
        }
        catch (Exception e) {
            fail("Unexpected exception while creating a new stream instance: " + e);
        }
    }

    /**
     * <pre>
     * Tests for method stop.
     * 
     * Wrong state
     * -----------
     * Action: 
     *  1. Call stop before create has been called.
     *  2. Call stop after calls to create and delete.
     * Result: IllegalStateException.
     * 
     * Wrong arguments
     * ---------------
     * Condition: A created stream.
     * Action: Call stop(null).
     * Result: IllegalArgumentException.
     * 
     * Correct state and arguments
     * ---------------------------
     * Condition: A stream is created.
     * Action: Call stop.
     * Result: No exception is thrown.
     * </pre>
     */
    public void testStop() {
        // The tests only make sense for subclasses to this class
        if (getClass().equals(MediaStreamSupportTest.class)) {
            return;
        }
        IMediaStream stream = null;
        
        // Wrong state 1
        stream = getStreamInstance();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.stop(new Object());
            fail("Wrong state 1 should have caused exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                    IllegalStateException.class, e);
        }

        // Wrong state 2
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        stream.delete();
        try {
            stream.stop(new Object());
            fail("Wrong state 2 should have caused exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 2: Unexpected exception.",
                    IllegalStateException.class, e);
        }

        // Wrong arguments
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.stop(null);
            fail("Wrong arguments should have caused exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong arguments: Unexpected exception.",
                    IllegalArgumentException.class, e);
        }
        stream.delete();

        // Correct state and arguments
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.stop(new Object());
        }
        catch (Exception e) {
            fail("Correct state and arguments: Unexpected exception: " + e);
        }
        stream.delete();
    }
    
    /**
     * <pre>
     * Tests for methods 
     * getAudioPort(), getAudioControlPort(), 
     * getVideoPort(), getVideoControlPort().
     * 
     * Before create
     * -------------
     * Action: 
     *     1. getAudioPort()
     *     2. getAudioControlPort()
     *     3. getVideoPort()
     *     4. getVideoControlPort()
     * Result: UNDEFINED_PORTNUMBER.
     * 
     * After create
     * ------------
     * Condition: An Outbound stream is created.
     * Action: 
     *     1. getAudioPort()
     *     2. getAudioControlPort()
     *     3. getVideoPort()
     *     4. getVideoControlPort()
     * Result: 
     *     1. audioPort > 0
     *     2. audioPort+1
     *     3. UNDEFINED_PORTNUMBER (video not supported yet)
     *     4. UNDEFINED_PORTNUMBER (video not supported yet)
     * </pre>
     */
    public void testGetLocalPorts() {
        // The tests only make sense for subclasses to this class
        if (getClass().equals(MediaStreamSupportTest.class)) {
            return;
        }

        // Before create
        IMediaStream stream = getStreamInstance();
        stream.setCallSession(MockCallSession.getSession());
        assertEquals("Before create: Unexpected value for audioPort.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getAudioPort());
        assertEquals("Before create: Unexpected value for audioControlPort.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getAudioControlPort());
        assertEquals("Before create: Unexpected value for videoPort.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getVideoPort());
        assertEquals("Before create: Unexpected value for videoControlPort.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getVideoControlPort());
        
        // After create
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        int audioPort = stream.getAudioPort();
        assertTrue("After create: audioPort should be > 0.",
                audioPort > 0);
        assertEquals("After create: audioControlPort should be audioPort+1.",
                audioPort + 1, stream.getAudioControlPort());
        assertEquals("After create: videoPort should be UNDEFINED_PORTNUMBER.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getVideoPort());
        assertEquals("After create: vdeoControlPort should be UNDEFINED_PORTNUMBER.",
                MediaStreamSupport.UNDEFINED_PORTNUMBER, 
                stream.getVideoControlPort());
    }
    
    /**
     * <pre>
     * Tests for method delete.
     * 
     * Delete before create
     * --------------------
     * Action: Call delete before create has been called.
     * Result: No exceptions are thrown.
     * 
     * Single call to delete
     * ---------------------
     * Condition: A media stream is created.
     * Action: Call delete.
     * Result: No exceptions are thrown.
     * 
     * Multiple calls to delete
     * ------------------------
     * Condition: A media stream is created.
     * Action: Call delete twice.
     * Result: No exceptions are thrown.
     * </pre>
     */
    public void testDelete() {
        // The tests only make sense for subclasses to this class
        if (getClass().equals(MediaStreamSupportTest.class)) {
            return;
        }

        // Delete before create
        IMediaStream stream = getStreamInstance();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.delete();
            fail("Delete before create, expected exception!");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                    IllegalStateException.class, e);
        }

        // Single call to delete
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.delete();
        } 
        catch (Exception e) {
            fail("Single call to delete: Unexpected exception: " + e);
        }
        
        // Multiple calls to delete
        stream = getCreatedStream();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.delete();
            stream.delete();
            fail("Multiple calls to delete, expected exception!");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                    IllegalStateException.class, e);
         }
    }
    
    /**
     * <pre>
     * Tests for method setEventDispatcher.
     * 
     * Wrong arguments
     * ---------------
     * Condition: A created stream.
     * Action: Call setEventDispatcher(null).
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * ---------------------------
     * Condition: A stream is created.
     * Action: Call setEventDispatcher with a non-null argument.
     * Result: No exception is thrown.
     * </pre>
     */
    public void testSetEventDispatcher() {
        // The tests only make sense for subclasses to this class
        if (getClass().equals(MediaStreamSupportTest.class)) {
            return;
        }
        IMediaStream stream = null;
        
        // Wrong arguments
        stream = getStreamInstance();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.setEventDispatcher(null);
            fail("Wrong arguments should have caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong arguments: Unexpected exception.",
                    IllegalArgumentException.class, e);
        }

        // Correct arguments
        stream = getStreamInstance();
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.setEventDispatcher(
                    (IEventDispatcher)mEventDispatcher.proxy());
        }
        catch (Exception e) {
            fail("Correct arguments: Unexpected exception: " + e);
        }
    }
    
    
    /**
     * Creates a MediaObject instance from the audio test media file.
     * 
     * @return A MediaObject instance.
     */
    protected IMediaObject createAudioMediaObject() {
        return createAudioMediaObject(FILENAME);
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
    
    /**
     * Creates a MediaObject instance from the video test media file.
     * 
     * @return A MediaObject instance.
     */
    protected IMediaObject createVideoMediaObject(String filename) {
        return createVideoMediaObject(filename, VIDEO_QUICKTIME);
    }
    
    /**
     * Creates a MediaObject instance from the video test media file.
     * 
     * @return A MediaObject instance.
     */
    protected IMediaObject createVideoMediaObject(String filename, MimeType contentType) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            File f = new File(filename);
            result = factory.create(f);
            result.getMediaProperties().setContentType(contentType);
        }
        catch (Exception e) {
            fail("Unexpected error in createVideoMediaObject: " + e);
        }
        return result;
    }

    /**
     * Creates a recordable MediaObject instance.
     * 
     * @return A recordable MediaObject instance.
     */
    protected IMediaObject createRecordableMediaObject() {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            result = factory.create();
        }
        catch (Exception e) {
            fail("Unexpected error in createRecordableMediaObject: " + e);
        }
        assertNotNull("Created media object is NULL", result);
        return result;
    }
    
    protected Collection<RTPPayload> getAudioMediaPayloads() {
        List<RTPPayload> list = new ArrayList<RTPPayload>();
        RTPPayload p1 = RTPPayload.get(RTPPayload.AUDIO_PCMU);
        if (p1 == null) {
            fail("No default payload configured for AUDIO_PCMU");
        }
        list.add(p1);
        return list;
    }
    
    protected Collection<RTPPayload> getVideoMediaPayloads() {
        List<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
        return list;
    }

    protected MediaMimeTypes getAudioMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        return mediaMimeTypes;
    }

    protected MediaMimeTypes getVideoMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        return mediaMimeTypes;
    }
}
