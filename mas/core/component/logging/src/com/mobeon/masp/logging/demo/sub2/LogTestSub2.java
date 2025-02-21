/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging.demo.sub2;

import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;

/**
  * A demo class used to illustrate usage of the ILogger interface. Used from the LogTestMain class.
 * @author David Looberger
 * */
public class LogTestSub2 {
    private static final ILogger logger = ILoggerFactory.getILogger(LogTestSub2.class);
    public void logDebug() {
        logger.debug("Debug Sub 2");
    }

    public void logInfo() {
        if (logger.isInfoEnabled()) logger.info("Info Sub 2");
    }

    public void logWarn() {
        logger.warn("Warn Sub 2");
    }

    public void logError() {
        logger.error("Error Sub 2");
    }

    /**
     *  Generate some log traces
     */
    public void run() {
        this.logDebug();
        this.logInfo();
        this.logWarn();
        this.logError();
    }

     public static void main(String[] args) {
         // Get config file from cmd line
       String config = args[0];
       // Read logging cfg file. Watch it for changes one per minute
         logger.debug("STARTING");
         LogTestSub2 t = new LogTestSub2();
         t.run();
     }
}
