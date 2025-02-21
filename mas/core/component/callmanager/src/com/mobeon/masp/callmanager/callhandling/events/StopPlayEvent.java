/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

/**
 * A stop event contains all information regarding a request to stop an ongoing
 * play on a call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class StopPlayEvent extends CallCommandEvent {

    private final Object id;

    public StopPlayEvent(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public String toString() {
        return "StopPlayEvent (id = " + id + ")";
    }
}
