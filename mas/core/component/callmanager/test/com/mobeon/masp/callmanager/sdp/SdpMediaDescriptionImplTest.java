/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.masp.callmanager.sdp.attributes.SdpRtpMap;
import com.mobeon.masp.callmanager.sdp.attributes.SdpFmtp;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPTime;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.sdp.*;

import java.util.Collection;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.jmock.Mock;

import jakarta.activation.MimeType;

/**
 * SdpMediaDescriptionImpl Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaDescriptionImplTest extends SdpCase {

    private static final String SDP =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "b=AS:64\r\n" +
                    "b=RR:800\r\n" +
                    "b=RS:2400\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n" +
                    "a=sendrecv";

    private static final String SDP_2 =
        "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                "c=IN IP4 0.0.0.0\r\n" +
                "b=AS:64\r\n" +
                "b=RR:800\r\n" +
                "b=RS:2400\r\n" +
                "a=rtpmap:0 PCMU/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:40\r\n" +
                "a=sendrecv";

    private static final String SDP_AMR1 =
        "m=audio 17434 RTP/AVP 96 101\r\n" +
                "c=IN IP4 224.2.1.1\r\n" +
                "b=AS:13\r\n" +
                "a=rtpmap:96 AMR/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:96 mode-set=7;octet-align=1\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:20\r\n" +
                "a=maxptime:40\r\n" +
                "a=sendrecv";

    private static final String SDP_AMR2 =
        "m=audio 17434 RTP/AVP 101 122\r\n" +
                "c=IN IP4 224.2.1.1\r\n" +
                "b=AS:13\r\n" +
                "a=rtpmap:122 AMR/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:122 mode-set=7;octet-align=1;robust-sorting=0\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:30\r\n" +
                "a=maxptime:60\r\n" +
                "a=sendrecv";

    private static final String SDP_AMR3 =
        "m=audio 17434 RTP/AVP 96 101\r\n" +
                "c=IN IP4 224.2.1.1\r\n" +
                "b=AS:13\r\n" +
                "a=rtpmap:96 AMR/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:96 mode-set=1,2,3,4,5,6,7;octet-align=1\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:20\r\n" +
                "a=maxptime:40\r\n" +
                "a=sendrecv";

    private static final String SDP_AMR4 =
        "m=audio 17434 RTP/AVP 96 101\r\n" +
                "c=IN IP4 224.2.1.1\r\n" +
                "b=AS:13\r\n" +
                "a=rtpmap:96 AMR/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:96 mode-set=7\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:20\r\n" +
                "a=maxptime:40\r\n" +
                "a=sendrecv";

    private static final String SDP_AMR5 =
        "m=audio 17434 RTP/AVP 97 101\r\n" +
                "c=IN IP4 224.2.1.1\r\n" +
                "b=AS:13\r\n" +
                "a=rtpmap:97 AMR/8000\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:97 mode-set=6;octet-align=1\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:20\r\n" +
                "a=maxptime:40\r\n" +
                "a=sendrecv";


    private static final Vector<Integer> AUDIO_ONLY_FORMATS = new Vector<Integer>();
    private static final Vector<Integer> AUDIO_AND_DTMF_FORMATS = new Vector<Integer>();
    private static final SdpFmtp DTMF_FMTP;
    private static final SdpRtpMap AUDIO_RTPMAP;
    private static final SdpRtpMap DTMF_RTPMAP;

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        AUDIO_ONLY_FORMATS.add(0);
        AUDIO_AND_DTMF_FORMATS.add(0);
        AUDIO_AND_DTMF_FORMATS.add(101);

        DTMF_FMTP = new SdpFmtp(101, "0-16");
        AUDIO_RTPMAP = new SdpRtpMap(0, "PCMU", 8000, 1);
        DTMF_RTPMAP = new SdpRtpMap(101, "telephone-event", 8000, 1);
    }

    private final Mock mdMock = mock(MediaDescription.class);

    private SdpFactory sdpFactory = null;

    public void setUp() throws Exception {
        super.setUp();

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 800, 0, 5000));
        rtppayloads.add(new RTPPayload(8, new MimeType("audio/PCMA"), "PCMA", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(96, RTPPayload.AUDIO_AMR, "amr", 8000, 1, 12200, "mode-set=7;octet-align=1;robust-sorting=0"));
        RTPPayload.updateDefs(rtppayloads);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that the constructor throws NullPointerException if any
     * mandatory input parameter is null.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testConstructor() throws Exception {
        SdpMedia media = new SdpMedia(null, 0, 0, null, null);
        SdpConnection connection = new SdpConnection("host");
        HashMap<String,SdpBandwidth> bandwidths =
                new HashMap<String,SdpBandwidth>();
        SdpAttributes attributes = new SdpAttributes();
        Mock sdMock = new Mock(SdpSessionDescription.class);
        SdpSessionDescription sd = (SdpSessionDescription)sdMock.proxy();
        sdMock.stubs().method("getAttributes").will(returnValue(attributes));


        // Verify that exception is thrown when media is null
        try {
            new SdpMediaDescriptionImpl(
                    null, connection, bandwidths, attributes, sd);
            fail("Expected a NullPointerException but it was not thrown.");
        } catch (NullPointerException e) {
        }

        // Verify that exception is thrown when bandwidths are null
        try {
            new SdpMediaDescriptionImpl(
                    media, connection, null, attributes, sd);
            fail("Expected a NullPointerException but it was not thrown.");
        } catch (NullPointerException e) {
        }

        // Verify that exception is NOT thrown when connection is null
        try {
            new SdpMediaDescriptionImpl(
                    media, null, bandwidths, attributes, sd);
        } catch (NullPointerException e) {
            fail("A NullPointerException was received but unexpected.");
        }

        // Verify that exception is thrown when attributes are null
        try {
            new SdpMediaDescriptionImpl(
                    media, connection, bandwidths, null, sd);
            fail("Expected a NullPointerException but it was not thrown.");
        } catch (NullPointerException e) {
        }

        // Verify that exception is thrown when session description is null
        try {
            new SdpMediaDescriptionImpl(
                    media, connection, bandwidths, attributes, null);
            fail("Expected a NullPointerException but it was not thrown.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verifies that if connection is not set on media description, the one set
     * in the session description will be used instead.
     * @throws Exception if test case fails.
     */
    public void testGetConnection() throws Exception {
        Mock parsedSDMock = mock(SdpSessionDescription.class);
        parsedSDMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("host")));
        parsedSDMock.expects(once()).method("getAttributes").
                will(returnValue(new SdpAttributes()));

        String SDP = "m=audio 17434 RTP/AVP 0 101";
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        (SdpSessionDescription)parsedSDMock.proxy());

        assertConnectionField(sdpMd.getConnection(), "host");
    }

    /**
     * Verifies that the bandwidths are retrieved correctly when parsing a
     * media description.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testGetBandwidths() throws Exception {

        // First create a test input
        String SDP =
                "m=audio 17434 RTP/AVP 0 101\r\n" +
                "b=AS:64\r\n" +
                "b=AS:20\r\n" +
                "b=CT:120\r\n" +
                "b=RR:800\r\n" +
                "b=RS:2400\r\n";
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);
        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        // Then parse the test input
        Mock parsedSDMock = mock(SdpSessionDescription.class);
        parsedSDMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("host")));
        parsedSDMock.expects(once()).method("getAttributes").
                will(returnValue(new SdpAttributes()));

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        (SdpSessionDescription)parsedSDMock.proxy());

        HashMap<String, SdpBandwidth> bandwidths = sdpMd.getBandwidths();
        assertEquals(4, bandwidths.size());
        assertEquals(20, bandwidths.get("AS").getValue());
        assertEquals("AS", bandwidths.get("AS").getType());
        assertEquals(120, bandwidths.get("CT").getValue());
        assertEquals("CT", bandwidths.get("CT").getType());
        assertEquals(800, bandwidths.get("RR").getValue());
        assertEquals("RR", bandwidths.get("RR").getType());
        assertEquals(2400, bandwidths.get("RS").getValue());
        assertEquals("RS", bandwidths.get("RS").getType());
    }

    /**
     * Verifies that if an attribute is not set on media description, the one set
     * in the session description will be used instead.
     * @throws Exception if test case fails.
     */
    public void testGetAttributes() throws Exception {
        Mock parsedSDMock = mock(SdpSessionDescription.class);
        parsedSDMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("host")));
        SdpAttributes attributes = new SdpAttributes();
        attributes.setPTime(new SdpPTime(20));
        attributes.setTransmissionMode(SdpTransmissionMode.SENDRECV);
        SdpFmtp fmtp = new SdpFmtp(101, "0-16");
        attributes.addFmtp(fmtp);
        SdpRtpMap rtpmap = new SdpRtpMap(0, "PCMU", 8000, 1);
        attributes.addRtpMap(rtpmap);
        parsedSDMock.stubs().method("getAttributes").will(returnValue(attributes));

        String SDP = "m=audio 17434 RTP/AVP 0 101";
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        (SdpSessionDescription)parsedSDMock.proxy());

        assertFmtp(sdpMd.getAttributes().getFmtps(), fmtp);
        assertRtpMap(sdpMd.getAttributes().getRtpMaps(), rtpmap);
        assertPTime(sdpMd.getAttributes().getPTime(), 20);
        assertTransmissionMode(sdpMd.getAttributes().getTransmissionMode(),
                SdpTransmissionMode.SENDRECV);
    }

    /**
     * Verify that a NullPointerException is thrown when creating a media
     * description with a parameter (that MUST not be null) set to null.
     * @throws Exception if test case fails.
     */
    public void testCreateMediaDescriptionWithNullParameters() throws Exception {

        // Media type is null
        try {
            SdpMediaDescriptionImpl.createMediaDescription(
                    null, RTPPayload.get(RTPPayload.AUDIO_PCMU), null,
                    "host", 0, null,null, new SdpSessionDescriptionImpl(), null, null, null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Media payload is null
        try {
            SdpMediaDescriptionImpl.createMediaDescription(
                    SdpMediaType.AUDIO, null, null,
                    "host", 0, null, null, new SdpSessionDescriptionImpl(), null, null, null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Host is null
        try {
            SdpMediaDescriptionImpl.createMediaDescription(
                    SdpMediaType.AUDIO, RTPPayload.get(RTPPayload.AUDIO_PCMU),
                    null, null, 0, null, null, new SdpSessionDescriptionImpl(), null, null, null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }

        // Session Description is null
        try {
            SdpMediaDescriptionImpl.createMediaDescription(
                    SdpMediaType.AUDIO, RTPPayload.get(RTPPayload.AUDIO_PCMU),
                    null, "host", 0, null, null, null, null, null, null);
            fail("Exception not thrown when expected.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verify that a Media Description without DTMF support can be created from
     * an RTPPayload.
     * @throws Exception if test case fails.
     */
    public void testCreateMediaDescriptionWithoutDtmf() throws Exception {
        SdpMediaDescription md = SdpMediaDescriptionImpl.createMediaDescription(
                SdpMediaType.AUDIO,
                RTPPayload.get(RTPPayload.AUDIO_PCMU),
                null,
                "host", 1234, null, null, new SdpSessionDescriptionImpl(), null, null, null);

        // Verify the Media field
        assertMediaField(
                md.getMedia(), SdpMediaType.AUDIO, 1234, 0, AUDIO_ONLY_FORMATS);

        // Verify the Connection field
        assertConnectionField(md.getConnection(), "host");

        // Verify the Bandwidth fields
        assertEquals(3, md.getBandwidths().size());
        assertEquals(64, md.getBandwidth("AS").getValue());
        assertEquals(800, md.getBandwidth("RR").getValue());
        assertEquals(2400, md.getBandwidth("RS").getValue());


        // Verify the Attribute fields
        assertFmtp(md.getAttributes().getFmtps(), null);
        assertRtpMap(md.getAttributes().getRtpMaps(), AUDIO_RTPMAP);
        assertPTime(md.getAttributes().getPTime(), null);
        assertTransmissionMode(md.getAttributes().getTransmissionMode(), null);
    }

    /**
     * Verify that a Media Description with DTMF support can be created from
     * an RTPPayload.
     * @throws Exception if test case fails.
     */
    public void testCreateMediaDescriptionWithDtmf() throws Exception {
        SdpMediaDescription md = SdpMediaDescriptionImpl.createMediaDescription(
                SdpMediaType.AUDIO,
                RTPPayload.get(RTPPayload.AUDIO_PCMU),
                RTPPayload.get(RTPPayload.AUDIO_DTMF),
                "host", 1234, 40, 40, new SdpSessionDescriptionImpl(), null, null, null);

        // Verify the Media field
        assertMediaField(
                md.getMedia(), SdpMediaType.AUDIO, 1234, 0, AUDIO_AND_DTMF_FORMATS);

        // Verify the Connection field
        assertConnectionField(md.getConnection(), "host");

        // Verify the Bandwidth fields
        assertEquals(3, md.getBandwidths().size());
        assertEquals(64, md.getBandwidth("AS").getValue());
        assertEquals(800, md.getBandwidth("RR").getValue());
        assertEquals(2400, md.getBandwidth("RS").getValue());

        // Verify the Attribute fields
        assertFmtp(md.getAttributes().getFmtps(), DTMF_FMTP);
        assertRtpMap(md.getAttributes().getRtpMaps(), AUDIO_RTPMAP);
        assertRtpMap(md.getAttributes().getRtpMaps(), DTMF_RTPMAP);
        assertPTime(md.getAttributes().getPTime(), 40);
        assertTransmissionMode(md.getAttributes().getTransmissionMode(), null);
    }

    /**
     * Verify that null is returned when parsing a media description that is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescriptionWhenMediaDescriptionIsNull()
            throws Exception {
        SdpMediaDescription sdpMD = SdpMediaDescriptionImpl.parseMediaDescription(
                null, new SdpSessionDescriptionImpl());
        assertNull(sdpMD);
    }

    /**
     * Verify that a NullPointerException is thrown if session description is
     * null when parsing a media description.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescriptionWhenSessionDescriptionIsNull()
            throws Exception {
        try {
            SdpMediaDescriptionImpl.parseMediaDescription(
                    (MediaDescription)mdMock.proxy(), null);
            fail("Exception not trown when expected.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the parsed media is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescriptionWhenMediaIsNull() throws Exception {
        mdMock.expects(once()).method("getMedia").will(returnValue(null));

        try {
            SdpMediaDescriptionImpl.parseMediaDescription(
                    (MediaDescription)mdMock.proxy(),
                    new SdpSessionDescriptionImpl());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the parsed connection is null
     * on both media description and session description level.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescriptionWhenConnectionIsNull() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        Media media =
                sdpFactory.createMedia("audio", 1234, 1, "RTP/AVP", formats);
        mdMock.expects(once()).method("getMedia").will(returnValue(media));
        mdMock.expects(once()).method("getConnection").will(returnValue(null));
        mdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        mdMock.expects(once()).method("getAttributes").
                will(returnValue(new Vector<Attribute>()));
        mdMock.expects(once()).method("getKey").will(returnValue(null));

        try {
            SdpMediaDescriptionImpl.parseMediaDescription(
                    (MediaDescription)mdMock.proxy(),
                    new SdpSessionDescriptionImpl());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ENCRYPTION_NOT_SUPPORTED) is thrown if the media description
     * contains a key.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescriptionWithKey() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        Media media =
                sdpFactory.createMedia("audio", 1234, 1, "RTP/AVP", formats);
        Connection connection = sdpFactory.createConnection("host");
        Key key = sdpFactory.createKey("clear", "mykey");
        mdMock.expects(once()).method("getMedia").will(returnValue(media));
        mdMock.expects(once()).method("getConnection").will(returnValue(connection));
        mdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        mdMock.expects(once()).method("getAttributes").will(returnValue(null));
        mdMock.expects(once()).method("getKey").will(returnValue(key));

        try {
            SdpMediaDescriptionImpl.parseMediaDescription(
                    (MediaDescription)mdMock.proxy(),
                    new SdpSessionDescriptionImpl());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.ENCRYPTION_NOT_SUPPORTED,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that a correct media description is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseMediaDescription() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        Vector<Integer> integerFormats = new Vector<Integer>();
        integerFormats.add(5);
        Media media =
                sdpFactory.createMedia("audio", 1234, 1, "RTP/AVP", formats);
        Connection connection = sdpFactory.createConnection("host");
        mdMock.expects(once()).method("getMedia").will(returnValue(media));
        mdMock.expects(once()).method("getConnection").will(returnValue(connection));
        mdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        mdMock.expects(once()).method("getAttributes").will(returnValue(null));
        mdMock.expects(once()).method("getKey").will(returnValue(null));

        SdpMediaDescription md =
                SdpMediaDescriptionImpl.parseMediaDescription(
                (MediaDescription)mdMock.proxy(),
                new SdpSessionDescriptionImpl());

        assertConnectionField(md.getConnection(), "host");
        assertFmtp(md.getAttributes().getFmtps(), null);
        assertRtpMap(md.getAttributes().getRtpMaps(), null);
        assertMediaField(md.getMedia(), SdpMediaType.AUDIO, 1234, 1, integerFormats);
        assertPTime(md.getAttributes().getPTime(), null);
        assertTransmissionMode(md.getAttributes().getTransmissionMode(), null);
    }

    /**
     * Verify that when creating an unused media description copy with a
     * session description that is null, a NullPointerException is thrown.
     * @throws Exception    Exception is thrown if test case fails
     */
    public void testCreateUnusedMediaDescriptionCopyWhenSdNull() throws Exception {
        SdpMediaDescription md = SdpMediaDescriptionImpl.createMediaDescription(
                SdpMediaType.AUDIO,
                RTPPayload.get(RTPPayload.AUDIO_PCMU),
                RTPPayload.get(RTPPayload.AUDIO_DTMF),
                "host", 1234, 40, 40, new SdpSessionDescriptionImpl(), null, null, null);

        try {
            md.createUnusedMediaDescriptionCopy(null);
            fail("Expected a NullPointerException but it was not thrown.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Verify that when creating an unused media description copy, it has the
     * same values as the original but the media port is set to zero.
     * @throws Exception if test case fails
     */
    public void testCreateUnusedMediaDescriptionCopy() throws Exception {
        SdpMediaDescription md = SdpMediaDescriptionImpl.createMediaDescription(
                SdpMediaType.AUDIO,
                RTPPayload.get(RTPPayload.AUDIO_PCMU),
                RTPPayload.get(RTPPayload.AUDIO_DTMF),
                "host", 1234, 40, 40, new SdpSessionDescriptionImpl(), null, null, null);

        SdpMediaDescription mdCopy =
                md.createUnusedMediaDescriptionCopy(new SdpSessionDescriptionImpl());

        // Verify the Media field
        assertMediaField(
                mdCopy.getMedia(), SdpMediaType.AUDIO, 0, 0, AUDIO_AND_DTMF_FORMATS);

        // Verify the Connection field
        assertConnectionField(mdCopy.getConnection(), "host");

        // Verify the Attribute fields
        assertFmtp(mdCopy.getAttributes().getFmtps(), DTMF_FMTP);
        assertRtpMap(mdCopy.getAttributes().getRtpMaps(), AUDIO_RTPMAP);
        assertRtpMap(mdCopy.getAttributes().getRtpMaps(), DTMF_RTPMAP);
        assertPTime(mdCopy.getAttributes().getPTime(), 40);
        assertTransmissionMode(mdCopy.getAttributes().getTransmissionMode(), null);
    }

    /**
     * Verifies that a media description can be translated into an SDP stack
     * media description.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);
        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());
        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        // Encode to stack format
        MediaDescription encodedMd = sdpMd.encodeToStackFormat(sdpFactory);

        // Verify connection
        assertEquals("224.2.1.1", encodedMd.getConnection().getAddress());

        // Verify bandwidth
        assertEquals(64, encodedMd.getBandwidth("AS"));
        assertEquals(800, encodedMd.getBandwidth("RR"));
        assertEquals(2400, encodedMd.getBandwidth("RS"));
        assertEquals(3, encodedMd.getBandwidths(true).size());

        // Verify media
        assertEquals(3, encodedMd.getMedia().getMediaFormats(true).size());
        assertTrue(encodedMd.getMedia().getMediaFormats(true).contains("0"));
        assertTrue(encodedMd.getMedia().getMediaFormats(true).contains("8"));
        assertTrue(encodedMd.getMedia().getMediaFormats(true).contains("101"));
        assertEquals(17434, encodedMd.getMedia().getMediaPort());
        assertEquals("audio", encodedMd.getMedia().getMediaType());
        assertEquals(0, encodedMd.getMedia().getPortCount());
        assertEquals("RTP/AVP", encodedMd.getMedia().getProtocol());

        // Verify attributes
        Vector<Attribute> attributes = encodedMd.getAttributes(true);
        assertEquals(5, attributes.size());
        assertEquals("40", encodedMd.getAttribute("ptime"));
        assertEquals("101 0-16", encodedMd.getAttribute("fmtp"));
        Vector<String> rtpmaps = new Vector<String>();
        Vector<Attribute> sendrecv = new Vector<Attribute>();
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals("rtpmap"))
                rtpmaps.add(attribute.getValue());
            else if (attribute.getName().equals("sendrecv"))
                sendrecv.add(attribute);
        }
        assertEquals(1, sendrecv.size());
        assertEquals(2, rtpmaps.size());
        assertTrue(rtpmaps.contains("0 PCMU/8000"));
        assertTrue(rtpmaps.contains("101 telephone-event/8000"));
    }

    /**
     * Verifies that areEncodingSupported returns true if all encodings in the
     * list are supported.
     * @throws Exception if test case fails.
     */
    public void testAreEncodingsSupportedWhenSupported() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        encodings.add(RTPPayload.AUDIO_DTMF);
        encodings.add(new MimeType("audio/PCMA"));
        assertTrue(sdpMd.areEncodingsSupported(encodings));
    }

    /**
     * Verifies that areEncodingSupported returns false if at least one
     * encoding in the list is unsupported.
     * @throws Exception if test case fails.
     */
    public void testAreEncodingsSupportedWhenEncodingNotSupported()
            throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        encodings.add(RTPPayload.AUDIO_DTMF);
        encodings.add(RTPPayload.AUDIO_AMR);
        assertFalse(sdpMd.areEncodingsSupported(encodings));
    }

    /**
     * Verifies that areEncodingSupported returns false if the bandwidth for
     * at least one encoding in the list is unsupported.
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testAreEncodingsSupportedWhenBandwidthNotSupported()
            throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 128000, null));
        rtppayloads.add(new RTPPayload(101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        encodings.add(RTPPayload.AUDIO_DTMF);
        assertFalse(sdpMd.areEncodingsSupported(encodings));
    }

    /**
     * Verifies that en SDP offer with RR to low.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersNotSupportedRRtoLow()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 1600, 1200, 5000));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertFalse(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer with RS to low.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersNotSupportedRStoLow()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 4000, 3000, 5000, 800, 0, 5000));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertFalse(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer with RR to high.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersNotSupportedRRtoHigh()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 400, 0, 600));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertFalse(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer with RS to high.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersNotSupportedRStoHigh()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 1200, 600, 2000, 800, 0, 3000));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertFalse(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer is supported with normal values for RR and RS.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersSupported()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 800, 0, 3000));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertTrue(sdpMd.areEncodingsSupported(encodings));
    }

    /**
     * Verifies that en SDP offer is accepted with default values for RTPPayload.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersSupportedDefaultValues()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertTrue(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer without RR and RS is accepted with default values for RTPPayload.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersSupportedMissingValuesDefaultValues()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 800, 0, 5000));
        RTPPayload.updateDefs(rtppayloads);

        String SDP_2 =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "b=AS:64\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n" +
                    "a=sendrecv";

        SessionDescription sd = sdpFactory.createSessionDescription(SDP_2);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertTrue(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer with RR zero is accepted if RR is confgifured to allow 0 for RR.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersSupportedRRisZero()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 5000, 800, 0, 5000));
        RTPPayload.updateDefs(rtppayloads);

        String SDP_2 =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "b=AS:64\r\n" +
                    "b=RR:0\r\n" +
                    "b=RS:2400\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n" +
                    "a=sendrecv";

        SessionDescription sd = sdpFactory.createSessionDescription(SDP_2);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertTrue(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that en SDP offer with RR + RS bigger than AS is rejected.
     * @throws Exception
     */
    public void testRTCPBandwidthModifiersNotSupportedHighValues()
        throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null, 2400, 1000, 60000, 800, 0, 50000));
        RTPPayload.updateDefs(rtppayloads);

        String SDP_2 =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "b=AS:64\r\n" +
                    "b=RR:30000\r\n" +
                    "b=RS:50000\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n" +
                    "a=sendrecv";

        SessionDescription sd = sdpFactory.createSessionDescription(SDP_2);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        assertFalse(sdpMd.areEncodingsSupported(encodings));

    }

    /**
     * Verifies that areEncodingSupported returns false if the local bandwidth
     * requirements for at least one encoding in the list cannot be retrieved..
     * @throws Exception    Exception is thrown if test case fails.
     */
    public void testAreEncodingsSupportedWhenLocalBandwidthNotFound()
            throws Exception {

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        RTPPayload.updateDefs(rtppayloads);

        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        ArrayList<MimeType> encodings = new ArrayList<MimeType>();
        encodings.add(RTPPayload.AUDIO_PCMU);
        encodings.add(RTPPayload.AUDIO_DTMF);
        assertFalse(sdpMd.areEncodingsSupported(encodings));
    }

    /**
     * Verifies that isEncodingSupported returns true for an encoding supported
     * by the RTPMAP.
     * @throws Exception if test case fails.
     */
    public void testIsEncodingSupportedForDynamicallyMappedEncoding()
            throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        sdpMd.getSupportedRtpPayload(new MimeType("audio/pcmu"));
    }

    /**
     * Verifies that isEncodingSupported returns true for an encoding supported
     * by the static RTP mappings listed in the media formats in the SDP.
     * @throws Exception if test case fails.
     */
    public void testIsEncodingSupportedForStaticallyMappedEncoding()
            throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        sdpMd.getSupportedRtpPayload(new MimeType("audio/PCMA"));
    }

    /**
     * Verifies that isEncodingSupported returns false for an encoding that is
     * neither supported by the dynamic rtp maps nor by the static mapping.
     * @throws Exception if test case fails.
     */
    public void testIsEncodingSupportedForNonSupportedEncoding() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);

        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());

        assertFalse(!sdpMd.getSupportedRtpPayload(new MimeType("audio/AMR")).isEmpty());
    }


    public void testIsFormatSpecificParametersSupported() throws Exception {
        SessionDescription sd;
        Vector<MediaDescription> mdList;
        SdpMediaDescription amrMd;

        Collection<MimeType> supportedMimeTypes = new ArrayList<MimeType>();
        supportedMimeTypes.add(RTPPayload.AUDIO_AMR);


        // Correct SDP for AMR
        sd = sdpFactory.createSessionDescription(SDP_AMR1);
        mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());
        amrMd = SdpMediaDescriptionImpl.parseMediaDescription(
                    mdList.get(0), new SdpSessionDescriptionImpl());
	assertTrue(amrMd.areEncodingsSupported(supportedMimeTypes));
	assertEquals(2, amrMd.getMedia().getFormats().getFormats().size());
	assertTrue(amrMd.getMedia().getFormats().isFormatSupported(101));
	assertTrue(amrMd.getMedia().getFormats().isFormatSupported(96));
	assertEquals(20,amrMd.getAttributes().getPTime().getpTime());
	assertEquals(40,amrMd.getAttributes().getMaxPTime().getValue());
	assertEquals("mode-set=7;octet-align=1",
		amrMd.getAttributes().getFmtps().get(96).getParameters());

        // Correct SDP for AMR with alternate payload type 122
        sd = sdpFactory.createSessionDescription(SDP_AMR2);
        mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());
        amrMd = SdpMediaDescriptionImpl.parseMediaDescription(
                    mdList.get(0), new SdpSessionDescriptionImpl());
	assertTrue(amrMd.areEncodingsSupported(supportedMimeTypes));
	assertEquals(2, amrMd.getMedia().getFormats().getFormats().size());
	assertTrue(amrMd.getMedia().getFormats().isFormatSupported(101));
	assertTrue(amrMd.getMedia().getFormats().isFormatSupported(122));
	assertEquals(30,amrMd.getAttributes().getPTime().getpTime());
	assertEquals(60,amrMd.getAttributes().getMaxPTime().getValue());
	assertEquals("mode-set=7;octet-align=1;robust-sorting=0",
		amrMd.getAttributes().getFmtps().get(122).getParameters());


	// Unsupported format specific parameters, wrong mode-set
        sd = sdpFactory.createSessionDescription(SDP_AMR3);
        mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());
        amrMd = SdpMediaDescriptionImpl.parseMediaDescription(
                    mdList.get(0), new SdpSessionDescriptionImpl());
	assertFalse(amrMd.areEncodingsSupported(supportedMimeTypes));


	// Unsupported format specific parameters, wrong mode-set, alt payload type 97
        sd = sdpFactory.createSessionDescription(SDP_AMR5);
        mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());
        amrMd = SdpMediaDescriptionImpl.parseMediaDescription(
                    mdList.get(0), new SdpSessionDescriptionImpl());
	assertFalse(amrMd.areEncodingsSupported(supportedMimeTypes));

    }

    /**
     * Verifies that isSdpMediaDescriptionOnHold returns true when the
     * SDP media description connection-address is on hold (zeroed)
     * and false otherwise.
     * @throws Exception if test case fails.
     */
    public void testIsSdpMediaDescriptionOnHold() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(SDP);
        Vector<MediaDescription> mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        SdpMediaDescription sdpMd =
                SdpMediaDescriptionImpl.parseMediaDescription(
                        mdList.get(0),
                        new SdpSessionDescriptionImpl());
        assertFalse(sdpMd.isSdpMediaDescriptionOnHold());

        sd = sdpFactory.createSessionDescription(SDP_2);
        mdList = sd.getMediaDescriptions(true);
        assertEquals(1, mdList.size());

        sdpMd = SdpMediaDescriptionImpl.parseMediaDescription(
        		mdList.get(0),
                new SdpSessionDescriptionImpl());
        assertTrue(sdpMd.isSdpMediaDescriptionOnHold());
    }

}
