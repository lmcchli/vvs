/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComConfigException is thrown when the SMSCom can not work
 * because of some error in its configuration. It indicates a serious error that
 * will remain until the configuration has been corrected, so it is useless to
 * retry the operation.
 ****************************************************************/
public class SMSComConfigException extends SMSComException {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComConfigException(String s) {
        super(s);
    }
}
