/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import junit.framework.TestCase;

/**
 * SdpMediaType Tester.
 *
 * @author Malin Flodin
 */
public class SdpMediaTypeTest extends TestCase
{
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that
     * "audio" => {@link SdpMediaType.AUDIO}
     * "video" => {@link SdpMediaType.VIDEO}
     * other string => null
     * null => null
     * @throws Exception
     */
    public void testParseMediaType() throws Exception {
        assertEquals(SdpMediaType.AUDIO, SdpMediaType.parseMediaType("audio"));
        assertEquals(SdpMediaType.VIDEO, SdpMediaType.parseMediaType("video"));
        assertEquals(null, SdpMediaType.parseMediaType("other"));
    }

}
