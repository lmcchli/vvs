/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp.applications.vxml.ecma;

import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class ECMADummy {
    private static final ILogger log = ILoggerFactory.getILoggerFromCategory("Application");

    public ECMADummy() {
    }

    public void acceptIntArray(int[] values) {
        for (int i : values) {
            log.info("Value: " + i);
        }
    }

    public void raisePlatformAccessException() {
        throw new PlatformAccessException("datanotfound", "Did not find data");
    }
}
