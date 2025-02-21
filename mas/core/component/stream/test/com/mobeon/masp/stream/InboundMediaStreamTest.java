/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.util.Collection;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.mock.MockCallSession;

/**
 * Testclass for InboundMediaStream.
 * 
 * @author Jörgen Terner
 */
public class InboundMediaStreamTest extends MediaStreamSupportTest {
    /**
     * <pre>
     * Tests for method create.
     *
     * Wrong state
     * ------------
     * Condition: No Event dispatcher is set.
     * Action:
     *    1. mediaProperties=null
     *    2. mediaProperties=not null
     * Result:
     *    1. IllegalArgumentException.
     *    2. IllegalStateException.
     *
     * Wrong arguments
     * ---------------
     * Condition: An non-null Event dispatcher is set.
     * Action: mediaProperties=null
     * Result: IllegalArgumentException.
     *
     * Correct state and arguments
     * ---------------------------
     * Condition: A non-null Event dispatcher is set.
     * Action: mediaProperties = not null
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
     * </pre>
     */
    public void testCreate() {
        MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();

        // Wrong state
        MediaMimeTypes[] testArgs = new MediaMimeTypes[] {null, mediaMimeTypes};
        Class[] key = new Class[] {
            IllegalArgumentException.class,
            IllegalStateException.class
        };
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        for (int i = 0; i < testArgs.length; i++) {
            try {
                stream.setCallSession(MockCallSession.getSession());
                stream.create(testArgs[i]);
                fail("Exception expected when no event dispatcher " +
                        "is set (index i=" + i + ").");
            }
            catch (Exception e) {
                JUnitUtil.assertException("Unexpected exception while " +
                        "calling create (index i=" + i + ").", key[i], e);
            }
        }
//        stream.delete();

        // Wrong arguments
/*
        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create((MediaMimeTypes)null);
            fail("IllegalArgumentException expected for create(null)");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Unexpected exception while " +
                    "calling create(null)",
                    IllegalArgumentException.class, e);
        }
*/
//        stream.delete();

        // Correct state and arguments
        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(mediaMimeTypes);
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state and arguments: " +
                "Unexpected exception while calling create: " + e);
        }
        // Create on deleted stream
        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(mediaMimeTypes);
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
            stream.create(mediaMimeTypes);
            fail("Create on deleted stream should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Create on deleted stream: " +
                "Unexpected exception.",
                IllegalStateException.class, e);
        }

        // Create on created stream
        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        stream.setCallSession(MockCallSession.getSession());
        try {
            stream.create(mediaMimeTypes);
        }
        catch (Exception e) {
            fail("Create on created stream: " +
                "Unexpected exception while calling create: " + e);
        }
        try {
            stream.create(mediaMimeTypes);
            fail("Create on mediaMimeTypes stream should case an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Create on created stream: " +
                "Unexpected exception.",
                IllegalStateException.class, e);
        }
//        stream.delete();
    }

    /* JavaDoc in base class. */
    protected IMediaStream getStreamInstance() {
        return mFactory.getInboundMediaStream();
    }

    /* JavaDoc in base class. */
    protected IMediaStream getCreatedStream() {
        MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();

        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        }
        catch (Exception e) {
            fail("getCreatedStream: " +
                "Unexpected exception while calling create: " + e);
        }
        return stream;
    }

    /**
     * <pre>
     * Tests for method getFractionLost.
     * 
     * Wrong state
     * ------------
     * Condition:
     *    1. No Event dispatcher is set.
     *    2. Event dispatcher set, create not called.
     *    3. Create is called, followed by a call to delete.
     * Action: Call method.
     * Result: IllegalStateException

     * Correct state
     * ------------
     * Condition:
     *    1. Event dispatcher is set.
     *    2. Create has been called.
     * Action: Call method.
     * Result: 0
     * </pre>
     */
    public void testGetFractionLost() {
        IInboundMediaStream stream = mFactory.getInboundMediaStream();

        //Wrong state 1
        try {
            stream.getFractionLost();
            fail("Wrong state 1 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                IllegalStateException.class, e);
        }
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());

        //Wrong state 2
        try {
            stream.getFractionLost();
            fail("Wrong state 2 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 2: Unexpected exception.",
                IllegalStateException.class, e);
        }
 //       stream.delete();

        //Wrong state 3
        stream = (IInboundMediaStream)getCreatedStream();
        stream.delete();
        try {
            stream.getFractionLost();
            fail("Wrong state 3 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 3: Unexpected exception.",
                IllegalStateException.class, e);
        }

        // Correct state
        stream = (IInboundMediaStream)getCreatedStream();
        try {
            assertEquals("Correct state: Unexpected result.",
                    0, stream.getFractionLost());
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state: Unexpected exception: " + e);
        }
//        stream.delete();
    }

    /**
     * <pre>
     * Tests for method getPTime().
     * 
     * Wrong state
     * ------------
     * Condition:
     *    1. No Event dispatcher is set.
     *    2. Event dispatcher set, create not called.
     * Action: Call method.
     * Result: IllegalStateException
     * 
     * Correct state
     * ------------
     * Condition:
     *    Event dispatcher is set and create has been called.
     * Action: Call method.
     * Result: No exeptions are thrown.
     * </pre>
     */
    public void testGetPTime() {

        // Wrong state
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        try {
            stream.getPTime();
            fail("Wrong state should cause an exception");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state: Unexpected exception",
                    IllegalStateException.class, e);
        }
//        stream.delete();

        // Correct state
        stream = (IInboundMediaStream)getCreatedStream();
        try {
            stream.getPTime();
        }
        catch (Exception e) {
            fail("Correct state: Unexpected exception: " + e);
        }
        stream.delete();
    }

    /**
     * <pre>
     * Tests for method getHost().
     * 
     * Wrong state
     * ------------
     * Condition:
     *    1. No Event dispatcher is set.
     *    2. Event dispatcher set, create not called.
     * Action: Call method.
     * Result: No exeptions are thrown. Non-null result is returned.
     * 
     * Correct state
     * ------------
     * Condition:
     *    Event dispatcher is set and create has been called.
     * Action: Call method.
     * Result: No exeptions are thrown. Non-null result is returned.
     * </pre>
     */
    public void testGetHost() {

        // Wrong state
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        try {
            assertNotNull("Wrong state: Address of local host should " +
                    "never be null.", stream.getHost());
        }
        catch (Exception e) {
            fail("Wrong state: Unexpected exception: " + e);
        }
//        stream.delete();

        // Correct state
        stream = (IInboundMediaStream)getCreatedStream();
        try {
            assertNotNull("Correct state: Address of local host should " +
                    "never be null.", stream.getHost());
            System.out.println("HOST=" + stream.getHost());
        }
        catch (Exception e) {
            fail("Correct state: Unexpected exception: " + e);
        }
        stream.delete();
    }

    /**
     * <pre>
     * Tests for method getCumulativePacketLost.
     * 
     * Wrong state
     * ------------
     * Condition:
     *    1. No Event dispatcher is set.
     *    2. Event dispatcher set, create not called.
     *    3. Create is called, followed by a call to delete.
     * Action: Call method.
     * Result: IllegalStateException
     * 
     * Correct state
     * ------------
     * Condition:
     *    1. Event dispatcher is set.
     *    2. Create has been called.
     * Action: Call method.
     * Result: 0
     * </pre>
     */
    public void testGetCumulativePacketLost() {
        IInboundMediaStream stream = mFactory.getInboundMediaStream();

        //Wrong state 1
        try {
            stream.getCumulativePacketLost();
            fail("Wrong state 1 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 1: Unexpected exception.",
                IllegalStateException.class, e);
        }
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        //Wrong state 2
        try {
            stream.getCumulativePacketLost();
            fail("Wrong state 2 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 2: Unexpected exception.",
                IllegalStateException.class, e);
        }
//        stream.delete();

        //Wrong state 3
        stream = (IInboundMediaStream)getCreatedStream();
        stream.delete();
        try {
            stream.getCumulativePacketLost();
            fail("Wrong state 3 should cause an IllegalStateException.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong state 3: Unexpected exception.",
                IllegalStateException.class, e);
        }

        // Correct state
        stream = (IInboundMediaStream)getCreatedStream();
        try {
            assertEquals("Correct state: Unexpected result.",
                    0, stream.getCumulativePacketLost());
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state: Unexpected exception: " + e);
        }
//        stream.delete();
    }


    /**
     * <pre>
     * Performs a number of recordings in sequence on the same stream instance.
     * 
     * Syncronous sequence
     * -------------------
     * Condition: A created InboundMediaStreamImpl instance.
     * Action: 
     *     Call record a number of times, each call waits for the previous one
     *     to finish before making the call.
     * Result: No exceptions occur
     * 
     * Asyncronous sequence
     * --------------------
     * Condition: A created InboundMediaStreamImpl instance.
     * Action: 
     *     Call record a number of times. Each recording
     *     is started directly after the previous one.
     * Result: UnsupportedOperationException
     * </pre>
     */
    public void testSequentialAudioRecord() {
        final int NROFRECORDS = 3;
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mProp = getAudioMediaMimeTypes();

        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(2000);
        MediaStreamSupport.updateConfiguration();

        // Syncronous sequence
        InboundMediaStreamImpl stream =
            (InboundMediaStreamImpl)mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(RecordFinishedEvent.class));
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        for (int i = 0; i < NROFRECORDS; i++) {
            try {
                Object callId = new Object();
                stream.record(callId, createRecordableMediaObject(), prop);
            }
            catch (Exception e) {
                fail("Syncronous sequence: " +
                        "Unexpected exception during record: " + e);
            }
        }
        stream.delete();

        // Asyncronous sequence
        stream = (InboundMediaStreamImpl)mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mProp);
        }
        catch (Exception e) {
            fail("syncronous sequence: Unexpected exception while calling create: " + e);
        }
        Object callId = null;
        prop.setWaitForRecordToFinish(false);
        try {
            for (int i = 0; i < NROFRECORDS; i++) {
                callId = new Object();
                stream.record(callId, createRecordableMediaObject(), prop);
            }
            fail("Asyncronous sequence should throw an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Asyncronous sequence: " +
                    "Unexpected exception.",
                    UnsupportedOperationException.class, e);
        }
        stream.delete();
    }

    /**
     * <pre>
     * Tests for method record when a play mediaobject is passed as argument.
     * 
     * 
     * ------------
     * Condition:
     * Action:
     * Result:
     */
    public void testPlayBeforeRecord() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        prop.setMaxRecordingDuration(3*1000);
        MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();
        Collection<RTPPayload> payloads = getAudioMediaPayloads();

        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(4000);
        MediaStreamSupport.updateConfiguration();

        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());

        IOutboundMediaStream outboundStream = mFactory.getOutboundMediaStream();
        outboundStream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(atLeastOnce()).method("fireEvent").with(isA(RecordFinishedEvent.class));
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
            outboundStream.setCallSession(MockCallSession.getSession());
            outboundStream.create(payloads, cProp, null, stream);
        }
        catch (Exception e) {
            fail("Failed to create stream: " + e);
        }
        try {
            stream.record(new Object(),
                    createAudioMediaObject(LONG_AUDIOFILE_NAME),
                    outboundStream,
                    createRecordableMediaObject(), prop);
        }
        catch (Exception e) {
            fail("Unexpected exception during record: " + e);
        }
        stream.delete();
    }

    /**
     * <pre>
     * Tests for method record.
     * 
     * Wrong state
     * ------------
     * Condition:
     *     1. Create is not called.
     *     2. Create is called and then delete.
     * Action: Call method with non-null arguments.
     * Result: IllegalStateException.
     * 
     * Wrong arguments
     * ---------------
     * Condition: Create is called.
     * Action:
     *     1. callId == null, mediaObject != null, recordingProperties != null.
     *     2. callId =! null, mediaObject == null, recordingProperties != null.
     *     3. callId != null, mediaObject != null, recordingProperties == null.
     *     4. callId != null, mediaObject != null and is immutable, 
     *        recordingProperties != null.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Condition: Create is called.
     * Action:
     *     1. callId != null.
     *        mediaObject != null and is not immutable. 
     *        recordingProperties != null.
     *        prop.setMaxRecordingDuration(2).
     *        prop.setMinRecordingDuration(1).
     *        play is called after record.
     *     2. callId != null.
     *        mediaObject != null and is not immutable. 
     *        recordingProperties != null.
     *        prop.setMaxRecordingDuration(2).
     *        prop.setMinRecordingDuration(0). 
     *        play is called after record.
     *     3. callId != null.
     *        mediaObject != null and is not immutable. 
     *        recordingProperties != null.
     *        prop.setMaxRecordingDuration(2).
     *        prop.setMinRecordingDuration(0).
     *        Call delete on the stream directly after the call to record. 
     *        play is called after record.
     *     4. callId != null.
     *        mediaObject != null and is not immutable. 
     *        recordingProperties != null.
     *        prop.setMaxRecordingDuration(2).
     *        prop.setMinRecordingDuration(0).
     *        Call stop on the stream directly after the call to record. 
     *        play is called after record.
     * Result:
     *     1. isImmutable() == false.
     *        recordFailed is called once with cause = MIN_RECORDING_DURATION.
     *        the play returns without.
     *     2. isImmutable() == true.
     *        recordFinished is called once with cause = STREAM_ABANDONED.
     *        the media object can be played on an outbound stream.
     *        playFinished is called once with cause = PLAY_FINISHED.
     *     3. isImmutable() == true.
     *        recordFinished is called once with cause = STREAM_DELETED.
     *        the media object can be played on an outbound stream.
     *        playFinished is called once with cause = PLAY_FINISHED.
     *     3. isImmutable() == true.
     *        recordFinished is called once with cause = RECORDING_STOPPED.
     *        the media object can be played on an outbound stream.
     *        playFinished is called once with cause = PLAY_FINISHED.
     * </pre>
     */
    public void testRecord() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();

        // Wrong state 1
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(2000);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.record(new Object(), super.createRecordableMediaObject(), prop);
            fail("Wrong state 1 should cause an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong State 1: Unexpected exception.",
                    IllegalStateException.class, e);
        }
//        stream.delete();

        // Wrong state 2
        stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
            stream.delete();
            stream.record(new Object(), createRecordableMediaObject(), prop);
            fail("Wrong state 2 should cause an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong State 2: Unexpected exception.",
                    IllegalStateException.class, e);
        }
//        stream.delete();

        // Wrong arguments
        Object[] illegalCallIds = new Object[] {
                null, new Object(), new Object(), new Object()
        };
        RecordingProperties[] illegalProp = new RecordingProperties[] {
                prop, prop, null, prop
        };
        IMediaObject mo = createRecordableMediaObject();
        IMediaObject immutableMo = createRecordableMediaObject();
        immutableMo.setImmutable();
        IMediaObject[] illegalMos = new IMediaObject[] {
                mo, null, mo, immutableMo
        };
        for (int i = 0; i < illegalCallIds.length; i++) {
            stream = mFactory.getInboundMediaStream();
            stream.setCallSession(MockCallSession.getSession());
            stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
            try {
                stream.create(mediaMimeTypes);
            }
            catch (Exception e) {
                fail("Wrong arguments " + i +
                        ": Failed to create stream: " + e);
            }
            try {
                stream.record(illegalCallIds[i], illegalMos[i],
                        illegalProp[i]);
                fail("Wrong arguments " + i + " should case an exception.");
            } catch (NullPointerException e) {
            } catch (Exception e) {
                //e.printStackTrace();
                JUnitUtil.assertException("Wrong arguments " + i +
                        ": Unexpected exception.",
                        IllegalArgumentException.class, e);
            }
 //           stream.delete();
        }

        // Correct arguments
        MyRecordTestCase[] recordTestCases = new MyRecordTestCase[] {
                // Correct arguments 1
                new MyRecordTestCase() {
                    public void run(IInboundMediaStream stream,
                                    Object callId, IMediaObject mo,
                                    RecordingProperties prop) throws StackException {
                        stream.record(callId, mo, prop);
                        assertFalse("The mediaobject should not be immutable "+
                                "when a recording has finished but " +
                                "length < minRecordingDuration.",
                                mo.isImmutable());
                        mo.getMediaProperties().setContentType(AUDIO_WAV);
                        mo.getMediaProperties().setFileExtension("wav");
                        mo.getMediaProperties().setSize(0);
                        mo.setImmutable();
                        // Check that play does not crash for the empty MO
                        play(mo);
                    }
                },
                // Correct arguments 2
                new MyRecordTestCase() {
                    public void run(IInboundMediaStream stream,
                                    Object callId, IMediaObject mo,
                                    RecordingProperties prop) throws StackException {
                        stream.record(callId, mo, prop);
                        assertTrue("The mediaobject should be immutable when "+
                                "a recording has finished.", mo.isImmutable());
                        play(mo);
                    }
                },
                // Correct arguments 3
                new MyRecordTestCase() {
                    public void run(IInboundMediaStream stream,
                                    Object callId, IMediaObject mo,
                                    RecordingProperties prop) throws StackException {
                        stream.record(callId, mo, prop);
                        stream.delete();
                        assertTrue("The mediaobject should be immutable when "+
                                "a recording has finished.", mo.isImmutable());
                        play(mo);
                    }
                },
                // Correct arguments 4
                new MyRecordTestCase() {
                    public void run(IInboundMediaStream stream,
                                    Object callId, IMediaObject mo,
                                    RecordingProperties prop) throws StackException {
                        stream.record(callId, mo, prop);
                        stream.stop(callId);
                        assertTrue("The mediaobject should be immutable when "+
                                "a recording has finished.", mo.isImmutable());
                        play(mo);
                    }
                }
        };
	// Unit is milliseconds
        int[] maxRecordingDurations = new int[] {
                2000, 2000, 2000, 2000
        };
	// Unit is milliseconds
        int[] minRecordingDurations = new int[] {
                1000, 0, 0, 0
        };
        boolean[] waitForRecordToFinish = new boolean[] {
                true, true, false, false
        };
        Object[] callIds = new Object[] {
                new Integer(1), new Integer(2), new Integer(3), new Integer(4)
        };
        StreamEvent[] events = new StreamEvent[] {
                new RecordFailedEvent(callIds[0],
                    RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION.ordinal(), null),
                new RecordFinishedEvent(callIds[1],
                    RecordFinishedEvent.CAUSE.STREAM_ABANDONED.ordinal()),
                new RecordFinishedEvent(callIds[2],
                    RecordFinishedEvent.CAUSE.STREAM_DELETED.ordinal()),
                new RecordFinishedEvent(callIds[3],
                    RecordFinishedEvent.CAUSE.RECORDING_STOPPED.ordinal())
        };

        for (int i = 0; i < recordTestCases.length; i++) {
            tearDown();
            setUp();
            StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(4000);
            MediaStreamSupport.updateConfiguration();

            stream = mFactory.getInboundMediaStream();
            stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
            mEventDispatcher.expects(once()).method("fireEvent").with(eq(events[i]));

            try {
                stream.setCallSession(MockCallSession.getSession());
                stream.create(mediaMimeTypes);
            }
            catch (Exception e) {
                fail("Record: Unexpected exception while calling create: " + e);
            }
            prop.setMaxRecordingDuration(maxRecordingDurations[i]);
            prop.setMinRecordingDuration(minRecordingDurations[i]);
            prop.setWaitForRecordToFinish(waitForRecordToFinish[i]);
            mo = createRecordableMediaObject();
            try {
                recordTestCases[i].run(stream, callIds[i], mo, prop);
            }
            catch (Exception e) {
                fail("Record: Unexpected exception while calling record: " + e);
            }
        }
    }

    private void play(IMediaObject mo) {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(remoteHostAddress);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        // Single play
        OutboundMediaStreamImpl stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        Object callId = new Object();
        PlayFinishedEvent playFinEvent = new PlayFinishedEvent(callId,
                PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0);
        mEventDispatcher.expects(once()).method("fireEvent").with(eq(playFinEvent));

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception while calling create: " + e);
        }

        try {
            stream.play(callId, mo, PlayOption.WAIT_FOR_AUDIO, 0);
            Thread.sleep(2000);
            stream.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception while calling play: " + e);
        }
    }

    abstract class MyRecordTestCase {
        public abstract void run(IInboundMediaStream stream,
                                 Object callId, IMediaObject mo,
                                 RecordingProperties prop) throws StackException;
    };

    public static void main(String argv[]) {
        InboundMediaStreamTest test = new InboundMediaStreamTest();
        test.setUp();
        test.testCreate();
        test.tearDown();
        /*
        test.setUp();
        test.testGetFractionLost();
        test.tearDown();
        test.setUp();
        test.testGetPTime();
        test.tearDown();
        test.setUp();
        test.testGetHost();
        test.tearDown();
        test.setUp();
        test.testGetCumulativePacketLost();
        test.tearDown();
        test.setUp();
        test.testCreate();
        test.tearDown();
        test.setUp();
        test.testSequentialAudioRecord();
        test.tearDown();
        test.setUp();
        test.testPlayBeforeRecord();
        test.tearDown();
        test.setUp();
        test.testRecord();
        test.tearDown();
        */
    }
 }
