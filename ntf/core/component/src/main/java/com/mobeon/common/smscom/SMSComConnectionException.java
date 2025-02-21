/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/**
 * SMSComConnectionException is thrown when the SMSCom connection fails, i.e.
 * <UL>
 * <LI>The connection can not be established.
 * <LI>Login to the SMSC fails.
 * <LI>The SMSC disconnects.
 * <LI>A logout operation initiated by the SMSC has successfully completed.
 * </UL>
 * This means that the request should be retried, on the backup SMSC if there is
 * any, otherwise at a later time.
 */
public class SMSComConnectionException extends SMSComTempException {
    /**
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComConnectionException(String s) {
        super(s);
    }
}
