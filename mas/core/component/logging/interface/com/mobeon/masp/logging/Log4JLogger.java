/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.spi.LoggerFactory;

import java.util.Hashtable;

import com.mobeon.masp.util.debug.Tools;


/**
 * Implements the ILogger interface by extending the Log4J framework. Extends the Log4J Logger class
 *
 * @author David Looberger
 */
public class Log4JLogger extends Logger implements ILogger {
    // It's usually a good idea to add a dot suffix to the fully
    // qualified class name. This makes caller localization to work
    // properly even from classes that have almost the same fully
    // qualified class name as MyLogger, e.g. MyLoggerTest.
    static String FQCN = Log4JLogger.class.getName() + ".";

    // It's enough to instantiate a factory once and for all.
    private static Log4JLoggerFactory myFactory = new Log4JLoggerFactory();
    private static Logger loggerLogger = RepositorySelectorImpl.getInstance().getDefaultLogger("LoggerLogger");
    
    /**
     * This method overrides {@link Logger#getLogger} by supplying
     * its own factory type as a parameter.
     */
    public
    static Logger getLogger(String name) {
        return Logger.getLogger(name, myFactory);
    }

    protected Log4JLogger(String s) {
        super(s);
    }

    public void debug(Object object) {
        if(assertDebugGuard())
            super.debug(object);
    }

    public void info(Object object) {
        if(assertInfoGuard())
            super.info(object);
    }

    private boolean assertDebugGuard() {
        boolean debugEnabled = super.isDebugEnabled();
        if(!debugEnabled) {
            if(loggerLogger.isInfoEnabled())
                loggerLogger.info("DEBUG statement not guarded with isDebugEnabled() at "+ Tools.outerCaller(1,true));
        }
        return debugEnabled;
    }

    private boolean assertInfoGuard() {
        boolean infoEnabled = super.isInfoEnabled();
        if(!infoEnabled) {
            if(loggerLogger.isInfoEnabled())
                loggerLogger.info("INFO statement not guarded with isInfoEnabled() at "+ Tools.outerCaller(1,true));
        }
        return infoEnabled;
    }

    /**
     * Indicate to which session the log items from the current thread (and is sub-threads)
     * correspond to.
     *
     * @param name
     * @param sessionInfo
     */
    public void registerSessionInfo(String name, Object sessionInfo) {
        if (sessionInfo == null) {
            sessionInfo = "RegisterSessionInfo with no session information called by " + outerCaller(0, true);
        }
        MDC.put(name, sessionInfo.toString());
    }

    /**
     * Clear all session log items from the current thread (and its sub-threads).
     */
    public void clearSessionInfo() {
        Hashtable context = MDC.getContext();
        if (context != null)
            context.clear();
    }

    private static String outerCaller(int i, boolean isDebugEnabled) {
        if (isDebugEnabled) {
            try {
                thrower();
            } catch (Exception e) {
                return e.getStackTrace()[3 + i].toString();
            }
            return "<unknown>";
        } else {
            return "<enable debug to show caller>";
        }
    }

    private static void thrower() throws Exception {
        throw new Exception();
    }

    public static Logger getLoggerLogger() {
        return loggerLogger;
    }
}
