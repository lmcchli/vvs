/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.TestCase;

/**
 * RemotePartyAddress Tester.
 *
 * @author Malin Flodin
 */
public class RemotePartyAddressTest extends TestCase
{
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that the equals methods returns true if the two addresses are
     * the same object or if the host and port of the objects are equal.
     * @throws Exception if test case fails.
     */
    public void testEquals() throws Exception {
        RemotePartyAddress address1 = new RemotePartyAddress("host", 1234);
        RemotePartyAddress address2;

        // Verify for two addresses that differ in host name
        address2 = new RemotePartyAddress("hosten", 1234);
        assertFalse(address1.equals(address2));

        // Verify for two addresses that differ in port
        address2 = new RemotePartyAddress("host", 1235);
        assertFalse(address1.equals(address2));

        // Verify for two addresses that are equivalent
        address2 = new RemotePartyAddress("host", 1234);
        assertEquals(address1, address2);

        // Verify for the same object
        assertEquals(address1, address1);

        // Verify for objects of different types
        assertFalse(address1.equals(false));
    }

    /**
     * Verifies that the hash code is equal for two equal addresses.
     * @throws Exception if test case fails.
     */
    public void testHashCode() throws Exception {
        RemotePartyAddress address1 = new RemotePartyAddress("host", 1234);
        int hashCode1 = address1.hashCode();

        RemotePartyAddress address2;
        int hashCode2;

        // Verify for two addresses that differ in host name
        address2 = new RemotePartyAddress("hosten", 1234);
        hashCode2 = address2.hashCode();
        assertFalse(hashCode1 == hashCode2);

        // Verify for two addresses that differ in port
        address2 = new RemotePartyAddress("host", 1235);
        hashCode2 = address2.hashCode();
        assertFalse(hashCode1 == hashCode2);

        // Verify for two addresses that are equivalent
        address2 = new RemotePartyAddress("host", 1234);
        hashCode2 = address2.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

}
