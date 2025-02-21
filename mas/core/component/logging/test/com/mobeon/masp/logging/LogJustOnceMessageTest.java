/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * LogJustOnceMessage Tester.
 *
 * @author qhast
 */
public class LogJustOnceMessageTest extends TestCase
{
    LogJustOnceMessage m;
    LogJustOnceMessage m2;
    LogJustOnceMessage m3;
    LogJustOnceMessage m4;
    LogJustOnceMessage diff;
    BasicLogContext logContextA;
    BasicLogContext logContextB;

    public LogJustOnceMessageTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        logContextA = new BasicLogContext("A");
        logContextB = new BasicLogContext("B");

        m     = new LogJustOnceMessage(logContextA, "error", false);
        m2    = new LogJustOnceMessage(logContextA, "error", false);
        m3    = new LogJustOnceMessage(logContextA, "error", true);
        m4  = new LogJustOnceMessage(logContextA, "error2",false);
        diff = new LogJustOnceMessage(logContextB,"error", false);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Tests that logContextA is the same as set in constructor.
     * @throws Exception
     */
    public void testGetLogObject() throws Exception
    {
        assertEquals("LogContext should be "+logContextA,logContextA,m.getLogContext());
        assertEquals("LogContext should be "+logContextA,logContextA,m2.getLogContext());
        assertEquals("LogContext should be "+logContextA,logContextA,m3.getLogContext());
        assertEquals("LogContext should be "+logContextA,logContextA,m4.getLogContext());
        assertEquals("LogContext should be "+logContextB,logContextB,diff.getLogContext());
    }

    /**
     * Tests that triggReset is the same as set in constructor.
     * @throws Exception
     */
    public void testGetTriggReset() throws Exception
    {
        assertFalse("Trigg reset should be false",m.getTriggReset());
        assertFalse("Trigg reset should be false",m2.getTriggReset());
        assertTrue("Trigg reset should be true",m3.getTriggReset());
        assertFalse("Trigg reset should be false",m4.getTriggReset());
        assertFalse("Trigg reset should be false",diff.getTriggReset());
    }

    /**
     * Tests that message text is the same as set in constructor.
     * @throws Exception
     */
    public void testGetMessageText() throws Exception
    {
        assertEquals("Message text should be \"error\"","error",m.getMessageText());
        assertEquals("Message text should be \"error\"","error",m2.getMessageText());
        assertEquals("Message text should be \"error\"","error",m3.getMessageText());
        assertEquals("Message text should be \"error2\"","error2",m4.getMessageText());
        assertEquals("Message text should be \"error\"","error",diff.getMessageText());
    }

    /**
     * Tests that toString not returns null.
     * @throws Exception
     */
    public void testToString() throws Exception
    {
        assertNotNull("toString() should not return null!",m.toString());
        assertNotNull("toString() should not return null!",m2.toString());
        assertNotNull("toString() should not return null!",m3.toString());
        assertNotNull("toString() should not return null!",m4.toString());
        assertNotNull("toString() should not return null!",diff.toString());
    }

    /**
     * Tests that messages are equal if they have the same logContextA.
     * @throws Exception
     */
    public void testEqual() throws Exception
    {
        assertTrue(m+" should be equal to "+m2,m.equals(m2));
        assertTrue(m+" should be equal to "+m3,m.equals(m3));
        assertTrue(m+" should be equal to "+m4,m.equals(m4));
    }

    /**
     * Tests that messages are NOT equal if they NOT have the same logContextA.
     * @throws Exception
     */
    public void testNotEqual() throws Exception
    {
        assertFalse(m+" should NOT be equal to "+diff,m.equals(diff));
    }

    /**
     * Tests that hashCode() not crashes.
     * @throws Exception
     */
    public void testHashCode() throws Exception
    {
        m.hashCode();
    }

    public static Test suite()
    {
        return new TestSuite(LogJustOnceMessageTest.class);
    }
}
