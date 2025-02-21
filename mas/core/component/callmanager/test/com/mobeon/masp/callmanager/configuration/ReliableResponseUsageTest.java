/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.*;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;

/**
 * ReliableResponseUsage Tester.
 *
 * @author Malin Nyfeldt
 */
public class ReliableResponseUsageTest extends TestCase {

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    /**
     * Verifies that parsing a string and creating a {@link ReliableResponseUsage}
     * works as expected.
     * <ul>
     * <li>null => {@link ReliableResponseUsage.SDPONLY}</li>
     * <li>"yes" => {@link ReliableResponseUsage.YES}</li>
     * <li>"no" => {@link ReliableResponseUsage.NO}</li>
     * <li><all other values => {@link ReliableResponseUsage.SDPONLY}/li>
     * </ul>
     * @throws Exception
     */
    public void testParseReliableResponseUsage() throws Exception {
        // Verify for null
        assertEquals(ReliableResponseUsage.SDPONLY,
                ReliableResponseUsage.parseReliableResponseUsage(null));

        // Verify for "sdponly"
        assertEquals(ReliableResponseUsage.SDPONLY,
                ReliableResponseUsage.parseReliableResponseUsage("sdponly"));

        // Verify for "yes"
        assertEquals(ReliableResponseUsage.YES,
                ReliableResponseUsage.parseReliableResponseUsage("yes"));

        // Verify for "no"
        assertEquals(ReliableResponseUsage.NO,
                ReliableResponseUsage.parseReliableResponseUsage("no"));

        // Verify for other string 
        assertEquals(ReliableResponseUsage.SDPONLY,
                ReliableResponseUsage.parseReliableResponseUsage("Yes"));
    }
}
