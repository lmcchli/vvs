package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.mediatranslationmanager.TextToSpeech;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.mock.MockRTPSessionFactory;
import com.mobeon.masp.stream.mock.MockCallSession;
import com.mobeon.masp.execution_engine.session.ISession;

import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;

import jakarta.activation.MimeType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Contains som more complicated tests for method
 * {@link OutboundMediaStreamImpl#play(Object, IMediaObject, PlayOption, long)}. 
 * 
 * @author Jörgen Terner
 */
public class PlayTest extends MockObjectTestCase {
    private static final String LONG_FILE = "nativestreamhandling/medialibrary/mtest/test_pcmu.wav";
    private static final int LONG_FILE_LENGTH = 4810;
    private static final String MEDIUM_FILE = "nativestreamhandling/medialibrary/mtest/gillty_pcmu.wav";
    private static final int MEDIUM_FILE_LENGTH = 3010;
    private static final String SHORT_FILE = "nativestreamhandling/medialibrary/mtest/beep_pcmu.wav";
    private static final int SHORT_FILE_LENGTH = 150;
    private static final String CORRUPT_VIDEO_FILE = "nativestreamhandling/medialibrary/mtest/corrupt.mov";
    private static final int CORRUPT_VIDEO_FILE_LENGTH = 6000;
    private static final String VIDEO_FILE = "nativestreamhandling/medialibrary/mtest/test_pcmu.mov";
    private static final int VIDEO_FILE_LENGTH = 6000;
    private static final String LONG_VIDEO_FILE = "nativestreamhandling/medialibrary/mtest/UM_0604.mov";
    private static final int LONG_VIDEO_FILE_LENGTH = 23000;
    
    private static final String HOST_BRAGE = "150.132.5.213";
    private static final String HOST_BUR = "150.132.5.158";
    private static final String HOST_PC = "10.16.2.133";
    public String REMOTE_HOST = "0.0.0.0";
    
    private static final int REMOTE_AUDIO_PORT = 23000;
    
    private Mock mEventDispatcher;
    private Mock mMTM;
    private Mock mTextToSpeech;
//    private Mock mockCallSession;
    
    private StreamFactoryImpl mFactory;
    
    private MimeType VIDEO_QUICKTIME;
    private MimeType AUDIO_WAV;
    private MimeType TEXT_PLAIN;

    private int globalTranslatorInstanceCounter;

    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public PlayTest(String name)
    {
       super(name);
    }
    
    public PlayTest(String name, String ipAddress)
    {
       super(name);
       REMOTE_HOST = ipAddress;
    }

    /* JavaDoc in base class. */
    public void setUp() {        
        mEventDispatcher = mock(IEventDispatcher.class);

        mFactory = new StreamFactoryImpl();
        mFactory.setSessionFactory(new MockRTPSessionFactory());

        try {
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory.");
        }

        try {

            // Setup the MTM
            mMTM = mock(MediaTranslationManager.class);
            mTextToSpeech = mock(TextToSpeech.class);

            ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
            mFactory.setContentTypeMapper(ctm);
            cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());

            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.setMediaTranslationManager(
                    (MediaTranslationManager)mMTM.proxy());
            mFactory.init();
            
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            AUDIO_WAV = new MimeType(" audio/wav ; name=message.wav");
            TEXT_PLAIN = new MimeType("text/plain");

//            mockCallSession = mock(ISession.class);
//            mockCallSession.expects(atLeastOnce()).method("registerSessionInLogger");
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory: " + e);
        }
    }

    /**
     * <pre>
     * Tests to play a MediaObject while specifying a cursor.
     * 
     * A new MediaObject
     * -------------------
     * Condition: A created OutboundMediaStreamImpl instance.
     * Action: 
     *   1. Play a new audio MediaObject instance, cursor == 0.
     *   2. Play a new audio MediaObject instance, cursor == 500.
     *   3. Play a new audio MediaObject instance, cursor == Integer.MAX_VALUE.
     *   4. Play a new video MediaObject instance, cursor == 0.
     *   5. Play a new video MediaObject instance, cursor == 1700.
     *   6. Play a new video MediaObject instance, cursor == Integer.MAX_VALUE.
     * Result: No exceptions occur
     * 
     * The same MediaObject
     * --------------------
     * Condition: A created OutboundMediaStreamImpl instance.
     *            A created audio MediaObject instance; audioMo.
     *            A created video MediaObject instance; videoMo.
     * Action: 
     *     1. Play audioMo, cursor == 0.
     *     2. Play audioMo, cursor == 500.
     *     3. Play audioMo, cursor == Integer.MAX_VALUE.
     *     4. Play videoMo, cursor == 0.
     *     5. Play videoMo, cursor == 1700.
     *     6. Play videoMo, cursor == Integer.MAX_VALUE.
     * Result: No exceptions occur
     * </pre>
     */
    public void testCursor() {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        long[] audioCursors = new long[] {
                0, 500, Integer.MAX_VALUE, 
                0, 500, Integer.MAX_VALUE};
        IMediaObject mo = createMediaObject(LONG_FILE, AUDIO_WAV);
        IMediaObject[] audioMediaObjects = new IMediaObject[] {
                createMediaObject(LONG_FILE, AUDIO_WAV),
                createMediaObject(LONG_FILE, AUDIO_WAV),
                createMediaObject(LONG_FILE, AUDIO_WAV),
                mo,
                mo,
                mo
        };
        
        // audio
        OutboundMediaStreamImpl stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(new JUnitUtil.InvokeCountMatcher(audioMediaObjects.length)).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        
        for (int i = 0; i < audioMediaObjects.length; i++) {
            try {
                Object callId = new Object();
                stream.play(callId, audioMediaObjects[i], 
                    PlayOption.WAIT_FOR_AUDIO, audioCursors[i]);
                stream.getEventNotifier().waitForCallToFinish(callId);
            }
            catch (Exception e) {
                fail("Testcase " + i + 
                        ": Unexpected exception during play: " + e);
            }
        }
        stream.delete();

        // video
        long[] videoCursors = new long[] {
                5555, 11111, Integer.MAX_VALUE, 
                5555, 11111, Integer.MAX_VALUE};
        IMediaObject videoMo = 
            createMediaObject(LONG_VIDEO_FILE, VIDEO_QUICKTIME);
        IMediaObject[] videoMediaObjects = new IMediaObject[] {
                createMediaObject(LONG_VIDEO_FILE, VIDEO_QUICKTIME),
                createMediaObject(LONG_VIDEO_FILE, VIDEO_QUICKTIME),
                createMediaObject(LONG_VIDEO_FILE, VIDEO_QUICKTIME),
                videoMo,
                videoMo,
                videoMo
        };
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setVideoHost(REMOTE_HOST);
        payloads = getVideoMediaPayloads();
        stream = 
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(new JUnitUtil.InvokeCountMatcher(videoMediaObjects.length)).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        
        for (int i = 0; i < videoMediaObjects.length; i++) {
            try {
                Object callId = new Object();
                stream.play(callId, videoMediaObjects[i], 
                    PlayOption.WAIT_FOR_AUDIO, videoCursors[i]);
                stream.getEventNotifier().waitForCallToFinish(callId);
            }
            catch (Exception e) {
                fail("Testcase " + i + 
                        ": Unexpected exception during play: " + e);
            }
        }
        stream.delete();
    }

    /**
     * <pre>
     * Streams a number of different media files in sequence over the
     * same stream instance.
     * 
     * Syncronous sequence
     * -------------------
     * Condition: A created OutboundMediaStreamImpl instance.
     * Action: 
     *     Play a sequence consisting of three different media files,
     *     a longer file, a shorter file and a medium file. The sequence
     *     is played twice.
     * Result: No exceptions occur
     * 
     * Asyncronous sequence
     * --------------------
     * Condition: A created OutboundMediaStreamImpl instance.
     * Action: 
     *     Play a sequence consisting of three different media files,
     *     a longer file, a shorter file and a medium file. Each play
     *     is started directly after the previous one.
     * Result: UnsupportedOperationException
     * </pre>
     */
    public void testSyncronousSequentialAudioPlay() {
        final int NROFPLAYS = 6;
        
        final String[] FILENAMES = new String[] {
            LONG_FILE, SHORT_FILE
        };

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
        mEventDispatcher.expects(new JUnitUtil.InvokeCountMatcher(NROFPLAYS)).method("fireEvent")
                .with(isA(PlayFinishedEvent.class))
                .will(new MyPlayFinishedStub());
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        } 
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
    	try {
    	    Object callId = new Object();
    	    for (int i = 0; i < NROFPLAYS; i++) {
                    stream.play(callId, 
    			    createMediaObject(FILENAMES[i % FILENAMES.length], 
    					      AUDIO_WAV), 
    			    PlayOption.WAIT_FOR_AUDIO, 0);
                    stream.getEventNotifier().waitForCallToFinish(callId);
                }
            }
    	catch (Exception e) {
    	    fail("Syncronous sequence: Unexpected exception during play: " + e);
    	}
        stream.delete();
  }

// TODO: check this
// Was the intention that when one shold not be allowed to execute play upon a
// session with different call IDs, or what?

//    public void testAsyncronousSequentialAudioPlay() {
//        final int NROFPLAYS = 6;
//
//        final String[] FILENAMES = new String[] {
//            LONG_FILE, SHORT_FILE
//        };
//
//        Collection<RTPPayload> payloads = getAudioMediaPayloads();
//        ConnectionProperties cProp = new ConnectionProperties();
//        cProp.setAudioPort(REMOTE_AUDIO_PORT);
//        cProp.setAudioHost(REMOTE_HOST);
//        cProp.setPTime(20);
//
//        int filenameIndex = 0;
//
//        // Asyncronous sequence
//        OutboundMediaStreamImpl stream = (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
//        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
//        try {
//            stream.create(payloads, cProp);
//        }
//        catch (Exception e) {
//            fail("syncronous sequence: Unexpected exception while calling create: " + e);
//        }
//        filenameIndex = 0;
//        Object callId = null;
//        try {
//            for (int i = 0; i < NROFPLAYS; i++) {
//                callId = new Object();
//                stream.play(callId,
//                    createMediaObject(FILENAMES[filenameIndex], AUDIO_WAV),
//                    PlayOption.WAIT_FOR_AUDIO, 0);
//                filenameIndex = (filenameIndex + 1) % FILENAMES.length;
//            }
//            // TODO: is this really so?
//            fail("Asyncronous sequence should throw an exception.");
//        }
//        catch (Exception e) {
//            JUnitUtil.assertException("Asyncronous sequence: " +
//                    "Unexpected exception.",
//                    UnsupportedOperationException.class, e);
//        }
//        stream.delete();
//  }
//
    /**
     * Streams a number of different media files on different stream
     * instances.
     * 
     * Parallel streams
     * ----------------
     * Action: Create 10 threads that plays different audio files
     *         at the same time. Each thread plays its task on a different
     *         stream.
     * Result: No exceptions occur.
     */
    public void testParallelAudioPlay() {
        final String[] FILENAMES = new String[] {
                LONG_FILE, MEDIUM_FILE, SHORT_FILE
        };
        final int NROFTHREADS = 10;
        
        // Parallel streams
        mEventDispatcher.expects(new JUnitUtil.InvokeCountMatcher(NROFTHREADS)).method("fireEvent");
        ExecutorService threadPool = Executors.newFixedThreadPool(NROFTHREADS);
        Object[] tasks = new Object[NROFTHREADS];
        IInboundMediaStream[] inbounds = new IInboundMediaStream[NROFTHREADS];
        int filenameIndex = 0;
        try {
            for (int i = 0; i < tasks.length; i++) {
                inbounds[i] = mFactory.getInboundMediaStream();                
                MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();//getVideoMediaMimeTypes();
                inbounds[i].setCNAME("FOOBAR");
                inbounds[i].setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());                
                inbounds[i].setCallSession(MockCallSession.getSession());
                try {
                    inbounds[i].create(mediaMimeTypes);
                }
                catch (Exception e) {
                    fail("Record: Unexpected exception while calling create: " + e);
                }
                
                OutboundMediaStreamImpl rtp = 
                    (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
                    rtp.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
                tasks[i] = 
                    threadPool.submit(new PlayAudioTask(rtp, 
                            FILENAMES[filenameIndex], 23030 +i*2));
                filenameIndex = (filenameIndex + 1) % FILENAMES.length;
            }
        }
        catch (Exception e) {
            fail("Parallel streams: Unexpected exception: " + e);
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            // Do nothing
        }
    }

    public void testPortPoolFailDuringPlay() {
        final String[] FILENAMES = new String[] {
                LONG_FILE, MEDIUM_FILE, SHORT_FILE
        };
        final int NROFTHREADS = 1000;
        
        // Parallel streams
        ExecutorService threadPool = Executors.newFixedThreadPool(NROFTHREADS);
        Object[] tasks = new Object[NROFTHREADS];
        int filenameIndex = 0;
        try {
            for (int i = 0; i < tasks.length; i++) {
                OutboundMediaStreamImpl rtp = 
                    (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
                    rtp.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
                tasks[i] = 
                    threadPool.submit(new PlayAudioTask(rtp, FILENAMES[filenameIndex], 27000+i*2));
                filenameIndex = (filenameIndex + 1) % FILENAMES.length;
            }
        }
        catch (Exception e) {
            fail("Parallel streams: Unexpected exception: " + e);
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            // Do nothing
        }
    }

    /**
     * Creates a recordable MediaObject instance.
     * 
     * @return A recordable MediaObject instance.
     */
    protected IMediaObject createRecordableMediaObject(MimeType mt) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            result = factory.create();
        }
        catch (Exception e) {
            fail("Unexpected error in createRecordableMediaObject: " + e);
        }
        assertNotNull("Created media object is NULL", result);
        result.getMediaProperties().setContentType(mt);
        return result;
    }
    
    /**
     * Streams a number of different media files in one play.
     */
    public void testMultipleMediaObjectPlay() {
        final int N_OF_MEDIA_OBJECTS = 7;

        final String[] FILENAMES = new String[] {
                SHORT_FILE, LONG_FILE
        };
        final int[] FILE_LENGTHS = new int[] {
                SHORT_FILE_LENGTH, LONG_FILE_LENGTH
        };

        IMediaObject[] mediaObjects = new IMediaObject[N_OF_MEDIA_OBJECTS];

        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        for (int i = 0; i < N_OF_MEDIA_OBJECTS; i++) {
            int index = i % FILENAMES.length;
            mediaObjects[i] = createMediaObject(FILENAMES[index], 
                    FILE_LENGTHS[index], AUDIO_WAV);
        }

        OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        try {
            Object callId = new Object();
            System.err.println("Before play");
            stream.play(callId, mediaObjects, PlayOption.WAIT_FOR_AUDIO, 
                    LONG_FILE_LENGTH + SHORT_FILE_LENGTH - 10);
            //Thread.sleep(2000);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Multiple MOs: Unexpected exception during play: " + e);
        }
        stream.delete();
    }

    public void testMultipleVideoMediaObjectPlay() {
        final int N_OF_MEDIA_OBJECTS = 5;

        final String[] FILENAMES = new String[] {
	    VIDEO_FILE, LONG_VIDEO_FILE
        };

        IMediaObject[] mediaObjects = new IMediaObject[N_OF_MEDIA_OBJECTS];

        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setVideoHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        for (int i = 0; i < N_OF_MEDIA_OBJECTS; i++) {
            mediaObjects[i] = createMediaObject(FILENAMES[i % 2], 
						VIDEO_QUICKTIME);
        }

        // Synchronous sequence
        OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, mediaObjects, PlayOption.WAIT_FOR_AUDIO, 6000);
            //Thread.sleep(2000);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Syncronous sequence: Unexpected exception during play: " + e);
        }
        stream.delete();
    }

    public void testPlayVideo() {
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setVideoHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
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
            stream.play(callId, 
                    createMediaObject(LONG_VIDEO_FILE, LONG_VIDEO_FILE_LENGTH, VIDEO_QUICKTIME), 
                    PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
    }
        
    public void testPlayCorruptVideo()
    {
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setVideoHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
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
            stream.play(callId,
                    createMediaObject(CORRUPT_VIDEO_FILE, CORRUPT_VIDEO_FILE_LENGTH, VIDEO_QUICKTIME),
                    PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
    }

    /**
     * Tests to play a sequence of mediaobjects of different types:
     * audio, text and video.
     */
    public void testMixedMediaObjectPlay() {
        final int N_OF_MEDIA_OBJECTS = 5;

        final String[] FILENAMES = new String[] {
                SHORT_FILE, "texttest",
                VIDEO_FILE, LONG_FILE, LONG_VIDEO_FILE
        };
        final MimeType[] CONTENT_TYPES = new MimeType[] {
                AUDIO_WAV, TEXT_PLAIN,
                VIDEO_QUICKTIME, AUDIO_WAV, VIDEO_QUICKTIME 
        };
        final int[] FILE_LENGTHS = new int[] {
                SHORT_FILE_LENGTH, LONG_FILE_LENGTH,
                VIDEO_FILE_LENGTH, 
                LONG_FILE_LENGTH, LONG_VIDEO_FILE_LENGTH
        };
        IMediaObject[] mediaObjects = new IMediaObject[N_OF_MEDIA_OBJECTS];

        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setVideoHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);

        for (int i = 0; i < N_OF_MEDIA_OBJECTS; i++) {
            mediaObjects[i] = createMediaObject(FILENAMES[i%FILENAMES.length],
                    FILE_LENGTHS[i%FILENAMES.length], 
                    CONTENT_TYPES[i%FILENAMES.length]);
        }

        OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        ExecutorService threadPool = Executors.newFixedThreadPool(1);

        // There is one text object among the media objects that will
        // be sent for translation to the MTM. Mock this behaviour.
        mMTM.expects(once()).method("getTextToSpeech")
            .will(returnValue(mTextToSpeech.proxy()));
        IMediaObject translatedMO = mediaObjects[0];
        mTextToSpeech.expects(once()).method("translate")
            .with(eq(mediaObjects[1]), eq(stream))
            .will(new MyTranslationStub(stream, translatedMO, threadPool));
        
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, mediaObjects, PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Unexpected exception during play: " + e);
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }        
        stream.delete();
    }

    public void testTranslation() {
        IMediaObject mediaObject = createMediaObject("texttest", TEXT_PLAIN);
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
//        stream.setCallSession((ISession)mockCallSession.proxy());

        // There is one text object among the media objects that will
        // be sent for translation to the MTM. Mock this behaviour.
        mMTM.expects(once()).method("getTextToSpeech")
                .will(returnValue(mTextToSpeech.proxy()));
        mTextToSpeech.expects(once()).method("translate")
                .with(eq(mediaObject), eq(stream))
                .will(new MtmTranslationStub(stream));
        mTextToSpeech.expects(once()).method("close");

        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, mediaObject, PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Unexpected exception during play: " + e);
        }
    }

    public void testMultiTranslation() {
        final int NOF_MEDIA_OBJECTS = 3;
        IMediaObject[] mediaObjects = new IMediaObject[NOF_MEDIA_OBJECTS];
        IMediaObject mediaObject = createMediaObject("texttest", TEXT_PLAIN);
        for (int index=0; index < NOF_MEDIA_OBJECTS; index++) {
            mediaObjects[index] = mediaObject;
        }
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
//        stream.setCallSession((ISession)mockCallSession.proxy());

        // There is one text object among the media objects that will
        // be sent for translation to the MTM. Mock this behaviour.
        mMTM.expects(atLeastOnce()).method("getTextToSpeech")
                .will(returnValue(mTextToSpeech.proxy()));
        globalTranslatorInstanceCounter = 0;
        mTextToSpeech.expects(atLeastOnce()).method("translate")
                .with(eq(mediaObject), eq(stream))
                .will(new MtmTranslationStub(stream));
        mTextToSpeech.expects(atLeastOnce()).method("close");

        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");
        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Unexpected exception while calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, mediaObjects, PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
        }
        catch (Exception e) {
            fail("Unexpected exception during play: " + e);
        }
        assertEquals(NOF_MEDIA_OBJECTS, globalTranslatorInstanceCounter);
    }

    private class MyPlayFinishedStub implements Stub {
        public MyPlayFinishedStub() {
        }
        public Object invoke( Invocation invocation ) throws Throwable {
            PlayFinishedEvent e = (PlayFinishedEvent)invocation.parameterValues.get(0);
            PlayTest.assertTrue("Cursor should be greater than zero", 
                    e.getCursor() > 0);
            System.err.println("Cursor=" + e.getCursor());
            return null;
        }
        public StringBuffer describeTo(StringBuffer arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
    };
    
    private class MyTranslationStub implements Stub {
        private OutboundMediaStreamImpl mStream;
        private IMediaObject mMo;
        private ExecutorService mThreadPool;

        public MyTranslationStub(OutboundMediaStreamImpl s, IMediaObject mo,
                ExecutorService threadPool) {
            mStream = s;
            mMo = mo;
            mThreadPool = threadPool;
        }
        public Object invoke( Invocation invocation ) throws Throwable {
            mThreadPool.submit(new TranslationTask(mStream, mMo));
            return null;
        }
        public StringBuffer describeTo(StringBuffer arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
    };

    private class MtmTranslationStub implements Stub {
        private OutboundMediaStreamImpl mStream;

        public MtmTranslationStub(OutboundMediaStreamImpl s) {
            mStream = s;
        }
        public Object invoke( Invocation invocation ) throws Throwable {
            globalTranslatorInstanceCounter++;
            // audio
            IInboundMediaStream inbound = mFactory.getInboundMediaStream();
            inbound.setEventDispatcher(mStream.getEventDispatcher());
            inbound.setCallSession(MockCallSession.getSession());
            inbound.create(getAudioMediaMimeTypes());
            inbound.join(mStream);
            inbound.unjoin(mStream);
            inbound.delete();
            mStream.translationDone();
            return null;
        }
        public StringBuffer describeTo(StringBuffer arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
    };

    /**
     * Creates a MediaObject instance from the test media file.
     * 
     * @param filename Absolute path to media file.
     * 
     * @return A MediaObject instance.
     */
    private IMediaObject createMediaObject(String filename, 
            MimeType contentType) {
        return createMediaObject(filename, 0, contentType);
    }
    
    /**
     * Creates a MediaObject instance from the test media file.
     * 
     * @param filename Absolute path to media file.
     * 
     * @return A MediaObject instance.
     */
    private IMediaObject createMediaObject(String filename, 
            int lengthMs,
            MimeType contentType) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            if (contentType == TEXT_PLAIN) {
                result = factory.create(filename, new MediaProperties());
                result.setImmutable();
            }
            else {
                File f = new File(filename);
                result = factory.create(f);
            }
            result.getMediaProperties().setContentType(contentType);
            result.getMediaProperties().addLengthInUnit(
                    MediaLength.LengthUnit.MILLISECONDS, lengthMs);
        }
        catch (Exception e) {
            fail("Unexpected error in createMediaObject: " + e);
        }
        return result;
    }

    /**
     * Helper class for streaming a media file. 
     */
    private class TranslationTask implements Runnable {
        private OutboundMediaStreamImpl mStream;
        private IMediaObject mMo;
        
        TranslationTask(OutboundMediaStreamImpl s, IMediaObject mo) {
            mStream = s;
            mMo = mo;
        }
        
        public void run() {
            try {
                mStream.translationDone(mMo);
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }            
    }

    /**
     * Helper class for streaming a media file. 
     */
    private class PlayAudioTask implements Runnable {
        private OutboundMediaStreamImpl mStream;
        private String mFilename;
        private int mPort;
        
        PlayAudioTask(OutboundMediaStreamImpl s, String filename) {
            mStream = s;
            mFilename = filename;
            mPort = -1;
        }
        
        PlayAudioTask(OutboundMediaStreamImpl s, String filename, int port) {
            mStream = s;
            mFilename = filename;
            mPort = port;
        }
        
        public void run() {
            Collection<RTPPayload> payloads = getAudioMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            if (mPort > 0) cProp.setAudioPort(mPort);
            cProp.setAudioHost(REMOTE_HOST);
            //cProp.setVideoPort(mPort+2);
            //cProp.setVideoHost(REMOTE_HOST);
            cProp.setPTime(40);
            cProp.setMaxPTime(40);
        
            try {
                mStream.setCallSession(MockCallSession.getSession());
                mStream.create(payloads, cProp);
            } 
            catch (Exception e) {
                System.out.println("Create failed: " +e);
            }
            
            try {
                Object callId = new Object();
                IMediaObject[] mos = new IMediaObject[] {
                        createMediaObject(mFilename, AUDIO_WAV)
                };
                mStream.play(callId, mos,//AUDIO_WAV), 
                        PlayOption.WAIT_FOR_AUDIO, 0);
                //Thread.sleep(2000);
                mStream.getEventNotifier().waitForCallToFinish(callId);
                mStream.delete();
            }
            catch (Exception e) {
                System.out.println("Play failed: " +e);
            }
        }            
    };

    /**
     * Helper class for streaming a media file. 
     */
    private class PlayVideoTask implements Runnable {
        private OutboundMediaStreamImpl mStream;
        private String mFilename;
        int mPort = 23000;
        
        PlayVideoTask(OutboundMediaStreamImpl s, String filename) {
            mStream = s;
            mFilename = filename;
        }
        
        public void run() {
            Collection<RTPPayload> payloads = getVideoMediaPayloads();
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(mPort);
            cProp.setAudioHost(REMOTE_HOST);
            cProp.setVideoPort(mPort+2);
            cProp.setVideoHost(REMOTE_HOST);
            cProp.setPTime(40);
            cProp.setMaxPTime(40);
        
            try {
                mStream.setCallSession(MockCallSession.getSession());
                mStream.create(payloads, cProp);
            } 
            catch (Exception e) {
                fail("Unexpected exception while calling create: " + e);
            }
            
            try {
                Object callId = new Object();
                IMediaObject[] mos = new IMediaObject[] {
                        createMediaObject(mFilename, VIDEO_QUICKTIME),
                };
                mStream.play(callId, mos, PlayOption.WAIT_FOR_AUDIO_AND_VIDEO, 0);
                mStream.getEventNotifier().waitForCallToFinish(callId);
                mStream.delete();
            }
            catch (Exception e) {
                fail("Unexpected exception while calling play: " + e);
            }
        }            
    };

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
    
    protected Collection<RTPPayload> getAudioMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        //list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        return list;
    }
    
    protected Collection<RTPPayload> getVideoMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
        return list;
    }
    
    public static void main(String argv[]) {
        HashMap<Integer, String> testCases = new HashMap<Integer, String>();
        testCases.put(0, "testCursor");
        testCases.put(1, "testMultipleMediaObjectPlay");
        testCases.put(2, "testMultipleVideoMediaObjectPlay");
        testCases.put(3, "testPlayCorruptVideo");
        testCases.put(4, "testParallelAudioPlay");
        testCases.put(5, "testPortPoolFailDuringPlay");
        testCases.put(6, "testPlayVideo");
 
        if (argv.length < 2) {
            System.out.println("syntax: <ip address> <test #1> ... <test #n>");
            for (int i = 0; i < testCases.size(); i++) {
                System.out.println("Test #" + i + " : " + testCases.get(i));
            }
        } else {
            String ipAddress = argv[0];
            for (int i = 1; i < argv.length; i++) {
                PlayTest test = new PlayTest("test", ipAddress);
                test.setUp();
                int testNumber = Integer.parseInt(argv[i]);
                switch (testNumber) {
                    case 0:
                        test.testCursor();
                        break;

                    case 1:
                        test.testMultipleMediaObjectPlay();
                        break;

                    case 2:
                        test.testMultipleVideoMediaObjectPlay();
                        break;

                    case 3:
                        test.testPlayCorruptVideo();
                        break;

                    case 4:
                        test.testParallelAudioPlay();
                        break;

//                    case 5:
//                        test.testPortPoolFailDuringPlay();
//                        break;

                    case 6:
                        test.testPlayVideo();
                        break;

                    default:
                        break;
                }
                try {
                    test.tearDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
