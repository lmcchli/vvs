/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComDataException is thrown when there is an error with a particular
 * request. Retrying the same request is useless, but trying with another
 * request mya work.
 ****************************************************************/
public class SMSComDataException extends SMSComException {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComDataException(String s) {
        super(s);
    }
}
