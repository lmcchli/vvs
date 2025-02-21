/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * InvalidMessageException Tester.
 *
 * @author qhast
 */
public class InvalidMessageExceptionTest extends TestCase
{
    private InvalidMessageException invalidMessageException;

    public InvalidMessageExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        invalidMessageException = new InvalidMessageException();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAddInvalidPropertyValue() throws Exception
    {
        assertEquals(0,invalidMessageException.getInvalidProperties().size());
        invalidMessageException.addInvalidPropertyValue("x","y");
        assertEquals(1,invalidMessageException.getInvalidProperties().size());
        assertEquals("y",invalidMessageException.getInvalidProperties().get("x"));
    }

    public void testAddInvalidPropertyValueWithIllegalArgument() throws Exception
    {
        try {
            invalidMessageException.addInvalidPropertyValue(null,"y");
            fail("Calling addInvalidPropertyValue with propertyName argument equals to null should throw an Exception");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            invalidMessageException.addInvalidPropertyValue("","y");
            fail("Calling addInvalidPropertyValue with propertyName argument equals to empty string should throw an Exception");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    public void testGetInvalidProperties() throws Exception
    {
        assertEquals(0,invalidMessageException.getInvalidProperties().size());
    }

    public void testGetMessage() throws Exception
    {
        assertNotNull(invalidMessageException.getMessage());
    }

    public static Test suite()
    {
        return new TestSuite(InvalidMessageExceptionTest.class);
    }
}
