/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send;

/**
 * 
 * The NotifierSendException class signals that an error has occurred while sending the notification.
 * <p>
 * The cause of the error can be specified in this exception by using the constructor 
 * {@link NotifierSendException#NotifierSendException(String, NotifierSendExceptionCause)}.
 */
public class NotifierSendException extends Exception {

    /**
     * The NotifierSendExceptionCause enum contains the possible causes of the error 
     * that occurred while sending the notification.
     */
    public enum NotifierSendExceptionCause {
        /** Cause of send error is not specified. */
        NO_CAUSE_SPECIFIED,
        
        /** Settings in the subscriber's database profile prevents the notification from being sent. */
        SUBSCRIBER_DATABASE_PROFILE,
        
        /** Unable to retrieve required information from the message store. */
        MESSAGE_STORE_ERROR
    }
    
    private NotifierSendExceptionCause cause = NotifierSendExceptionCause.NO_CAUSE_SPECIFIED;
    
    /**
     * Constructs a NotifierSendException instance with the specified error message.
     * Since no error cause is specified, the default {@link NotifierSendExceptionCause#NO_CAUSE_SPECIFIED} is used.
     * @param message the message containing the details of the error
     */
    public NotifierSendException(String message) {
        super(message);
    }

    /**
     * Constructs a NotifierSendException instance with the specified error message and cause.
     * @param message the message containing the details of the error
     * @param cause the cause of the send notification error
     */
    public NotifierSendException(String message, NotifierSendExceptionCause cause) {
        super(message);
        this.cause = cause;
    }

    /**
     * Gets the cause of the send notification error.
     * @return the cause of the send notification error
     */
    public NotifierSendExceptionCause getNotifierSendExceptionCause() {
        return cause;
    }
}
