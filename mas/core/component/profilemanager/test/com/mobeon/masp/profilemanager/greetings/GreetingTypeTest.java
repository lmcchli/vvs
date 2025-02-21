/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * GreetingType Tester.
 *
 * @author mande
 * @since <pre>01/17/2006</pre>
 * @version 1.0
 */
public class GreetingTypeTest extends TestCase
{
    public GreetingTypeTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testValueOf() throws Exception {
        assertSame(GreetingType.ALL_CALLS, GreetingType.getValueOf("allcalls"));
        assertSame(GreetingType.BUSY, GreetingType.getValueOf("busy"));
        assertSame(GreetingType.CDG, GreetingType.getValueOf("cdg"));
        assertSame(GreetingType.EXTENDED_ABSENCE, GreetingType.getValueOf("extended_absence"));
        assertSame(GreetingType.NO_ANSWER, GreetingType.getValueOf("noanswer"));
        assertSame(GreetingType.OUT_OF_HOURS, GreetingType.getValueOf("outofhours"));
        assertSame(GreetingType.OWN_RECORDED, GreetingType.getValueOf("ownrecorded"));
        assertSame(GreetingType.TEMPORARY, GreetingType.getValueOf("temporary"));
        try {
            GreetingType.getValueOf("unknownvalue");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public static Test suite() {
        return new TestSuite(GreetingTypeTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
