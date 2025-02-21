package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeType;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.xml.DOMConfigurator;

public class VideoRecorder {
    private static ILogger logger = ILoggerFactory.getILogger(VideoRecorder.class);
    MockEventDispatcher mEventDispatcher = new MockEventDispatcher();
    protected StreamFactoryImpl mFactory;

    protected MimeType VIDEO_QUICKTIME;
    protected MimeType AUDIO_WAV;

    public void initialize() {
        mFactory = new StreamFactoryImpl();
        try {
            ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("../lib/contenttypemapper.xml");
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
            mFactory.setContentTypeMapper(ctm);
            cm = new ConfigurationManagerImpl();
            cm.setConfigFile("../lib/stream.xml");
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            AUDIO_WAV = new MimeType("audio/wav");
        }
        catch (Exception e) {
            logger.debug("Failed to initiate the stream factory: " + e);
        }
    }

    public void record() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getVideoMediaMimeTypes();
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher(mEventDispatcher);

        try {
            stream.create(mediaMimeTypes);
        }
        catch (Exception e) {
            logger.debug("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(40*1000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(false);
        IMediaObject mo = createRecordableMediaObject(VIDEO_QUICKTIME);

        Object callId = new Object();

        try {
            stream.record(callId, mo, prop);
        }
        catch (Exception e) {
            logger.debug("Unexpected exception: " + e);
        }

        try {
            Thread.sleep(1500000);
        }
        catch (InterruptedException e) {
            logger.debug("Interrupted: " + e);
        }
        try {
            logger.debug("Cumulative packet lost=" + stream.getCumulativePacketLost());
            logger.debug("Fraction lost=" + stream.getFractionLost());
        }
        catch (Exception e) {
            logger.debug("Unexpected exception: " + e);
        }
        stream.delete();
        save(mo, "myTest.mov");
    }

    public static void main(String[] args) {
        VideoRecorder recorder = new VideoRecorder();

        recorder.initialize();
        recorder.record();
    }
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
            logger.debug("Unexpected exception: " + e);
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
            logger.debug("Unexpected error in createRecordableMediaObject: " + e);
        }
        logger.debug("Created media object is NULL");
        result.getMediaProperties().setContentType(mt);
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
}
