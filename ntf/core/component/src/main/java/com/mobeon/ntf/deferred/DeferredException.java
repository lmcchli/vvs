/*
 * DeferredException.java
 *
 * Created on den 13 september 2004, 09:56
 */

package com.mobeon.ntf.deferred;

/**
 * Exception when handling deferred messages.
 * These exception should be used when its the deferred message
 * itself that is the problem, or wnen  there are problems with
 * completing the command. For problems with delaying, use DelayException.
 */
public class DeferredException extends java.lang.Exception {


    /**
     * Constructs an instance of <code>DeferredException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DeferredException(String msg) {
        super(msg);
    }
}
