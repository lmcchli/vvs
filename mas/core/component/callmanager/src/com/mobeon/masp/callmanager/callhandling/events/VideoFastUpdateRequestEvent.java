/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.callmanager.events.EventObject;

/**
 * A Video Fast Update request event is an indication that a Video Fast Update
 * request should be sent over SIP.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class VideoFastUpdateRequestEvent implements EventObject {

    public String toString() {
        return "VideoFastUpdateRequestEvent";
    }
}
