/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.Attribute;

/**
 * SdpTransmissionMode Tester.
 *
 * @author Malin Flodin
 */
public class SdpTransmissionModeTest extends MockObjectTestCase
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
     * Verifies that the transmission mode can be translated into an SDP
     * attribute with null value.
     * @throws Exception if test case fails.
     */
    public void testEncodeToStackFormat() throws Exception {
        SdpTransmissionMode mode = SdpTransmissionMode.SENDRECV;
        Attribute attr = mode.encodeToStackFormat(sdpFactory);
        assertEquals(attr.getName(), "sendrecv");
        assertNull("Attribute value should be null but is: " + attr.getValue(),
                attr.getValue());
    }

}
