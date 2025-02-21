/*
 * SDLogger.java
 *
 * Created on den 31 augusti 2004, 15:39
 */

package com.mobeon.common.storedelay;

/**
 * Proxy to normal logging mechanism.
 * Used to avoid coupling to logging in other places in storedelay.
 * Move to 'real' logging when integrated.
 * @author  QMIER
 */
public class SDLogger {


    private static SDLogger theLogger = new SDLogger();;

    protected SDLogger() {
    }

    /**
     * Set a logger to use.
     * @param newLog New logger to use, if it is null the default logger will
     *               be used.
     */
    public static void setLogger(SDLogger newLog)
    {
        theLogger = newLog;
        if (theLogger == null) theLogger = new SDLogger();
    }

    // LOG Levelse
    public static final int TRACE   = 0;  // Logs doen even when no external events
    public static final int DEBUG   = 1;  // Detailed info and data about control flow
    public static final int INFO    = 2;  // Info about normal operation
    public static final int WARNING = 3;  // Unexcpected things
    public static final int ERROR   = 4;  // Excpected error occured
    public static final int SEVERE  = 5;  // Unexcpected error occured
    public static final int FATAL   = 6;  // Can not run more

    protected static int logLevel = DEBUG;

    protected static final String LEVELS[] =
    {"TRACE","DEBUG","INFO", "WARNING","ERROR","SEVERE","FATAL"};



    protected void doLog(int level, String message)
    {
        System.out.println("[" + LEVELS[level] + "] " + message);
    }

    public static void log(int level, String message)
    {
        if (!willLog(level)) return;
        theLogger.doLog(level, message);
    }

    public static void setLevel(int wantLevel)
    {
        logLevel = wantLevel;
    }

    public static int getLevel()
    {
        return logLevel;
    }

    public static boolean willLog(int level)
    {
        return level >= logLevel;
    }

    protected void doLog(int level, String message, Throwable t)
    {
        System.out.println("[" + LEVELS[level] + "] " + message + " : Exc:" +
                            t.getMessage());
        t.printStackTrace();
    }

     public static void log(int level, String message, Throwable t)
    {
        if (!willLog(level)) return;
        theLogger.doLog(level, message, t);
    }

    protected void doLogObject(int level, String message, Object obj)
    {
        System.out.println("[" + LEVELS[level] + "] " + message +
                            ":" + obj.toString());

    }

    public static void logObject(int level, String message, Object obj)
    {
        if (!willLog(level)) return;
        theLogger.doLogObject(level, message, obj);

    }

}
