/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessLogger;
import com.mobeon.common.logging.ILogger;

public class PlatformAccessLogger implements IPlatformAccessLogger {

    private ILogger log = null;

    public PlatformAccessLogger(ILogger log) {
        this.log = log;
    }

    @Override
    public void debug(String logMessage) {
        log.debug(logMessage);
    }

    @Override
    public void debug(String logMessage, Throwable throwable) {
        log.debug(logMessage, throwable);
    }

    @Override
    public void info(String logMessage) {
        log.info(logMessage);
    }

    @Override
    public void info(String logMessage, Throwable throwable) {
        log.info(logMessage, throwable);
    }

    @Override
    public void warn(String logMessage) {
        log.warn(logMessage);
    }

    @Override
    public void warn(String logMessage, Throwable throwable) {
        log.warn(logMessage, throwable);
    }

    @Override
    public void error(String logMessage) {
        log.error(logMessage);
    }

    @Override
    public void error(String logMessage, Throwable throwable) {
        log.error(logMessage, throwable);
    }

    @Override
    public void fatal(String logMessage) {
        log.fatal(logMessage);
    }

    @Override
    public void fatal(String logMessage, Throwable throwable) {
        log.fatal(logMessage, throwable);
    }

}
