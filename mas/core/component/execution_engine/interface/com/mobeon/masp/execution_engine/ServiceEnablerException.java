/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

public class ServiceEnablerException extends Exception {

    public ServiceEnablerException(String message) {
        super(message);
    }

    public ServiceEnablerException(String message, Throwable exception) {
        super(message, exception);
    }
}
