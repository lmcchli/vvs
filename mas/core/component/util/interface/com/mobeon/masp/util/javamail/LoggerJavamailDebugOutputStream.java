/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.javamail;

import com.mobeon.common.logging.ILogger;

/**
 * @author QHAST
 */

public class LoggerJavamailDebugOutputStream extends JavamailDebugOutputStream {


    private ILogger logger;

    public LoggerJavamailDebugOutputStream(ILogger logger) {
        if(logger == null) throw new IllegalArgumentException("logger cannot be null!");
        this.logger = logger;
    }

    protected void flushMessage(String message) {
        logger.debug(message);
    }




}
