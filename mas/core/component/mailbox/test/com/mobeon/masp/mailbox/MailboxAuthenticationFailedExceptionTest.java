/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MailboxAuthenticationFailedException Tester.
 *
 * @author qhast
 */
public class MailboxAuthenticationFailedExceptionTest extends TestCase
{

    private MailboxAuthenticationFailedException mailboxAuthenticationFailedException;

    public MailboxAuthenticationFailedExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        mailboxAuthenticationFailedException = new MailboxAuthenticationFailedException("host","aid");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetHost() throws Exception
    {
        assertEquals("host",mailboxAuthenticationFailedException.getHost());
    }

    public void testGetAccountId() throws Exception
    {
        assertEquals("aid",mailboxAuthenticationFailedException.getAccountId());        
    }

    public static Test suite()
    {
        return new TestSuite(MailboxAuthenticationFailedExceptionTest.class);
    }
}
