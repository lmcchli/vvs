/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import com.mobeon.masp.util.test.MASTestSwitches;
import org.apache.log4j.BasicConfigurator;


/**
 * The factory used to instanciate ILogger objects.
 * @author David Looberger
 */
public class ILoggerFactory {

    static {
        BasicConfigurator.configure();
    }

    public ILogger getILogger(String clazz) {
        return (ILogger)Log4JLogger.getLogger(clazz);
    }

    public static ILogger getILogger(Class clazz) {
        String reg = clazz.getName();
        return (ILogger) Log4JLogger.getLogger(reg);
    }

    public static ILogger getILoggerFromCategory(String category) {
        return (ILogger) Log4JLogger.getLogger(category);
    }


    public static void configureAndWatch(String config) {
        Logger loggerLogger = Log4JLogger.getLoggerLogger();
        if(loggerLogger.getLevel() == null || loggerLogger.getLevel().isGreaterOrEqual(Level.WARN))
            loggerLogger.setLevel(Level.INFO);
        Log4JLoggerFactory.configureAndWatch(config);
        if(loggerLogger.isInfoEnabled())
            loggerLogger.info("Using configuration from: "+config);
    }
}
