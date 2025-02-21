/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.MockObjectTestCase;

/**
 * HostedServiceLogger Tester.
 *
 * @author qhast
 */
public class HostedServiceAvailabilityLogContextTest extends MockObjectTestCase
{
    HostedServiceLogger.HostedServiceAvailabilityLogContext availableLogContext;

    public HostedServiceAvailabilityLogContextTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        availableLogContext = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:143");
    }

    /**
     * Test that method hashCode not crashes.
     * @throws Exception
     */
    public void testHashCode() throws Exception
    {
        availableLogContext.hashCode();
    }

    /**
     * Tests that two log context objects having the same HostedService and subname are equal.
     * @throws Exception
     */
    public void testEqual() throws Exception
    {
        HostedServiceLogger.HostedServiceAvailabilityLogContext c = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:143");

        assertTrue(c+" should be equal to "+availableLogContext,c.equals(availableLogContext));
        assertTrue(availableLogContext+" should be equal to "+c,availableLogContext.equals(c));

    }

    /**
     * Tests that two log context objects differing on either HostedService or Class are NOT equal.
     * @throws Exception
     */
    public void testNotEqual() throws Exception
    {

        HostedServiceLogger.HostedServiceAvailabilityLogContext diffContext1 = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:188");
        HostedServiceLogger.HostedServiceAvailabilityLogContext diffContext2 = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:143","response");

        assertFalse(diffContext1+" should NOT be equal to "+availableLogContext,diffContext1.equals(availableLogContext));
        assertFalse(availableLogContext+" should NOT be equal to "+diffContext1,availableLogContext.equals(diffContext1));

        assertFalse(diffContext2+" should NOT be equal to "+availableLogContext,diffContext2.equals(availableLogContext));
        assertFalse(availableLogContext+" should NOT be equal to "+diffContext2,availableLogContext.equals(diffContext2));

    }

    /**
     * Tests that one log context object c having the same HostedService and Class as
     * the tested log context object implies each other.
     * Also tests that a subnamed context is implied by this the tested class without subname.
     *
     * @throws Exception
     */
    public void testImplies() throws Exception
    {

        HostedServiceLogger.HostedServiceAvailabilityLogContext c1 = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:143");
        HostedServiceLogger.HostedServiceAvailabilityLogContext responseLogContext = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:143","repsonse");

        assertTrue(c1+" should be implied by "+availableLogContext,c1.implies(availableLogContext));
        assertTrue(availableLogContext+" should be implied by "+c1,availableLogContext.implies(c1));

        assertTrue(responseLogContext+" should be implied by "+availableLogContext,availableLogContext.implies(responseLogContext));

    }


    /**
     * Tests that one log context object c NOT having the same HostedService or Class as
     * the tested log context object NOT implies each other.
     * @throws Exception
     */
    public void testNotImplies() throws Exception
    {
        HostedServiceLogger.HostedServiceAvailabilityLogContext c1 = new HostedServiceLogger.HostedServiceAvailabilityLogContext("imap://bighost:888");
        BasicLogContext c2 = new BasicLogContext("diff");

        assertFalse(availableLogContext+" should NOT be implied by "+c1,c1.implies(availableLogContext));
        assertFalse(c1+" should NOT be implied by "+availableLogContext,availableLogContext.implies(c1));

        assertFalse(c2+" should NOT be implied by "+availableLogContext,availableLogContext.implies(c1));


    }


    public static Test suite()
    {
        return new TestSuite(HostedServiceAvailabilityLogContextTest.class);
    }
}
