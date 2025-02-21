/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComLoadException is thrown when smscom or the SMS-C is overloaded.
 * The request may succeed if retried at a later time, but the retry should not
 * be immediate.
 ****************************************************************/
public class SMSComLoadException extends SMSComTempException {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComLoadException(String s) {
        super(s);
    }
}
