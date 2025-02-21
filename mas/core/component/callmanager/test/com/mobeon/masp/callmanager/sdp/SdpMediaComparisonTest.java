/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaTransport;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import java.util.ArrayList;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

/**
 * SdpMediaComparison Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaComparisonTest extends MockObjectTestCase
{
    private static final String SDP =
            "v=0\r\n" +
            "o=user 1 2 IN IP4 224.2.1.1\r\n" +
            "s=-\r\n" +
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
            "m=video 17438 RTP/AVP 31 34\r\n";

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private SdpSessionDescriptionFactory sdpFactory = new SdpSessionDescriptionFactory();
    private SdpMediaComparison comparer = SdpMediaComparison.getInstance();
    private List<MimeType> audioMimeTypes = new ArrayList<MimeType>();
    private List<MimeType> videoMimeTypes = new ArrayList<MimeType>();
    private CallMediaTypes[] callMediaTypes = null;

    // Session Descriptions
    private Mock sdMock = mock(SdpSessionDescription.class);
    private SdpSessionDescription sd = (SdpSessionDescription)sdMock.proxy();

    // Media descriptions
    private Mock audioMDMock                = mock(SdpMediaDescription.class);
    private Mock videoMDMock                = mock(SdpMediaDescription.class);
    private SdpMediaDescription audioMD     = (SdpMediaDescription)audioMDMock.proxy();
    private SdpMediaDescription videoMD     = (SdpMediaDescription)videoMDMock.proxy();
    List<SdpMediaDescription> mdList        = new ArrayList<SdpMediaDescription>();
    private SdpMedia audioMedia             = new SdpMedia(
            SdpMediaType.AUDIO, 1234, 0, SdpMediaTransport.RTP_AVP, null);
    private SdpMedia videoMedia             = new SdpMedia(
            SdpMediaType.VIDEO, 1234, 0, SdpMediaTransport.RTP_AVP, null);

    // Other fields
    private SdpConnection connection = new SdpConnection("1.2.3.4");
    private SdpAttributes attributes = new SdpAttributes();

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
                31, new MimeType("video/H261"), "H261", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        ConnectionProperties.updateDefaultPTimes(20,40);
        audioMimeTypes.add(RTPPayload.AUDIO_PCMU);
        videoMimeTypes.add(RTPPayload.VIDEO_H263);

        MediaMimeTypes mimeTypes = new MediaMimeTypes(audioMimeTypes);
        mimeTypes.addAll(new MediaMimeTypes(videoMimeTypes));
        callMediaTypes = new CallMediaTypes[] {new CallMediaTypes(mimeTypes, this)};

        sdpFactory.init();

        setupExpectations();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that null intersection is returned in input parameter to
     * getSdpIntersection is null.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenInputParameterIsNull()
            throws Exception {
        // Set session description to null
        assertNull(comparer.getSdpIntersection(
                null, audioMimeTypes, videoMimeTypes, null));

        // Set mandatory audio types to null
        assertNull(comparer.getSdpIntersection(
                new SdpSessionDescriptionImpl(), null, videoMimeTypes, null));

        // Set mandatory video types to null
        assertNull(comparer.getSdpIntersection(
                new SdpSessionDescriptionImpl(), audioMimeTypes, null, null));
    }

    /**
     * Verifies that a media description that lacks media level and session level
     * connection field is ignored and will not match. Null is returned from
     * getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenMediaDescriptionLacksConnection()
            throws Exception {
        audioMDMock.expects(once()).method("getConnection").will(returnValue(null));

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, null));
    }

    /**
     * Verifies that a media description that has a transmission mode set that
     * differs from sendrecv is ignored and will not match.
     * Null is returned from getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenNotSupportedIllegalTransmissionModeInMD()
            throws Exception {
        attributes.setTransmissionMode(SdpTransmissionMode.RECVONLY);

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, null));
    }

    /**
     * Verifies that if all media descriptions do not support the mandatory
     * audio media type, null is returned from getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenMandatoryAudioEncodingIsNotSupported()
            throws Exception {
        audioMDMock.expects(once()).method("areEncodingsSupported").
                will(returnValue(false));

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, null));
    }

    /**
     * Verifies that if all media descriptions do not support the mandatory
     * video media type, an SdpIntersection is returned from getSdpIntersection
     * that has call type VOICE.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenMandatoryVideoEncodingIsNotSupported()
            throws Exception {
        videoMDMock.expects(once()).method("areEncodingsSupported").will(returnValue(false));

        SdpIntersection intersection = comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, null);
        assertNotNull(intersection);
        assertEquals(CallProperties.CallType.VOICE, intersection.getCallType());
        assertTrue(intersection.getAudioIndex() == 0);
        assertNull(intersection.getVideoIndex());
    }

    /**
     * Verifies that if no media description supports the call specific
     * audio media type, null is returned from getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenCallSpecificAudioEncodingIsNotSupported()
            throws Exception {
        audioMDMock.expects(once()).method("isEncodingSupported").
                will(returnValue(false));

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, callMediaTypes));
    }

    /**
     * Verifies that if no media description supports the call specific
     * video media type, null is returned from getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenCallSpecificVideoEncodingIsNotSupported()
            throws Exception {
        videoMDMock.expects(once()).method("isEncodingSupported").
                will(returnValue(false));

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, callMediaTypes));
    }

    /**
     * Verifies that if the remote SDP contains video, but there are no
     * matching video index for the call,
     * null is returned from getSdpIntersection.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenVideoInSdpButNotInCallSpecificMedia()
            throws Exception {
        MediaMimeTypes mimeTypes = new MediaMimeTypes(audioMimeTypes);
        callMediaTypes = new CallMediaTypes[] {new CallMediaTypes(mimeTypes, this)};

        assertNull(comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, callMediaTypes));
    }

    /**
     * Verifies that an SDP intersection can be retrieved correctly when
     * call specific requirements on media exists.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenCallSpecificMediaRequirementsExists()
            throws Exception {
        SdpSessionDescription sd = sdpFactory.parseRemoteSdp(SDP);

        MediaMimeTypes mimeTypes = new MediaMimeTypes(audioMimeTypes);
        mimeTypes.addMimeType(new MimeType("video/H261"));

        SdpIntersection intersection = comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes,
                new CallMediaTypes[] {new CallMediaTypes(mimeTypes, this)});

        assertTrue(intersection.getAudioIndex() == 0);
        assertTrue(intersection.getVideoIndex() == 2);
        assertEquals(CallProperties.CallType.VIDEO, intersection.getCallType());
    }

    /**
     * Verifies that an SDP intersection can be retrieved correctly when
     * no call specific requirements on media exists.
     * @throws Exception if test case fails.
     */
    public void testGetSdpIntersectionWhenNoCallSpecificMediaRequirementsExists()
            throws Exception {
        SdpSessionDescription sd = sdpFactory.parseRemoteSdp(SDP);

        SdpIntersection intersection = comparer.getSdpIntersection(
                sd, audioMimeTypes, videoMimeTypes, null);

        assertTrue(intersection.getAudioIndex() == 0);
        assertTrue(intersection.getVideoIndex() == 1);
        assertEquals(CallProperties.CallType.VIDEO, intersection.getCallType());
    }

    // ============================= Private methods =====================

    private void setupExpectations() {
        mdList.add(audioMD);
        mdList.add(videoMD);

        sdMock.stubs().method("getMediaDescriptions").will(returnValue(mdList));
        sdMock.stubs().method("containsVideo").will(returnValue(true));

        audioMDMock.stubs().method("getConnection").will(returnValue(connection));
        audioMDMock.stubs().method("getAttributes").will(returnValue(attributes));
        audioMDMock.stubs().method("getMedia").will(returnValue(audioMedia));
        audioMDMock.stubs().method("areEncodingsSupported").will(returnValue(true));
        audioMDMock.stubs().method("isEncodingSupported").will(returnValue(true));

        videoMDMock.stubs().method("getConnection").will(returnValue(connection));
        videoMDMock.stubs().method("getAttributes").will(returnValue(attributes));
        videoMDMock.stubs().method("getMedia").will(returnValue(videoMedia));
        videoMDMock.stubs().method("areEncodingsSupported").will(returnValue(true));
        videoMDMock.stubs().method("isEncodingSupported").will(returnValue(true));

    }

}
