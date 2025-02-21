/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;

import jakarta.activation.MimeType;

/**
 * SdpRtpMap Tester.
 *
 * @author Malin Flodin
 */
public class SdpRtpMapTest extends TestCase
{
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
     * Verify that an IllegalArgumentException is thrown when creating an
     * rtpmap with null for encoding which is mandatory.
     * @throws Exception if test case fails.
     */
    public void testConstructorWithNullParameter() throws Exception {
        try {
            new SdpRtpMap(96, null, 8000, 1);
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verify that isEquivalent returns false if the given mime type is null.
     * @throws Exception if test case fails.
     */
    public void testIsEquivalentWhenMimeTypeIsNull() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000");
        assertFalse(sdpRtpMap.isEquivalent(null, 8000));
    }

    /**
     * Verify that isEquivalent returns false if the given mime type is NOT
     * handled by the rtpmap.
     * @throws Exception if test case fails.
     */
    public void testIsEquivalentWhenMimeTypeIsNotEquivalent() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("8 PCMA/8000");
        assertFalse(sdpRtpMap.isEquivalent(new MimeType("audio/pcmu"), 8000));
    }

    /**
     * Verify that isEquivalent returns true if the given mime type is handled
     * by the rtpmap.
     * @throws Exception if test case fails.
     */
    public void testIsEquivalentWhenMimeTypeIsEquivalent() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("8 PCMU/8000");
        assertTrue(sdpRtpMap.isEquivalent(new MimeType("audio/pcmu"), 8000));
    }

    /**
     * Verifies that the toString method returns the rtpmap attribute in the
     * following format:
     * <payload type> <encoding name>/<clock rate>[/<encoding parameters>]
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000");
        assertEquals("96 L8/8000", sdpRtpMap.toString());

        sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000/2");
        assertEquals("96 L8/8000/2", sdpRtpMap.toString());
    }

    /**
     * Verify that null is returned when parsing an rtpmap attribute that is null.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenRtpMapIsNull() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute(null);
        assertNull(sdpRtpMap);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the rtpmap only consists of one
     * part, i.e. the encoding details are missing.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenNoEncodingDetails() throws Exception {
        try {
            SdpRtpMap.parseRtpMapAttribute("1");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }

        try {
            SdpRtpMap.parseRtpMapAttribute("1 ");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the rtpmap consists of a
     * non-integer payload type.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenPayloadTypeNotInteger()
            throws Exception {
        try {
            SdpRtpMap.parseRtpMapAttribute("x pcmu/123/12");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the rtpmap lacks some mandatory
     * encoding details.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenLackingSomeEncodingDetails()
            throws Exception {
        try {
            SdpRtpMap.parseRtpMapAttribute("96 L8");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }

        try {
            SdpRtpMap.parseRtpMapAttribute("96 L8/");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the rtpmap consists of a
     * non-integer clock rate.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenClockRateNotInteger()
            throws Exception {
        try {
            SdpRtpMap.parseRtpMapAttribute("12 L8/xxxx/2");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the rtpmap consists of a
     * non-integer amount of channels.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttributeWhenChannelsNotInteger()
            throws Exception {
        try {
            SdpRtpMap.parseRtpMapAttribute("12 L8/8000/x");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that a correct rtpmap is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseRtpMapAttribute() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000");
        assertEquals(96, sdpRtpMap.getPayloadType());
        assertEquals("L8", sdpRtpMap.getEncodingName());
        assertEquals(8000, sdpRtpMap.getClockRate());
        assertEquals(null, sdpRtpMap.getChannels());

        sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000/2");
        assertEquals(96, sdpRtpMap.getPayloadType());
        assertEquals("L8", sdpRtpMap.getEncodingName());
        assertEquals(8000, sdpRtpMap.getClockRate());
        assertEquals(2, sdpRtpMap.getChannels().intValue());
    }

    /**
     * Verifies that an rtpmap can be translated into an SDP attribute.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SdpRtpMap sdpRtpMap = SdpRtpMap.parseRtpMapAttribute("96 L8/8000");
        Attribute attr = sdpRtpMap.encodeToStackFormat(sdpFactory);
        assertEquals(attr.getName(), "rtpmap");
        assertEquals("96 L8/8000", attr.getValue());
    }

}
