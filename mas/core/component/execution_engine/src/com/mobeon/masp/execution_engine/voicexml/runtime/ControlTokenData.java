/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.ControlToken;

/**
 * @author David Looberger
 */
public class ControlTokenData {
    private ControlToken token;
    private long timestamp;
    private static final ILogger log = ILoggerFactory.getILogger(ControlTokenData.class);

    public ControlTokenData(ControlToken token) {
        this.token = token;
        this.timestamp = System.currentTimeMillis();
    }

    public ControlToken getToken() {
        return token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the time difference in milliseconds between this instance and another ControlTokenData object
     * @param ct
     * @return Time difference in milliseconds
     */
    public long getTimeDifference(ControlTokenData ct) {
        if (log.isDebugEnabled()) log.debug("Time difference between tokens :" + (this.timestamp - ct.getTimestamp()));
        return this.timestamp - ct.getTimestamp();
    }
}
