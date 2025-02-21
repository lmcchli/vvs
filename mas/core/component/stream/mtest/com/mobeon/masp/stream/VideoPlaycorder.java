package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import jakarta.activation.MimeType;

import org.apache.log4j.xml.DOMConfigurator;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public class VideoPlaycorder {
    private static ILogger logger = ILoggerFactory.getILogger(VideoPlaycorder.class);
    MockEventDispatcher mEventDispatcher = new MockEventDispatcher();
    protected StreamFactoryImpl mFactory;

    protected MimeType VIDEO_QUICKTIME;
    protected MimeType AUDIO_WAV;
    private static final String HOST_BRAGE = "150.132.5.213";
    private static final String HOST_PC = "10.16.2.133";
    private static final String PC229 = "10.16.2.84";
    private static final String REMOTE_HOST = PC229;
    private static final int REMOTE_RECEIVER_AUDIO_PORT = 4712;

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

    public void play(String ... fileNames) {
        Collection<RTPPayload> payloads = getVideoMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(REMOTE_RECEIVER_AUDIO_PORT);
        cProp.setVideoPort(REMOTE_RECEIVER_AUDIO_PORT +2);
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
            IMediaObject[] mediaObjects = new IMediaObject[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                mediaObjects[i] = createVideoMediaObject(fileNames[i]);
            }
            stream.play(callId, mediaObjects, IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
           logger.debug("Correct state 2: Unexpected exception: " + e);
        }
    }

    public void record(String fileName) {
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
            Thread.sleep(6000);
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
        try {
        stream.stop(callId);
        }
        catch (Exception e) {
            logger.debug("Unexpected exception: " + e);
        }
        stream.delete();
        save(mo, fileName);
    }

    public static void main(String[] args) {
        VideoPlaycorder video = new VideoPlaycorder();

        try {
        System.out.println("************************* Playcorder .");
        Thread.sleep(1000);
        System.out.println("************************* Playcorder ..");
        Thread.sleep(1000);
        System.out.println("************************* Playcorder ...");
        Thread.sleep(1000);
        video.initialize();

        for (int i=0; i < 1000; i++) {
        System.out.println("************************* Play ...");
        video.play(args[0], args[1]);
        System.out.println("************************* Played ...");
        //        Thread.sleep(2500);
        System.out.println("************************* Record ...");
        video.record(args[2]);
        System.out.println("************************* Recorded ...");
        //        Thread.sleep(2500);
        System.out.println("************************* Play ...");
        video.play(args[0], args[1]);
        System.out.println("************************* Played ...");
        //Thread.sleep(2500);
        }
        System.out.println("************************* Done ...");
        }
        catch (InterruptedException e) {
            logger.debug("Interrupted: " + e);
        }
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

    protected IMediaObject createRecordableMediaObject(MimeType mt) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            result = factory.create();
        }
        catch (Exception e) {
            logger.debug("Unexpected error in createRecordableMediaObject: " + e);
            logger.debug("Created media object is NULL");
        }
        result.getMediaProperties().setContentType(mt);
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
}
