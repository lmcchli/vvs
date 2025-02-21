package com.mobeon.ntf.text;

/**
 * An exception which is thrown whenever a template cannot generate a message.
 *
 * @author ejonpit
 */
public class TemplateMessageGenerationException extends Exception {
    
    /**
     * An enum detailing the various actions to do when an exception is thrown.
     * @author ejonpit
     */
    public enum TemplateExceptionCause {
        CAUSE_NONE,
        CAUSE_PAYLOAD_FILE_DOES_NOT_EXIST,
        CAUSE_PAYLOAD_FILE_NOT_ACCESSIBLE;
    }
    
    /** The cause for this exception, if there is one. */
    private TemplateExceptionCause cause = TemplateExceptionCause.CAUSE_NONE;
    
    /**
     * Default constructor with no cause.
     * @param message The exception message.
     */
    public TemplateMessageGenerationException(String message) {
        super(message);
    }
    
    /**
     * Default constructor with a cause.
     * @param message The exception message.
     * @param action The cause.
     */
    public TemplateMessageGenerationException(String message, TemplateExceptionCause cause) {
        super(message);
        this.cause = cause;
    }
    
    public TemplateExceptionCause getTemplateExceptionCause() {
        return cause;
    }

}
