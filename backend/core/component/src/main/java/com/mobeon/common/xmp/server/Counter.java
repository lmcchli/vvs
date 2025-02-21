/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

/**
 * Information about the component, counters etc.
 */
public class Counter {
    /** The counter value */
    private int v;

    /**
     * Constructor that clears the counter.
     */
    public Counter() {
        v = 0;
    }

    /**
     * Constructor that sets an initial value.
     *@param v the initial counter value.
     */
    public Counter(int v) {
        this.v = v;
    }

    /**
     * Increments the counter.
     */
    public synchronized void incr() {
        v++;
    }

    /**
     * Decrements the counter.
     */
    public synchronized void decr() {
        v--;
    }

    /**
     * Increments the counter by a specified amount.
     *@param v how much the counter shall be incremented. Specify a negative
     * number to decrement the counter.
     */
    public synchronized void incr(int v) {
        this.v += v;
    }

    /**
     * Set the counter to a specific value.
     *@param v the value the counter should be set to.
     */
    public synchronized void setValue(int v) {
        this.v = v;
    }

    /**
     * Get the counter value.
     *@return the counter value.
     */
    public int getValue() {
        return v;
    }
}
