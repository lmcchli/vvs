package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * BasicLogContext Tester.
 *
 * @author qhast
 */
public class BasicLogContextTest extends TestCase
{
    BasicLogContext logContextA;
    BasicLogContext logContextB;
    BasicLogContext logContextBQ;


    public BasicLogContextTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        logContextA = new BasicLogContext("A");
        logContextB = new BasicLogContext("B.*");
        logContextBQ = new BasicLogContext("B.Q");

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testImplies() throws Exception
    {
        assertTrue("logContextB should imply logContextBQ",logContextB.implies(logContextBQ));
        assertFalse("logContextB should NOT imply logContextA",logContextB.implies(logContextA));
        assertFalse("logContextA should NOT imply logContextB",logContextA.implies(logContextB));
        assertFalse("logContextA should NOT imply logContextBQ",logContextA.implies(logContextBQ));
        assertFalse("logContextBQ should NOT imply logContextB",logContextBQ.implies(logContextB));
        assertFalse("logContextBQ should NOT imply logContextA",logContextBQ.implies(logContextA));
    }

    public static Test suite()
    {
        return new TestSuite(BasicLogContextTest.class);
    }
}
