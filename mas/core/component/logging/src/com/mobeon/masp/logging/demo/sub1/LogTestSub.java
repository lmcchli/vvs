/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging.demo.sub1;

import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;

/**
 * A demo class used to illustrate usage of the ILogger interface. Used from the LogTestMain class.
 * @author David Looberger
 */
public class LogTestSub implements Runnable{
    private static final ILogger logger = ILoggerFactory.getILogger(LogTestSub.class);
    public void logDebug() {
        logger.debug("Debug Sub ");
    }

    public void logInfo() {
        if (logger.isInfoEnabled()) logger.info("Info Sub ");
    }

    public void logWarn() {
        logger.warn("Warn Sub ");
    }

    public void logError() {
        logger.error("Error Sub ");
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
}
