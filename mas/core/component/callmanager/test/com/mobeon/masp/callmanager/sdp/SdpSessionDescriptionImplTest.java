/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.MediaDescription;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * SdpSessionDescriptionImpl Tester.
 *
 * @author Malin Flodin
 */
public class SdpSessionDescriptionImplTest extends MockObjectTestCase
{
    private static final String VERSION =
            "v=0\r\n";

    private static final String INVALID_VERSION =
            "v=1\r\n";

    private static final String ORIGIN =
            "o=user 1 2 IN IP4 224.2.1.1\r\n";

    private static final String SESSION =
            "s=mas\r\n";

    private static final String CONNECTION =
            "c=IN IP4 224.2.1.1\r\n";

    private static final String CONNECTION_2 =
        "c=IN IP4 0.0.0.0\r\n";

    private static final String BANDWIDTH =
            "b=AS:128\r\n";

    private static final String TIME =
            "t=0 0\r\n";

    private static final String KEY =
            "k=prompt\r\n";

    private static final String CHARSET =
            "a=charset:UTF-8\r\n";

    private static final String SESSION_ATTRIBUTES =
            "a=sendrecv\r\n";

    private static final String AUDIO_MD =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n";

    private static final String AUDIO_MD_2 =
        "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                "c=IN IP4 0.0.0.0\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:40\r\n";
    
    private static final String AUDIO_MD_3 =
        "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=fmtp:101 0-16\r\n" +
                "a=ptime:40\r\n";

    private static final String AUDIO_MD_WITH_KEY =
            "m=audio 17434 RTP/AVP 0 8 101\r\n" +
                    "c=IN IP4 224.2.1.1\r\n" +
                    "k=prompt\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=fmtp:101 0-16\r\n" +
                    "a=ptime:40\r\n";

    private static final String VIDEO_MD =
            "m=video 17438 RTP/AVP 34\r\n" +
                    "a=rtpmap:34 H263/8000\r\n";

    private static final String EXPECTED_SDP = "v=0" +
            "o=user 1 2 IN IP4 224.2.1.1" +
            "s=-" +
            "c=IN IP4 224.2.1.1" +
            "b=AS:128" +
            "t=0 0" +
            "a=sendrecv" +
            "m=audio 17434 RTP/AVP 0 8 101" +
            "c=IN IP4 224.2.1.1" +
            "a=ptime:40" +
            "a=rtpmap:101 telephone-event/8000" +
            "a=fmtp:101 0-16" +
            "m=video 17438 RTP/AVP 34" +
            "a=rtpmap:34 H263/8000";

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private SdpFactory sdpFactory = null;

    public void setUp() throws Exception {
        super.setUp();

        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(8, new MimeType("audio/PCMA"), "PCMA", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that containsVideo returns true when session description contains
     * a media description of type video.
     * @throws Exception if test case fails.
     */
    public void testContainsVideoWhenThereIsVideo() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD + VIDEO_MD);

        SdpSessionDescription parsedSd =
                SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertTrue(parsedSd.containsVideo());
    }

    /**
     * Verify that containsVideo returns false when session description contains
     * no media description of type video.
     * @throws Exception if test case fails.
     */
    public void testContainsVideoWhenThereIsNoVideo() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD);

        SdpSessionDescription parsedSd =
                SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertFalse(parsedSd.containsVideo());
    }

    /**
     * Verifies that when encoding a Session Description into an SDP string,
     * the output string is as expected.
     * @throws Exception if test case fails.
     */
    public void testEncodeToSdp() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD + VIDEO_MD);

        SdpSessionDescription parsedSd =
                SdpSessionDescriptionImpl.parseSessionDescription(sd);

        String sdp = parsedSd.encodeToSdp(sdpFactory);
        sdp = sdp.replaceAll("\r", "");
        sdp = sdp.replaceAll("\n", "");
        assertEquals(EXPECTED_SDP, sdp);
    }

    /**
     * Verify that null is returned when parsing a session description that is
     * null.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWhenSessionDescriptionIsNull()
            throws Exception {
        SdpSessionDescription sdpSD =
                SdpSessionDescriptionImpl.parseSessionDescription(null);
        assertNull(sdpSD);
    }

    /**
     * Verify that an SdpNotSupportedException is thrown if a parse exception
     * occurs while retrieving the list of media descriptions.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWhenRetrievingMediaDescriptionGivesException()
            throws Exception {
        Mock sdMock = mock(SessionDescription.class);
        sdMock.expects(once()).method("getConnection").will(returnValue(null));
        sdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        sdMock.expects(once()).method("getOrigin").will(returnValue(null));
        sdMock.expects(once()).method("getKey").will(returnValue(null));
        sdMock.expects(once()).method("getAttributes").will(returnValue(null));
        sdMock.expects(once()).method("getVersion").will(returnValue(null));
        sdMock.expects(once()).method("getMediaDescriptions").
                will(throwException(new SdpParseException(0, 0, "Error")));
        try {
            SdpSessionDescriptionImpl.parseSessionDescription(
                    (SessionDescription)sdMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, e.getSipWarning());
            assertEquals("Could not retrieve Media Description fields " +
                    "from remote SDP. The call will not be setup.",
                    e.getMessage());

        }
    }

    /**
     * Verify that an empty session description is returned when parsing an SDP
     * for which null is returned when retrieving the list of media descriptions.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWhenMediaDescriptionListIsNull()
            throws Exception {
        Mock sdMock = mock(SessionDescription.class);
        sdMock.expects(once()).method("getConnection").will(returnValue(null));
        sdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        sdMock.expects(once()).method("getOrigin").will(returnValue(null));
        sdMock.expects(once()).method("getKey").will(returnValue(null));
        sdMock.expects(once()).method("getAttributes").will(returnValue(null));
        sdMock.expects(once()).method("getVersion").will(returnValue(null));
        sdMock.expects(once()).method("getMediaDescriptions").will(returnValue(null));
        SdpSessionDescription sd =
                SdpSessionDescriptionImpl.parseSessionDescription(
                    (SessionDescription)sdMock.proxy());
        assertTrue(sd.getMediaDescriptions().isEmpty());
    }

    /**
     * Verify that an SdpNotSupportedException is thrown if a parsing a media
     * description results in a null element.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWhenParsedMediaDescriptionIsNull()
            throws Exception {
        Mock sdMock = mock(SessionDescription.class);
        sdMock.expects(once()).method("getConnection").will(returnValue(null));
        sdMock.expects(once()).method("getBandwidths").will(returnValue(null));
        sdMock.expects(once()).method("getOrigin").will(returnValue(null));
        sdMock.expects(once()).method("getKey").will(returnValue(null));
        sdMock.expects(once()).method("getAttributes").will(returnValue(null));
        sdMock.expects(once()).method("getVersion").will(returnValue(null));
        Vector<MediaDescription> md = new Vector<MediaDescription>();
        md.add(null);
        sdMock.expects(once()).method("getMediaDescriptions").will(returnValue(md));
        try {
            SdpSessionDescriptionImpl.parseSessionDescription(
                    (SessionDescription)sdMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, e.getSipWarning());
            assertEquals("Could not retrieve Media Description " +
                    "from remote SDP. The call will not be setup.",
                    e.getMessage());

        }
    }

    /**
     * Verify that an SdpNotSupportedException is thrown if the SDP contains
     * encryption keys.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWithEncryptionKeys()
            throws Exception {

        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + KEY + SESSION_ATTRIBUTES + AUDIO_MD + VIDEO_MD);

        try {
            SdpSessionDescriptionImpl.parseSessionDescription(sd);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ENCRYPTION_NOT_SUPPORTED, e.getSipWarning());
        }
    }

    public void testParseMediaDescriptionWithEncryptionKeys()
            throws Exception {

        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + CHARSET + SESSION_ATTRIBUTES +
                        AUDIO_MD_WITH_KEY + VIDEO_MD);

        try {
            SdpSessionDescriptionImpl.parseSessionDescription(sd);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ENCRYPTION_NOT_SUPPORTED, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException is thrown if the SDP contains
     * unsupported charset.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWithNonSupportedCharset()
            throws Exception {

        String attributes = SESSION_ATTRIBUTES + "a=charset:ISO-8859-1\r\n";

        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + attributes + AUDIO_MD + VIDEO_MD);

        try {
            SdpSessionDescriptionImpl.parseSessionDescription(sd);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.CHARSET_NOT_SUPPORTED, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException is thrown if the SDP has
     * an unsupported version.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescriptionWithNonSupportedVersion()
            throws Exception {

        SessionDescription sd = sdpFactory.createSessionDescription(
                INVALID_VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + CHARSET + SESSION_ATTRIBUTES + AUDIO_MD + VIDEO_MD);

        try {
            SdpSessionDescriptionImpl.parseSessionDescription(sd);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.VERSION_NOT_SUPPORTED, e.getSipWarning());
        }
    }

    /**
     * Verify that a received session description can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseSessionDescription() throws Exception {
        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + CHARSET + SESSION_ATTRIBUTES + AUDIO_MD + VIDEO_MD);

        SdpSessionDescription parsedSd =
                SdpSessionDescriptionImpl.parseSessionDescription(sd);

        // Verify origin
        assertEquals("user", parsedSd.getOrigin().getUserName());
        assertEquals("224.2.1.1", parsedSd.getOrigin().getAddress());

        // Verify connection
        assertEquals("224.2.1.1", parsedSd.getConnection().getAddress());

        // Verify bandwidth
        assertEquals(128, parsedSd.getBandwidth("AS").getValue());

        // Verify attributes
        assertEquals(SdpTransmissionMode.SENDRECV,
                parsedSd.getAttributes().getTransmissionMode());
        assertTrue(parsedSd.getAttributes().getRtpMaps().isEmpty());
        assertTrue(parsedSd.getAttributes().getFmtps().isEmpty());
        assertNull(parsedSd.getAttributes().getPTime());

        // Verify media descriptions
        SdpMediaDescription audioMd = parsedSd.getMediaDescription(0);
        SdpMediaDescription videoMd = parsedSd.getMediaDescription(1);
        assertEquals(SdpMediaType.AUDIO, audioMd.getMedia().getType());
        assertEquals(SdpMediaType.VIDEO, videoMd.getMedia().getType());
        assertEquals(2, parsedSd.getMediaDescriptions().size());
    }

    /**
     * Verifies that isSdpMediaDescriptionOnHold returns true when the
     * SDP media description(s) connection-address(es) is(are) on hold (zeroed)
     * and false otherwise.
     * @throws Exception if test case fails.
     */
    public void testIsSdpMediaDescriptionOnHold() throws Exception {
    	// SdpMediaDescription contains connection with a zerod address
        SessionDescription sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD_2);
        SdpSessionDescription parsedSd = SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertTrue(parsedSd.isSdpMediaDescriptionOnHold());
        
    	// SdpMediaDescription does not contain connection, 
        // SdpSession (which contain a non-zero address) is retrieved
        sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD_3);
        parsedSd = SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertFalse(parsedSd.isSdpMediaDescriptionOnHold());
        
    	// SdpMediaDescription does not contain connection, 
        // SdpSession (which contain an on hold address (zeroed)) is retrieved
        sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION_2 + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD_3);
        parsedSd = SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertTrue(parsedSd.isSdpMediaDescriptionOnHold());

        // One SdpMediaDescription contain a connection with zerod address, 
        // Another SdpMediaDescription does not contain connection, 
        // SdpSession (which contain an on hold address (zeroed)) is retrieved
        sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION_2 + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD_2 + AUDIO_MD_3);
        parsedSd = SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertTrue(parsedSd.isSdpMediaDescriptionOnHold());

        // One SdpMediaDescription contain a connection with zerod address, 
        // Another SdpMediaDescription does not contain connection, 
        // SdpSession (which contain a non-zero address) is retrieved
        sd = sdpFactory.createSessionDescription(
                VERSION + ORIGIN + SESSION + CONNECTION + BANDWIDTH +
                        TIME + SESSION_ATTRIBUTES + AUDIO_MD_2 + AUDIO_MD_3);
        parsedSd = SdpSessionDescriptionImpl.parseSessionDescription(sd);
        assertFalse(parsedSd.isSdpMediaDescriptionOnHold());
    }

}
