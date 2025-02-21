/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * Exception thrown when a specified greeting could not be found for a subscriber
 */
public class GreetingNotFoundException extends ProfileManagerException {
    public GreetingNotFoundException(String message) {
        super(message);
    }

    public GreetingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GreetingNotFoundException(Throwable cause) {
        super(cause);
    }

    public GreetingNotFoundException(GreetingSpecification specification) {
        super(specification.getType() + "(" + specification.getFormat() + ")" +
                ((specification.getSubId() == null) ? "" : "[" + specification.getSubId() + "]"));
    }
}
