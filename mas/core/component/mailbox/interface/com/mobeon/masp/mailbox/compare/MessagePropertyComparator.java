/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

/**
 * Base class for all message property comparators
 * Provides base functionality for applying descending order
 * and comparing null objects.
 *
 * @author qhast
 */
public abstract class MessagePropertyComparator<T extends Comparable<T>>{

    /**
     * Indicates if the result of the compare should be ordered descended.
     */
    boolean descending = false;

    /**
     * Contructs with and sets the descending indicator.
     * @param descending value of the decending indicator.
     */
    MessagePropertyComparator(boolean descending) {
        this.descending = descending;
    }

    /**
     * Choose the order according to the descending indicator.
     * @param comparation a calculated comparision result.
     * @return the comparision result after applying the descending indicator.
     */
    int order(int comparation) {
        return descending?-comparation:comparation;
    }

     /**
     * Compares this object against the specified object.
     * The result is true if and only if the argument is not null and is an
     * object of the same class.
     *
     * @param obj
     * @return true if obj is equal to this object.
     */
    public boolean equals(Object obj) {
        return (obj != null && obj.getClass().equals(this.getClass()));
    }

    /**
     * Object hash code.
     * @return hash value
     */
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }


    /**
     * Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     * If one of the objects is null it will be considered less than the non-null object.
     * If both are null they are considered equal.
     * If the descending indicator is set to true the result of the compare will be ordered descended.
     * @param t1 the first object to be compared.
     * @param t2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     */
    public int compare(T t1, T t2) {
        if(t1 == null && t2 == null) {
            return 0;
        } else if(t1 == null) {
            return order(-1);
        } else if(t2 == null) {
            return order(1);
        } else {
            return order(t1.compareTo(t2));
        }

    }

}
