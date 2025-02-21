package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import jakarta.activation.MimeType;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;

public class VideoPlayer {
    private static ILogger logger = ILoggerFactory.getILogger(VideoPlayer.class);
    MockEventDispatcher mEventDispatcher = new MockEventDispatcher();
    protected StreamFactoryImpl mFactory;

    protected MimeType VIDEO_QUICKTIME;
    protected MimeType AUDIO_WAV;
    private static final String HOST_BRAGE = "150.132.5.213";
    private static final String HOST_PC = "10.16.2.133";
    private static final String PC229 = "10.16.2.45";
    private static final String REMOTE_HOST = PC229;
    private static final int REMOTE_AUDIO_PORT = 4712;

    String fileName = "myTest.mov";

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

    public void play(String fileName) {
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_AUDIO_PORT);
        cProp.setVideoPort(REMOTE_AUDIO_PORT+2);
        cProp.setAudioHost(REMOTE_HOST);
        cProp.setVideoHost(REMOTE_HOST);
        cProp.setPTime(20);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setEventDispatcher(mEventDispatcher);
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            logger.debug("Correct state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId, createVideoMediaObject(fileName), IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
           logger.debug("Correct state 2: Unexpected exception: " + e);
        }
    }

    public static void main(String[] args) {
        VideoPlayer videoPlayer = new VideoPlayer();

        videoPlayer.initialize();
        videoPlayer.play(args[0]);
    }
    /**
     * Creates a MediaObject instance from the video test media file.
     *
     * @return A MediaObject instance.
     */
    protected IMediaObject createVideoMediaObject(String filename) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            File f = new File(filename);
            result = factory.create(f);
            result.getMediaProperties().setContentType(VIDEO_QUICKTIME);
        }
        catch (Exception e) {
            logger.debug("Unexpected error in createVideoMediaObject: " + e);
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
            logger.debug("Unexpected error in createRecordableMediaObject: " + e);
        }
        logger.debug("Created media object is NULL");
        return result;
    }

    protected Collection<RTPPayload> getAudioMediaPayloads() {
        List<RTPPayload> list = new ArrayList<RTPPayload>();
        RTPPayload p1 = RTPPayload.get(RTPPayload.AUDIO_PCMU);
        if (p1 == null) {
            logger.debug("No default payload configured for AUDIO_PCMU");
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
