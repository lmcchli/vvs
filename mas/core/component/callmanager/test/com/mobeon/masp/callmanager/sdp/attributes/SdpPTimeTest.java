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

/**
 * SdpPTime Tester.
 *
 * @author Malin Flodin
 */
public class SdpPTimeTest extends TestCase
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
     * Verifies that the toString method returns the ptime attribute in the
     * following format:
     * <ptime>
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        SdpPTime sdpPTime = SdpPTime.parsePTimeAttribute("12");
        assertEquals("12", sdpPTime.toString());
    }

    /**
     * Verify that null is returned when parsing a ptime attribute that is null.
     * @throws Exception if test case fails.
     */
    public void testParsePTimeAttributeWhenPTimeIsNull() throws Exception {
        SdpPTime sdpPTime = SdpPTime.parsePTimeAttribute(null);
        assertNull(sdpPTime);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the ptime consists of a non-integer.
     * @throws Exception if test case fails.
     */
    public void testParsePTimeAttributeWhenPTimeNotInteger() throws Exception {
        try {
            SdpPTime.parsePTimeAttribute("x");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }

        try {
            SdpPTime.parsePTimeAttribute(" ");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that a correct ptime is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParsePTimeAttribute() throws Exception {
        SdpPTime sdpPTime = SdpPTime.parsePTimeAttribute("1");
        assertEquals(1, sdpPTime.getpTime());

        sdpPTime = SdpPTime.parsePTimeAttribute("12");
        assertEquals(12, sdpPTime.getpTime());
    }

    /**
     * Verifies that a ptime can be translated into an SDP attribute.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SdpPTime sdpPTime = SdpPTime.parsePTimeAttribute("1");
        Attribute attr = sdpPTime.encodeToStackFormat(sdpFactory);
        assertEquals(attr.getName(), "ptime");
        assertEquals("1", attr.getValue());
    }

}
