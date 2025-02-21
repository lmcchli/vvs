/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import java.io.Serializable;

/**
 * Base class for all String value message property comparators.
 * Adds support for doing case insensitive compare.
 * @author qhast
 */
public abstract class StringValuePropertyComparator extends MessagePropertyComparator<String> {

    /**
     * Indicates that the compare should be done case insensitive.
     */
    private boolean ignoreCase = false;

    /**
     * Contructs with and sets the descending indicator and the case insensitive indicator.
     * @param descending value of the decending indicator.
     * @param ignoreCase
     */
    StringValuePropertyComparator(boolean descending,boolean ignoreCase) {
        super(descending);
        this.ignoreCase = ignoreCase;
    }


    /**
     * Compares its two arguments for order.
     * If the ignore case indicator is set to true the result of the compare will be ordered case insensitive.
     * If one of the objects is null it will be considered less than the non-null object.
     * If the descending indicator is set to true the result of the compare will be ordered descended.
     * @param s1 the first String object.
     * @param s2 the second String object.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     */
    public int compare(String s1, String s2) {
        if(ignoreCase) {
            return super.compare(s1.toLowerCase(),s2.toLowerCase());
        } else {
            return super.compare(s1,s2);
        }
    }
}
