/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.Media;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.SdpFactory;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.util.Vector;

/**
 * SdpMedia Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaTest extends MockObjectTestCase
{
    private static final SdpParseException PARSE_EXCEPTION =
            new SdpParseException(0, 0, "Parse error");

    private final Mock mediaMock = mock(Media.class);

    private SdpFactory sdpFactory = null;

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that null is returned when parsing a media field that is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenMediaIsNull() throws Exception {
        SdpMedia sdpMedia = SdpMedia.parseMedia(null);
        assertNull(sdpMedia);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the media type cannot be parsed.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenParsingTypeThrowsException() throws Exception {
        mediaMock.expects(once()).method("getMediaType").
                will(throwException(PARSE_EXCEPTION));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the media port cannot be parsed.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenParsingPortThrowsException() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").
                will(throwException(PARSE_EXCEPTION));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the media port count cannot be
     * parsed.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenParsingPortCountThrowsException() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").
                will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").
                will(throwException(PARSE_EXCEPTION));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the media transport cannot be
     * parsed.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenParsingTransportThrowsException() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").will(returnValue(1));
        mediaMock.expects(once()).method("getProtocol").
                will(throwException(PARSE_EXCEPTION));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the media formats cannot be
     * parsed.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenParsingFormatsThrowsException() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").will(returnValue(1));
        mediaMock.expects(once()).method("getProtocol").will(returnValue("RTP/AVP"));
        mediaMock.expects(once()).method("getMediaFormats").
                will(throwException(PARSE_EXCEPTION));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * MEDIA_TYPE_NOT_AVAILABLE) is thrown if the media type is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenMediaTypeIsNull() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue(null));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.MEDIA_TYPE_NOT_AVAILABLE,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * PORT_COUNT_NOT_ALLOWED) is thrown if the media port count is too large.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenMediaPortCountIsTooLarge() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").will(returnValue(2));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.PORT_COUNT_NOT_ALLOWED,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_TRANSPORT_PROTOCOL) is thrown if the media transport is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenMediaTransportIsNull() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").will(returnValue(1));
        mediaMock.expects(once()).method("getProtocol").will(returnValue(null));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_TRANSPORT_PROTOCOL,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_MEDIA_FORMAT) is thrown if the media formats is null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaWhenMediaFormatsIsNull() throws Exception {
        mediaMock.expects(once()).method("getMediaType").will(returnValue("audio"));
        mediaMock.expects(once()).method("getMediaPort").will(returnValue(1234));
        mediaMock.expects(once()).method("getPortCount").will(returnValue(1));
        mediaMock.expects(once()).method("getProtocol").will(returnValue("RTP/AVP"));
        mediaMock.expects(once()).method("getMediaFormats").will(returnValue(null));

        try {
            SdpMedia.parseMedia((Media)mediaMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_MEDIA_FORMAT,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that a correct media field is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseMedia() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        formats.add("50");
        Media media =
                sdpFactory.createMedia("audio", 1234, 1, "RTP/AVP", formats);
        SdpMedia sdpMedia = SdpMedia.parseMedia(media);
        assertEquals(SdpMediaType.AUDIO, sdpMedia.getType());
        assertEquals(1234, sdpMedia.getPort());
        assertEquals(1, sdpMedia.getPortCount());
        assertEquals(SdpMediaTransport.RTP_AVP, sdpMedia.getTransport());
        Vector<Integer> integerFormats = new Vector<Integer>();
        integerFormats.add(5);
        integerFormats.add(50);
        assertEquals(integerFormats, sdpMedia.getFormats().getFormats());
    }
}
