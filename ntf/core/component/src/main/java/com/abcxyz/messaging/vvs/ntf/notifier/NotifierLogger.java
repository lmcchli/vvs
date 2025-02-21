/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;


public class NotifierLogger implements INotifierLogger {
    private LogAgent logAgent = null;
    
    public NotifierLogger(LogAgent logAgent) {
        this.logAgent = logAgent;
    }

    @Override
    public void debug(String logMessage) {
        logAgent.debug(logMessage);
    }

    @Override
    public void debug(String logMessage, Throwable throwable) {
        logAgent.debug(logMessage, throwable);
    }

    @Override
    public void info(String logMessage) {
        logAgent.info(logMessage);
    }

    @Override
    public void info(String logMessage, Throwable throwable) {
        logAgent.info(logMessage, throwable);
    }

    @Override
    public void warn(String logMessage) {
        logAgent.warn(logMessage);
    }

    @Override
    public void warn(String logMessage, Throwable throwable) {
        logAgent.warn(logMessage, throwable);
    }

    @Override
    public void error(String logMessage) {
        logAgent.error(logMessage);
    }

    @Override
    public void error(String logMessage, Throwable throwable) {
        logAgent.error(logMessage, throwable);
    }

    @Override
    public void fatal(String logMessage) {
        logAgent.fatal(logMessage);
    }

    @Override
    public void fatal(String logMessage, Throwable throwable) {
        logAgent.fatal(logMessage, throwable);
    }
    
}
