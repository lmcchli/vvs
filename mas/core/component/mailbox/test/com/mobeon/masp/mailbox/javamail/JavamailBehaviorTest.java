/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * JavamailBehavior Tester.
 *
 * @author qhast
 */
public class JavamailBehaviorTest extends TestCase
{
    private JavamailBehavior behavior;

    public JavamailBehaviorTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        behavior = new JavamailBehavior();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSetCloseNonSelectedFolders() throws Exception
    {
        assertEquals(true,behavior.getCloseNonSelectedFolders());
        behavior.setCloseNonSelectedFolders(false);
        assertEquals(false,behavior.getCloseNonSelectedFolders());
        behavior.setCloseNonSelectedFolders(true);
        assertEquals(true,behavior.getCloseNonSelectedFolders());

    }


    public static Test suite()
    {
        return new TestSuite(JavamailBehaviorTest.class);
    }
}
