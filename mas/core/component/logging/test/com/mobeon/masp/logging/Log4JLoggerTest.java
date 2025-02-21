/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import junit.framework.*;
import com.mobeon.masp.logging.Log4JLogger;
import com.mobeon.masp.logging.demo.LogTestMain;

public class Log4JLoggerTest extends TestCase {
    Log4JLogger log4JLogger;

    public void testLog4JLogger() throws Exception {
        // How to verify that the logentries actually end up in the Log??
        try {
            String config = "src/com/mobeon/masp/logging/demo/mobeon_log.xml";
            ILoggerFactory.configureAndWatch(config);
            LogTestMain lt = new LogTestMain();
            // lt.run();
            lt.logDebug();
            lt.logInfo();
            lt.logWarn();
            lt.logError();
        }
        catch(Exception e) {
            e.printStackTrace();
            fail("Test failed due to an Exception!");
        }
        assertTrue(1==1); // If no exception is caught, for now, consider the testcase a success.
    }
}
