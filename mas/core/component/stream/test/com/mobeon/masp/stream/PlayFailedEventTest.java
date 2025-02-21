/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import junit.framework.TestCase;

/**
 * Testclass for class of {@link PlayFailedEvent}.
 * 
 * @author Jörgen Terner
 */
public class PlayFailedEventTest extends TestCase {
    
    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public PlayFailedEventTest(String name)
    {
       super(name);
    }
    
    /* JavaDoc in base class. */
    public void setUp() {
    }

    /* JavaDoc in base class. */
    public void tearDown() {
    }
    
    /**
     * <pre>
     * Constructor test.
     * 
     * Wrong arguments
     * ----------------------------
     * Action: Call constructor with id = null and message = null.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * ----------------------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    message = null, "", "foo"
     * Result: No exceptions are thrown.
     *         getMessage returns the given message.
     *         getId returns the given id.
     * </pre>
     */
    public void testConstructor() {
        // Wrong arguments
        try {
            new PlayFailedEvent(null, null);
            fail("Wrong arguments should have caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException(
                    "Wrong arguments: Unexpected exception",
                    IllegalArgumentException.class, e);
        }

        // Correct arguments
        Object[] ids = new Object[] {
                new Object()
        };
        String[] messages = new String[] {
                null, "", "foo"
        };
        for (int idIndex = 0; idIndex < ids.length; idIndex++) {
            for (int messIndex = 0; messIndex < messages.length; messIndex++)
            {
                try {
                    PlayFailedEvent event = 
                        new PlayFailedEvent(ids[idIndex], messages[messIndex]);
                    assertEquals("Correct arguments (" + idIndex + ", " + 
                            messIndex + "): unexpected message value.",
                            messages[messIndex], event.getMessage());
                    assertSame("Correct arguments (" + idIndex + ", " + 
                            messIndex + "): unexpected id value.",
                            ids[idIndex], event.getId());
                }
                catch (Exception e) {
                    fail("Correct arguments (" + idIndex + ", " + messIndex +
                            "): Unexpected exception: " + e);
                }
            }
        }
    }

    /**
     * Tests for equals(Object obj).
     * 
     * Common cases
     * ------------
     * Description: Control that two equal object gives true and two different
     *              objects give false. 
     * Condition: An array with three objects: 
     *     o1 = new PlayFailedEvent(new Object(), null);
     *     o2 = new PlayFailedEvent(new Object(), "");
     *     o3 = new PlayFailedEvent(new Object(), "Foo");
     * Action: Call equals for all combinations.
     * Result: true for same index, false for different index. 
     * 
     * Compare to self
     * ---------------
     * Description: Control that equals to self gives true. 
     * Condition: obj = new PlayFailedEvent(new Object(), null)
     * Result: obj.equals(obj) = true 
     * 
     * Compare to null
     * ---------------
     * Description: Control that comparison to null gives false. 
     * Condition: obj = new PlayFailedEvent(new Object(), null)
     * Result:  obj.equals(null) = false 
     * 
     * Wrong type
     * ----------
     * Description: Control that comparison with different type gives false.
     * 
     * Condition: obj = new PlayFailedEvent(obj1, null)
     * Action:
     *    1. obj.equals(new Integer(0)).
     *    2. obj.equals(new PlayFailedEvent(obj1, null) {}) (subclass)
     * Resultat: false 
     */
    public void testEquals()
    {
        // Common cases
        PlayFailedEvent[] objList = new PlayFailedEvent[] {
                new PlayFailedEvent(new Object(), null),
                new PlayFailedEvent(new Object(), ""),
                new PlayFailedEvent(new Object(), "Foo")
        };
        for (int i = 0; i < objList.length; i++)
        {
            for (int j = 0; j < objList.length; j++)
            {
                if (i == j) 
                {
                    assertTrue("Equals, " + i + " and " + j,
                            objList[i].equals(objList[j]));
                }
                else 
                {
                    assertFalse("Different, " + i + " and " + j,
                            objList[i].equals(objList[j]));
                }
            }
        }
        
        // Compare to self
        PlayFailedEvent event = new PlayFailedEvent(new Object(), null);
        assertTrue("Compare to self", event.equals(event));
        
        // Compare to null
        assertFalse("Compare to null", event.equals(null));
        
        // Wrong type 1
        Object id = new Object();
        event = new PlayFailedEvent(id, null);
        assertFalse("Wrong type 1", event.equals(new Integer(0)));
        // Wrong type 2
        assertFalse("Wrong type 2", 
                event.equals(new PlayFailedEvent(id, null){}));
    }
    
    /**
     * Tests for hashCode().
     * 
     * Common cases
     * ------------
     * Description: Controls that two object that are equal gives the same
     *              hashcode. Also controls that two objects that are not
     *              equals gives different hashcode (not required by the spec
     *              but is preferrable). 
     * Condition: An array with the following objects: 
     *    1. o1 = new PlayFailedEvent(new Object(), null)
     *    2. o2 = new PlayFailedEvent(new Object(), "")
     *    3. o3 = new PlayFailedEvent(obj1, null)
     *    4. o4 = new PlayFailedEvent(obj1, "") 
     *
     * Action: Test equals on all combinations.
     * Result: If equal, hashCode should be equal for both objects.
     *         If not equal, hashCode should give different values.
     */
    public void testHashCode()
    {
        // Common cases
        Object obj1 = new Object();
        PlayFailedEvent[] objList = new PlayFailedEvent[] {
                new PlayFailedEvent(new Object(), null),
                new PlayFailedEvent(new Object(), ""),
                new PlayFailedEvent(obj1, null),
                new PlayFailedEvent(obj1, "")
        };
        for (int i = 0; i < objList.length; i++) {
            for (int j = 0; j < objList.length; j++) {
                if (objList[i].equals(objList[j])) {
                    assertEquals("Equals, " + i + " and " + j,
                        objList[i].hashCode(), objList[j].hashCode());
                }
                else {
                    assertTrue("Different, " + i + " and " + j,
                        objList[i].hashCode() != objList[j].hashCode());
                }
            }
        }
    }
}