/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

/****************************************************************
 * XXX
 * Logger specifies an interface for clients that want to receive log messages
 * from the SMSC. SMSCom can send messages to a Logger that is registered with
 * the setLogger method of SMSCom. If no Logger is registered SMSCom will not
 * log anything. If a Logger is registered, the amount of logging is controlled
 * by the implementation of the ifLog method.
 * <CODE>
 * public boolean ifLog(int level) {
 *   return true;
 * }
 * </CODE>
 * will log everything.
 * <CODE>
 * public boolean ifLog(int level) {
 *   return (level <= LOG_DEBUG);
 * }
 * </CODE>
 * will log everything but the dump information.
 ****************************************************************/
public interface Logger {
    
    /**LOG_ERROR is 1. SMSCom will not use this level since it reports errors
       with exceptions.*/
    static final int LOG_ERROR = 1;
    /**LOG_VERBOSE is 2. SMSCom will use this level for messages that occur
       infrequently. This level will not give any messages related to individual
       notifications.*/
    static final int LOG_VERBOSE = 2;
    /**LOG_DEBUG is 3. SMSCom will use this for some information about each
     * notification.*/
    static final int LOG_DEBUG = 3;
    /**LOG_DUMP will dump all relevant information for every notification,
       giving a lot of log printouts.*/
    static final int LOG_DUMP = 4;
    

    /****************************************************************
     * When an interesting event has occured, the SMSComException calls
     * logString with a text message about the event.
     * @param msg Message with information about an event.
     * @param l the level of the message. The interpretation and effect of this
     * parameter is up to the user.
     */
    void logString(String msg, int l);

    /****************************************************************
     * This function lets the listener for log messages filter messages of
     * different importance. Before logging a message, SMSCom will use this
     * function to ask the log listener if it wants the message and log only if
     * the answer is true.
     * @param level The importance of the of the message, as defined in the
     * description of the constants LOG_ERROR, LOG_VERBOSE, LOG_DEBUG and
     * LOG_DUMP above.
     * @return true if the log listener wants messages of the level.
     */
    boolean ifLog(int level);
}
