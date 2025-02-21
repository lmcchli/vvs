/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MailboxException Tester.
 *
 * @author qhast
 */
public class MailboxExceptionTest extends TestCase
{
    private MailboxException mailboxException;
    private MailboxException mailboxExceptionWithCause;
    private Throwable cause;

    public MailboxExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        mailboxException = new MailboxException("x");

        cause = new NumberFormatException();
        mailboxExceptionWithCause = new MailboxException("Not a number",cause);
    }

    public void testGetMessage() throws Exception {
        assertEquals("x",mailboxException.getMessage());
        assertEquals("Not a number : "+cause.getClass().getName(),mailboxExceptionWithCause.getMessage());
    }

    public void testGetCause() throws Exception {
        assertEquals(null,mailboxException.getCause());
        assertEquals(null,mailboxExceptionWithCause.getCause());
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public static Test suite()
    {
        return new TestSuite(MailboxExceptionTest.class);
    }
}
