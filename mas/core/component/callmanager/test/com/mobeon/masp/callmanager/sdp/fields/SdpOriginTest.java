/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.Origin;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * SdpOrigin Tester.
 *
 * @author Malin Flodin
 */
public class SdpOriginTest extends MockObjectTestCase
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
     * Verify that an SdpNotSupportedException (indicating
     * SD_PARAMETER_NOT_UNDERSTOOD) is thrown if an
     * {@link SdpParseException} was thrown while parsing the SDP.
     * @throws Exception if test case fails.
     */
    public void testParseOriginWhenSdpExceptionIsThrown() throws Exception {
        Mock originMock = mock(Origin.class);
        originMock.expects(once()).method("getUsername").
                will(throwException(new SdpParseException(0, 0, "SDP Error")));
        try {
            SdpOrigin.parseOrigin((Origin)originMock.proxy());
            fail("Exception not thrown when expected.");
        } catch (SdpNotSupportedException e) {
            assertEquals(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD,
                    e.getSipWarning());
        }
    }

    /**
     * Verify that a correct origin is parsed correctly.
     * @throws Exception if test case fails.
     */
    public void testParseOrigin() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("o=user 1 2 IN IP4 224.2.1.1");
        SdpOrigin origin = SdpOrigin.parseOrigin(sd.getOrigin());
        assertEquals("user", origin.getUserName());
        assertEquals(1, origin.getSessionId());
        assertEquals(2, origin.getSessionVersion());
        assertEquals("IN", origin.getNetworkType());
        assertEquals("IP4", origin.getAddressType());
        assertEquals("224.2.1.1", origin.getAddress());
    }

    /**
     * Verifies the toString method.
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("o=user 1 2 IN IP4 224.2.1.1");
        SdpOrigin origin = SdpOrigin.parseOrigin(sd.getOrigin());
        assertEquals("<User name = user>, <SessionId = 1>, " +
                "<Session Version = 2>, <Address = 224.2.1.1>",
                origin.toString());
    }

    /**
     * Verifies that the transmission mode can be translated into an SDP
     * attribute with null value.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SessionDescription sd =
                sdpFactory.createSessionDescription("o=user 1 2 IN IP4 224.2.1.1");
        SdpOrigin sdpOrigin = SdpOrigin.parseOrigin(sd.getOrigin());
        Origin origin = sdpOrigin.encodeToStackFormat(sdpFactory);
        assertEquals("224.2.1.1", origin.getAddress());
        assertEquals("IP4", origin.getAddressType());
        assertEquals("IN", origin.getNetworkType());
        assertEquals("user", origin.getUsername());
        assertEquals(1, origin.getSessionId());
        assertEquals(2, origin.getSessionVersion());
    }

}
