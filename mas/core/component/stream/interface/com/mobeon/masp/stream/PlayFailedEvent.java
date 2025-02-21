/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a play-operation has failed.
 * The included message describes the cause of failure.
 * 
 * @author Jörgen Terner
 */
public class PlayFailedEvent extends StreamEvent {
    
    /* JavaDoc in base class */
    public PlayFailedEvent(Object id, String message) {
        super(id, message);
    }
}
