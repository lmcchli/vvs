/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallProperties;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * SdpSessionDescriptionFactory Tester.
 *
 * @author Malin Flodin
 */
public class SdpSessionDescriptionFactoryTest extends SdpCase
{
    private static final String SDP =
            "v=0\r\n" +
            "o=user 1 2 IN IP4 224.2.1.1\r\n" +
            "s=-\r\n" +
            "c=IN IP4 224.2.1.1\r\n" +
            "t=0 0\r\n" +
            "a=sendrecv\r\n" +
            "m=audio 17434 RTP/AVP 99 8 101\r\n" +
            "c=IN IP4 224.2.1.1\r\n" +
            "a=rtpmap:99 PCMU/8000\r\n" +
            "a=rtpmap:101 telephone-event/8000\r\n" +
            "a=fmtp:101 0-16\r\n" +
            "a=ptime:40\r\n" +
            "m=video 17436 RTP/AVP 34\r\n" +
            "m=video 17438 RTP/AVP 34\r\n";

    private static final String ALT_SDP =
            "v=0\r\n" +
                    "o=user 1 2 IN IP4 224.2.1.1\r\n" +
                    "s= \r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "t=0 0\r\n" +
                    "a=sendrecv\r\n" +
                    "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n" +
                    "m=video 17436 RTP/AVP 34\r\n" +
                    "m=video 17438 RTP/AVP 34\r\n";

    private static final String EXPECTED_SDP_ANSWER =
            "v=0" +
            "o=origin 0 0 IN IP4 1.2.3.4" +
            "s=-" +
            "c=IN IP4 1.2.3.4" +
            "t=0 0" +
            "m=audio 1234 RTP/AVP 99 101" +
            "c=IN IP4 1.2.3.4" +
            "a=ptime:20" +
            "a=maxptime:40" +
            "a=rtpmap:99 PCMU/8000/1" +
            "a=rtpmap:101 telephone-event/8000/1" +
            "a=fmtp:101 0-16" +
            "m=video 4321 RTP/AVP 34" +
            "c=IN IP4 4.3.2.1" +
            "a=rtpmap:34 H263/8000/1" +
            "m=video 0 RTP/AVP 34";

    private static final String EXPECTED_SDP_OFFER =
            "v=0" +
            "o=origin 0 0 IN IP4 1.2.3.4" +
            "s=-" +
            "c=IN IP4 1.2.3.4" +
            "t=0 0" +
            "m=audio 1234 RTP/AVP 0 101" +
            "c=IN IP4 1.2.3.4" +
            "a=ptime:20" +
            "a=maxptime:40" +
            "a=rtpmap:101 telephone-event/8000/1" +
            "a=rtpmap:0 PCMU/8000/1" +
            "a=fmtp:101 0-16" +
            "m=video 4321 RTP/AVP 34" +
            "c=IN IP4 4.3.2.1" +
            "a=rtpmap:34 H263/8000/1";

    private SdpIntersection emptySdpIntersection;
    private Collection<MimeType> audioMimeTypes = new ArrayList<MimeType>();
    private Collection<MimeType> videoMimeTypes = new ArrayList<MimeType>();

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private SdpSessionDescriptionFactory factory;

    public void setUp() throws Exception {
        super.setUp();

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                8, new MimeType("audio/PCMA"), "PCMA", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        ConnectionProperties.updateDefaultPTimes(40,40);
        
        audioMimeTypes.add(RTPPayload.AUDIO_PCMU);
        videoMimeTypes.add(RTPPayload.VIDEO_H263);
        emptySdpIntersection = new SdpIntersection(
                new SdpSessionDescriptionImpl(), 1, 2, 0, null,
                audioMimeTypes,videoMimeTypes);

        factory = new SdpSessionDescriptionFactory();
        factory.init();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that null is returned when parsing a null SDP.
     * @throws Exception if test case fails.
     */
    public void testParseRemoteSdpWhenSdpIsNull() throws Exception {
        assertNull(factory.parseRemoteSdp(null));
    }

    /**
     * Verify that an {@link SdpNotSupportedException} with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD} is thrown when the SDP
     * could not be parsed.
     *
     * @throws Exception if test case fails.
     */
    public void testParseRemoteSdpWhenSdpIsInvalid() throws Exception {
        try {
            factory.parseRemoteSdp("o=user");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verifies that an SDP can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseRemoteSdp() throws Exception {
        SdpSessionDescription sd = factory.parseRemoteSdp(SDP);
        assertConnectionField(sd.getConnection(), "224.2.1.1");
        assertFmtp(sd.getAttributes().getFmtps(), null);
        assertRtpMap(sd.getAttributes().getRtpMaps(), null);
        assertPTime(sd.getAttributes().getPTime(), null);
        assertTransmissionMode(sd.getAttributes().getTransmissionMode(),
                SdpTransmissionMode.SENDRECV);
        assertOrigin(sd.getOrigin(), "user");
        assertEquals(3, sd.getMediaDescriptions().size());

        //2 Test with s=0x20 (space)
        sd = factory.parseRemoteSdp(ALT_SDP);
        assertConnectionField(sd.getConnection(), "224.2.1.1");
        assertFmtp(sd.getAttributes().getFmtps(), null);
        assertRtpMap(sd.getAttributes().getRtpMaps(), null);
        assertPTime(sd.getAttributes().getPTime(), null);
        assertTransmissionMode(sd.getAttributes().getTransmissionMode(),
                SdpTransmissionMode.SENDRECV);
        assertOrigin(sd.getOrigin(), "user");
        assertEquals(3, sd.getMediaDescriptions().size());        
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when creating an
     * SDP answer will a null argument.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpAnswerIfParameterIsNull () throws Exception {
        // Set SDP intersection to null.
        try {
            factory.createSdpAnswer(
                    null, RTPPayload.AUDIO_PCMU, RTPPayload.VIDEO_H263,
                    new ConnectionProperties(), "origin", null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set audio mime type to null.
        try {
            factory.createSdpAnswer(
                    emptySdpIntersection, null, RTPPayload.VIDEO_H263,
                    new ConnectionProperties(), "origin", null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set video mime type to null.
        try {
            factory.createSdpAnswer(
                    emptySdpIntersection, RTPPayload.AUDIO_PCMU, null,
                    new ConnectionProperties(), "origin", null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set connection properties to null.
        try {
            factory.createSdpAnswer(
                    emptySdpIntersection, RTPPayload.AUDIO_PCMU,
                    RTPPayload.VIDEO_H263, null, "origin", null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set origin to null.
        try {
            factory.createSdpAnswer(
                    emptySdpIntersection, RTPPayload.AUDIO_PCMU,
                    RTPPayload.VIDEO_H263, new ConnectionProperties(), null, null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verifies that an SDP answer can be created from an SDP intersection.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpAnswer() throws Exception {
	
	ConnectionProperties.updateDefaultPTimes(20,40);
        SdpSessionDescription sd = factory.parseRemoteSdp(SDP);
        SdpIntersection sdpIntersection = new SdpIntersection(
                sd, 0, 1, 0, null, audioMimeTypes, videoMimeTypes);

        ConnectionProperties cp = new ConnectionProperties();
        cp.setAudioHost("1.2.3.4");
        cp.setAudioPort(1234);
        cp.setVideoHost("4.3.2.1");
        cp.setVideoPort(4321);
        cp.setPTime(20);
        cp.setMaxPTime(40);
        String answer = factory.createSdpAnswer(
                sdpIntersection, RTPPayload.AUDIO_PCMU,
                RTPPayload.VIDEO_H263, cp, "origin", null);

        answer = answer.replaceAll("\r", "");
        answer = answer.replaceAll("\n", "");
        assertEquals(EXPECTED_SDP_ANSWER, answer);
    }

    /**
     * Verifies that a {@link NullPointerException} is thrown when creating an
     * SDP offer will a null argument.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpOfferIfParameterIsNull () throws Exception {
        // Set call type to null.
        try {
            factory.createSdpOffer(
                    null, RTPPayload.AUDIO_PCMU, RTPPayload.VIDEO_H263,
                    new ConnectionProperties(), "origin");
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set audio mime type to null.
        try {
            factory.createSdpOffer(
                    CallProperties.CallType.VOICE, null, RTPPayload.VIDEO_H263,
                    new ConnectionProperties(), "origin");
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set video mime type to null.
        try {
            factory.createSdpOffer(
                    CallProperties.CallType.VIDEO, RTPPayload.AUDIO_PCMU, null,
                    new ConnectionProperties(), "origin");
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set connection properties to null.
        try {
            factory.createSdpOffer(
                    CallProperties.CallType.VOICE, RTPPayload.AUDIO_PCMU,
                    RTPPayload.VIDEO_H263, null, "origin");
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Set origin to null.
        try {
            factory.createSdpOffer(
                    CallProperties.CallType.VOICE, RTPPayload.AUDIO_PCMU,
                    RTPPayload.VIDEO_H263, new ConnectionProperties(), null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verifies that an {@link SdpInternalErrorException} is thrown when
     * creating an SDP offer and a Mime type cannot be found in RTPPayload.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpOfferWhenMimeTypeDoesNotExist() throws Exception {
	ConnectionProperties.updateDefaultPTimes(20,40);
        SdpSessionDescription sd = factory.parseRemoteSdp(SDP);
        SdpIntersection sdpIntersection = new SdpIntersection(
                sd, 0, 1, 0, null, audioMimeTypes, videoMimeTypes);

        ConnectionProperties cp = new ConnectionProperties();
        cp.setAudioHost("1.2.3.4");
        cp.setAudioPort(1234);
        cp.setVideoHost("4.3.2.1");
        cp.setVideoPort(4321);
        cp.setPTime(20);
        cp.setMaxPTime(40);

        try {
            factory.createSdpAnswer(
                    sdpIntersection, RTPPayload.AUDIO_AMR,
                    RTPPayload.VIDEO_H263, cp, "origin", null);
            fail("Exception not thrown when expected.");
        } catch (SdpInternalErrorException e) {
        }
    }

    /**
     * Verifies that an SDP offer can be created.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpOffer() throws Exception {
        ConnectionProperties cp = new ConnectionProperties();
        cp.setAudioHost("1.2.3.4");
        cp.setAudioPort(1234);
        cp.setVideoHost("4.3.2.1");
        cp.setVideoPort(4321);
        cp.setPTime(20);
        cp.setMaxPTime(40);
        String answer = factory.createSdpOffer(
                CallProperties.CallType.VIDEO, RTPPayload.AUDIO_PCMU,
                RTPPayload.VIDEO_H263, cp, "origin");

        answer = answer.replaceAll("\r", "");
        answer = answer.replaceAll("\n", "");
        assertEquals(EXPECTED_SDP_OFFER, answer);
    }

}
