/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

/**
 * This class is an event that carries all information regarding a forced
 * close request from a Call Manager operator.
 * <P>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public final class CloseForcedEvent implements EventObject {

    public String toString() {
        return "CloseForcedEvent";
    }
}
