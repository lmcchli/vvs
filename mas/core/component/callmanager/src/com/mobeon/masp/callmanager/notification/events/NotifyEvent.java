/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.notification.events;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * Internal event triggering an outbound notification to be sent.
 * @author Mats Hägg
 */
public class NotifyEvent implements EventObject {

    public String toString() {
        return "NotifyEvent";
    }
}
