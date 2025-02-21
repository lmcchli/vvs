/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Indicates a stack error. This could be anything from failure to 
 * establish a connection to problems communicating between native
 * code and the JVM.
 *  
 * @author Jörgen terner
 */
public class StackException extends Exception {
    public StackException(String message) {
        super(message);
    }

    public StackException(String message, Throwable exception) {
        super(message, exception);
    }
}
