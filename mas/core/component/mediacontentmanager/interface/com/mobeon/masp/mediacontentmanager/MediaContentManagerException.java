/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

/**
 * Exception thrown by the MediaContentManager package.
 *
 * @author Mats Egland
 */
public final class MediaContentManagerException extends Exception {
	static final long serialVersionUID = -4706897414356732421L;;
    /**
     * Constructor that creates a MediaObjectException with the message
     * given as argument.
     *
     * @param msg Message that describes of the exception.
     */
    public MediaContentManagerException(String msg) {
        super(msg);
    }

    /**
     * Constructor that creates a MediaContentManagerException with a message and a
     * root cause - the cause parameter.
     *
     * @param msg   Message that describes the exception.
     * @param cause The underlying Throwable that caused the exception.
     */
    public MediaContentManagerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
