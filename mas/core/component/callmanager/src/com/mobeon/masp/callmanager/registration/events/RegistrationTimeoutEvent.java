/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.events;

import com.mobeon.masp.callmanager.registration.RegistrationTimerTask;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * TODO: Drop 4!
 *
 * @author Malin Flodin
 */
public class RegistrationTimeoutEvent implements EventObject {

    private final RegistrationTimerTask.Type type;

    public RegistrationTimeoutEvent(RegistrationTimerTask.Type type) {
        this.type = type;
    }

    public RegistrationTimerTask.Type getType() {
        return type;
    }

    public String toString() {
        return "RegistrationTimeoutEvent (type = " + type + ")";
    }

}
