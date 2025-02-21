/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a record-operation has finished.
 * 
 * @author Jörgen Terner
 * 
 * @see RecordingProperties
 * @see StreamConfiguration
 */
public class RecordFinishedEvent extends StreamEvent {

    public static enum CAUSE {
        /** Max recording duration reached. */ 
        MAX_RECORDING_DURATION_REACHED, 
        /** stop has been called. */ 
        RECORDING_STOPPED, 
        /** Maximum silence duration reached. NOT IMPLEMENTED YET! */ 
        MAX_SILENCE_DURATION_REACHED, 
        /** Silence was detected. NOT IMPLEMENTED YET! */ 
        SILENCE_DETECTED, 
        /** The stream has been deleted. */ 
        STREAM_DELETED, 
        /** An "abandoned stream" timeout has been reached. */
        STREAM_ABANDONED
    };

    private CAUSE mCause;
    
    /**
     * Creates a new event instance without message.
     * 
     * @param id    Event identifier. May not be <code>null</code>. This can,
     *              for example, be an identifier for the call that generated 
     *              this event.
     * @param cause The cause of this event.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    /* package */ RecordFinishedEvent(Object id, int cause) {
        super(id);
        
        switch (cause) {
        case 0:
            mCause = CAUSE.MAX_RECORDING_DURATION_REACHED;
            break;
        case 1:
            mCause = CAUSE.RECORDING_STOPPED;
            break;
        case 2:
            mCause = CAUSE.MAX_SILENCE_DURATION_REACHED;
            break;
        case 3:
            mCause = CAUSE.SILENCE_DETECTED;
            break;
        case 4:
            mCause = CAUSE.STREAM_DELETED;
            break;
        case 5:
            mCause = CAUSE.STREAM_ABANDONED;
            break;
        default:
            throw new IllegalArgumentException("Unknown cause" + cause);
        }
    }
    
    /**
     * Creates a new event instance without message.
     * 
     * @param id    Event identifier. May not be <code>null</code>. This can,
     *              for example, be an identifier for the call that generated 
     *              this event.
     * @param cause The cause of this event.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    public RecordFinishedEvent(Object id, CAUSE cause) {
        super(id);
        
        if (cause == null) {
            throw new IllegalArgumentException("cause may not be null.");
        }
        mCause = cause;
    }

    public CAUSE getCause() {
        return mCause;
    }
    
    public boolean equals(Object obj) {       
        //System.out.println("E=" + ((RecordFinishedEvent)obj).getId() + ", CAUSE=" + ((RecordFinishedEvent)obj).mCause);
        //System.out.println("This=" + getId() + ", CAUSE=" + mCause);
        if ((obj != null) && (obj.getClass().equals(this.getClass())))
        {
            RecordFinishedEvent e = (RecordFinishedEvent)obj;
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
