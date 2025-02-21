/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComTempException is thrown when there is a temporary error that stops
 * SMSSCom from fulfilling a request. The same request may succeed if retried at
 * a later time.
 ****************************************************************/
public class SMSComTempException extends SMSComException {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComTempException(String s) {
        super(s);
    }
}
