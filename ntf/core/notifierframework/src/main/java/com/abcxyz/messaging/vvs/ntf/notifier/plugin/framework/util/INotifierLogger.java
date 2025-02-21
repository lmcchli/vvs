/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * An interface providing access to the logging functionality. 
 * It provides access to the underlying logging framework used by the implementing class.
 */
public interface INotifierLogger {

    /**
     * Log a message at the DEBUG level.
     * @param logMessage the message to log
     */
    public void debug(String logMessage);

    /**
     * Log a message at the DEBUG level with the included stack trace.
     * @param logMessage the message to log
     * @param throwable the exception to log, including the stack trace
     */
    public void debug(String logMessage, Throwable throwable);

    /**
     * Log a message at the INFO level.
     * @param logMessage the message to log
     */
    public void info(String logMessage);

    /**
     * Log a message at the INFO level with the included stack trace.
     * @param logMessage the message to log
     * @param throwable the exception to log, including the stack trace
     */
    public void info(String logMessage, Throwable throwable);

    /**
     * Log a message at the WARN level.
     * @param logMessage the message to log
     */
    public void warn(String logMessage);

    /**
     * Log a message at the WARN level with the included stack trace.
     * @param logMessage the message to log
     * @param throwable the exception to log, including the stack trace
     */
    public void warn(String logMessage, Throwable throwable);

    /**
     * Log a message at the ERROR level.
     * @param logMessage the message to log
     */
    public void error(String logMessage);

    /**
     * Log a message at the ERROR level with the included stack trace.
     * @param logMessage the message to log
     * @param throwable the exception to log, including the stack trace
     */
    public void error(String logMessage, Throwable throwable);

    /**
     * Log a message at the FATAL level.
     * @param logMessage the message to log
     */
    public void fatal(String logMessage);

    /**
     * Log a message at the FATAL level with the included stack trace.
     * @param logMessage the message to log
     * @param throwable the exception to log, including the stack trace
     */
    public void fatal(String logMessage, Throwable throwable);

}
