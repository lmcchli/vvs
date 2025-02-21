/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send;

/**
 * 
 * The NotifierMessageGenerationException class signals that an error has occurred while generating the notification message.
 * <p>
 * The cause of the error can be specified in this exception by using the constructor 
 * {@link NotifierMessageGenerationException#NotifierMessageGenerationException(String, MessageGenerationExceptionCause)}.
 */
public class NotifierMessageGenerationException extends Exception {
    
    /**
     * The MessageGenerationExceptionCause enum contains the possible causes of the error 
     * that occurred while generating the notification message.
     */
    public enum MessageGenerationExceptionCause {
        /**
         * No cause specified for the error.
         */
        NO_CAUSE_SPECIFIED,
        
        /**
         * The pay-load file does not exist in the file system.
         */
        PAYLOAD_FILE_DOES_NOT_EXIST,
        
        /**
         * The file is not accessible.  
         * For example, this could be due to limited file permissions, or failure to access the file system.
         */
        PAYLOAD_FILE_NOT_ACCESSIBLE;
    }

    
    private MessageGenerationExceptionCause exceptionCause = MessageGenerationExceptionCause.NO_CAUSE_SPECIFIED;

    
    /**
     * Constructs a NotifierMessageGenerationException instance with the specified error message.
     * Since no error cause is specified, the default {@link MessageGenerationExceptionCause#NO_CAUSE_SPECIFIED} is used.
     * @param message the message containing the details of the error
     */
    public NotifierMessageGenerationException(String message) {
        this(message, MessageGenerationExceptionCause.NO_CAUSE_SPECIFIED);
    }

    /**
     * Constructs a NotifierMessageGenerationException instance with the specified error message and cause.
     * @param message the message containing the details of the error
     * @param exceptionCause the cause of the notification message generation error
     */
    public NotifierMessageGenerationException(String message, MessageGenerationExceptionCause exceptionCause) {
        super(message);
        this.exceptionCause = exceptionCause;
    }
    
    /**
     * Gets the cause of the notification message generation error.
     * @return the cause of the notification message generation error
     */
    public MessageGenerationExceptionCause getMessageGenerationExceptionCause(){
        return exceptionCause;
    }
}
