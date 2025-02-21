/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs;

/**
 * The NotifierMfsException class signals that an error has occurred while performing an action on the MiO file system.
 * <p>
 * The cause of the error can be specified in this exception by using the constructor 
 * {@link NotifierMfsException#NotifierMfsException(String, NotifierMfsExceptionCause)}.
 */
public class NotifierMfsException extends Exception {

    /**
     * The NotifierMfsExceptionCause enum contains the possible causes of the failure 
     * to perform an action on the MiO file system.
     */
    public enum NotifierMfsExceptionCause {
        /**
         * No cause specified for the error.
         */
        NO_CAUSE_SPECIFIED,
        
        /**
         * The file does not exist in the file system.
         */
        FILE_DOES_NOT_EXIST,
        
        /**
         * The file is not accessible.  
         * For example, this could be due to limited file permissions, or failure to access the file system.
         */
        FILE_NOT_ACCESSIBLE,
        
        /**
         * The file read failed due to a file parsing error.
         */
        FILE_PARSING_ERROR;
    }
    
    
    private NotifierMfsExceptionCause exceptionCause = NotifierMfsExceptionCause.NO_CAUSE_SPECIFIED;

    /**
     * Constructs a NotifierMfsException instance with the specified error message.
     * Since no failure cause is specified, the default {@link NotifierMfsExceptionCause#NO_CAUSE_SPECIFIED} is used.
     * @param message the message containing the details of the error
     */
    public NotifierMfsException(String message) {
        this(message, NotifierMfsExceptionCause.NO_CAUSE_SPECIFIED);
    }
    
    /**
     * Constructs a NotifierMfsException instance with the specified error message and cause.
     * @param message the message containing the details of the error
     * @param exceptionCause the cause of the failure to perform the action on the file system
     */
    public NotifierMfsException(String message, NotifierMfsExceptionCause exceptionCause) {
        super(message);
        this.exceptionCause = exceptionCause;
    }
    
    /**
     * Gets the cause of the failure to perform the action on the file system.
     * @return the cause of the failure to perform the action on the file system
     */
    public NotifierMfsExceptionCause getNotifierMfsExceptionCause(){
        return exceptionCause;
    }
}
