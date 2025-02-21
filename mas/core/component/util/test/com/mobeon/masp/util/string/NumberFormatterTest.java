/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.string;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Locale;

/**
 * NumberFormatter Tester.
 *
 * @author qhast
 */
public class NumberFormatterTest extends TestCase
{
    public NumberFormatterTest(String name)
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

    public void testClassCall() throws Exception {
        new NumberFormatter();
    }

    public void testPlain() throws Exception {

        assertEquals("123",NumberFormatter.PLAIN.format(123));
        assertEquals("1234567890",NumberFormatter.PLAIN.format(1234567890));        
    }

    public static Test suite()
    {
        return new TestSuite(NumberFormatterTest.class);
    }
}
