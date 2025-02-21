/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * RemoteParty Tester.
 * <p>
 * This test case only covers the part of the RemoteParty class that is not
 * verified in the Call Manager "component" tests.
 *
 * @author Malin Flodin
 */

public class RemotePartyTest extends TestCase {
    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("cfg/log4j2.xml");
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that a remote party is always sip.
     * @throws Exception if test case fails.
     */
    public void testParseRemoteParty() throws Exception {
            RemoteParty aRemoteParty = RemoteParty.parseRemoteParty("testsipproxy", 8888);
            assertTrue(aRemoteParty.isSipProxy());
            assertEquals(aRemoteParty.getSipProxy().getHost(), "testsipproxy");
            assertEquals(aRemoteParty.getSipProxy().getPort(), 8888);
    }
}
