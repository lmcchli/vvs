/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * MessageContentProperties Tester.
 *
 * @author qhast
 */
public class MessageContentPropertiesTest extends TestCase
{

    MessageContentProperties props1a;
    MessageContentProperties props1b;
    MessageContentProperties props1c;
    MessageContentProperties props2;
    MessageContentProperties props3;
    MessageContentProperties props4;
    MessageContentProperties nullProps;

    public MessageContentPropertiesTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        props1a = new MessageContentProperties("a.abc","ABC","en");
        props1b = new MessageContentProperties("a.abc","ABC","en");
        props1c = new MessageContentProperties("a.abc","ABC","en");
        props2  = new MessageContentProperties("ZZZZ.abc","ABC","en");
        props3  = new MessageContentProperties("a.abc","ABCXYZ","en");
        props4  = new MessageContentProperties("a.abc","ABC","fr");
        nullProps = new MessageContentProperties();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetFilename() throws Exception
    {
        assertEquals("Filename should be equal to \"a.abc\"","a.abc",props1a.getFilename());
        assertNull("Filename should be null",nullProps.getFilename());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to setter.
     * @throws Exception
     */
    public void testSetFilename() throws Exception
    {
        props1a.setFilename("qwerty.bat");
        assertEquals("Filename should be equal to \"qwerty.bat\"","qwerty.bat",props1a.getFilename());

        nullProps.setFilename("qwerty.exe");
        assertEquals("Filename should be equal to \"qwerty.exe\"","qwerty.exe",nullProps.getFilename());

    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetDescription() throws Exception
    {
        assertEquals("Description should be equal to \"ABC\"","ABC",props1a.getDescription());
        assertNull("Description should be null",nullProps.getDescription());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to setter.
     * @throws Exception
     */
    public void testSetDescription() throws Exception
    {
        props1a.setDescription("QWERTY");
        assertEquals("Description should be equal to \"QWERTY\"","QWERTY",props1a.getDescription());

        nullProps.setDescription("QWERTY");
        assertEquals("Description should be equal to \"QWERTY\"","QWERTY",nullProps.getDescription());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to constructor.
     * @throws Exception
     */
    public void testGetLanguage() throws Exception
    {
        assertEquals("Language should be equal to \"en\"","en",props1a.getLanguage());
        assertNull("Language should be null",nullProps.getLanguage());
    }

    /**
     * Tests that object getter returns the same value as the value supplied to setter.
     * @throws Exception
     */
    public void testSetLanguage() throws Exception
    {
        props1a.setLanguage("fr");
        assertEquals("Language should be equal to \"fr\"","fr",props1a.getLanguage());

        nullProps.setLanguage("fr");
        assertEquals("Language should be equal to \"fr\"","fr",nullProps.getLanguage());

    }

    /**
     * Tests that hashCode() not crashes.
     * @throws Exception
     */
    public void testHashCode() throws Exception
    {
        props1a.hashCode();
        nullProps.hashCode();
    }

    /**
     * Tests that object with equal property values are equal.
     * @throws Exception
     */
    public void testEqual() throws Exception
    {
        //reflexive
        assertTrue(props1a+" should be equal to "+props1a,props1a.equals(props1a));

        //symmetric
        assertTrue(props1a+" should be equal to "+props1b,props1a.equals(props1b));
        assertTrue(props1b+" should be equal to "+props1a,props1b.equals(props1a));

        //consistent
        assertTrue(props1a+" should still be equal to "+props1b,props1a.equals(props1b));
        assertTrue(props1a+" should still be equal to "+props1b,props1a.equals(props1b));

        //transitive
        assertTrue(props1a+" should be equal to "+props1b, props1a.equals(props1b));
        assertTrue(props1b+" should be equal to "+props1c, props1b.equals(props1c));
        assertTrue(props1a+" should be equal to "+props1c, props1a.equals(props1c));

    }

    /**
     * Tests that objects with different property values are NOT equal.
     * @throws Exception
     */
    public void testNotEqual() throws Exception
    {
        assertFalse(props1a+" should NOT be equal to "+props2,     props1a.equals(props2));
        assertFalse(props2+" should NOT be equal to "+props1a,     props2.equals(props1a));

        assertFalse(props1a+" should NOT be equal to "+props3,     props1a.equals(props3));
        assertFalse(props3+" should NOT be equal to "+props1a,     props3.equals(props1a));

        assertFalse(props1a+" should NOT be equal to "+props4,     props1a.equals(props4));
        assertFalse(props4+" should NOT be equal to "+props1a,     props4.equals(props1a));

        assertFalse(props1a+" should NOT be equal to "+nullProps,  props1a.equals(nullProps));
        assertFalse(nullProps+" should NOT be equal to "+props1a,  nullProps.equals(props1a));
    }

    /**
     * Tests that an object is NOT equal to null.
     * @throws Exception
     */
    public void testNotEqualToNull() throws Exception
    {
        assertFalse(props1a+" should NOT be equal to null",  props1a.equals(null));
    }

    /**
     * Tests thar toString returns a proper String.
     * @throws Exception
     */
    public void testToString() throws Exception
    {
        assertEquals("{filename=a.abc,description=ABC,language=en}",props1a.toString());
        assertEquals("{filename=null,description=null,language=null}",nullProps.toString());
    }

    public static Test suite()
    {
        return new TestSuite(MessageContentPropertiesTest.class);
    }
}
