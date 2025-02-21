/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

/**
 * An  error that occurs when creating or handling a call.
 *
 * @author Malin Nyfeldt
 */
public class CallException extends Exception {

    public CallException(String message) {
        super(message);
    }

    public CallException(String message, Throwable exception) {
        super(message, exception);
    }
}
