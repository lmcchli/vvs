package com.mobeon.common.sms;

/**
 * Used to communicate errors in the SMSUnit that need to be handled by the caller.
 */
public class SMSUnitException extends Exception {

    public SMSUnitException(String msg) {
        super(msg);
    }
}
