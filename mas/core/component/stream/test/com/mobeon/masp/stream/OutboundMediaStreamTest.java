/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.mock.MockCallSession;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

/**
 * Testclass for OutboundMediaStream.
 * 
 * @author Jörgen Terner
 */
public class OutboundMediaStreamTest extends MediaStreamSupportTest {

    /* JavaDoc in base class. */
    public void setUp() {
        super.setUp();
    }

    /* JavaDoc in base class. */
    public void tearDown() {
        super.tearDown();
    }

    /* JavaDoc in base class. */
    protected IMediaStream getStreamInstance() {
        return mFactory.getOutboundMediaStream();
    }

    /* JavaDoc in base class. */
    protected IMediaStream getCreatedStream() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        IOutboundMediaStream stream =
            mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("getCreatedStream: " +
                "Unexpected exception while calling create: " + e);
        }
        return stream;
    }

    /**
     * <pre>
     * Tests for method create.
     * 
     * Wrong state
     * -----------
     * Condition: No Event dispatcher is set.
     * Action:
     *    1. mediaProperties=null, connectionProperties=null
     *    2. mediaProperties=null, connectionProperties=not null
     *    3. mediaProperties=not null, connectionProperties=null
     *    4. mediaProperties=not null, connectionProperties=not null
     * Result:
     *    1-3. IllegalArgumentException.
     *    4.   IllegalStateException.
     *
     * Wrong arguments
     * ---------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    1. mediaProperties=null, connectionProperties=null
     *    2. mediaProperties=null, connectionProperties=not null
     *    3. mediaProperties=not null, connectionProperties=null
     * Result: IllegalArgumentException.
     * 
     * Correct state and arguments
     * ---------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    mediaProperties=not null, connectionProperties=not null
     * Result: No exceptions are thrown.
     * 
     * Create on deleted stream
     * ------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    Call create, delete and create again on the same stream.
     * Result: IllegalStateException.
     * 
     * Create on created stream
     * ------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    Call create twice on the same stream.
     * Result: IllegalStateException.
     * 
     * No audiohost in connection properties
     * -------------------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    Call create when no host is set in the connection properties.
     * Result: IllegalArgumentException.
     * 
     * No audioport in connection properties
     * -------------------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    Call create when no audioport is set in the connection properties.
     * Result: IllegalArgumentException.
     * 
     * Videohost without videoport in connection properties
     * ----------------------------------------------------
     * Condition: An non-null Event dispatcher is set.
     * Action:
     *    Call create when no audioport is set in the connection properties.
     * Result: IllegalArgumentException.
     * </pre>
     */
    public void testCreate() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        IOutboundMediaStream stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();


        // Wrong state
        Collection[] testPayloads = {null, payloads};
        ConnectionProperties[] testConnectionProp =
            new ConnectionProperties[] {null, cProp};
        Class[] key = new Class[] {
            IllegalArgumentException.class,
            IllegalArgumentException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
        };
        for (int i = 0; i < testPayloads.length; i++) {
            for (int j = 0; j < testConnectionProp.length; j++) {
                try {
                    stream.create(testPayloads[i], testConnectionProp[j]);
                    fail("Exception expected when no event dispatcher " +
                            "is set (index i=" + i + " j=" + j + ").");
                }
                catch (Exception e) {
                    JUnitUtil.assertException("Unexpected exception while " +
                            "calling create (index i=" + i + " j=" + j + ").",
                            key[i*testConnectionProp.length + j], e);
                }
            }
        }

        // Wrong arguments
        Object[] testProps = new Object[] {
            null, null, null, cProp, payloads, null
        };
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        for (int i = 0; i < testProps.length; i+=2) {
            try {
                stream.create((Collection)testProps[i],
                        (ConnectionProperties)testProps[i+1]);
                fail("IllegalArgumentException expected (index i=" + i + ").");
            }
            catch (Exception e) {
                JUnitUtil.assertException("Unexpected exception while " +
                        "calling create (index i=" + i + ").",
                        IllegalArgumentException.class, e);
            }
        }
//        stream.delete();

        // Correct state and arguments
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state and arguments: " +
                "Unexpected exception while calling create: " + e);
        }

        // Create on deleted stream
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Create on deleted stream: " +
                "Unexpected exception while calling create: " + e);
        }
        try {
            stream.delete();
        }
        catch (Exception e) {
            fail("Create on deleted stream: " +
                "Unexpected exception while calling delete: " + e);
        }
        try {
            stream.create(payloads, cProp);
            fail("Create on deleted stream should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Create on deleted stream: " +
                "Unexpected exception.",
                IllegalStateException.class, e);
        }
//        stream.delete();

        // Create on created stream
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Create on created stream: " +
                "Unexpected exception while calling create: " + e);
        }
        try {
            stream.create(payloads, cProp);
            fail("Create on created stream should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Create on created stream: " +
                "Unexpected exception.",
                IllegalStateException.class, e);
        }
        stream.delete();


        // No audiohost in connection properties
        cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
            fail("No host in connection properties should throw an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("No host in connection properties: " +
                    "Unexpected exception.", IllegalArgumentException.class, e);
        }
//        stream.delete();

        // No audioport in connection properties
        cProp = new ConnectionProperties();
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        cProp.setAudioHost(remoteHostAddress);
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
            fail("No audioport in connection properties should throw an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("No audioport in connection properties: " +
                    "Unexpected exception.", IllegalArgumentException.class, e);
        }
//        stream.delete();

        // Videohost without videoport in connection properties
        cProp = new ConnectionProperties();
        cProp.setAudioHost(remoteHostAddress);
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setVideoHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        stream = mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
            fail("Videohost without videoport should throw an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Videohost without videoport: " +
                    "Unexpected exception.", IllegalArgumentException.class, e);
        }
//        stream.delete();
    }

    /**
     * <pre>
     * Simple testcases for method play. 
     * Se separate testfile for more complex testcases.
     * 
     * Wrong arguments
     * ---------------
     * Condition: An Outbound stream is created.
     * Action: Given the following arguments:
     *     callId = new Object(), null
     *     mediaObject = MediaObject instance, null, 
     *                   mutable MediaObject instance
     *     playOption = PlayOption.WAIT_FOR_AUDIO, null
     *     cursor = 0, -1
     *     
     *     Call play with all combinations of argument values.     
     * Result:
     *     IllegalArgumentException in all cases, except when all 
     *     argument values have index 0.
     * 
     * Play before create 
     * ------------------
     * Action: Call play when create has not been called. 
     * Result: IllegalStateException. 
     * 
     * Play on deleted stream
     * ---------------------- 
     * Condition: An Outbound stream is created. 
     * Action: Call delete followed by a play. 
     * Result: IllegalStateException.
     * </pre> 
     */
    public void testAudioPlay() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        // Wrong arguments
        final Object[] CALLIDS = new Object[] {
                new Object(), null
        };
        final IMediaObject[] MEDIA_OBJECTS = new IMediaObject[] {
                createAudioMediaObject(), null, createRecordableMediaObject()
        };
        final PlayOption[] PLAY_OPTIONS = new PlayOption[] {
                PlayOption.WAIT_FOR_AUDIO, null
        };
        final int[] CURSORS = new int[] {
                0, -1
        };
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(PlayFinishedEvent.class));
        for (int callId = 0; callId < CALLIDS.length; callId++) {
            for (int mo = 0; mo < MEDIA_OBJECTS.length; mo++) {
                for (int po = 0; po < PLAY_OPTIONS.length; po++) {
                    for (int c = 0; c < CURSORS.length; c++) {
                        IOutboundMediaStream stream = 
                            mFactory.getOutboundMediaStream();
                        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
                        stream.setCallSession(MockCallSession.getSession());
                        try {
                            stream.create(payloads, cProp);
                        } 
                        catch (Exception e) {
                            fail("Wrong arguments (" + callId + ", " +
                                  mo + ", " + po + ", " + c + 
                                  "): Unexpected exception while calling create: " + 
                                  e);
                        }
                        if ((callId == 0) && (mo == 0) && (po == 0) && (c == 0)) {
                            try {
                                stream.play(CALLIDS[callId], MEDIA_OBJECTS[mo], PLAY_OPTIONS[po], CURSORS[c]);
                            }
                            catch (Exception e) {
                                fail("Wrong arguments: For indexes==0 no exception should occur: " + e);          
                            }
                        }
                        else {
                            try {
                                stream.play(CALLIDS[callId], MEDIA_OBJECTS[mo], PLAY_OPTIONS[po], CURSORS[c]);
                                fail("Wrong arguments (" + callId + ", " + mo + ", " +
                                     po + ", " + c + ") should case an exception.");
                            }
                            catch (Exception e) {
                                JUnitUtil.assertException("Wrong arguments (" + callId + 
                                    ", " + mo + ", " + po + ", " + c +
                                    ") Unexpected exception.",
                                    IllegalArgumentException.class, e);
                            }
                        }
                        // stream.delete();
                    }
                }
            }
        }

        // Play before create
        OutboundMediaStreamImpl stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.play(new Object(), createAudioMediaObject(), PlayOption.WAIT_FOR_AUDIO, 0);
            fail("Play before create should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Play before create: " +
                "Unexpected exception.",
                IllegalStateException.class, e);
        }
//        stream.delete();

        // Play after delete
        stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
            stream.delete();
        } 
        catch (Exception e) {
            fail("Play after delete: Unexpected exception while calling " +
                "create/delete: " + e);
        }
        try {
            stream.play(new Object(), createAudioMediaObject(), PlayOption.WAIT_FOR_AUDIO, 0);
            fail("Play after delete should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Play after delete: " +
                "Unexpected exception.",
                IllegalStateException.class, e);           
        }
//        stream.delete();
    }

    /**
     * <pre>
     * Simple testcases for method play. 
     * Se separate testfile for more complex testcases.
     * 
     * Single play
     * -----------
     * Condition: An Outbound stream is created.
     * Action: Call play 
     * Result:  
     *     No exceptions are thrown. A PlayFinishedEvent is sent to the
     *     event dispatcher. 
     * </pre> 
     */
    public void testSingleAudioPlay() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT); // 4712
        cProp.setAudioHost(remoteHostAddress);//remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        // Single play
        OutboundMediaStreamImpl stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent").with(isA(PlayFinishedEvent.class));
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        
        try {
            Object callId = new Object();
            stream.play(callId, createAudioMediaObject(LONG_AUDIOFILE_NAME), PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Unexpected exception while calling play: " + e);
        }
    }
    
    /**
     * <pre>
     * Tests to stop a play. 
     * 
     * Stop ongoing play
     * -----------------
     * Condition: An Outbound stream is created.
     * Action: 
     *    1. Call play
     *    2. Wait a short time and call stop before the play has finished
     *    3. Call play again
     *     
     * Result:  
     *     No exceptions are thrown. 2 PlayFinishedEvent:s is sent to the
     *     event dispatcher.
     * </pre> 
     */
    public void testStop() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT); // 4712
        cProp.setAudioHost(remoteHostAddress);//remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        // Stop ongoing play
        OutboundMediaStreamImpl stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(JUnitUtil.getCountMatcher(2)).method("fireEvent").with(isA(PlayFinishedEvent.class));
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        
        try {
            Object callId = new Object();
            stream.play(callId, createAudioMediaObject(LONG_AUDIOFILE_NAME), PlayOption.WAIT_FOR_AUDIO, 0);
            Thread.sleep(500);
            stream.stop(callId);
            callId = new Object();
            stream.play(callId, createAudioMediaObject(LONG_AUDIOFILE_NAME), PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Unexpected exception while calling play: " + e);
        }
    }

    /**
     * <pre>
     * Tests play of multiple audio media objects.
     * 
     */
    public void testMultipleAudioPlay() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT); // 4712
        cProp.setAudioHost(remoteHostAddress);//remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        OutboundMediaStreamImpl stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(PlayFinishedEvent.class));
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        
        // Wrong arguments
        try {
            Object callId = new Object();
            stream.play(callId, (IMediaObject[])null, 
                    PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            fail("Wrong arguments should have caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong arguments: Unexpected exception.",
                    IllegalArgumentException.class, e);
        }

        // Empty array
        IMediaObject[] emptyArray = new IMediaObject[] {};
        try {
            Object callId = new Object();
            stream.play(callId, emptyArray, PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Empty array: Unexpected exception while calling play: " + e);
        }
        
        // Non-empty array
        IMediaObject[] mos = new IMediaObject[] {
                createAudioMediaObject(FILENAME),
                createAudioMediaObject(LONG_AUDIOFILE_NAME),
                createAudioMediaObject(FILENAME)
        };
        try {
            Object callId = new Object();
            stream.play(callId, mos, PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Non-empty array: Unexpected exception while calling play: " +
                    e);
        }
    }

    /**
     * Verifying that it is possible to perform play on raw media objects.
     * A raw media object is a media object containing raw PCM data only.
     * For example an .au file containing u-law data (G.711/u-law or audio/PCMU).
     * In this test we will attempt to play an .au file.
     */
    public void testTranslated() {
        byte[] data = null;
        MimeType mimeType = null;

        try {
            File input = new File("nativestreamhandling/medialibrary/mtest/one.au");
            FileInputStream inputStream = new FileInputStream(input);
            int length = (int)input.length();
            data = new byte[length];
            int readBytes = inputStream.read(data, 0, length);
            mimeType = new MimeType("audio/pcmu");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MimeTypeParseException e) {
            e.printStackTrace();
        }

        List<ByteBuffer> audioBufferList = new LinkedList<ByteBuffer>();
        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(data.length);
        audioBuffer.put(data);
        audioBufferList.add(audioBuffer);

        MediaProperties mediaProperties = new MediaProperties(mimeType);
        MediaObjectFactory factory = new MediaObjectFactory(10000);
        IMediaObject mo = factory.create(audioBufferList, mediaProperties);

        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        // Syncronous sequence
        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(PlayFinishedEvent.class));

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }

        Object callId = new Object();

        // Setting fields that are supposed to be set by play()
        stream.setRequestId(callId);
        stream.setPlayOption(PlayOption.WAIT_FOR_AUDIO);
        stream.setCursor(0);

        try {
            // "Returning" managed object
            stream.translationDone(mo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * <pre>
     * Tests for method cancel.
     * 
     * Wrong state
     * -----------
     * Condition: 
     *    1. Create has not been called.
     *    2. Create followed by delete have been called.
     * Action: Call cancel.
     * Result: IllegalStateException.
     * 
     * Correct state
     * -----------
     * Condition: 
     *    1. Create has been called.
     *    2. Create has been called, play has been called (async).
     * Action: Call cancel.
     * Result: No exceptions are thrown.
     * </pre>
     */
    public void testCancel() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        OutboundMediaStreamImpl stream = null;

        // Wrong state 1
        stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        try {
            stream.cancel();
            fail("Wrong state 1 should has caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                    IllegalStateException.class, e);
        }
//        stream.delete();

        // Wrong state 2
        stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Wrong state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        stream.delete();
        try {
            stream.cancel();
            fail("Wrong state 2 should has caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 2: Unexpected exception.",
                    IllegalStateException.class, e);
        }
  //      stream.delete();

        // Correct state 1
        stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state 1: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            stream.cancel();
        }
        catch (Exception e) {
            fail("Correct state 1: Unexpected exception: " + e);
        }
        stream.delete();

        // Correct state 2
        tearDown();
        setUp();
        stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent").
            with(isA(PlayFinishedEvent.class));
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            stream.play(new Object(), createAudioMediaObject(),
                    PlayOption.DO_NOT_WAIT, 0);
            stream.cancel();
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
        stream.delete();
    }

    public void testPlayVideo() {
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setAudioHost("10.16.2.133");//remoteHostAddress);//"150.132.5.213");//"10.16.2.133");
        cProp.setVideoHost("10.16.2.133");//remoteHostAddress);//"150.132.5.213");//"10.16.2.133");
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(PlayFinishedEvent.class));
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, createVideoMediaObject(VIDEO_FILENAME), PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
    }

    public void testSendTokens() {
        ControlToken[] tokens = new ControlToken[200];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = new ControlToken(ControlToken.toToken(i%17), i % 64, i*10 + 14);
        }
        tokens[3] = new ControlToken(ControlToken.DTMFToken.SILENCE_BETWEEN_TOKENS,
                33, 200);

        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);//"150.132.5.213");//"10.16.2.133");
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            stream.send(tokens);
            Thread.sleep(4000);
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
        stream.delete();
    }

    public static void main(String argv[]) {
        OutboundMediaStreamTest test = new OutboundMediaStreamTest();
        test.setUp();
        test.testStop();
        test.tearDown();
    }
}
