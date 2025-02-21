/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

/**
 * Unchecked Exception thrown if a XML-to-Object mapping fails. Indicates
 * that either:
 * <ul>
 *      <li>An <code>IOException</code> is thrown while reading the XML.</li>
 *      <li>An <code>SAXException</code> is thrown whíle parsing the XML.</li>
 *      <li>The XML was in some way illegal, so that the mapping failed.</li>
 * </ul>
 * In either case the mapping failed: Indicating an error in the XML-file.
 *
 * @author Mats Egland
 */
public final class SaxMapperException extends RuntimeException {
	static final long serialVersionUID = -4706897414356732421L;

    /**
     * Constructor that creates a SaxMapperException with the message
     * given as argument.
     *
     * @param msg Message that describes of the exception.
     */
    public SaxMapperException(String msg) {
        super(msg);
    }

    /**
     * Constructor that creates a SaxMapperException with a message and a
     * root cause - the cause parameter.
     *
     * @param msg   Message that describes the exception.
     * @param cause The underlying Throwable that caused the exception.
     */
    public SaxMapperException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
