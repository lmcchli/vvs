/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import junit.framework.TestCase;

/**
 * SdpMediaTransport Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaTransportTest extends TestCase
{
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that
     * "RTP/AVP" => {@link SdpMediaTransport.RTP_AVP}
     * other string => null
     * @throws Exception
     */
    public void testParseMediaTranport() throws Exception {
        assertEquals(SdpMediaTransport.RTP_AVP, 
                SdpMediaTransport.parseMediaTranport("RTP/AVP"));
        assertEquals(null, SdpMediaTransport.parseMediaTranport("other"));
    }
}
