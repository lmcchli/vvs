/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * LogContext Tester.
 *
 * @author qhast
 */
public class LogContextTest extends TestCase
{
    MyLogContext contextA;
    MyLogContext contextB1;
    MyLogContext contextB2;

    public LogContextTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        contextA = new MyLogContext("A");
        contextB1 = new MyLogContext("B");
        contextB2 = new MyLogContext("B");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }


    /**
     * Test that hashcode is the same two object that should be equal.
     * @throws Exception
     */
    public void testHashCode() throws Exception
    {
        assertTrue("contextB1 and contextB2 should have the same hashCode",contextB1.hashCode()==contextB2.hashCode());
    }

    /**
     * Tests that two instances with the same name are equal.
     * @throws Exception
     */
    public void testEqual() throws Exception
    {
        assertTrue("contextB1 and contextB2 should be equal",contextB1.equals(contextB2));
        assertTrue("contextB2 and contextB1 should be equal",contextB2.equals(contextB1));
    }

    /**
     * Tests that two instances with the different names NOT are equal.
     * @throws Exception
     */
    public void testNotEqual() throws Exception
    {
        assertFalse("contextA and contextB1 should NOT be equal",contextA.equals(contextB1));
        assertFalse("contextB1 and contextA should NOT be equal",contextB1.equals(contextA));
    }

    /**
     * Tests that toString not returns null.
     * @throws Exception
     */
    public void testToString() throws Exception
    {
        assertNotNull("toString should not return null!",contextA.toString());
        assertNotNull("toString should not return null!",contextB1.toString());
        assertNotNull("toString should not return null!",contextB2.toString());
    }

    /**
     * Tests that constructor has initialized name.
     * @throws Exception
     */
    public void testGetName() throws Exception
    {
        assertEquals("Name should be \"A\"","A",contextA.getName());
        assertEquals("Name should be \"B\"","B",contextB1.getName());
        assertEquals("Name should be \"B\"","B",contextB2.getName());
    }

    public static Test suite()
    {
        return new TestSuite(LogContextTest.class);
    }

    /**
     * Temporay test class.
     */
    private static class MyLogContext extends LogContext {

        private MyLogContext(String name) {
            super(name);
        }

        public boolean implies(LogContext logContext) {
            return false;
        }
    }
}
