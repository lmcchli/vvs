/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

/**
 * An accept event contains all information regarding accepting an inbound call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class AcceptEvent extends CallCommandEvent {

    public String toString() {
        return "AcceptEvent";
    }
}
