package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import org.apache.log4j.xml.DOMConfigurator;

import jakarta.activation.MimeType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

public class MultiStream {
    enum Media { UNDEFINED, WAV, MOV, AMR }

    private String remoteHost = "10.16.2.68";

    protected StreamFactoryImpl mFactory;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    private static MimeType VIDEO_QUICKTIME;
    private static MimeType VIDEO_3GPP;
    private static MimeType AUDIO_WAV;
    private final int nOfMediaObjects = 3;
    IMediaObject[] mediaObjects = new IMediaObject[nOfMediaObjects];
    Media media = Media.WAV;
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        200, 200, 50000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    static {
        try {
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            VIDEO_3GPP = new MimeType("video/3gpp");
            AUDIO_WAV = new MimeType("audio/wav");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MultiStream() {
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
        for (int i = 0; i < nOfMediaObjects; i++) {
            mediaObjects[i] = createPlayableMediaObject("medialibrary/mtest/test_pcmu.wav", media);
        }
    }

    public void execute(int nOfPlay) {
        System.out.println("Executing test of multiple media object play");

        for (int playerCount = 0; playerCount < nOfPlay; playerCount++) {
            Player player = new Player(playerCount, 500000);

            player.start();
        }

        System.out.println("Players started");
     }

    public static void main(String[] args) {
        int nOfPlay = Integer.parseInt(args[0]);

        MultiStream multiPlayTest = new MultiStream();

        multiPlayTest.execute(nOfPlay);
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

    static IMediaObject createPlayableMediaObject(String inputFileName, Media media) {
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
            e.printStackTrace();
        }
        return result;
    }

    class Player extends Thread {
        int iteration = 0;
        int nOfPlay = 10;
        int remotePort = 23000;
        int index;
        OutboundMediaStreamImpl outboundStream;

        public Player(int playerNumber, int nOfPlay) {
            this.iteration = playerNumber;
            this.nOfPlay = nOfPlay;
        }

        public void slamdown(int index) {
            synchronized(this) {
                try {
                    if (this.index == index) {
                        System.out.println("Play #" + index + ":" + iteration + " slamdown");
                        outboundStream.cancel();
                    }
                } catch (Exception e) {}
            }
        }

        public void run() {
            for (index = 0; index < nOfPlay; index++) {
                Collection<RTPPayload> payloads = getPayloads(media);
                ConnectionProperties cProp = new ConnectionProperties();
                cProp.setAudioPort(remotePort+2*iteration);
                cProp.setAudioHost(remoteHost);
                cProp.setPTime(20);

                System.out.println("Starting #" + index + ":" + iteration);


                synchronized(this) {
                    outboundStream  =
                            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
                    outboundStream.setEventDispatcher(eventDispatcher);
                }

                long delay = (long) (Math.random()*4000);
                try {
                    System.out.println("Create #" + index + ":" + iteration);
                    outboundStream.create(payloads, cProp);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Player: Unexpected exception while " +
                            "calling create: " + e);
                }
                Object callId = new Object();
                Slammer slammer = new Slammer(delay, this, index);
                if (delay >= 500) {
                    System.out.println("Play #" + index + ":" + iteration + " will slam after " + delay + "ms");
                    threadPoolExecutor.execute(slammer);
                }
                System.out.println("Play #" + index + ":" + iteration);
                try {
                    outboundStream.play(callId,
                            mediaObjects,
                            IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);
                } catch (Exception e) {
                    System.out.println("Play #" + index + ":" + iteration + " failed!");
                    e.printStackTrace();
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Delete #" + index + ":" + iteration);
                slammer.setDone();
                try {
                    outboundStream.delete();
                } catch (Exception e) {
                    System.out.println("Delete #" + index + ":" + iteration + " failed!");
                    e.printStackTrace();
                }
            }
        }
    }

    class Slammer implements Runnable {
        long delay;
        Player player;
        boolean isDone = false;
        int index;

        public Slammer(long delay, Player player, int index) {
            this.delay = delay;
            this.player = player;
            this.index = index;
        }

        public void setDone() {
            isDone = true;
        }

        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isDone) player.slamdown(index);
        }
    }

}
