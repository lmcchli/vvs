/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.stream.mock.MockRTPSessionFactory;
import com.mobeon.masp.stream.mock.MockCallSession;

import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.activation.MimeType;

/**
 * Contains some more complicated tests for method <code>record</code>.
 * 
 * @author Jörgen Terner
 */
public class RecordTest extends MockObjectTestCase {
    // (Brage=150.132.5.213");
    protected String remoteHostAddress = "10.16.2.68"; 
        
    protected static final int REMOTE_AUDIO_PORT = 4712;
    
    private static final ILogger LOGGER =
        ILoggerFactory.getILogger(RecordTest.class);
    
    protected Mock mEventDispatcher;

    protected StreamFactoryImpl mFactory;

    protected MimeType VIDEO_QUICKTIME;
    protected MimeType VIDEO_3GPP;
    protected MimeType AUDIO_WAV;

    static {
        System.loadLibrary("ccrtpadapter");
    }

    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public RecordTest(String name)
    {
       super(name);
    }
    
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
        mFactory.setSessionFactory(new MockRTPSessionFactory());

        ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();

        try {
            cm.setConfigFile("cfg/mas_stream.xml");
        }
        catch (Exception e) {
            fail("Failed to initiate the configuration manager: " + e);
        }

        try {
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
        }
        catch (Exception e) {
            fail("Failed to initiate content type mapper: " + e);
        }

        try {
            mFactory.setContentTypeMapper(ctm);
            //            cm = new ConfigurationManagerImpl();
            //            cm.setConfigFile("../cfg/mas.xml");
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory: " + e);
        }

        try {
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            VIDEO_3GPP = new MimeType("video/3gpp");
            AUDIO_WAV = new MimeType("audio/wav");
        }
        catch (Exception e) {
            fail("Failed to initiate mime types: " + e);
        }
    }

    /**
     * <pre>
     * Tests for method record.
     * 
     * </pre>
     */
    public void testRecordAudio() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getAudioMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        } 
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(4*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(false);
        IMediaObject mo = createRecordableMediaObject(AUDIO_WAV);
        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(5000 + prop.getMaxRecordingDuration());
            stream.stop(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Interrupted: " + e);
        }
        
        try {
            System.err.println("LENGTH=" + mo.getMediaProperties().getLengthInUnit(
                            MediaLength.LengthUnit.MILLISECONDS));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
        save(mo, "myTest.wav"); 
    }
    

    /**
     * <pre>
     * Tests for method record.
     * 
     * </pre>
     */
    public void testRecordVideo() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getVideoMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        StreamConfiguration.getInstance().setMovFileVersion(1);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        
        mEventDispatcher.expects(once()).method("fireEvent");

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        } 
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(10*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(true);
        IMediaObject mo = createRecordableMediaObject(VIDEO_QUICKTIME);
        
        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {            fail("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }
        try {
            LOGGER.debug("Cumulative packet lost=" + stream.getCumulativePacketLost());
            LOGGER.debug("Fraction lost=" + stream.getFractionLost());
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        stream.delete();
        save(mo, "myTest.mov"); 
    }


    /**
     * <pre>
     * Tests for method record.
     * 
     * </pre>
     */
    public void testRecord3gpVideo() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = get3gpVideoMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        StreamConfiguration.getInstance().setMovFileVersion(1);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        } 
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(1*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(true);
        IMediaObject mo = createRecordableMediaObject(VIDEO_3GPP);
        
        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {            
            fail("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }
        try {
            LOGGER.debug("Cumulative packet lost=" + stream.getCumulativePacketLost());
            LOGGER.debug("Fraction lost=" + stream.getFractionLost());
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        stream.delete();
        save(mo, "myTest.3gp"); 
    }

    /**
     * <pre>
     * Tests for method record.
     * 
     * </pre>
     */
    public void testRecordAudioOnVideoStream() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getVideoMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        } 
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(40*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(false);
        IMediaObject mo = createRecordableMediaObject(VIDEO_QUICKTIME);
        mo.getMediaProperties().setFileExtension("wav");
        mo.getMediaProperties().setContentType(AUDIO_WAV);
        
        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }
        
        stream.delete();
        save(mo, "myTest.wav"); 
    }
    
    /**
     * <pre>
     * Tests for method record.
     *
     * </pre>
     */
    public void testRecordAnyAudioOnVideoStream() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getVideoMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());
        mEventDispatcher.expects(once()).method("fireEvent");

        try {
            stream.setCallSession(MockCallSession.getSession());
            stream.create(mediaMimeTypes);
        }
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(40*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(false);
        prop.setRecordingType(RecordingProperties.RecordingType.AUDIO);
        IMediaObject mo = createRecordableMediaObject(null);

        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        stream.delete();
        save(mo, "myTest." + mo.getMediaProperties().getFileExtension());
    }

    /**
     * 
     */
    public void testParallelRecord() {
        final int NROFTHREADS = 1;
        
        // Parallel streams
        ExecutorService threadPool = Executors.newFixedThreadPool(NROFTHREADS);
        Object[] tasks = new Object[NROFTHREADS];
        mEventDispatcher.expects(new JUnitUtil.InvokeCountMatcher(NROFTHREADS)).method("fireEvent");
        try {
            for (int i = 0; i < tasks.length; i++) {
                IMediaObject mo = null;
                IInboundMediaStream stream = null;
                MediaMimeTypes mediaMimeTypes = getVideoMediaMimeTypes();
                StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(5000);
                StreamConfiguration.getInstance().setAudioSkip(0);
                StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
                MediaStreamSupport.updateConfiguration();
                stream = mFactory.getInboundMediaStream();
                stream.setCNAME("FOOBAR");
                stream.setEventDispatcher((IEventDispatcher)mEventDispatcher.proxy());                
                
                try {
                    stream.setCallSession(MockCallSession.getSession());
                    stream.create(mediaMimeTypes);
                } 
                catch (Exception e) {
                    fail("Record: Unexpected exception while calling create: " + e);
                }
                mo = createRecordableMediaObject(VIDEO_QUICKTIME);
                tasks[i] = 
                    threadPool.submit(new RecordAudioTask(stream, mo, "test" + i + ".mov"));
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
     * Helper class for recording media. 
     */
    private class RecordAudioTask implements Runnable {
        private IInboundMediaStream mStream;
        private IMediaObject mMo;
        private String mName;
        RecordAudioTask(IInboundMediaStream s, IMediaObject mo, String name) {
            mStream = s;
            mMo = mo;
            mName = name;
        }
        
        public void run() {
           try {
                RecordingProperties prop = new RecordingProperties();
                prop.setWaitForRecordToFinish(true);
                prop.setMaxRecordingDuration(999*1000);
               
                Object callId = new Object();
                mStream.record(callId, mMo, prop);
                Thread.sleep(100);
                mStream.delete();
                save(mMo, mName);
            }
            catch (Exception e) {
                fail("Unexpected exception while calling record: " + e);
            }
        }            
    };
    
    private void save(IMediaObject mo, String name) {
        try {
            InputStream is = mo.getInputStream();
            File f = new File(System.getProperty("user.dir") + 
                    File.separator + name);
            FileOutputStream os = new FileOutputStream(f);
            int b = -1;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
            os.close();
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
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
        if (mt != null) result.getMediaProperties().setContentType(mt);
        return result;
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

    protected MediaMimeTypes get3gpVideoMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_AMR);
        mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
        return mediaMimeTypes;
    }


    public static void main(String argv[]) {
        RecordTest test = new RecordTest("test");
        test.setUp(); 
        // test.testParallelRecord();
        test.testRecord3gpVideo();
        //test.testRecordVideo();
        //        test.testRecordAudio();
        //        test.testRecordAnyAudioOnVideoStream();
        try {
            test.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 }
