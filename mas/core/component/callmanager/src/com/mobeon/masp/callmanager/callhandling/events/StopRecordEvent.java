/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

/**
 * A stop event contains all information regarding a request to stop an ongoing
 * record on a call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class StopRecordEvent extends CallCommandEvent {

    private final Object id;

    public StopRecordEvent(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public String toString() {
        return "StopRecordEvent (id = " + id + ")";
    }
}
