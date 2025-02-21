package com.mobeon.ntf.event;

/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

import java.util.*;

/**
 * PhoneOnEventListener specifies an interface for clients that want to know
 * when a phone is turned on.
 */
public interface PhoneOnEventListener extends EventListener {
    /**
     * Handles a phone on event.
     *@param e - the event object.
     */
    public void phoneOn(PhoneOnEvent e);
}

