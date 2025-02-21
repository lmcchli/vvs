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

public class CreatePlayReference implements IEventReceiver {

    protected StreamFactoryImpl mFactory;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    ISession session = new MockSession("SessionID_4711");

    private MediaUtil mediaUtil;

    public void doEvent(Event event) {
        System.out.println("Got event: " + event);
    }

    public void doGlobalEvent(Event event) {
    }

    public CreatePlayReference() throws Exception {
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

    private void recordReferences(String host, IMediaObject mediaObject, long cursor, MediaUtil.Media media, int removeCount, int removeRange, int pTime, String audioReference, String videoReference) throws StackException, IOException, UnknownHostException {
        RTPDumpRecorder audioRecorder = null;
        RTPDumpRecorder videoRecorder = null;
        FreePortHandler freePortHandler = FreePortHandler.getInstance();
        ConnectionProperties cProp = new ConnectionProperties();

        try {
        Collection<RTPPayload> payloads = mediaUtil.getPayloads(media);

        cProp.setAudioPort(freePortHandler.lockPair());
        cProp.setVideoPort(freePortHandler.lockPair());

        cProp.setAudioHost(host);
        cProp.setVideoHost(host);
        cProp.setPTime(pTime);
        cProp.setMaxPTime(pTime);

        // Setup a Stream
        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setCallSession(session);
        stream.setEventDispatcher(eventDispatcher);

        stream.create(payloads, cProp);

        if (audioReference != null) {
            audioRecorder = new RTPDumpRecorder(host, cProp.getAudioPort(), cProp.getAudioPort() + 1, 2000);
            audioRecorder.record();
        }

        if (videoReference != null) {
            videoRecorder = new RTPDumpRecorder(host, cProp.getVideoPort(), cProp.getVideoPort() + 1, 2000);
            videoRecorder.record();
        }

        System.out.println("Calling play...");

        Object callId = new Object();
        stream.play(callId,
                mediaObject,
                IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, cursor);

        stream.getEventNotifier().waitForCallToFinish(callId);
        System.out.println("play finished");
        stream.delete();

        RTPDump audioDump = null;
        if (audioRecorder != null) {
            System.out.println("Waiting for audio");
            audioRecorder.waitUntilFinished();
            System.out.println("audio ready");
            audioDump = audioRecorder.getDump();
            audioDump.sortRTPPackets();
            audioDump.remove(removeCount, removeRange);
            audioDump.store(audioReference);
            audioDump.dumpRTP(audioReference + ".packets");
        }

        RTPDump videoDump = null;
        if (videoRecorder != null) {
            System.out.println("Waiting for video");
            videoRecorder.waitUntilFinished();
            System.out.println("video ready");
            videoDump = videoRecorder.getDump();
            videoDump.sortRTPPackets();
            videoDump.store(videoReference);
            videoDump.dumpRTP(videoReference + ".packets");
        }

        } finally {
            if (audioRecorder != null) {
                audioRecorder.close();
            }

            if (videoRecorder != null) {
                videoRecorder.close();
            }

            freePortHandler.releasePair(cProp.getAudioPort());
            freePortHandler.releasePair(cProp.getVideoPort());
        }
    }

    public void createReferenceFiles(String[] args) throws Exception {
        final String host = "localhost";

        int pTime = Integer.parseInt(args[5]);

        long cursor = Long.parseLong(args[1]);

        MediaUtil.Media media = mediaUtil.toMedia(args[2]);
        int removeCount = Integer.parseInt(args[3]);
        int removeRange = Integer.parseInt(args[4]);
        recordReferences(host, mediaUtil.createPlayableMediaObject(args[0], media), cursor, media, removeCount, removeRange, pTime, args[6], args[7]);
    }

    public static void main(String[] args) {

        try {
            if (args.length < 8) {
                System.out.println(args.length);
                System.out.println("uasage: CreatePlayReference infile cursor (WAV|MOV|AMR_AUDIO|AMR_VIDEO) removeCount removeRange pTime audioOut videoOut");
            } else {
                CreatePlayReference p = new CreatePlayReference();
                p.createReferenceFiles(args);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}