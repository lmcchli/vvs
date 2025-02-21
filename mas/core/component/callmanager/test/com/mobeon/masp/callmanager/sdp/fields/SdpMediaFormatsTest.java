/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import java.util.Vector;

/**
 * SdpMediaFormats Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaFormatsTest extends TestCase
{
    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that true is returned if format is supported and false is
     * returned otherwise.
     * @throws Exception if test case fails.
     */
    public void testIsFormatSupported() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        formats.add("50");
        SdpMediaFormats sdpMediaFormats =
                SdpMediaFormats.parseMediaFormats(formats);
        assertFalse(sdpMediaFormats.isFormatSupported(4));
        assertTrue(sdpMediaFormats.isFormatSupported(5));
    }

    /**
     * Verify that the integer format vector is printed in the toString method.
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        formats.add("50");
        SdpMediaFormats sdpMediaFormats =
                SdpMediaFormats.parseMediaFormats(formats);
        assertEquals("[5, 50]", sdpMediaFormats.toString());
    }
    /**
     * Verify that null is returned when parsing a media format vector that is
     * null.
     * @throws Exception if test case fails.
     */
    public void testParseMediaFormatsWhenMediaFormatsIsNull() throws Exception {
        SdpMediaFormats formats = SdpMediaFormats.parseMediaFormats(null);
        assertNull(formats);
    }

    /**
     * Verify that null is returned when parsing a media format vector that is
     * empty.
     * @throws Exception if test case fails.
     */
    public void testParseMediaFormatsWhenMediaFormatsIsEmpty() throws Exception {
        SdpMediaFormats formats =
                SdpMediaFormats.parseMediaFormats(new Vector<String>());
        assertNull(formats);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * INCOMPATIBLE_MEDIA_FORMAT) is thrown if the media formats vector
     * contains a non-integer format.
     * @throws Exception if test case fails.
     */
    public void testParseMediaFormatsForNonIntegerFormat() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        formats.add("50");
        formats.add("x");

        try {
            SdpMediaFormats.parseMediaFormats(formats);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.INCOMPATIBLE_MEDIA_FORMAT, e.getSipWarning());
        }
    }

    /**
     * Verify that a correct media format vector is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseMediaFormats() throws Exception {
        Vector<String> formats = new Vector<String>();
        formats.add("5");
        formats.add("50");
        SdpMediaFormats sdpMediaFormats =
                SdpMediaFormats.parseMediaFormats(formats);
        Vector<Integer> integerFormats = new Vector<Integer>();
        integerFormats.add(5);
        integerFormats.add(50);
        assertEquals(integerFormats, sdpMediaFormats.getFormats());
    }
}
