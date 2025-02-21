/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * SpokenNameNotFoundException Tester.
 *
 * @author qhast
 */
public class SpokenNameNotFoundExceptionTest extends TestCase
{
    public SpokenNameNotFoundExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();        
    }

    public void testConstruct() throws Exception {
        SpokenNameNotFoundException e = new SpokenNameNotFoundException();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public static Test suite()
    {
        return new TestSuite(SpokenNameNotFoundExceptionTest.class);
    }
}
