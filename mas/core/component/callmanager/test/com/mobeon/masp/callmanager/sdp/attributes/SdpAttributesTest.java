/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.SdpFactory;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.util.Vector;

/**
 * SdpAttributes Tester.
 *
 * @author Malin Flodin
 */
public class SdpAttributesTest extends MockObjectTestCase
{
    private SdpFactory sdpFactory = null;

    private final Mock attributeMock = mock(Attribute.class);
    private final Vector<Attribute> mockedAttributes = new Vector<Attribute>();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();

        // Initialize mocked attributes
        mockedAttributes.add((Attribute)attributeMock.proxy());
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that an empty SdpAttributes is returned when parsing an attributes
     * vector that is null.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenVectorIsNull() throws Exception {
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(null);
        assertEmpty(sdpAttributes);
    }

    /**
     * Verify that an empty SdpAttributes is returned when parsing an attributes
     * vector of one object that causes an exception to be thrown when parsing
     * the name of the attribute.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenParsingNameException() throws Exception {
        attributeMock.expects(once()).method("getName").will(throwException(
                new SdpParseException(0, 0, "Parse error")));
        SdpAttributes sdpAttributes =
                SdpAttributes.parseAttributes(mockedAttributes);
        assertEmpty(sdpAttributes);
    }

    /**
     * Verify that an empty SdpAttributes is returned when parsing an attributes
     * vector of one object that causes an exception to be thrown when parsing
     * the value of the attribute.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenParsingValueException() throws Exception {
        attributeMock.expects(once()).method("getName").will(returnValue("name"));
        attributeMock.expects(once()).method("getValue").will(throwException(
                new SdpParseException(0, 0, "Parse error")));
        SdpAttributes sdpAttributes =
                SdpAttributes.parseAttributes(mockedAttributes);
        assertEmpty(sdpAttributes);
    }

    /**
     * Verify that an empty SdpAttributes is returned when parsing an attributes
     * vector of one object for which the name of the attribute is null.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenAttributeNameIsNull() throws Exception {
        attributeMock.expects(once()).method("getName").will(returnValue(null));
        attributeMock.expects(once()).method("getValue").will(returnValue(null));
        SdpAttributes sdpAttributes =
                SdpAttributes.parseAttributes(mockedAttributes);
        assertEmpty(sdpAttributes);
    }

    /**
     * Verify that an empty SdpAttributes is returned when parsing an unsupported
     * attribute.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenAttributeIsUnsupported() throws Exception {
        attributeMock.expects(once()).method("getName").will(returnValue("unsupported"));
        attributeMock.expects(once()).method("getValue").will(returnValue(null));
        SdpAttributes sdpAttributes =
                SdpAttributes.parseAttributes(mockedAttributes);
        assertEmpty(sdpAttributes);
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if parsing attributes and one
     * attribute is an invalid rtpmap.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenRtpmapThrowsException() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("rtpmap", "x"));

        try {
            SdpAttributes.parseAttributes(attributes);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if parsing attributes and one
     * attribute is an invalid ptime.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenPTimeThrowsException() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("ptime", "x"));

        try {
            SdpAttributes.parseAttributes(attributes);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * ATTRIBUTE_NOT_UNDERSTOOD) is thrown if parsing attributes and one
     * attribute is an invalid fmtp.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWhenFmtpThrowsException() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("fmtp", "x"));

        try {
            SdpAttributes.parseAttributes(attributes);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, e.getSipWarning());
        }
    }

    /**
     * Verify that an rtpmap attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectRtmap() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("rtpmap", "96 L8/8000"));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        SdpRtpMap rtpMap = sdpAttributes.getRtpMaps().get(96);
        assertEquals(96, rtpMap.getPayloadType());
        assertEquals("L8", rtpMap.getEncodingName());
        assertEquals(8000, rtpMap.getClockRate());
        assertNull(rtpMap.getChannels());
    }

    /**
     * Verify that an fmtp attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectFmtp() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("fmtp", "101 0-16"));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        SdpFmtp fmtp = sdpAttributes.getFmtps().get(101);
        assertEquals(101, fmtp.getFormat());
        assertEquals("0-16", fmtp.getParameters());
    }

    /**
     * Verify that a charset attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectCharset() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("charset", "utf-8"));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals("utf-8", sdpAttributes.getCharset());
    }

    /**
     * Verify that a ptime attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectPTime() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("ptime", "40"));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals(40, sdpAttributes.getPTime().getpTime());
    }

    /**
     * Verify that a sendrecv attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectSendRecv() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("sendrecv", null));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals(SdpTransmissionMode.SENDRECV,
                sdpAttributes.getTransmissionMode());
    }

    /**
     * Verify that a sendonly attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectSendOnly() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("sendonly", null));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals(SdpTransmissionMode.SENDONLY,
                sdpAttributes.getTransmissionMode());
    }

    /**
     * Verify that a recvonly attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectRecvOnly() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("recvonly", null));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals(SdpTransmissionMode.RECVONLY,
                sdpAttributes.getTransmissionMode());
    }

    /**
     * Verify that an inactive attribute can be parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseAttributesWithCorrectInactive() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("inactive", null));
        SdpAttributes sdpAttributes = SdpAttributes.parseAttributes(attributes);
        assertEquals(SdpTransmissionMode.INACTIVE,
                sdpAttributes.getTransmissionMode());
    }

    /**
     * Verify that a merge between default attributes and no override attributes
     * results in all default attributes being copied to the merged result.
     * @throws Exception if test case fails.
     */
    public void testMergeAttributesWithNoOverrides() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();

        SdpAttributes overrideAttributes = SdpAttributes.parseAttributes(attributes);

        attributes.add(sdpFactory.createAttribute("sendrecv", null));
        attributes.add(sdpFactory.createAttribute("ptime", "40"));
        attributes.add(sdpFactory.createAttribute("fmtp", "101 0-16"));
        attributes.add(sdpFactory.createAttribute("rtpmap", "96 L8/8000"));

        SdpAttributes defaultAttributes = SdpAttributes.parseAttributes(attributes);

        SdpAttributes merged =
                SdpAttributes.mergeAttributes(defaultAttributes, overrideAttributes);

        assertEquals(SdpTransmissionMode.SENDRECV, merged.getTransmissionMode());
        assertEquals(40, merged.getPTime().getpTime());
        assertFalse(merged.getFmtps().isEmpty());
        assertFalse(merged.getRtpMaps().isEmpty());
    }

    /**
     * Verify that a merge between no default attributes and override attributes
     * results in all override attributes being copied to the merged result.
     * @throws Exception if test case fails.
     */
    public void testMergeAttributesWithNoDefaults() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();

        SdpAttributes defaultAttributes = SdpAttributes.parseAttributes(attributes);

        attributes.add(sdpFactory.createAttribute("sendrecv", null));
        attributes.add(sdpFactory.createAttribute("ptime", "40"));
        attributes.add(sdpFactory.createAttribute("fmtp", "101 0-16"));
        attributes.add(sdpFactory.createAttribute("rtpmap", "96 L8/8000"));

        SdpAttributes overrideAttributes = SdpAttributes.parseAttributes(attributes);

        SdpAttributes merged =
                SdpAttributes.mergeAttributes(defaultAttributes, overrideAttributes);

        assertEquals(SdpTransmissionMode.SENDRECV, merged.getTransmissionMode());
        assertEquals(40, merged.getPTime().getpTime());
        assertFalse(merged.getFmtps().isEmpty());
        assertFalse(merged.getRtpMaps().isEmpty());
    }

    /**
     * Verify that a merge between default rtpmaps and override rtpmaps
     * results in the override map overwriting the default map in the merged result.
     * @throws Exception if test case fails.
     */
    public void testMergeAttributesWithMultipleRtpMaps() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("rtpmap", "8 PCMU/8000"));
        attributes.add(sdpFactory.createAttribute("rtpmap", "96 L8/8000"));
        SdpAttributes defaultAttributes = SdpAttributes.parseAttributes(attributes);

        attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("rtpmap", "96 L8/7000"));
        SdpAttributes overrideAttributes = SdpAttributes.parseAttributes(attributes);

        SdpAttributes merged =
                SdpAttributes.mergeAttributes(defaultAttributes, overrideAttributes);

        assertEquals(2, merged.getRtpMaps().size());
        assertEquals(8000, merged.getRtpMaps().get(8).getClockRate());
        assertEquals(7000, merged.getRtpMaps().get(96).getClockRate());
    }

    /**
     * Verify that a merge between default fmtp's and override fmtp's
     * results in the override map overwriting the default fmtp's in the
     * merged result.
     * @throws Exception if test case fails.
     */
    public void testMergeAttributesWithMultipleFmtps() throws Exception {
        Vector<Attribute> attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("fmtp", "100 0-16"));
        attributes.add(sdpFactory.createAttribute("fmtp", "101 0-16"));
        SdpAttributes defaultAttributes = SdpAttributes.parseAttributes(attributes);

        attributes = new Vector<Attribute>();
        attributes.add(sdpFactory.createAttribute("fmtp", "101 0-15"));
        attributes.add(sdpFactory.createAttribute("fmtp", "102 0-16"));
        SdpAttributes overrideAttributes = SdpAttributes.parseAttributes(attributes);

        SdpAttributes merged =
                SdpAttributes.mergeAttributes(defaultAttributes, overrideAttributes);

        assertEquals(3, merged.getFmtps().size());
        assertEquals("0-16", merged.getFmtps().get(100).getParameters());
        assertEquals("0-15", merged.getFmtps().get(101).getParameters());
        assertEquals("0-16", merged.getFmtps().get(102).getParameters());
    }

    /**
     * Verifies that a charset can be translated into an SDP attribute.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormatWhenCharset() throws Exception {
        SdpAttributes sdpAttributes = new SdpAttributes();
        sdpAttributes.setCharset("utf-8");
        Vector<Attribute> attrList = sdpAttributes.encodeToStackFormat(sdpFactory);
        assertEquals(1, attrList.size());
        assertEquals("utf-8", attrList.get(0).getValue());
    }


    // ========================= Private methods =============================

    /**
     * Verifies that the given <param>sdpAttributes</param> is empty.
     * @param sdpAttributes
     */
    private void assertEmpty(SdpAttributes sdpAttributes) {
        assertNull(sdpAttributes.getPTime());
        assertNull(sdpAttributes.getTransmissionMode());
        assertTrue(sdpAttributes.getFmtps().isEmpty());
        assertTrue(sdpAttributes.getRtpMaps().isEmpty());
    }

}
