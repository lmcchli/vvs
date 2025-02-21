/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Indicates that a local session could not be established. This might,
 * for example, be because the local ports are already in use.
 *  
 * @author Jörgen terner
 */
public class CreateSessionException extends StackException {
    public CreateSessionException(String message) {
        super(message);
    }

    public CreateSessionException(String message, Throwable exception) {
        super(message, exception);
    }
}
