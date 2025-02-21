package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.*;
import org.apache.log4j.xml.DOMConfigurator;

import java.util.Collection;

public class PlayRecorder implements IEventReceiver {

    protected StreamFactoryImpl mFactory;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    ISession session = new MockSession("SessionID_4711");

    private MediaUtil mediaUtil;
    

    public void doEvent(Event event) {
        System.out.println("Got event: " + event);
    }

    public void doGlobalEvent(Event event) {
    }

    public PlayRecorder() throws Exception {
        eventDispatcher.addEventReceiver(this);
        session.registerSessionInLogger();
        mFactory = new StreamFactoryImpl();
        ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();

        cm.setConfigFile("cfg/mas_stream.xml");

        ctm.setConfiguration(cm.getConfiguration());
        ctm.init();

        mFactory.setContentTypeMapper(ctm);
        mFactory.setConfiguration(cm.getConfiguration());
        mFactory.init();

        mediaUtil = new MediaUtil();
    }

    private String play(String host, int audioPort, int videoPort, IMediaObject mediaObject, MediaUtil.Media media, int pTime) throws StackException {
        String result = null;
        RTPDumpRecorder audioRecorder = null;
        RTPDumpRecorder videoRecorder = null;
        FreePortHandler freePortHandler = FreePortHandler.getInstance();
        ConnectionProperties cProp = new ConnectionProperties();

        try {
        Collection<RTPPayload> payloads = mediaUtil.getPayloads(media);

        cProp.setAudioPort(audioPort);
        cProp.setVideoPort(videoPort);

        cProp.setAudioHost(host);
        if (videoPort > 0)
            cProp.setVideoHost(host);
        cProp.setPTime(pTime);
        cProp.setMaxPTime(pTime);

        // Setup a Stream
        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setCallSession(session);
        stream.setEventDispatcher(eventDispatcher);

        stream.create(payloads, cProp);

        Object callId = new Object();
        stream.play(callId,
                mediaObject,
                IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);

        stream.getEventNotifier().waitForCallToFinish(callId);

        stream.delete();

        } finally {
            freePortHandler.releasePair(cProp.getAudioPort());
            freePortHandler.releasePair(cProp.getVideoPort());
        }
        return result;
    }

    private void doPlayRecord(String outfile, IMediaObject mediaObject, MediaUtil.Media media, int pTime) throws Exception {
        RecordingProperties prop = new RecordingProperties();
        //MediaMimeTypes mediaMimeTypes = mediaUtil.getMimeTypes(media);
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        StreamConfiguration.getInstance().setMovFileVersion(1);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCallSession(session);
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher(eventDispatcher);

        stream.create(mediaUtil.getPayloads(media));

        prop.setMaxRecordingDuration(120000);
        prop.setMinRecordingDuration(0);
        prop.setWaitForRecordToFinish(false);

        IMediaObject mo = mediaUtil.createRecordableMediaObject(media);

        Object callId = new Object();
        System.out.println("AudioPort: " + stream.getAudioPort());
        System.out.println("VideoPort: " + stream.getVideoPort());

        stream.record(callId, mo, prop);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        System.out.println("AudioPort: " + stream.getAudioPort());
        System.out.println("VideoPort: " + stream.getVideoPort());

        play(stream.getHost(), stream.getAudioPort(), stream.getVideoPort(), mediaObject, media, pTime);

        try {
            Thread.sleep(600);
        } catch (Exception e) {
        }

        System.out.println("Finished playing, stopping recording..");

        stream.stop(callId);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        mediaUtil.save(mo, outfile);

        stream.delete();
    }

    public void playRecord(String[] args) throws Exception {
        int pTime = Integer.parseInt(args[3]);

        MediaUtil.Media media = mediaUtil.toMedia(args[2]);
        doPlayRecord(args[1], mediaUtil.createPlayableMediaObject(args[0], media), media, pTime);
    }

    public static void main(String[] args) {

        try {
            if (args.length < 4) {
                System.out.println(args.length);
                System.out.println("usage: PlayRecorder infile outfile (WAV|MOV|AMR_AUDIO|AMR_VIDEO) pTime");
            } else {
                PlayRecorder p = new PlayRecorder();
                p.playRecord(args);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}