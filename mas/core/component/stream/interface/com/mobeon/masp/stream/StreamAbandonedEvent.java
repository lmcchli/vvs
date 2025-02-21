/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a stream has been considered as abandoned.
 * 
 * @author Jörgen Terner
 */
public class StreamAbandonedEvent extends StreamEvent {
    /**
     * Creates a new event instance without message.
     * 
     * @param stream The stream that detected that is should be considered as
     *               abandoned.
     *           
     * @throws IllegalArgumentException If <code>stream</code> is 
     *         <code>null</code>.
     */
    public StreamAbandonedEvent(IMediaStream stream) {
        super(stream);
    }
    
    /**
     * @return The stream instance that detected that is should be considered
     *         as abandoned.
     */
    public IMediaStream getStream() {
        return (IMediaStream)getId();
    }
}
