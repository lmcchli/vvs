/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

/**
 * The base class for all exceptions thrown by Message Sender classes
 *
 * @author qhast
 */
public class InternetMailSenderException extends Exception {

    public InternetMailSenderException(String message) {
        super(message);
    }

    public InternetMailSenderException(String message, Throwable cause) {
        super(message + " : " + cause.getClass().getName() + (cause.getMessage() != null ? ": " + cause.getMessage() : ""));
    }
}
