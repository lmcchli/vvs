/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a record-operation has failed.
 * The included message describes the cause of failure.
 * 
 * @author Jörgen Terner
 */
public class RecordFailedEvent extends StreamEvent {
    
    public static enum CAUSE {
        /** An exception has occured. */ 
        EXCEPTION, 
        /** Min recording duration was not reached. */ 
        MIN_RECORDING_DURATION
    };

    private CAUSE mCause;
    
    /**
     * Creates a new event instance with a cause and a message.
     * 
     * @param id      Event identifier. May not be <code>null</code>. This can,
     *                for example, be an identifier for the call that generated
     *                this event.
     * @param cause   The cause of this event.
     * @param message Optional message describing the cause of this event.
     *                May be <code>null</code>.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    /* package */ RecordFailedEvent(Object id, int cause, String message) {
        super(id, message);
        
        switch (cause) {
        case 0:
            mCause = CAUSE.EXCEPTION;
            break;
        case 1:
            mCause = CAUSE.MIN_RECORDING_DURATION;
            break;
        default:
            throw new IllegalArgumentException("Unknown cause" + cause);
        }
    }

    /**
     * Creates a new event instance without message.
     * 
     * @param id      Event identifier. May not be <code>null</code>. This can,
     *                for example, be an identifier for the call that generated 
     *                this event.
     * @param cause   The cause of this event.
     * @param message Optional message describing the cause of this event.
     *                May be <code>null</code>.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    public RecordFailedEvent(Object id, CAUSE cause, String message) {
        super(id, message);
        
        if (cause == null) {
            throw new IllegalArgumentException("cause may not be null.");
        }
        mCause = cause;
    }

    public CAUSE getCause() {
        return mCause;
    }
    
    public boolean equals(Object obj) {       
        //System.out.println("Expected: " + mCause + ", Actual: " + ((RecordFailedEvent)obj).mCause);
        if ((obj != null) && (obj.getClass().equals(this.getClass())))
        {
            RecordFailedEvent e = (RecordFailedEvent)obj;
            return (e.getId() == getId()) && (e.mCause == mCause);
        }
        return false;
    }

    public int hashCode() {
        // Mimic a common hashCode-implementation for lists by
        // multiplying the first contributor to the hashCode by a
        // prime value to get better distribution.
        return 31*mCause.hashCode() + getId().hashCode();
    }
}
