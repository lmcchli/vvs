/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.BandWidth;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import java.util.Vector;
import java.util.HashMap;

/**
 * SdpBandwidth Tester.
 *
 * @author Malin Nyfeldt
 */
public class SdpBandwidthTest extends MockObjectTestCase {

    private SdpFactory sdpFactory = null;
    private Mock bandwidthMock;
    private Vector<BandWidth> bandwidthVector = new Vector<BandWidth>();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Initialize SDP factory
        SdpFactory.setPathName("gov.nist");
        sdpFactory = SdpFactory.getInstance();

        bandwidthMock = mock(BandWidth.class);
        bandwidthVector.add((BandWidth)bandwidthMock.proxy());
    }

    public void tearDown() throws Exception {
        super.tearDown();
        bandwidthVector.clear();
    }

    /**
     * Verify that an empty map is returned when parsing an SDP bandwidth
     * vector that is null.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthWhenBandwidthVectorIsNull() throws Exception {
        HashMap<String, SdpBandwidth> sdpBandwidths =
                SdpBandwidth.parseBandwidth(null);
        assertNotNull(sdpBandwidths);
        assertTrue(sdpBandwidths.isEmpty());
    }

    /**
     * Verify that an empty map is returned when parsing an SDP bandwidth
     * vector that is empty.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthWhenBandwidthVectorIsEmpty() throws Exception {
        HashMap<String, SdpBandwidth> sdpBandwidths =
                SdpBandwidth.parseBandwidth(new Vector<BandWidth>());
        assertNotNull(sdpBandwidths);
        assertTrue(sdpBandwidths.isEmpty());
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if the bandwidth type for the
     * bandwidth is null.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthForNullBandwidthType() throws Exception {
        bandwidthMock.expects(once()).method("getType").
                will(returnValue(null));
        try {
            SdpBandwidth.parseBandwidth(bandwidthVector);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if an
     * {@link SdpParseException} was thrown while parsing the bandwidth type.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthWhenParsingTypeGivesSdpException()
            throws Exception {
        bandwidthMock.expects(once()).method("getType").
                will(throwException(new SdpParseException(0, 0, "SDP Error")));
        try {
            SdpBandwidth.parseBandwidth(bandwidthVector);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if an
     * {@link SdpParseException} was thrown while parsing the bandwidth value.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthWhenParsingValueGivesSdpException()
            throws Exception {
        bandwidthMock.expects(once()).method("getType").
                will(returnValue("AS"));
        bandwidthMock.expects(once()).method("getValue").
                will(throwException(new SdpParseException(0, 0, "SDP Error")));
        try {
            SdpBandwidth.parseBandwidth(bandwidthVector);
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that a correct bandwidth is parsed correctly.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidth() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("b=AS:64000");
        Vector<BandWidth> bwVector = sd.getBandwidths(true);
        assertNotNull(bwVector);
        HashMap<String, SdpBandwidth> bandwidthMap =
                SdpBandwidth.parseBandwidth(bwVector);
        assertEquals(1, bandwidthMap.size());
        assertEquals(64000, bandwidthMap.get("AS").getValue());
    }

    /**
     * Verify that a correct bandwidth is parsed correctly.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testParseBandwidthMulti() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("b=AS:64000\r\n" +
                        "b=RR:1600\r\n" +
                        "b=RS:4800\r\n");
        Vector<BandWidth> bwVector = sd.getBandwidths(true);
        assertNotNull(bwVector);
        HashMap<String, SdpBandwidth> bandwidthMap =
                SdpBandwidth.parseBandwidth(bwVector);
        assertEquals(3, bandwidthMap.size());
        assertEquals(64000, bandwidthMap.get("AS").getValue());
        assertEquals(4800, bandwidthMap.get("RS").getValue());
        assertEquals(1600, bandwidthMap.get("RR").getValue());
    }

    /**
     * Verifies that the toString method returns the following string:
     * <type>:<value>
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testToString() throws Exception {
        SdpBandwidth sdpBandwidth = new SdpBandwidth(1234);
        assertEquals("AS:1234", sdpBandwidth.toString());
    }

    /**
     * Verifies that the bandwidth can be translated into an SDP stack
     * bandwidth field.
     * @throws Exception    An exception is thrown if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SdpBandwidth sdpBandwidth = new SdpBandwidth("CT", 9999);
        BandWidth bandwidth = sdpBandwidth.encodeToStackFormat(sdpFactory);
        assertEquals(bandwidth.getType(), "CT");
        assertEquals(bandwidth.getValue(), 9999);
    }
}
