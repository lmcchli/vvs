/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.condition;

/**
 * Exception thrown by the ConditionInterpreter if fails to
 * intepret an expression.
 *
 * @author Mats Egland
 */
public final class ConditionInterpreterException extends Exception {
	static final long serialVersionUID = -4706897414356732421L;;
    /**
     * Constructor that creates a ConditionInterpreterException with the message
     * given as argument.
     *
     * @param msg Message that describes of the exception.
     */
    public ConditionInterpreterException(String msg) {
        super(msg);
    }

    /**
     * Constructor that creates a ConditionInterpreterException with a message and a
     * root cause - the cause parameter.
     *
     * @param msg   Message that describes the exception.
     * @param cause The underlying Throwable that caused the exception.
     */
    public ConditionInterpreterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
