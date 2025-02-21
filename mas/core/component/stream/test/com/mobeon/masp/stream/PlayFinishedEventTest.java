/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import junit.framework.TestCase;

/**
 * Testclass for class of {@link PlayFinishedEvent}.
 * 
 * @author Jörgen Terner
 */
public class PlayFinishedEventTest extends TestCase {
    
    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public PlayFinishedEventTest(String name)
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
     * Test for constructor PlayFinishedEvent(Object, int).
     * 
     * Wrong arguments
     * ---------------
     * Action: Call constructor with:
     *    1. id = null, cause = 0.
     *    1. id = new Object(), cause = -1.
     *    1. id = new Object(), cause = 5.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = 0 - 4
     * Result: No exceptions are thrown.
     *         getMessage returns null.
     *         getCause() != null.
     *         getId returns the given id.
     * </pre>
     */
    public void testConstructorObjectInt() {
        // Wrong arguments
        // It is the combination of id and cause that is wrong.
        Object[] wrongIds = new Object[] {
                null, new Object(), new Object()
        };
        int[] wrongCause = new int[] {
                0, -1, 5
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new PlayFinishedEvent(wrongIds[i], wrongCause[i], 0);
                fail("Wrong arguments " + i + 
                " should have caused an exception.");
            }
            catch (Exception e) {
                JUnitUtil.assertException(
                        "Wrong arguments " + i + ": Unexpected exception",
                        IllegalArgumentException.class, e);
            }
        }
        
        // Correct arguments
        Object[] correctIds = new Object[] {
                new Object()
        };
        int[] correctCauses = new int[] {
                0, 1, 2, 3, 4
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
            causeIndex++)
            {
                try {
                    PlayFinishedEvent event = 
                        new PlayFinishedEvent(correctIds[idIndex], 
                                correctCauses[causeIndex], 0);
                    assertNull("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected message value.",
                            event.getMessage());
                    assertSame("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected id value.",
                            correctIds[idIndex], event.getId());
                    assertNotNull("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected cause value.",
                            event.getCause());
                }
                catch (Exception e) {
                    fail("Correct arguments (" + idIndex + ", " + causeIndex +
                            "): Unexpected exception: " + e);
                }
            }
        }
    }
    
    /**
     * <pre>
     * Test for constructor PlayFinishedEvent(Object, CAUSE).
     * 
     * Wrong arguments
     * ---------------
     * Action: Call constructor with:
     *    1. id = null, cause = PLAY_FINISHED.
     *    1. id = new Object(), cause = null.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = PLAY_FINISHED, PLAY_STOPPED, PLAY_CANCELLED, STREAM_DELETED,
     *            STREAM_JOINED
     * Result: No exceptions are thrown.
     *         getMessage returns null.
     *         getCause() returns the given cause.
     *         getId returns the given id.
     * </pre>
     */
    public void testConstructorObjectCAUSE() {
        // Wrong arguments
        // It is the combination of id and cause that is wrong.
        Object[] wrongIds = new Object[] {
                null, new Object()
        };
        PlayFinishedEvent.CAUSE[] wrongCause = new PlayFinishedEvent.CAUSE[] {
                PlayFinishedEvent.CAUSE.PLAY_FINISHED, null
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new PlayFinishedEvent(wrongIds[i], wrongCause[i], 0);
                fail("Wrong arguments " + i + 
                " should have caused an exception.");
            }
            catch (Exception e) {
                JUnitUtil.assertException(
                        "Wrong arguments " + i + ": Unexpected exception",
                        IllegalArgumentException.class, e);
            }
        }
        
        // Correct arguments
        Object[] correctIds = new Object[] {
                new Object()
        };
        PlayFinishedEvent.CAUSE[] correctCauses = 
            new PlayFinishedEvent.CAUSE[] {
                PlayFinishedEvent.CAUSE.PLAY_FINISHED,
                PlayFinishedEvent.CAUSE.PLAY_STOPPED,
                PlayFinishedEvent.CAUSE.PLAY_CANCELLED,
                PlayFinishedEvent.CAUSE.STREAM_DELETED,
                PlayFinishedEvent.CAUSE.STREAM_JOINED
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
            causeIndex++)
            {
                try {
                    PlayFinishedEvent event = 
                        new PlayFinishedEvent(correctIds[idIndex], 
                                correctCauses[causeIndex], 0);
                    assertNull("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected message value.",
                            event.getMessage());
                    assertSame("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected id value.",
                            correctIds[idIndex], event.getId());
                    assertSame("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): unexpected cause value.",
                            correctCauses[causeIndex], event.getCause());
                }
                catch (Exception e) {
                    fail("Correct arguments (" + idIndex + ", " + causeIndex +
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
     *     o1 = new PlayFinishedEvent(new Object(), CAUSE.PLAY_FINISHED);
     *     o2 = new PlayFinishedEvent(new Object(), 2);
     *     o3 = new PlayFinishedEvent(new Object(), STREAM_DELETED);
     * Action: Call equals for all combinations.
     * Result: true for same index, false for different index. 
     * 
     * Compare to self
     * ---------------
     * Description: Control that equals to self gives true. 
     * Condition: obj = new PlayFinishedEvent(new Object(), CAUSE.PLAY_FINISHED)
     * Result: obj.equals(obj) = true 
     * 
     * Compare to null
     * ---------------
     * Description: Control that comparison to null gives false. 
     * Condition: obj = new PlayFinishedEvent(new Object(), CAUSE.PLAY_FINISHED)
     * Result:  obj.equals(null) = false 
     * 
     * Wrong type
     * ----------
     * Description: Control that comparison with different type gives false.
     * 
     * Condition: obj = new PlayFinishedEvent(id, 0)
     * Action:
     *    1. obj.equals(new Integer(0)).
     *    2. obj.equals(new PlayFinishedEvent(id, 0) {}) (subclass)
     * Resultat: false 
     */
    public void testEquals()
    {
        // Common cases
        PlayFinishedEvent[] objList = new PlayFinishedEvent[] {
                new PlayFinishedEvent(new Object(), 
                    PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0),
                new PlayFinishedEvent(new Object(), 2, 0),
                new PlayFinishedEvent(new Object(), 
                        PlayFinishedEvent.CAUSE.STREAM_DELETED, 0)
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
        PlayFinishedEvent event = new PlayFinishedEvent(new Object(), 
                PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0);
        assertTrue("Compare to self", event.equals(event));
        
        // Compare to null
        assertFalse("Compare to null", event.equals(null));
        
        // Wrong type 1
        Object id = new Object();
        event = new PlayFinishedEvent(id, 0, 0);
        assertFalse("Wrong type 1", event.equals(new Integer(0)));
        // Wrong type 2
        assertFalse("Wrong type 2", 
                event.equals(new PlayFinishedEvent(id, 0, 0){}));
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
     * Condition: An array with five objects: 
     *    1. o1 = new PlayFinishedEvent(new Object(), 
     *       PlayFinishedEvent.CAUSE.PLAY_FINISHED)
     *    2. o2 = new PlayFinishedEvent(obj1, 
     *       PlayFinishedEvent.PLAY_STOPPED)
     *    3. o3 = new PlayFinishedEvent(obj1, 
     *       PlayFinishedEvent.CAUSE.PLAY_CANCELLED)
     *    4. o4 = new PlayFinishedEvent(new Object(), 
     *       PlayFinishedEvent.CAUSE.STREAM_DELETED)
     *    5. o5 = new PlayFinishedEvent(obj1, 1)
     *
     * Action: Test equals on all combinations.
     * Result: If equal, hashCode should be equal for both objects.
     *         If not equal, hashCode should give different values.
     */
    public void testHashCode()
    {
        // Common cases
        Object obj1 = new Object();
        PlayFinishedEvent[] objList = new PlayFinishedEvent[] {
                new PlayFinishedEvent(new Object(), 
                    PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0),
                new PlayFinishedEvent(obj1, 
                    PlayFinishedEvent.CAUSE.PLAY_STOPPED, 0),
                new PlayFinishedEvent(obj1, 
                    PlayFinishedEvent.CAUSE.PLAY_CANCELLED, 0),
                new PlayFinishedEvent(new Object(), 
                    PlayFinishedEvent.CAUSE.STREAM_DELETED, 0),
                new PlayFinishedEvent(obj1, 1, 0)
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