/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

import java.io.*;

/**********************************************************************************
 * This class represent a delay item that is persistent. The different from
 DelayItem is that it has a last arrival time that shall reflect the last time
 it was added into the persistent delay line.
*/
public class PersistentDelayItem extends DelayItem implements java.io.Serializable, java.lang.Comparable {
  
    /* The last arrival time. In seconds*/
    protected int lastArrivalTime;
    
    /**********************************************************************************
     * Create a persistent delay item
     @param o the delayable to delay
    */
    public PersistentDelayItem(Delayable o) {
	super(o);
    }

    public PersistentDelayItem(DelayItem o) {
        super(o.getItem());
        _arrivalTime = o._arrivalTime;
        _outTime = o._outTime;
        _queueNumber = o._queueNumber;
        _reQueue = o._reQueue;
        _onHold = o._onHold;
    }

    /**********************************************************************************
     * Set the last arrival time. No check is done to see that it is greater
     then now.
     @param lastArrivalTime the time to set
    */
    public void setLastArrivalTime(int lastArrivalTime) {
	this.lastArrivalTime = lastArrivalTime;
    }

    /**********************************************************************************
     * Get the last arrival time.
     @return the last arrival time.
    */
    public int getLastArrivalTime() {
	return lastArrivalTime;
    }

    
    public String toString() {
	return "{PersistentDelayItem: "
            + super.toString()
            + " lastArrivalTime=" + lastArrivalTime
            + "}";
    }

    /**********************************************************************************
     * Compares a PersistentDelayItem to another object.
     @param o the objetc to compare to
     @return 0 if the last arrival time is equal, 01 if the last arrival time
     for o is larger then this last arrival time. 1 otherwise.
     @throws ClassCastException if o is not a PersistentDelayItem
    */
    public int compareTo(Object o) throws ClassCastException{
	if(o instanceof PersistentDelayItem) {
	    PersistentDelayItem theItem = (PersistentDelayItem) o;
	    if(lastArrivalTime == theItem.getLastArrivalTime()) {
		return 0;
	    }
	    if(lastArrivalTime < theItem.getLastArrivalTime()) {
		return -1;
	    }
	    return 1;
	}
	throw new ClassCastException(o.getClass().getName() + " should be a " + getClass().getName());
    }
}
