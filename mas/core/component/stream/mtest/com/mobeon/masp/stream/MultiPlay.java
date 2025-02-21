package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.stream.mock.MockRTPSessionFactory;
import org.apache.log4j.xml.DOMConfigurator;

import jakarta.activation.MimeType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MultiPlay {
    enum Media { UNDEFINED, WAV, MOV, AMR }

    private String remoteHost = "10.16.2.68";
    private int remotePort = 23000;

    protected StreamFactoryImpl mFactory;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    private MimeType VIDEO_QUICKTIME;
    private MimeType VIDEO_3GPP;
    private MimeType AUDIO_WAV;
    private final int nOfMediaObjects = 3;
    private IMediaObject[] mediaObjects = new IMediaObject[nOfMediaObjects];
    Media media = Media.WAV;

    MultiPlay() {
        mFactory = new StreamFactoryImpl();
        //        mFactory.setSessionFactory(new MockRTPSessionFactory());
        ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();

        try {
            cm.setConfigFile("../cfg/mas.xml");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the configuration manager: " + e);
        }
        try {
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the content type mapper: " + e);
        }
        try {
            mFactory.setContentTypeMapper(ctm);
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the stream factory: " + e);
        }
        try {
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            VIDEO_3GPP = new MimeType("video/3gpp");
            AUDIO_WAV = new MimeType("audio/wav");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the mime types: " + e);
        }
        for (int i = 0; i < nOfMediaObjects; i++) {
            mediaObjects[i] = createPlayableMediaObject("medialibrary/mtest/test_pcmu.wav", media);
        }
    }

    public void execute() {
        System.out.println("Executing test of multiple media object play");
        for (int i = 0; i < 10000; i++) {
            Collection<RTPPayload> payloads = getPayloads(media);
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(remotePort);
            cProp.setVideoPort(remotePort+2);
            cProp.setAudioHost(remoteHost);
            cProp.setVideoHost(remoteHost);
            cProp.setPTime(20);

            OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
            stream.setEventDispatcher(eventDispatcher);
            try {
                System.out.println("Create #" + i);
                stream.create(payloads, cProp);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("Correct state 2: Unexpected exception while " +
                        "calling create: " + e);
            }

            try {
                int sleep = (int)(Math.random()*10000)%9000+1;
                Object callId = new Object();
                System.out.println("Play #" + i);
                stream.play(callId,
                        mediaObjects,
                        IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);
//                      IOutboundMediaStream.PlayOption.DO_NOT_WAIT, 0);
//                stream.getEventNotifier().waitForCallToFinish(callId);
                System.out.println("  Sleep " + sleep);
                Thread.sleep(sleep);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("Correct state 2: Unexpected exception: " + e);
            }
            finally {
                System.out.println("Delete #" + i);
                stream.delete();
            }
        }
        System.out.println("Done.");
    }

    public static void main(String[] args) {
        MultiPlay multiPlayTest = new MultiPlay();

        multiPlayTest.execute();
    }

    void fail(String message) {
        System.out.println("Error: " + message);
    }

    private Collection<RTPPayload> getPayloads(Media media) {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        switch (media) {
        case WAV:
            list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
            break;

        case MOV:
            list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
            list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
            break;

        case AMR:
            list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
            list.add(RTPPayload.get(RTPPayload.VIDEO_H263));
            break;

        default:
            break;
        }

        return list;
    }

    private IMediaObject createPlayableMediaObject(String inputFileName, Media media) {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            File f = new File(inputFileName);
            result = factory.create(f);
            switch (media) {
            case WAV:
                result.getMediaProperties().setContentType(AUDIO_WAV);
                break;

            case MOV:
                result.getMediaProperties().setContentType(VIDEO_QUICKTIME);
                break;

            case AMR:
                result.getMediaProperties().setContentType(VIDEO_3GPP);
                break;

            default:
                break;
            }
            result.getMediaProperties().addLengthInUnit(
                    MediaLength.LengthUnit.MILLISECONDS, 0);
        }
        catch (Exception e) {
            fail("Unexpected error in createMediaObject: " + e);
        }
        return result;
    }

}
