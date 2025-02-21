/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

/**
 * Exception thrown by the mediaobject package.
 *
 * This exception indicates that something went wrong when creating
 * an {@link IMediaObject}, or during operation on it.
 *
 * @see IMediaObject
 * @see com.mobeon.masp.mediaobject.factory.IMediaObjectFactory
 *
 * @author Mats Egland
 */
public final class MediaObjectException extends Exception {
	static final long serialVersionUID = -4706897414356732421L;
    /**
     * Constructor that creates a MediaObjectException with the message
     * given as argument.
     *
     * @param msg Message that describes of the exception.
     */
    public MediaObjectException(String msg) {
        super(msg);
    }

    /**
     * Constructor that creates a MediaObjectException with a message and a
     * root cause - the cause parameter.
     *
     * @param msg   Message that describes the exception.
     * @param cause The underlying Throwable that caused the exception. 
     */
    public MediaObjectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
