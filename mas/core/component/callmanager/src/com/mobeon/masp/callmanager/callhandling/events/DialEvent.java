/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

/**
 * This event contains all information regarding initiating the call setup of
 * an outbound call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class DialEvent extends CallCommandEvent {

    public String toString() {
        return "DialEvent";
    }
}
