package com.mobeon.masp.stream;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.MDC;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;

import jakarta.activation.MimeType;
import java.util.Collection;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;

public class StreamDriver {
    enum Action { UNDEFINED, PLAY, RECORD, JOIN };
    enum Media { UNDEFINED, WAV, MOV, AMR };
    private String remoteHost = null;
    private int remotePort = -1;
    private int localPort = -1;
    private String inputFileName = null;
    private String outputFileName = null;
    private Action action = Action.UNDEFINED;
    private Media media = Media.UNDEFINED;
    protected StreamFactoryImpl mFactory;
    private MimeType VIDEO_QUICKTIME;
    private MimeType VIDEO_3GPP;
    private MimeType AUDIO_WAV;
    private boolean handleDtmfAtInbound = true;
    private boolean forwardDtmfToOutbound = false;
    IEventDispatcher eventDispatcher = new MockEventDispatcher();
    ISession session = new MockSession("SessionID_4711");

    StreamDriver() {
        session.registerSessionInLogger();
        mFactory = new StreamFactoryImpl();
        ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();

        try {
            cm.setConfigFile("../cfg/mas.xml");
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
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            VIDEO_3GPP = new MimeType("video/3gpp");
            AUDIO_WAV = new MimeType("audio/wav");
        } catch (Exception e) {
            fail("Failed to initiate the mime types: " + e);
        }
     }

    private void execute() {
        switch (action) {
            case PLAY:
                play();
                break;

            case RECORD:
                record();
                break;

            case JOIN:
                join();
                break;

            default:
                break;
        }
    }

    private void play() {
        System.out.println("*** Play ***");
        Collection<RTPPayload> payloads = getPayloads(media);
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(remotePort);
        cProp.setVideoPort(remotePort+2);
        cProp.setAudioHost(remoteHost);
        cProp.setVideoHost(remoteHost);
        cProp.setPTime(20);
        cProp.setMaxPTime(40);

        OutboundMediaStreamImpl stream =
            (OutboundMediaStreamImpl)mFactory.getOutboundMediaStream();
        stream.setCallSession(session);
        stream.setEventDispatcher(eventDispatcher);
        try {
            stream.create(payloads, cProp);
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception while " +
                    "calling create: " + e);
        }
        try {
            Object callId = new Object();
            stream.play(callId,
                    createPlayableMediaObject(inputFileName, media),
                    IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO, 0);
            stream.getEventNotifier().waitForCallToFinish(callId);
            stream.delete();
        }
        catch (Exception e) {
            fail("Correct state 2: Unexpected exception: " + e);
        }
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
            e.printStackTrace();
            fail("Unexpected error in createMediaObject: " + e);
        }
        return result;
    }

    private void record() {
        System.out.println("*** Record ***");
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        MediaMimeTypes mediaMimeTypes = getMimeTypes(media);
        StreamConfiguration.getInstance().setAbandonedStreamDetectedTimeout(60000);
        StreamConfiguration.getInstance().setAudioSkip(0);
        StreamConfiguration.getInstance().setDispatchDTMFOnKeyDown(false);
        StreamConfiguration.getInstance().setMovFileVersion(1);
        MediaStreamSupport.updateConfiguration();
        IInboundMediaStream stream = mFactory.getInboundMediaStream();
        stream.setCallSession(session);
        stream.setCNAME("FOOBAR");
        stream.setEventDispatcher(eventDispatcher);

        try {
            stream.create(mediaMimeTypes);
        }
        catch (Exception e) {
            fail("Record: Unexpected exception while calling create: " + e);
        }
        prop.setMaxRecordingDuration(10*1000);
        prop.setMinRecordingDuration(0);
        prop.setMaxSilence(2*1000);
        prop.setWaitForRecordToFinish(true);
        IMediaObject mo = createRecordableMediaObject(media);

        Object callId = new Object();

        try {
            //set actual dtmf payload type, e.g. 97
            //Add commented out code if test of changed DTMF Payload Type is required, e.g. 
            //for a outbound call, when the SDP is renegotiated with respect to DTMF Payload Type
            //for example from 101 to 97
            //MimeType testMimeType = null;
            //RTPPayload dtmfPayLoad = new RTPPayload(97, testMimeType, "test1", 0, 0,0, "test2");
            
            //stream.reNegotiatedSdp(dtmfPayLoad); 
            
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
            System.out.println("Cumulative packet lost=" + stream.getCumulativePacketLost());
            System.out.println("Fraction lost=" + stream.getFractionLost());
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        stream.delete();
        save(mo, outputFileName);
    }

    private IMediaObject createRecordableMediaObject(Media media) {
        IMediaObject result = null;
        MimeType mimeType = null;

        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            result = factory.create();
        }
        catch (Exception e) {
            fail("Unexpected error in createRecordableMediaObject: " + e);
        }

        switch (media) {
        case WAV:
            mimeType = AUDIO_WAV;
            break;

        case MOV:
            mimeType = VIDEO_QUICKTIME;
            break;

        case AMR:
            mimeType = VIDEO_3GPP;
            break;

        default:
            break;
        }
        result.getMediaProperties().setContentType(mimeType);
        return result;
    }

    private void join() {
        System.out.println("*** Join ***");
        IInboundMediaStream inStream = mFactory.getInboundMediaStream();
        IOutboundMediaStream outStream = mFactory.getOutboundMediaStream();
        inStream.setCallSession(session);
        outStream.setCallSession(session);

        // Initializing the inbound PCMU stream
        try {
            inStream.setEventDispatcher(eventDispatcher);
            inStream.create(getMimeTypes(media));
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        // Initializing the inbound PCMU/AMR stream
        try {
            outStream.setEventDispatcher(eventDispatcher);
            Collection<RTPPayload> payloads = getPayloads(media);
            ConnectionProperties cProp = new ConnectionProperties();
            cProp.setAudioPort(remotePort);
            cProp.setAudioHost(remoteHost);
            cProp.setVideoPort(remotePort+2);
            cProp.setVideoHost(remoteHost);
            cProp.setPTime(20);
            outStream.create(payloads, cProp, null, inStream);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }

        try {
            inStream.join(handleDtmfAtInbound, outStream, forwardDtmfToOutbound);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
        try {
            Thread.sleep(2000000);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        try {
            inStream.unjoin(outStream);
        } catch (StackException e) {
            fail("Caught exception: " + e);
        }

        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            fail("Interrupted: " + e);
        }

        inStream.delete();
        outStream.delete();
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

    private MediaMimeTypes getMimeTypes(Media media) {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();

        switch (media) {
        case WAV:
            mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
            break;

        case MOV:
            mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
            mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
            break;

        case AMR:
            mediaMimeTypes.addMimeType(RTPPayload.AUDIO_AMR);
            mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
            break;

        default:
            break;
        }
        return mediaMimeTypes;
    }

    boolean parseArguments(String[] argv) {
        if (argv.length == 0) return false;
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            System.out.println("Argument: [" + arg + "]");
            if ("-h".equals(arg)) {
                return false;
            } else if ("-r".equals(arg)) {
                if (++i < argv.length) remoteHost = argv[i];
                else return false;
            } else if ("-p".equals(arg)) {
                if (++i < argv.length) remotePort = Integer.parseInt(argv[i]);
                else return false;
            } else if ("-l".equals(arg)) {
                if (++i < argv.length) localPort = Integer.parseInt(argv[i]);
                else return false;
            } else if ("-m".equals(arg)) {
                if (++i < argv.length) {
                    if ("wav".equalsIgnoreCase(argv[i])) media = Media.WAV;
                    else if ("mov".equalsIgnoreCase(argv[i])) media = Media.MOV;
                    else if ("amr".equalsIgnoreCase(argv[i])) media = Media.AMR;
                    else return false;
                }
            } else if ("-i".equals(arg)) {
                if (++i < argv.length) inputFileName = argv[i];
                else return false;
            } else if ("-o".equals(arg)) {
                if (++i < argv.length) outputFileName = argv[i];
                else return false;
            } else if ("-d".equals(arg)) {
                if (++i < argv.length) handleDtmfAtInbound = "true".equals(argv[i]);
                else return false;
            } else if ("-f".equals(arg)) {
                if (++i < argv.length) forwardDtmfToOutbound = "true".equals(argv[i]);
                else return false;
            } else if (i == 0) {
                if ("play".equalsIgnoreCase(arg)) action = Action.PLAY;
                else if ("record".equalsIgnoreCase(arg)) action = Action.RECORD;
                else if ("join".equalsIgnoreCase(arg)) action = Action.JOIN;
                else return false;
            }
        }
        switch (action) {
            case PLAY:
                if (inputFileName == null || media == Media.UNDEFINED ||
                        remoteHost == null || remotePort == -1) {
                    return false;
                }
                break;

            case RECORD:
                if (outputFileName == null || media == Media.UNDEFINED ||
                        localPort == -1) {
                    return false;
                }
                break;

            case JOIN:
                if (media == Media.UNDEFINED || localPort == -1 ||
                        remoteHost == null || remotePort == -1) {
                    return false;
                }
                break;

            default:
                return false;
        }
        return true;
    }

    void printSyntax() {
        System.out.println("Syntax: <action> <options>");
        System.out.println("  PLAY -m <media> -i <filename> -r <remote host> -p <remote port>");
        System.out.println("  RECORD -m <media> -o <filename> -l <local port>");
        System.out.println("  JOIN -m <media> -r <remote host> -p <remote port> -l <local port> [-d <inbound>][-f <outbound>]");
        System.out.println("");
        System.out.println("  media: 'wav', 'amr' or 'mov'");
        System.out.println("  inbound: 'true' or 'false' (handle DTMF in InboundStream)");
        System.out.println("  outbound: 'true' or 'false' (forward DTMF to OutboundStream)");
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
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    void fail(String message) {
        System.out.println("ERROR: " + message);
        System.exit(1);
    }

    public static void main(String[] argv) {
        StreamDriver streamDriver = new StreamDriver();

        if (streamDriver.parseArguments(argv)) {
            streamDriver.execute();
        } else {
            streamDriver.printSyntax();
        }
    }
}

