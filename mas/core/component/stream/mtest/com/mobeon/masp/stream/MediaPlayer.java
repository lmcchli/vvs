package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import org.apache.log4j.xml.DOMConfigurator;

import jakarta.activation.MimeType;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.net.UnknownHostException;

public class MediaPlayer implements IEventReceiver {

    enum Media { UNDEFINED, WAV, MOV, AMR_AUDIO, AMR_VIDEO, DTMF }
    protected StreamFactoryImpl mFactory;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    ISession session = new MockSession("SessionID_4711");

    private MediaUtil mediaUtil;

    public void doEvent(Event event) {
        System.out.println("Got event: " + event);
    }

    public void doGlobalEvent(Event event) {
    }

    public MediaPlayer() throws Exception {
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


    private String play(String host, int audioPort, int videoPort, IMediaObject mediaObject, long cursor, MediaUtil.Media media, int pTime) throws StackException {
        String result = null;
        ConnectionProperties cProp = new ConnectionProperties();

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
                IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, cursor);

        stream.getEventNotifier().waitForCallToFinish(callId);

        stream.delete();

        return result;
    }

    public void playMedia(String[] args) throws Exception {
        final String host = "localhost";

        int audioPort = Integer.parseInt(args[1]);
        int videoPort = Integer.parseInt(args[2]);
        long cursor = Long.parseLong(args[4]);
        MediaUtil.Media media = mediaUtil.toMedia(args[5]);
        int pTime = Integer.parseInt(args[6]);

        play(args[0], audioPort, videoPort, mediaUtil.createPlayableMediaObject(args[3], media), cursor, media, pTime);
    }

    public static void main(String[] args) {

        try {
            if (args.length < 7) {
                System.out.println(args.length);
                System.out.println("uasage: MediaPlayer host audioPort videoPort infile cursor (WAV|MOV|AMR_AUDIO|AMR_VIDEO) pTime");
            } else {
                MediaPlayer p = new MediaPlayer();
                p.playMedia(args);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}