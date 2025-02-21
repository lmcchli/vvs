/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import org.mozilla.javascript.EvaluatorException;

/**
 * Exception class that is thrown when some error occurred in the platform access
 *
 * @author ermmaha
 */
public class PlatformAccessException extends EvaluatorException {
    private static final String SEPARATOR = "%";
    /**
     * Description string
     */
    private String description;

    /**
     * Constructor
     *
     * @param name        The name on the VXML event. Should be one of the constans defined in
     *                    <code>com.mobeon.masp.execution_engine.platformaccess.EventType</code>
     * @param description detailed message about the error
     */
    public PlatformAccessException(String name, String description) {
        super(name);
        this.description = description;
    }

    /**
     * Constructor
     *
     * @param name    The name on the VXML event. Should be one of the constans defined in
     *                <code>com.mobeon.masp.execution_engine.platformaccess.EventType</code>
     * @param message A message that describes the error
     * @param details Additional information about the error
     */
    public PlatformAccessException(String name, String message, String details) {
        super(name);
        this.description = message + SEPARATOR + details;
    }

    /**
     * Constructor
     *
     * @param name    The name on the VXML event. Should be one of the constans defined in
     *                <code>com.mobeon.masp.execution_engine.platformaccess.EventType</code>
     * @param message A message that describes the error
     * @param ex      Nested exception object to get message and stacktrace from
     */
    public PlatformAccessException(String name, String message, Exception ex) {
        super(name);
        this.description = message + SEPARATOR + ex.getMessage();
        setStackTrace(ex.getStackTrace());
    }

    /**
     * Retrieves the description about the error
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }
}
