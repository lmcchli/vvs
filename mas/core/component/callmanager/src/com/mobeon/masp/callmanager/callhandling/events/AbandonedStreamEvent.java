/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * An abandoned call event occurs when the remote party has abandoned the call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 *
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class AbandonedStreamEvent implements EventObject {

    public String toString() {
        return "AbandonedCallEvent";
    }
}
