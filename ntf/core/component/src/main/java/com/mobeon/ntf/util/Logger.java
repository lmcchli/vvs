/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;

import java.util.*;
import java.text.SimpleDateFormat;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;

/**
 *
 * Functional Description
 * ----------------------
 *
 *      This class is to be used for Logging.
 *
 * <H4>Reduced logging</H4>

 * Sometimes it is of little value to log the same message several times,
 * telling about some condition such as a connection to another host that is
 * broken. Logger provides support to log a message once when the condition
 * occurs, and once when it ends. The log client calls the log method as usual,
 * and Logger knows if the message is repeated and should be suppressed, or
 * original and shall be logged. Logger can also automatically log the message
 * once every 100 (for example) repetions, adding the text (repeated 100 times)
 * to the message.
 *
 * Each group of equivalent messages is assigned an id, which the log client
 * requests from the Logger. The text may be different in equivalent messages,
 * but they shall be equivalent in the sense that if one of them has been
 * logged, the others can be suppressed.
 *
 * For each logMessage method, there is a corresponding logReduced method which
 * supresses repeated messages.
 * <example>
 * Logger l= Logger.getLogger();
 * int noConnectionMessageId= l.createMessageId(100);
 * while (!connected) {
 *     if (!makeConnection(otherHost)) {
 *         l.logReduced(noConnectionMessageId, "Could not connect to other host");
 *     }
 * }
 * l.logReducedOff(noConnectionMessageId, "Connection to other host succeeded");
 * <P>
 * 200-300 failed connection attempts will give a log looking like this:
 *<P>
 * Sep 21 2001 12:41:12:312 : Could not connect to other host
 * Sep 21 2001 12:45:29:197 : Could not connect to other host (Repeated 100 times)
 * Sep 21 2001 12:55:29:276 : Could not connect to other host (Repeated 100 times)
 * Sep 21 2001 12:55:29:507 : Connection to other host succeeded
 * </example>
 ********************************************************************/

public class Logger
{
    
    private static HashMap<String,Logger> loggers = new HashMap<String,Logger>();
    
    private LogAgent logAgent;

    public final static int L_OFF       = 0x00;
    public final static int L_ERROR     = 0x01;
    public final static int L_VERBOSE   = 0x02;
    public final static int L_DEBUG     = 0x03;
    
    private static int LogLevel =  L_DEBUG ; //disable in mio let log4j handle it...


    private Logger(LogAgent la)
    {
        //Set the class name to the caller..
        logAgent = la;
        createMessageId();
    }


    public static Logger getLogger()
    {
        String clazz = (new Throwable().getStackTrace()[1]).getClassName();       
        return (getLogger(clazz));
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return(getLogger(clazz.getName()));
    }
    
    public synchronized static Logger getLogger(String clazz)
    {       
        Logger log = loggers.get(clazz);
        if (log == null) {
            LogAgent logAgent = NtfCmnLogger.getLogAgent(clazz);
            log = new Logger(logAgent);
            loggers.put(clazz,log);
            return log;
        } else {
            return log;
        }                    
    }

         public void logMessage(String msg) {
             logMessage(msg, 0);
         }

         //Adaptation to use the backend LogAgentFactory
         public void logMessage(String msg, int log_level) {

             //map the existing ntf logging levels to the ones provided by the backend
             if ( log_level == L_DEBUG) {
                 logAgent.debug(msg);
             }
             if ( log_level == L_ERROR) {
                 logAgent.error(msg);
             }
             if ( log_level == L_VERBOSE) {
                 logAgent.info(msg);
             }
             if ( log_level == L_OFF) {
                 logAgent.fatal(msg);
             }
         }


    /**********************************************************************************
     * Set the log level. Check that log level is between off and debug
     @param logLevel the new log level to set.
     @return true if the log level is set (if the level is between off and
     debug). False if the value for the log level is outside the limits or if
     the log has not been created yet.
    */
    public static boolean setLogLevel(int logLevel) {
        return true;
    }



    /** logCount counts how many times a message with a specific id (i.e the
    index in the array) has been logged.*/
    private static int logCount[];
    /** logEvery specify how many log requests there shall be between actual
    loggings*/
    private static int logEvery[];
    /** lastId is the log id that was last used*/
    private static int lastId;
    private static final int sizeIncrement= 100;

    /****************************************************************
     * Requests an id for a message that shall be printed sometimes, as long as
     * it is active.
     * @param every tells how many log requests there shall be between actual
     * loggings. A value of 0 or less means the message will be printed only
     * once.
     * @return a new unique log id
     */
    public synchronized int createMessageId(int every) {
    int newCount[];
    int newEvery[];
    int i;

    ++lastId;
    if (logCount == null) {
        logCount= new int[sizeIncrement];
        logEvery= new int[sizeIncrement];
    }
    if (lastId >= logCount.length) {
        newCount= new int[logCount.length + sizeIncrement];
        newEvery= new int[logEvery.length + sizeIncrement];
        for (i= 0; i < logCount.length; i++) {
        newCount[i]= logCount[i];
        newEvery[i]= logEvery[i];
        }
        logCount= newCount;
        logEvery= newEvery;
    }
    logEvery[lastId]= every;
    return lastId;
    }


    /****************************************************************
     * Requests an id for a message that shall be printed only once.
     * @return a new unique log id
     */
    public int createMessageId() {
    return createMessageId(0);
    }

    /****************************************************************
     * @param id the message id
     * @return true iff the next call to logReduced for the message id will
     * really be logged. The purpose is to reduce execution time if the message
     * is the result of a time-consuming operation. It can not be relied upon
     * for anything else, some other thread may call logReduced and change the
     * situation. Example:<br>
     * <example>
     * if (willLog(connectionErrorMessageId)) {
     *     logReduced(connectionErrorMessageId, "value+" +
     *       primeLargerThan(1234567) + getStringFromFarAwayHost());
     * }
     *</example>
     */
    public synchronized boolean willLog(int id) {
    return (logCount[id] == 0 || logEvery[id] > 0 && logCount[id] >= logEvery[id]);
    }


    /****************************************************************
     * @param id the message id
     * @return true iff the message is active. i.e. has been logged at least
     * once. It can be used to call logReducedOff only when a message has been
     * printed. LogReducedOff will of course only print the "off" message if the
     * "on" message is active, but checking isActive avoids the overhead of
     * producing the message for logReducedOff when not needed.
     * Example:<br>
     * <example>
     * if (isActive(connectionErrorMessageId)) {
     *     logReducedOff(connectionErrorMessageId, "value+" +
     *       primeLargerThan(1234567) + getStringFromFarAwayHost());
     * }
     *</example>
     */
    public boolean isActive(int id) {
    return (logCount[id] > 0);
    }


    public synchronized void logReduced(int id, String msg) {
    if (willLog(id)) {
        if (logCount[id] > 0) { //logCount[id] has reached logEvery[id]
        logMessage(msg + " (Repeated " + logEvery[id] + " times)");
        logCount[id]= 0;
        } else {
        logMessage(msg);
        }
    }
    ++logCount[id];
    }

    public void logReduced(int id, String msg, int level) {
        if (level > LogLevel)
            return;
        synchronized (this) {
            if (willLog(id)) {
                if (logCount[id] > 0) { //logCount[id] has reached logEvery[id]
                    logMessage(msg + " (Repeated " + logEvery[id] + " times)", level);
                    logCount[id]= 0;
                } else {
                    logMessage(msg, level);
                }
            }
            ++logCount[id];
        }
    }

    public synchronized void logReducedOff(int id, String msg) {
    if (logCount[id] > 0) {
            logCount[id]= 0;
            logMessage(msg);
        }
    }

    public synchronized void logReducedOff(int id, String msg, int level) {
        if (logCount[id] > 0) {
            logCount[id]= 0;
            logMessage(msg, level);
        }
    }

    public String getCurDate() {
        Calendar cal = Calendar.getInstance();
        Date curdate = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss:SSS");
        df.setTimeZone(cal.getTimeZone());
        return df.format(curdate);
    }

    public String getCurDate(String format) {
    Calendar cal = Calendar.getInstance();
    Date curdate = cal.getTime();
    SimpleDateFormat df = new SimpleDateFormat(format);
    df.setTimeZone(cal.getTimeZone());
    return df.format(curdate);
    }

  
    public String getLogLevel(int log_level) {
        switch (log_level) {
        case 0: //Off is never logged. 0 means logging without level.
                return "";
            case L_ERROR:
                return "Error";
            case L_VERBOSE:
                return "Verbose";
            case L_DEBUG:
                return "Debug";
            default:
                return "<Unknown>";
        }
    }

    /****************************************************************
     * main is just for testing.
     */
    public static void main(String args[]) {
    Logger l= getLogger(Logger.class);
    int msg1;
    int msg2;

    msg1= l.createMessageId();
    msg2= l.createMessageId(10);

    for (int i= 0; i < 15; i++) {
        l.logReduced(msg1, "Message 1 error");
        l.logReduced(msg2, "Message 2 error");
    }
    l.logMessage(l.willLog(msg1)?"log":"nolog");
    l.logMessage(l.willLog(msg2)?"log":"nolog");
    l.logReducedOff(msg1, "Message 1 OK");
    l.logReducedOff(msg2, "Message 2 OK");
    l.logMessage(l.willLog(msg1)?"log":"nolog");
    l.logMessage(l.willLog(msg2)?"log":"nolog");
    for (int i= 0; i < 25; i++) {
        l.logReduced(msg1, "Message 1 error");
        l.logReduced(msg2, "Message 2 error");
    }
    }
}

