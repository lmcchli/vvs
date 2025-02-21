/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.logging;

/**
 * This class adds log filtering metadata to a log message.
 * User wraps a message with this class and call logging methods in {@link ILogger}
 * with this class as message.
 * If the logger is configured with a {@link RepetitiveLoggingFilter}, the filter will deny
 * all Repetitive calls for the same log context until reset messge is passed to the logger.
 *
 * @author Håkan Stolt
 * @see RepetitiveLoggingFilter
 */
public class LogJustOnceMessage {

    /**
     * Indicates if this message should reset the filter.
     */
    private boolean triggReset;

    /**
     * The message text to be logged.
     */
    private String messageText;

    /**
     * The object that caused the logger call.
     */
    private LogContext logContext;


    /**
     *
     * @param logContext context that caused the logger call.
     * @param messageText message text to be logged.
     * @param triggReset set to true if this message should reset the filter for this logContextA.
     */
    public LogJustOnceMessage(LogContext logContext, String messageText, boolean triggReset) {
        this.logContext = logContext;
        this.messageText = messageText;
        this.triggReset = triggReset;
    }

    /**
     * Gets the context that caused the logger call
     * @return context object
     */
    public LogContext getLogContext() {
        return logContext;
    }

    /**
     * Gets the trigg reset indicator.
     * @return true if this message triggs reset of the filter for this logContextA.
     */
    public boolean getTriggReset() {
        return triggReset;
    }

    /**
     * Gets the message text.
     * @return message text.
     */
    public String getMessageText() {
        return messageText;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{[");
        sb.append(logContext);
        if(triggReset) {
            sb.append("(reset)");
        }
        sb.append("]: ");
        sb.append(messageText);
        sb.append("}");
        return sb.toString();
    }

    /**
     * @param o
     * @return true if log contexts are equal.
     */
    public boolean equals(Object o) {
        if(o instanceof LogJustOnceMessage) {
            LogJustOnceMessage other = (LogJustOnceMessage) o;
            return other.logContext.equals(this.logContext);
        }
        return false;
    }

    public int hashCode() {
        return logContext.hashCode();
    }

}
