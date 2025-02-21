/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

/****************************************************************
 * SMSComRebindException is thrown when there is a bind error that
 * was recovered successfully, the message my be resent immediately by
 * the higher layers.
 ****************************************************************/
public class SMSComReBindException extends SMSComException {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public SMSComReBindException(String s) {
        super(s);
    }
}
