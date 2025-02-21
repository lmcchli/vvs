/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * DeliveryStatus Tester.
 *
 * @author qhast
 */
public class DeliveryStatusTest extends TestCase
{
    public DeliveryStatusTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testValues() throws Exception {
        assertNotNull(DeliveryStatus.PRINT_FAILED);
        assertNotNull(DeliveryStatus.STORE_FAILED);
    }

    public static Test suite()
    {
        return new TestSuite(DeliveryStatusTest.class);
    }
}
