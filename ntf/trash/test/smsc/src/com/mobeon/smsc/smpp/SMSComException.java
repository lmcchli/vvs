/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp;

/****************************************************************
 * SMSComException is a general exception for errors in communication
 * with the SMS-C.
 ****************************************************************/
public class SMSComException extends Exception {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComException(String s) {
        super(s);
    }
}
