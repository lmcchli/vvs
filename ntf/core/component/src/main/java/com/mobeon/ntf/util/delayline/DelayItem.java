 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

import java.io.*;

/**
 * The DelayItem is a class that can be delayed in a Delayer. It is a wrapper
 * around a Delayable, adding data used for the delay functions.
 */
public class DelayItem implements java.io.Serializable {
    /** DelayItem is a wrapper around this Object, so it can be delayed. */
    protected Delayable _payload;
    /** Time in seconds since epoch when this DelayItem arrived to the DelayLine. */
    protected int _arrivalTime;
    /** Time in seconds since epoch when this DelayItem will pop out of its current Delayer. */
    transient protected int _outTime;
    /** A DelayItem may be delayed in one of several Delayers. queueNumber
        serves to identify the Delayer that is the current home of a DelayItem. */
    protected byte _queueNumber;
    /** If a new notification arrives to a subscriber, information
        (i.e. payload) in an old notification to the same subscriber will be
        updated. The sequence of queues traversed by the notification must also
        be modified, so the new notification does not get too few outdial
        attempts. reQueue is true when the notification shall go to the first
        Delayer instead of the next one, when it pops out of its current
        Delayer.*/
    protected boolean _reQueue;
    /** true if the object shall not yet be delayed */
    protected boolean _onHold;

    /**
     *@param o - the Object that is carried inside the DelayItem wrapper
     * when it is delayed.
     *@param hold - tells if the item is being delayed or is on hold, waiting
     * to start.
     */
    public DelayItem(Delayable o, boolean hold) {
        _payload = o;
        _reQueue = false;
        _onHold = hold;
    }

    /**
     *@param o - the Object that is carried inside the DelayItem wrapper
     * when it is delayed.
     */
    public DelayItem(Delayable o) {
        this(o, false);
    }


    /**
     * Set the id of the current Delayer
     *@param queue the id of a Delayer
     */
    public void setQueueNumber(byte queue) {
        _queueNumber = queue;
    }

    /**
     *@return the id of the current Delayer
     */
    public byte getQueueNumber() {
        return _queueNumber;
    }

    /**
     * Calling cancel is logically equivalent to removing the DelayItem from the
     * Delayer it is in, but without searching for it. The payload will be
     * removed (i.e. set to null), and when the DelayItem pops out at a later
     * time, it will be removed.
     */
    public void cancel() {
        _payload = null;
    }

    /**
     *@return false iff this DelayItem is cancelled.
     */
    public boolean isCancelled() {
        return (_payload == null);
    }

    /**
     *@param r - true iff this DelayItem shall start from the first queue the
     * next time it pops out of a Delayer
     */
    public void setReQueue(boolean r) {
        _reQueue = r;
    }

    public void setOnHold(boolean onHold) {
        _onHold = onHold;
    }

    /**
     * isOnHold tells if the Delayable is on hold or not.
     *@return true if the Delayable is on hold.
     */
    public boolean isOnHold() {
        return _onHold;
    }

    /**
     *@return the payload of this DelayItem
     */
    public Delayable getItem() {
        return _payload;
    }

    /** return the transaction id of this DelayItem*/
    public int getItemTransactionId(){
        return _payload.getTransactionId();
    }

    /**
     * Set the time this DelayItem will exit its current Delayer
     *@param t time in seconds since epoch when the next delay expires
     */
    public void setOutTime(int t) {
        _outTime = t;
    }
    /**
     * Get the time this DelayItem will exit its current Delayer
     *@return time in seconds since epoch when the next delay expires.
     */
    public int getOutTime() {
        return _outTime;
    }

    /**
     * Set the time this DelayItem arrived
     *@param t time in seconds since epoch when the DelayItem arrived
     */
    public void setArrivalTime(int t) {
        _arrivalTime = t;
    }

    /**
     * Get the time this DelayItem arrived
     *@return time in seconds since epoch when the DelayItem arrived.
     */
    public int getArrivalTime() {
        return _arrivalTime;
    }

    /**
     */
    public String toString() {
        return "{DelayItem:"
            + " arrivalTime=" + _arrivalTime
            + " outTime=" + _outTime
            + " queueNumber=" + _queueNumber
            + (_reQueue ? " reQueue" : "")
            + (_onHold ? " onHold" : "")
            + " payload=" + _payload
            + "}";
    }
}

