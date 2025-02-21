/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MailboxNotFoundException Tester.
 *
 * @author qhast
 */
public class MailboxNotFoundExceptionTest extends TestCase
{
    private MailboxNotFoundException mailboxNotFoundException;

    public MailboxNotFoundExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        mailboxNotFoundException = new MailboxNotFoundException("host","aid");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetHost() throws Exception
    {
        assertEquals("host",mailboxNotFoundException.getHost());
    }

    public void testGetAccountId() throws Exception
    {
        assertEquals("aid",mailboxNotFoundException.getAccountId());        
    }

    public static Test suite()
    {
        return new TestSuite(MailboxNotFoundExceptionTest.class);
    }
}
