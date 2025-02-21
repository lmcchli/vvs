package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import org.apache.log4j.xml.DOMConfigurator;

import jakarta.activation.MimeType;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StreamTest extends TestCase implements IEventReceiver {
    private final int MAX_DIFF_AUDIO_VIDEO_OFFSET = 20;

    private static boolean initialized = false;

    private static StreamFactoryImpl mFactory;

    private static IEventDispatcher eventDispatcher = new MockEventDispatcher();
    private static ISession session = new MockSession("SessionID_4711");

    private static ArrayList<ControlToken> tokens;
    private static Event lastEvent;
    private static MediaUtil mediaUtil;

    private void initialize() {
        tokens = new ArrayList<ControlToken>();
        lastEvent = null;

        eventDispatcher.addEventReceiver(this);
        session.registerSessionInLogger();
        mFactory = new StreamFactoryImpl();
        ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();

        try {
            cm.setConfigFile("cfg/mas_stream.xml");
        } catch (Exception e) {
            fail("Failed to initiate the configuration manager: " + e);
        }
        try {
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
        } catch (Exception e) {
            fail("Failed to initiate the content type mapper: " + e);
        }
        try {
            mFactory.setContentTypeMapper(ctm);
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();
        } catch (Exception e) {
            fail("Failed to initiate the stream factory: " + e);
        }
        try {
            mediaUtil = new MediaUtil();
        } catch (Exception e) {
            fail("Failed to initiate the mime types: " + e);
        }
    }

    protected void setUp() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    public void doEvent(Event event) {
        System.out.println("Received event! : " + event);
        lastEvent = event;
        if (event instanceof ControlTokenEvent) {
            ControlToken t = ((ControlTokenEvent) event).getToken();
            System.out.println("Got token:" + t.getToken() + " : " + t.getDuration() + " : " + t.getVolume());
            tokens.add(t);
        }
    }

    public void doGlobalEvent(Event event) {
    }

    private String verifyPlay(String host, IMediaObject mediaObject, MediaUtil.Media media, int pTime, RTPDump audioReference, RTPDump videoReference, boolean verifySynchronization) throws StackException, IOException, UnknownHostException {
        String result = null;
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

        Object callId = new Object();
        stream.play(callId,
                mediaObject,
                IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);

        stream.getEventNotifier().waitForCallToFinish(callId);

        stream.delete();

        RTPDump audioDump = null;
        if (audioRecorder != null) {
            System.out.println("Waiting for audio");
            audioRecorder.waitUntilFinished();
            System.out.println("audio ready");
            audioDump = audioRecorder.getDump();
            audioDump.sortRTPPackets();
        }

        RTPDump videoDump = null;
        if (videoRecorder != null) {
            System.out.println("Waiting for video");
            videoRecorder.waitUntilFinished();
            System.out.println("video ready");
            videoDump = videoRecorder.getDump();
            videoDump.sortRTPPackets();
        }

        result = RTPDump.compareDumps(audioReference, videoReference, audioDump, videoDump, verifySynchronization);

        if (result != null) {
            if (audioDump != null)
                audioDump.store("playtest_audio.fail");
            if (videoDump != null)
                videoDump.store("playtest_video.fail");
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
        return result;
    }

    public void testPlay() {
        String[][] tests = {{"mtest/testfiles/fun1_test.wav", "WAV", "mtest/testfiles/referencePlayWav.rtp", null, "20"},
                            {"mtest/testfiles/message.mov", "MOV", "mtest/testfiles/message_audio.rtp", "mtest/testfiles/message_video.rtp", "40"},
                            {"mtest/testfiles/message.3gp", "AMR_VIDEO", "mtest/testfiles/message_3gp_audio.rtp", "mtest/testfiles/message_3gp_video.rtp", "20"}};

        final String host = "localhost";

        for (int i = 0; i < tests.length; ++i) {
            try {
                RTPDump audioReference = null;
                if (tests[i][2] != null) {
                    audioReference = new RTPDump(tests[i][2]);
                    audioReference.load(tests[i][2]);
                }

                RTPDump videoReference = null;
                if (tests[i][3] != null) {
                    videoReference = new RTPDump(tests[i][3]);
                    videoReference.load(tests[i][3]);
                }

                int pTime = Integer.parseInt(tests[i][4]);

                MediaUtil.Media media = mediaUtil.toMedia(tests[i][1]);
                String result = verifyPlay(host, mediaUtil.createPlayableMediaObject(tests[i][0], media), media, pTime, audioReference, videoReference, false);

                if (result != null) {
                    fail("Verify play failed for file: " + tests[i][0] + " : " + result);
                }
            } catch(Exception e) {
                e.printStackTrace();
                fail("Exception thrown when verifying play of file: " + tests[i][0] + "!");
            }
        }
    }

    public void testJoin() {
        String[][] tests = {{"WAV", "mtest/testfiles/referencePlayWav.rtp", null, "20"},
                            {"MOV", "mtest/testfiles/message_audio.rtp", "mtest/testfiles/message_video.rtp", "40"},
                            {"WAV", "mtest/testfiles/dtmftest.rtp", null, "40"},
                            {"AMR_VIDEO", "mtest/testfiles/message_3gp_audio.rtp", "mtest/testfiles/message_3gp_video.rtp", "20"}};

        final String host = "localhost";

        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        MediaStreamSupport.updateConfiguration();

        RTPDumpRecorder audioRecorder = null;
        RTPDumpRecorder videoRecorder = null;

        for (int i = 0; i < tests.length; ++i) {
            try {
                tokens.clear();

                boolean handleDtmfAtInbound = true;
                boolean forwardDtmfToOutbound = true;

                MediaUtil.Media media = mediaUtil.toMedia(tests[i][0]);

                RTPDump audioReference = null;
                if (tests[i][1] != null) {
                    audioReference = new RTPDump(tests[i][1]);
                    audioReference.load(tests[i][1]);
                    //audioReference.printDTMF();
                }

                RTPDump videoReference = null;
                if (tests[i][2] != null) {
                    videoReference = new RTPDump(tests[i][2]);
                    videoReference.load(tests[i][2]);
                }

                int pTime = Integer.parseInt(tests[i][3]);

                IInboundMediaStream inStream = mFactory.getInboundMediaStream();
                IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();
                inStream.setCallSession(session);
                outStream.setCallSession(session);

                // Initializing the inbound stream
                inStream.setEventDispatcher(eventDispatcher);
                inStream.create(mediaUtil.getPayloads(media));

                InboundMediaStreamImpl is = (InboundMediaStreamImpl) inStream;
                System.out.println("Audio payload type: " + is.getContentInfo().getAudioPayload().getPayloadType());
                System.out.println("Audio payload clock: " + is.getContentInfo().getAudioPayload().getClockRate());

                System.out.println("DTMF payload type: " + is.getContentInfo().getDTMFPayload().getPayloadType());
                System.out.println("DTMF payload clock: " + is.getContentInfo().getDTMFPayload().getClockRate());

                ConnectionProperties cProp = new ConnectionProperties();

                // Initializing the outbound stream
                outStream.setEventDispatcher(eventDispatcher);
                Collection<RTPPayload> payloads = mediaUtil.getPayloads(media);
                cProp.setAudioPort(FreePortHandler.getInstance().lockPair());
                cProp.setAudioHost(host);
                cProp.setVideoPort(FreePortHandler.getInstance().lockPair());
                cProp.setVideoHost(host);
                //cProp.setPTime(pTime);
                cProp.setPTime(20);
                cProp.setMaxPTime(20);
                outStream.create(payloads, cProp);

                OutboundMediaStreamImpl os = (OutboundMediaStreamImpl) outStream;

                try {
                    inStream.join(handleDtmfAtInbound, outStream, forwardDtmfToOutbound);

                    System.out.println("In pTime: " + inStream.getPTime());
                    System.out.println("Out pTime: " + os.getContentInfo().getPTime());

                    if (audioReference != null) {
                        audioRecorder = new RTPDumpRecorder(host, cProp.getAudioPort(), cProp.getAudioPort() + 1, 2000);
                        audioRecorder.record();
                    }

                    if (videoReference != null) {
                        videoRecorder = new RTPDumpRecorder(host, cProp.getVideoPort(), cProp.getVideoPort() + 1, 2000);
                        videoRecorder.record();
                    }

                    Thread.sleep(500);

                    playDumps(inStream.getHost(), inStream.getAudioPort(), inStream.getVideoPort(), audioReference, videoReference, true);

                    Thread.sleep(500);

                    RTPDump audioResult = null;
                    if (audioRecorder != null) {
                        audioRecorder.stop();
                        audioResult = audioRecorder.getDump();
                        audioResult.sortRTPPackets();
                        //audioResult.store("audio_join.rtp");
                    }

                    RTPDump videoResult = null;
                    if (videoRecorder != null) {
                        videoRecorder.stop();
                        videoResult = videoRecorder.getDump();
                        videoResult.sortRTPPackets();
                        //audioResult.store("video_join.rtp");
                    }

                    inStream.unjoin(outStream);

                    Thread.sleep(100);

                    inStream.delete();
                    FreePortHandler.getInstance().releasePair(outStream.getAudioPort());
                    FreePortHandler.getInstance().releasePair(outStream.getVideoPort());
                    outStream.delete();

                    if (!tokens.isEmpty()) {
                        ArrayList<ControlToken> dtmfRef = new ArrayList<ControlToken>();
                        System.out.println("Verifying DTMF:");
                        for (ControlToken t : tokens) {
                            dtmfRef.add(t);
                            System.out.println("Token:" + t.getToken() + " : " + t.getDuration() + " : " + t.getVolume());
                        }
                        if (!verifyInboundDTMF(host, media, pTime, audioResult, dtmfRef, false)) {
                            audioResult.store("jointest.fail.rtp");
                            fail("Forwarded dtmf is not the same as in the referrence file: " + audioReference);
                        }
                    }

                    if (audioReference != null) {
                        audioReference.removeDTMF();
                        audioResult.removeDTMF();
                        System.out.println("Audiopackets: " + audioReference.getPackets().size() + " , " + audioResult.getPackets().size());
                    }
                    if (videoReference != null)
                        System.out.println("Videopackets: " + videoReference.getPackets().size() + " , " + videoResult.getPackets().size());

                    String joinResult = RTPDump.compareDumps(audioReference,  videoReference, audioResult, videoResult, false);

                    if (joinResult != null) {
                        audioResult.store("jointest.fail.rtp");
                        fail("Verify join failed for file: " + tests[i][1] + " : " + joinResult);
                    }

                } catch (Exception e) {
                    fail("Exception thrown when performing playing file: " + tests[i][1] + " on joined streams!: " + e);
                } finally {
                    if (audioRecorder != null) {
                        audioRecorder.close();
                    }

                    if (videoRecorder != null) {
                        videoRecorder.close();
                    }
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception thrown when performing playing file: " + tests[i][1] + " on joined streams!: " + e);
            }
        }
    }

    private void playDumps(String host, int audioPort, int videoPort, RTPDump audioDump, RTPDump videoDump, boolean waitUntilFinished) {
        long audioStart = 0;
        long videoStart = 0;

        if (audioDump != null && videoDump != null) {
            long offset = RTPDump.calculateOffset(audioDump, videoDump);

            if (offset < 0) {
                videoStart = -offset;
            } else {
                audioStart = offset;
            }
        }

        System.out.println("Audiostart: " + audioStart);
        System.out.println("Videostart: " + videoStart);

        RTPDumpPlayer audioPlayer = null;
        if (audioDump != null) {
            audioPlayer = new RTPDumpPlayer(audioDump, host, audioPort, audioPort + 1, audioStart);
        }

        RTPDumpPlayer videoPlayer = null;
        if (videoDump != null) {
            videoPlayer = new RTPDumpPlayer(videoDump, host, videoPort, videoPort + 1, videoStart);
        }

        if (audioPlayer != null) {
            System.out.println("Playing Audio");
            audioPlayer.play();
        }

        if (videoPlayer != null) {
            System.out.println("Playing Video");
            videoPlayer.play();
        }

        if (waitUntilFinished) {
            if (audioPlayer != null) {
                audioPlayer.waitUntilFinished();
            }

            if (videoPlayer != null) {
                videoPlayer.waitUntilFinished();
            }
        }
    }

    private String verifyRecord(String host, MediaUtil.Media media, int pTime, RTPDump audioSource, RTPDump audioReference, RTPDump videoSource, RTPDump videoReference, boolean verifySynchronization) throws Exception {
        System.out.println("*** Record " + media + " ***");

        RecordingProperties prop = new RecordingProperties();
        if (videoSource == null)
            prop.setRecordingType(RecordingProperties.RecordingType.AUDIO);

        Collection<RTPPayload> payloads = mediaUtil.getPayloads(media);
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        StreamConfiguration.getInstance().setMovFileVersion(1);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCallSession(session);
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher(eventDispatcher);

        stream.create(payloads);

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

        playDumps(stream.getHost(), stream.getAudioPort(), stream.getVideoPort(), audioSource, videoSource, true);

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

        stream.delete();

        String result = verifyPlay(host, mo, media, pTime, audioReference, videoReference, verifySynchronization);

        mediaUtil.save(mo, "recordResult.test");

        return result;
    }

    public void testRecord() {
        String[][] tests = {{"WAV", "mtest/testfiles/referencePlayWav.rtp", null, "20", "false"},
                            {"MOV", "mtest/testfiles/message_audio.rtp", "mtest/testfiles/message_video.rtp", "40", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/referencePlayAMRAudio.rtp", null, "20", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_20.rtp", null, "20", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_40.rtp", null, "40", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_60.rtp", null, "60", "false"},
                            {"AMR_VIDEO", "mtest/testfiles/referencePlay3GPAudio.rtp", "mtest/testfiles/referencePlay3GPVideo.rtp", "20", "false"},
                            {"AMR_VIDEO", "mtest/testfiles/message_3gp_audio.rtp", "mtest/testfiles/message_3gp_video.rtp", "20", "false"}};


        final String host = "localhost";

        for (int i = 0; i < tests.length; ++i) {
            try {
                MediaUtil.Media media = mediaUtil.toMedia(tests[i][0]);

                RTPDump audioReference = null;
                if (tests[i][1] != null) {
                    audioReference = new RTPDump(tests[i][1]);
                    audioReference.load(tests[i][1]);
                }

                RTPDump videoReference = null;
                if (tests[i][2] != null) {
                    videoReference = new RTPDump(tests[i][2]);
                    videoReference.load(tests[i][2]);
                }


                int pTime = Integer.parseInt(tests[i][3]);

                String result = verifyRecord(host, media, pTime, audioReference, audioReference, videoReference, videoReference, Boolean.parseBoolean(tests[i][4]));

                if (result != null) {
                    fail("Verify record failed for file: " + tests[i][1] + " : " + result);
                }
            } catch(Exception e) {
                e.printStackTrace();
                fail("Exception thrown when verifying record of file: " + tests[i][1] + "!");
            }
        }
    }

    public void testRecordReordered() {
        String[][] tests = {{"WAV", "mtest/testfiles/referencePlayWav.rtp", null, "20", "false"},
                            {"MOV", "mtest/testfiles/message_audio.rtp", "mtest/testfiles/message_video.rtp", "40", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/referencePlayAMRAudio.rtp", null, "20", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_20.rtp", null, "20", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_40.rtp", null, "40", "false"},
                            {"AMR_AUDIO", "mtest/testfiles/amr_60.rtp", null, "60", "false"},
                            {"AMR_VIDEO", "mtest/testfiles/referencePlay3GPAudio.rtp", "mtest/testfiles/referencePlay3GPVideo.rtp", "20", "false"}};


        final String host = "localhost";

        for (int i = 0; i < tests.length; ++i) {
            try {
                MediaUtil.Media media = mediaUtil.toMedia(tests[i][0]);

                RTPDump audioReference = null;
                RTPDump audioSource = null;
                if (tests[i][1] != null) {
                    audioReference = new RTPDump(tests[i][1]);
                    audioReference.load(tests[i][1]);
                    audioSource = new RTPDump(tests[i][1]);
                    audioSource.load(tests[i][1]);
                    audioSource.shuffle(20);
                    //audioSource.print();
                }

                RTPDump videoReference = null;
                RTPDump videoSource = null;
                if (tests[i][2] != null) {
                    videoReference = new RTPDump(tests[i][2]);
                    videoReference.load(tests[i][2]);
                    videoSource = new RTPDump(tests[i][2]);
                    videoSource.load(tests[i][2]);
                    videoSource.shuffle(20);
                }

                int pTime = Integer.parseInt(tests[i][3]);

                String result = verifyRecord(host, media, pTime, audioSource, audioReference, videoSource, videoReference, Boolean.parseBoolean(tests[i][4]));

                if (result != null) {
                    fail("Verify recordReordered failed for file: " + tests[i][1] + " : " + result);
                }
            } catch(Exception e) {
                e.printStackTrace();
                fail("Exception thrown when verifying recordReordered of file: " + tests[i][1] + "!");
            }
        }
    }

    private boolean verifyInboundDTMF(String host, MediaUtil.Media media, int pTime, RTPDump dtmfReference, ArrayList<ControlToken> tokenRef, boolean checkDuration) throws Exception {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(false);
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

        IMediaObject mo = mediaUtil.createRecordableMediaObject(media);

        Object callId = new Object();

        tokens.clear();

        stream.record(callId, mo, prop);

        playDumps(stream.getHost(), stream.getAudioPort(), stream.getVideoPort(), dtmfReference, null, true);

        try {
            Thread.sleep(600);
        } catch (Exception e) {
        }

        stream.stop(callId);

        boolean result = true;

        if (tokens.size() == tokenRef.size()) {
            ListIterator refIter = tokenRef.listIterator();
            ListIterator resIter = tokens.listIterator();

            while (refIter.hasNext() && resIter.hasNext()) {
                ControlToken ref = (ControlToken) refIter.next();
                ControlToken res = (ControlToken) resIter.next();
                System.out.println("Ref: " + ref.getToken() + " : " + ref.getDuration() + " : " + ref.getVolume());
                System.out.println("Res: " + res.getToken() + " : " + res.getDuration() + " : " + res.getVolume());
                if (ref.getToken() != res.getToken() || (checkDuration && (ref.getDuration() != res.getDuration())) || ref.getVolume() != res.getVolume()) {
                    result = false;
                    break;
                }
            }
        } else {
            result = false;
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        stream.delete();

        System.out.println("Inbound result: " + result);

        return result;
    }

    public void testInboundDTMF() {
        String[][] tests = {{"WAV", "mtest/testfiles/dtmftest.rtp", null, "40"}};
        int[][][] tokens = {{{0, 1920, 3}, {1, 1920, 3}, {2, 960, 3}, {3, 960, 3}, {4, 960, 3}, {5, 1920, 3}, {6, 1920, 3}, {7, 1920, 3}, {8, 1920, 3}, {9, 960, 3}, {10, 960, 3}, {11, 960, 3}}};


        final String host = "localhost";

        for (int i = 0; i < tests.length; ++i) {
            try {
                MediaUtil.Media media = mediaUtil.toMedia(tests[i][0]);

                RTPDump audioReference = null;
                if (tests[i][1] != null) {
                    audioReference = new RTPDump(tests[i][1]);
                    audioReference.load(tests[i][1]);
                    //audioReference.printDTMF();
                }

                RTPDump videoReference = null;
                if (tests[i][2] != null) {
                    videoReference = new RTPDump(tests[i][2]);
                    videoReference.load(tests[i][2]);
                    //videoReference.printPayloadHeader();
                }

                int pTime = Integer.parseInt(tests[i][3]);

                ArrayList<ControlToken> tokenRefs = new ArrayList<ControlToken>();
                for (int j = 0; j < tokens[i].length; ++j) {
                    tokenRefs.add(new ControlToken(ControlToken.toToken(tokens[i][j][0]), tokens[i][j][2], tokens[i][j][1]));
                }

                if (!verifyInboundDTMF(host, media, pTime, audioReference, tokenRefs, true)) {
                    fail("Verify inbound DTMF failed for file: " + tests[i][1]);
                }
            } catch(Exception e) {
                e.printStackTrace();
                fail("Exception thrown when verifying record of file: " + tests[i][1] + "!");
            }
        }
    }

    public void testPlayCorruptMov() {
        FreePortHandler freePortHandler = FreePortHandler.getInstance();
        ConnectionProperties cProp = new ConnectionProperties();

        try {
            Collection<RTPPayload> payloads = mediaUtil.getPayloads(MediaUtil.Media.MOV);

            cProp.setAudioPort(freePortHandler.lockPair());
            cProp.setVideoPort(freePortHandler.lockPair());

            cProp.setAudioHost("localhost");
            cProp.setVideoHost("localhost");
            cProp.setPTime(40);
            cProp.setMaxPTime(40);

            // Setup a Stream
            OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
            stream.setCallSession(session);
            stream.setEventDispatcher(eventDispatcher);

            stream.create(payloads, cProp);

            Object callId = new Object();

            lastEvent = null;

            stream.play(callId,
                    mediaUtil.createPlayableMediaObject("mtest/testfiles/corrupt.mov", MediaUtil.Media.MOV),
                    IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);

            stream.getEventNotifier().waitForCallToFinish(callId);


            System.out.println("lastEvent: " + lastEvent);
            if (lastEvent == null || !(lastEvent instanceof PlayFailedEvent)) {
                stream.delete();
                fail("Playing a corrupt MOV-file does not generate a PlayFailedEvent!");
            }
            stream.delete();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown when playing a corrupt MOV-file: " + e.getMessage());
        } finally {
            freePortHandler.releasePair(cProp.getAudioPort());
            freePortHandler.releasePair(cProp.getVideoPort());
        }
    }


    /**
     * Test that calling play when another play is in progress generates a PlayFailedEvent.
     */
    public void testPlayInProgress() {
        FreePortHandler freePortHandler = FreePortHandler.getInstance();
        ConnectionProperties cProp = new ConnectionProperties();

        try {
            Collection<RTPPayload> payloads = mediaUtil.getPayloads(MediaUtil.Media.WAV);

            cProp.setAudioPort(freePortHandler.lockPair());
            cProp.setVideoPort(freePortHandler.lockPair());

            cProp.setAudioHost("localhost");
            cProp.setVideoHost("localhost");
            cProp.setPTime(40);
            cProp.setMaxPTime(40);

            // Setup a Stream
            OutboundMediaStreamImpl stream =
                (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
            stream.setCallSession(session);
            stream.setEventDispatcher(eventDispatcher);

            stream.create(payloads, cProp);

            Object callId = new Object();

            lastEvent = null;

            stream.play(callId,
                    mediaUtil.createPlayableMediaObject("mtest/testfiles/fun1.wav", MediaUtil.Media.WAV),
                    IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);

            Object callId2 = new Object();
            stream.play(callId2,
                    mediaUtil.createPlayableMediaObject("mtest/testfiles/fun1.wav", MediaUtil.Media.WAV),
                    IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);

            stream.getEventNotifier().waitForCallToFinish(callId2);

            if (lastEvent == null | !(lastEvent instanceof PlayFailedEvent)) {
                stream.delete();
                fail("Issuing a play when a play is already in progress does not generate a PlayFailedEvent!");
            }

            stream.getEventNotifier().waitForCallToFinish(callId);

            if (lastEvent == null || !(lastEvent instanceof PlayFinishedEvent)) {
                stream.delete();
                fail("first play does not finish OK!");
            }

            stream.delete();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown when testing play already in progress: " + e.getMessage());
        } finally {
            freePortHandler.releasePair(cProp.getAudioPort());
            freePortHandler.releasePair(cProp.getVideoPort());
        }
    }

    public void testRecordInProgress()  {

        try
        {
            RecordingProperties prop = new RecordingProperties();
            //MediaMimeTypes mediaMimeTypes = mediaUtil.getMimeTypes(MediaUtil.Media.WAV);
            StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(5000);
            StreamConfiguration.getInstance().setAudioSkip(0);
            StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
            StreamConfiguration.getInstance().setMovFileVersion(1);
            MediaStreamSupport.updateConfiguration();
            IInboundMediaStream stream = mFactory.getInboundMediaStream();
            stream.setCallSession(session);
            stream.setCNAME("FOOBAR");
            stream.setEventDispatcher(eventDispatcher);

            stream.create(mediaUtil.getPayloads(MediaUtil.Media.WAV));

            prop.setMaxRecordingDuration(5*1000);
            prop.setMinRecordingDuration(0);
            prop.setWaitForRecordToFinish(false);

            IMediaObject mo = mediaUtil.createRecordableMediaObject(MediaUtil.Media.WAV);

            Object callId = new Object();
            System.out.println("AudioPort: " + stream.getAudioPort());
            System.out.println("VideoPort: " + stream.getVideoPort());

            lastEvent = null;

            stream.record(callId, mo, prop);

            System.out.println("AudioPort: " + stream.getAudioPort());
            System.out.println("VideoPort: " + stream.getVideoPort());

            RTPDump audioReference = new RTPDump("referencePlayWav.rtp");
            audioReference.load("mtest/testfiles/referencePlayWav.rtp");

            playDumps("localhost", stream.getAudioPort(), stream.getVideoPort(), audioReference, null, false);


            try {
                Object callId2 = new Object();
                IMediaObject mo2 = mediaUtil.createRecordableMediaObject(MediaUtil.Media.WAV);
                stream.record(callId2, mo2, prop);
                fail("Issuing a record when a record is already in progress does not throw an UnsupportedOperationException!");
            } catch (UnsupportedOperationException e) {
                System.out.println("UnsupportedOperationException");
            }

            ((InboundMediaStreamImpl)stream).getEventNotifier().waitForCallToFinish(callId);

            if (lastEvent == null || !(lastEvent instanceof RecordFinishedEvent)) {
                stream.delete();
                fail("first record does not finish OK!");
            }

            stream.delete();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown when testing record already in progress: " + e.getMessage());
        }
    }

    public static void main(String[] argv) {
        new junit.textui.TestRunner().doRun(new TestSuite(StreamTest.class));
        System.exit(0);
    }

}