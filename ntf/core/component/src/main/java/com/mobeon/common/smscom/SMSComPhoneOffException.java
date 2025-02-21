/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComPhoneOffException is thrown when the phone is off for a
 * notification. Specifically, it is used as a flag to trigger the
 * <code>waitphoneon</code> action. As such, this is not a proper
 * exception (no errors had occured), but is simply thrown to
 * indicate message delivery status.
 ****************************************************************/
public class SMSComPhoneOffException extends SMSComException {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComPhoneOffException(String s) {
        super(s);
    }
}
