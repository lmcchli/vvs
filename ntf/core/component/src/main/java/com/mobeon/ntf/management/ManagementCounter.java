/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */



/**
 * This class counts events of some kind in NTF. The event is identified
 * with a name, e.g. "smsc_3".
 */

package com.mobeon.ntf.management;

public class ManagementCounter {

     /* Counter type*/
     public static final class CounterType {
        public static CounterType SUCCESS = new CounterType(1);
        public static CounterType FAIL = new CounterType(2);
        private int c_type;
        private CounterType(int type) {
            c_type = type;
        }
        public static String toString(ManagementCounter.CounterType type) {
            if (type == ManagementCounter.CounterType.SUCCESS)
                return "1";
            else
                return "2";
        }
        public static ManagementCounter.CounterType getState(int type) {
            if (type == 1)
                return ManagementCounter.CounterType.SUCCESS;
            else
                return ManagementCounter.CounterType.FAIL;
        }
     }

    private int d_counter;
    private String d_name;
    private int c_maxWrapAround;
    private int c_minWrapAround;

    /**
     * Constructor.
     *@param name - the name of the event.
     */
    public ManagementCounter(String name) {
        d_name = name;
        d_counter = 0;
        c_maxWrapAround = 2147483647;
        c_minWrapAround = 0;
    }

    /**
     * Constructor.
     *@param name - the name of the event.
     *@param maxWrap - the maximum limit for wrap around of the counter
     */
    public ManagementCounter(String name, int maxWrap) {
        d_name = name;
        d_counter = 0;
        c_maxWrapAround = maxWrap;
        c_minWrapAround = 0;
    }

    /**
     * Increments the counter.
     */
    public synchronized void incr() {
        ++d_counter;
        if (d_counter > c_maxWrapAround)
            d_counter = c_minWrapAround;
    }

    /**
     * Increments the counter with the given value.
     *@param  value increments the counter
     */
    public synchronized void incr(int value) {
        d_counter+=value;
        if (d_counter > c_maxWrapAround)
            d_counter = c_minWrapAround;
    }

    /**
     * Decrements the counter.
     */
    public synchronized void decr() {
        --d_counter;
        if (d_counter < c_minWrapAround)
            d_counter = c_minWrapAround;
    }

    /**
     * Decrements the counter with the given value.
     *@param  value decrements the counter
     */
    public synchronized void decr( int value ) {
        d_counter-=value;
        if (d_counter < c_minWrapAround)
            d_counter = c_minWrapAround;
    }

    /**
     * Sets the counter to 0.
     */
    public synchronized void reset() {
        d_counter = 0;
    }

    /**
     * Sets the counter to a value.
     *@param  value is assigned to the counter.
     */
    public synchronized void set( int value ) {
        if ( value >= c_minWrapAround && value <= c_maxWrapAround)
            d_counter = value;
    }

    /**
     * Returns the current count.
     *@return the current count.
     */
    public int getCount() {
        return d_counter;
    }

    /**
     * Gets the name of the NTF part.
     *@return the name of the NTF part associated with this management counter.
     */
    public String getName() {
        return d_name;
    }

    /**
     * Makes a printable representation of this management counter.
     *@return a String with this management counter.
     */
    public String toString() {
        return "{ManagementCounter: " + d_name + " is "
            + d_counter
            + "}";
    }
}
