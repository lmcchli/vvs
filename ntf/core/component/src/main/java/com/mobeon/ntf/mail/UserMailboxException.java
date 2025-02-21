/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */


package com.mobeon.ntf.mail;

/****************************************************************
 * UserMailboxException is a general exception for errors when accessing the
 * users mailbox for message count, quota check etc.
 ****************************************************************/
public class UserMailboxException extends Exception {
    /****************************************************************
     * Constructor.
     * @param s Message describing the problem.
     */
    public UserMailboxException(String s) {
        super(s);
    }
}
