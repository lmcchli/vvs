/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a control token has arrived on a stream.
 * 
 * @author Jörgen Terner
 */
public class ControlTokenEvent extends StreamEvent {
    /**
     * Creates a new event instance without message.
     * 
     * @param token The new token.
     *           
     * @throws IllegalArgumentException If <code>token</code> is 
     *         <code>null</code>.
     */
    public ControlTokenEvent(ControlToken token) {
        super(token);
    }
    
    /**
     * @return The token.
     */
    public ControlToken getToken() {
        return (ControlToken)getId();
    }
}
