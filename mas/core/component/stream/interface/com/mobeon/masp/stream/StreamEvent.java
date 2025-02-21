/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.Event;

/**
 * Defines common properties of an event generated from streams.
 * 
 * @author Jörgen Terner
 */
public class StreamEvent implements Event {
    /** Identifier for this event. */
    private Object id;
    
    /** Optional message associated with this event. */
    private String message;
    
    /**
     * Creates a new event instance with the giving identifier and no message.
     * 
     * @param id Event identifier. May not be <code>null</code>. This can,
     *           for example, be an identifier for the call that generated 
     *           this event.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code>.
     */
    /* package */ StreamEvent(Object id) {
        this(id, null);
    }
    
    /**
     * Creates a new event instance with the giving identifier.
     * 
     * @param id      Event identifier. May not be <code>null</code>. This can,
     *                for example, be an identifier for the call that generated 
     *                this event.
     * @param message Optional message describing the cause of this event.
     *                May be <code>null</code>.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code>.
     */
    /* package */ StreamEvent(Object id, String message) {
        if (id == null) {
            throw new IllegalArgumentException("Event id may not be null!");
        }
        this.id = id;
        this.message = message;
    }

    /**
     * Gets the identifier for this event. This can, for example, be
     * an identifier for the call that generated this event.
     * 
     * @return The event identifier. Can never be <code>null</code>.
     */
    public Object getId() {
        return this.id;
    }
    
    /**
     * Gets the optional message that describes the cause of this event.
     * 
     * @return Event message, <code>null</code> if no message has been
     *         specified.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * This instance is equals to <code>obj</code> if <code>obj</code> is not
     * <code>null</code>, is of the same class as this instance (exactly the 
     * same class, not a subclass) and have the same id. 
     * The messages may be different.
     * 
     * @param obj Instance to test for equality.
     * 
     * @return <code>true</code> If obj equals this instance.
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass())))
        {
            StreamEvent e = (StreamEvent)obj;
            return e.getId() == getId();
        }
        return false;
    }

    public int hashCode() {
        return getId().hashCode();
    }
}
