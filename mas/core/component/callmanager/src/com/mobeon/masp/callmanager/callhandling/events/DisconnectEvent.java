/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;


/**
 * A disconnect event contains all information regarding a disconnect.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class DisconnectEvent extends CallCommandEvent {

    public String toString() {
        return "DisconnectEvent";
    }
}
