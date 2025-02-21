/*
 * SDLogger.java
 *
 * Created on den 31 augusti 2004, 15:39
 */

package com.mobeon.common.commands;

import com.mobeon.common.storedelay.SDLogger;

/**
 * Proxy to normal logging mechanism.
 * Used to avoid coupling to logging in other places in storedelay.
 * Move to 'real' logging when integrated.
 * @author  QMIER
 */
public class CHLogger
{


    private static CHLogger theLogger = new CHLogger();;

    /**
     * Create a logger object.
     */
    protected CHLogger()
    {
    }

    /**
     * Set a logger to use.
     * @param newLog New logger to use, if it is null the default logger will
     *               be used.
     */
    public static void setLogger(CHLogger newLog)
    {
        theLogger = newLog;
        if (theLogger == null) theLogger = new CHLogger();
    }

    // LOG Levels
    /**  Logs doen even when no external events. */
    public static final int TRACE = 0;
    /** Detailed info and data about control flow. */
    public static final int DEBUG = 1;
    /** Info about normal operation. */
    public static final int INFO = 2;
    /** Unexcpected things. */
    public static final int WARNING = 3;
    /** Excpected error occured. */
    public static final int ERROR = 4;
    /** Unexcpected error occured. */
    public static final int SEVERE = 5;
    /** Can not run more. */
    public static final int FATAL = 6;

    /** Current loglevel. */
    protected static int logLevel = DEBUG;

    protected static final String LEVELS[] =
        {"TRACE", "DEBUG", "INFO", "WARNING", "ERROR", "SEVERE", "FATAL"};


    /**
     *  Log to stdout on given level.
     * @param level Log level
     * @param message Message to log
     */
    protected void doLog(int level, String message)
    {
        System.out.println("[" + LEVELS[level] + "] " + message);
    }

    /**
     * Let the current logger log.
     * @param level Log level
     * @param message Message to log
     */
    public static void log(int level, String message)
    {
        SDLogger.log(level, message);
    }

    /**
     * Set log level.
     * @param wantLevel wanted log level.
     */
    public static void setLevel(int wantLevel)
    {
        SDLogger.setLevel(wantLevel);
    }

    public static int getLevel()
    {
        return SDLogger.getLevel();
    }

    public static boolean willLog(int level)
    {
        return SDLogger.willLog(level);
    }

    protected void doLog(int level, String message, Throwable t)
    {
        SDLogger.log(level, message, t);
        t.printStackTrace();
    }

    public static void log(int level, String message, Throwable t)
    {
        SDLogger.log(level, message, t);
    }

    protected void doLogObject(int level, String message, Object obj)
    {
    	SDLogger.logObject(level, message, obj);

    }

    public static void logObject(int level, String message, Object obj)
    {
        SDLogger.logObject(level, message, obj);
    }

}
