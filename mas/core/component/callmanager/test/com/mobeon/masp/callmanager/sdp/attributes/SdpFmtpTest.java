package com.mobeon.masp.callmanager.sdp.attributes;

import junit.framework.TestCase;

import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;

/**
 * SdpFmtp Tester.
 *
 * @author Malin Flodin
 */
public class SdpFmtpTest extends TestCase
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
     * Verifies that the toString method returns the fmtp attribute in the
     * following format:
     * <format> <format parameters>
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        SdpFmtp sdpFmtp = SdpFmtp.parseFmtpAttribute("12 2x f g");
        assertEquals("12 2x f g", sdpFmtp.toString());
    }

    /**
     * Verify that null is returned when parsing an fmtp attribute that is null.
     * @throws Exception if test case fails.
     */
    public void testParseFmtpAttributeWhenFmtpIsNull() throws Exception {
        SdpFmtp sdpFmtp = SdpFmtp.parseFmtpAttribute(null);
        assertNull(sdpFmtp);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the fmtp only consists of one part,
     * i.e. the format specific parameters are missing.
     * @throws Exception if test case fails.
     */
    public void testParseFmtpAttributeWhenNoParameters() throws Exception {
        try {
            SdpFmtp.parseFmtpAttribute("1");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }

        try {
            SdpFmtp.parseFmtpAttribute("1 ");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if the fmtp consists of a non-integer
     * format.
     * @throws Exception if test case fails.
     */
    public void testParseFmtpAttributeWhenFormatNotInteger() throws Exception {
        try {
            SdpFmtp.parseFmtpAttribute("x 123");
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that a correct fmtp is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseFmtpAttribute() throws Exception {
        SdpFmtp sdpFmtp = SdpFmtp.parseFmtpAttribute("1 x f g");
        assertEquals(1, sdpFmtp.getFormat());
        assertEquals("x f g", sdpFmtp.getParameters());

        sdpFmtp = SdpFmtp.parseFmtpAttribute("12 2x f g ");
        assertEquals(12, sdpFmtp.getFormat());
        assertEquals("2x f g ", sdpFmtp.getParameters());
    }

    /**
     * Verifies that an fmtp can be translated into an SDP attribute.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SdpFmtp sdpFmtp = SdpFmtp.parseFmtpAttribute("1 x f g");
        Attribute attr = sdpFmtp.encodeToStackFormat(sdpFactory);
        assertEquals(attr.getName(), "fmtp");
        assertEquals("1 x f g", attr.getValue());
    }

}
