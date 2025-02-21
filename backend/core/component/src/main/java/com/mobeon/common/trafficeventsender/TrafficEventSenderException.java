/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

/**
 * Exception class that is thrown when some error occurred when calling the traffic eventsender
 *
 * @author ermmaha
 */
public class TrafficEventSenderException extends Exception {
    
    public enum TrafficEventSenderExceptionCause {
        NO_CAUSE_SPECIFIED,
        PAYLOAD_FILE_DOES_NOT_EXIST,
        PAYLOAD_FILE_PATH_NOT_ACCESSIBLE,
        PAYLOAD_FILE_NOT_ACCESSIBLE,
        PAYLOAD_FILE_PARSING_ERROR;
    }
    
    private TrafficEventSenderExceptionCause cause = TrafficEventSenderExceptionCause.NO_CAUSE_SPECIFIED;

    /**
     * Constructor
     *
     * @param msg detailed message about the error
     */
    public TrafficEventSenderException(String msg) {
        super(msg);
    }

    /**
     * Constructor
     *
     * @param msg detailed message about the error
     * @param cause The cause of the error.
     */
    public TrafficEventSenderException(String msg, TrafficEventSenderExceptionCause cause) {
        super(msg);
        this.cause = cause;
    }

    /**
     * Constructor
     *
     * @param cause the cause of the exception
     */
    public TrafficEventSenderException(Throwable cause) {
        super(cause);
    }
    
    public TrafficEventSenderExceptionCause getTrafficEventSenderExceptionCause() {
        return cause;
    }
}
