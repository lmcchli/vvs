/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Date: 2006-feb-08
 *
 * @author ermmaha
 */
public class XMPLogger implements ILogger {

    private static com.mobeon.common.logging.ILogger log = ILoggerFactory.getILogger(XMPLogger.class);

    public void debug(Object object) {
        if (log.isDebugEnabled()) log.debug(object);
    }

    public void debug(Object object, Throwable throwable) {
        if (log.isDebugEnabled()) log.debug(object, throwable);
    }

    public void info(Object object) {
        if (log.isInfoEnabled()) log.info(object);
    }

    public void info(Object object, Throwable throwable) {
        if (log.isInfoEnabled()) log.info(object, throwable);
    }

    public void warn(Object object) {
        log.warn(object);
    }

    public void warn(Object object, Throwable throwable) {
        log.warn(object, throwable);
    }

    public void error(Object object) {
        log.error(object);
    }

    public void error(Object object, Throwable throwable) {
        log.error(object, throwable);
    }

    public void fatal(Object object) {
        log.fatal(object);
    }

    public void fatal(Object object, Throwable throwable) {
        log.fatal(object, throwable);
    }

    public void registerSessionInfo(String string, Object object) {
        log.registerSessionInfo(string, object);
    }

    public void clearSessionInfo() {
        log.clearSessionInfo();
    }
    
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }
}
