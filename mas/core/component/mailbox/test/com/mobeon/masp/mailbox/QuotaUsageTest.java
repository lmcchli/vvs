/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * QuotaUsage Tester.
 *
 * @author qhast
 */
public class QuotaUsageTest extends TestCase
{
    private QuotaUsage nonInitializedTotalUsage = new QuotaUsage(QuotaName.TOTAL);
    private QuotaUsage initializedTotalUsage = new QuotaUsage(QuotaName.TOTAL);

    public QuotaUsageTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        nonInitializedTotalUsage = new QuotaUsage(QuotaName.TOTAL);
        initializedTotalUsage = new QuotaUsage(QuotaName.TOTAL,1,1);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetName() throws Exception
    {
        assertEquals(QuotaName.TOTAL,nonInitializedTotalUsage.getName());
        assertEquals(QuotaName.TOTAL,initializedTotalUsage.getName());
    }

    public void testGetMessageUsage() throws Exception
    {
        assertEquals(-1,nonInitializedTotalUsage.getMessageUsage());
        assertEquals(1,initializedTotalUsage.getMessageUsage());
    }

    public void testGetByteUsage() throws Exception
    {
        assertEquals(-1,nonInitializedTotalUsage.getByteUsage());
        assertEquals(1,initializedTotalUsage.getByteUsage());
    }

    public void testHashCode() throws Exception
    {
        nonInitializedTotalUsage.hashCode();
        initializedTotalUsage.hashCode();
    }

    public void testEquals() throws Exception
    {
        assertTrue(nonInitializedTotalUsage.equals(initializedTotalUsage));
    }

    public void testNotEquals() throws Exception
    {
        assertFalse(nonInitializedTotalUsage.equals(new Object()));
    }

    public void testToString() throws Exception
    {
        assertNotNull(nonInitializedTotalUsage.toString());
        assertNotNull(initializedTotalUsage.toString());
    }

    public static Test suite()
    {
        return new TestSuite(QuotaUsageTest.class);
    }
}
