/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * This event class indicates that a play-operation has finished.
 * 
 * @author Jörgen Terner
 */
public class PlayFinishedEvent extends StreamEvent {
    
    public static enum CAUSE {
        /** The play has finished normally. */
        PLAY_FINISHED, 
        /** stop has been called. */
        PLAY_STOPPED,
        /** cancel has been called. */ 
        PLAY_CANCELLED,
        /** The stream has been deleted. */ 
        STREAM_DELETED,
        /** The stream has been joined. */
        STREAM_JOINED
    };

    private CAUSE mCause;
    
    private long mCursor;
    
    /**
     * Creates a new event instance without message.
     * 
     * @param id     Event identifier. May not be <code>null</code>. This can,
     *               for example, be an identifier for the call that generated 
     *               this event.
     * @param cause  The cause of this event.
     * @param cursor Current cursor in milliseconds.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    /* package */ PlayFinishedEvent(Object id, int cause, long cursor) {
        super(id);
        
        mCause = toCAUSE(cause);
        mCursor = cursor;
    }
    
    /**
     * Maps the parameter to a CAUSE.
     * 
     * @param cause Integer representation of a CAUSE.
     * 
     * @throw IllegalArgumentException If <code>cause</code> could not be
     *        mapped to a CAUSE.
     */
    /* package */static CAUSE toCAUSE(int cause) {
        CAUSE result;
        
        switch (cause) {
        case 0:
            result = CAUSE.PLAY_FINISHED;
            break;
        case 1:
            result = CAUSE.PLAY_STOPPED;
            break;
        case 2:
            result = CAUSE.PLAY_CANCELLED;
            break;
        case 3:
            result = CAUSE.STREAM_DELETED;
            break;
        case 4:
            result = CAUSE.STREAM_JOINED;
            break;
        default:
            throw new IllegalArgumentException("Unknown cause" + cause);
        }
        return result;
    }
    
    /**
     * Creates a new event instance without message. This constructor can
     * be used when a play has finished with cause <code>PLAY_FINISHED</code>,
     * otherwise the constructor taking a <code>cursor</code> as well should be
     * used.
     * 
     * @param id     Event identifier. May not be <code>null</code>. This can,
     *               for example, be an identifier for the call that generated 
     *               this event.
     * @param cause  The cause of this event.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    public PlayFinishedEvent(Object id, CAUSE cause) {
        this(id, cause, 0);
    }

    /**
     * Creates a new event instance without message.
     * 
     * @param id     Event identifier. May not be <code>null</code>. This can,
     *               for example, be an identifier for the call that generated 
     *               this event.
     * @param cause  The cause of this event.
     * @param cursor Current cursor in milliseconds. If the play has finished 
     *               before playing all media, this is the number of 
     *               milliseconds played so far.
     *           
     * @throws IllegalArgumentException If <code>id</code> is 
     *         <code>null</code> or if the cause is unknown.
     */
    public PlayFinishedEvent(Object id, CAUSE cause, long cursor) {
        super(id);
        
        if (cause == null) {
            throw new IllegalArgumentException("cause may not be null.");
        }
        mCause = cause;
        mCursor = cursor;
    }

    public CAUSE getCause() {
        return mCause;
    }
    
    /**
     * @return Cursor in milliseconds.
     */
    public long getCursor() {
        return mCursor;
    }

    /**
     * This instance is equals to <code>obj</code> if <code>obj</code> is not
     * <code>null</code>, is of the same class as this instance (exactly the 
     * same class, not a subclass), have the same id and cause. 
     * The messages may be different.
     * 
     * @param obj Instance to test for equality.
     * 
     * @return <code>true</code> If obj equals this instance.
     */
    public boolean equals(Object obj) {
        //System.out.println("Cause=" + mCause + ", inCause=" + ((PlayFinishedEvent)obj).mCause);
        if ((obj != null) && (obj.getClass().equals(this.getClass())))
        {
            PlayFinishedEvent e = (PlayFinishedEvent)obj;
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
