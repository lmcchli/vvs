/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

/**
 * Exception thrown by the {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier}
 * and {@link com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory} classes.
 *
 * This exception indicates that something went wrong when creating
 * an {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier}, or during operation on it.
 *
 *
 * @author Mats Egland
 */
public final class MediaQualifierException extends Exception {
	static final long serialVersionUID = -4706897414356732421L;;
    /**
     * Constructor that creates a MediaQualifierException with the message
     * given as argument.
     *
     * @param msg Message that describes of the exception.
     */
    public MediaQualifierException(String msg) {
        super(msg);
    }

    /**
     * Constructor that creates a MediaQualifierException with a message and a
     * root cause - the cause parameter.
     *
     * @param msg   Message that describes the exception.
     * @param cause The underlying Throwable that caused the exception.
     */
    public MediaQualifierException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
