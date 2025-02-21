/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender.email;

import java.util.HashMap;

/**
 * This is an interface to be implemented by the event classes that should be sent as email.
 *
 * @author ermmaha
 */
public interface EmailEvent {

    /**
     * Retrieves the name on the event.
     *
     * @return the name
     */
    public String getName();

    /**
     * Retrieves the properties in the event
     *
     * @return map with properties
     */
    public HashMap<String, String> getProperties();
}
