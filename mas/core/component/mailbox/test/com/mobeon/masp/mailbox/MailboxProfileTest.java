/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MailboxProfile Tester.
 *
 * @author qhast
 */
public class MailboxProfileTest extends TestCase
{
    private MailboxProfile initializedProfile;
    private MailboxProfile nonInitializedProfile;

    public MailboxProfileTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        nonInitializedProfile = new MailboxProfile();
        initializedProfile = new MailboxProfile("id","pwd","email");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSetAccountId() throws Exception
    {
        assertEquals("id",initializedProfile.getAccountId());

        initializedProfile.setAccountId("nisse");
        assertEquals("nisse",initializedProfile.getAccountId());        

        assertNull(nonInitializedProfile.getAccountId());

        nonInitializedProfile.setAccountId("nisse");
        assertEquals("nisse",nonInitializedProfile.getAccountId());

    }

    public void testGetSetAccountPassword() throws Exception
    {
        assertEquals("pwd",initializedProfile.getAccountPassword());

        initializedProfile.setAccountPassword("nissepwd");
        assertEquals("nissepwd",initializedProfile.getAccountPassword());

        assertNull(nonInitializedProfile.getAccountPassword());

        nonInitializedProfile.setAccountPassword("nissepwd");
        assertEquals("nissepwd",nonInitializedProfile.getAccountPassword());

    }


    public void testGetSetEmailAddress() throws Exception
    {
        assertEquals("email",initializedProfile.getEmailAddress());

        initializedProfile.setEmailAddress("nisse@mobeon.com");
        assertEquals("nisse@mobeon.com",initializedProfile.getEmailAddress());

        assertNull(nonInitializedProfile.getEmailAddress());

        nonInitializedProfile.setEmailAddress("nisse@mobeon.com");
        assertEquals("nisse@mobeon.com",nonInitializedProfile.getEmailAddress());
    }    
   

    public void testToString() throws Exception {
        assertNotNull("toString() should not return null!",initializedProfile.toString());
        assertNotNull("toString() should not return null!",nonInitializedProfile.toString());
    }

    public static Test suite()
    {
        return new TestSuite(MailboxProfileTest.class);
    }
}
