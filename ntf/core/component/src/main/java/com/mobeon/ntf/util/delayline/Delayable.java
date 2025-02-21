/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

/**
 * Specification of objects that can be delayed.
 *
 */
public interface Delayable extends java.io.Serializable {

    /**
     *@return the key of the delayble
     */
    public Object getKey();

    /**
     *@return the transaction id for this delayble
     */
    public int getTransactionId();
}
