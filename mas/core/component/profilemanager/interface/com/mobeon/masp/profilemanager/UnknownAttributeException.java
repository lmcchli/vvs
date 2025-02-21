/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

/**
 * Exception thrown when an unknown attribute has been requested
 */
public class UnknownAttributeException extends ProfileManagerException {
    public UnknownAttributeException(String message) {
        super(message);
    }
}
