/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import junit.framework.TestCase;

/**
 * Testclass for class of {@link RecordFinishedEvent}.
 * 
 * @author Jörgen Terner
 */
public class RecordFinishedEventTest extends TestCase {
    
    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public RecordFinishedEventTest(String name)
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
     * Test for constructor RecordFinishedEvent(Object, int).
     * 
     * Wrong arguments
     * ---------------
     * Action: Call constructor with:
     *    1. id = null, cause = 0.
     *    1. id = new Object(), cause = -1.
     *    1. id = new Object(), cause = 6.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = 0 - 5
     * Result: No exceptions are thrown.
     *         getMessage returns null.
     *         getCause() != null.
     *         getId returns the given id.
     * </pre>
     */
    public void testConstructorObjectIntString() {
        // Wrong arguments
        // It is the combination of id, cause and message that is wrong.
        Object[] wrongIds = new Object[] {
                null, new Object(), new Object()
        };
        int[] wrongCause = new int[] {
                0, -1, 6
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new RecordFinishedEvent(wrongIds[i], wrongCause[i]);
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
                0, 1, 2, 3, 4, 5
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
                    causeIndex++) {
                try {
                    RecordFinishedEvent event = 
                        new RecordFinishedEvent(correctIds[idIndex], 
                                correctCauses[causeIndex]);
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
     * Test for constructor RecordFinishedEvent(Object, CAUSE).
     * 
     * Wrong arguments
     * ----------------------------
     * Action: Call constructor with:
     *    1. id = null, cause = RECORDING_STOPPED.
     *    1. id = new Object(), cause = null.
     * Result: IllegalArgumentException.
     * 
     * ----------------------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = MAX_RECORDING_DURATION_REACHED, RECORDING_STOPPED
     *            MAX_SILENCE_DURATION_REACHED, SILENCE_DETECTED,
     *            STREAM_DELETED, STREAM_ABANDONED
     *    message = null, "", "Foo"
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
        RecordFinishedEvent.CAUSE[] wrongCause = new RecordFinishedEvent.CAUSE[] {
                RecordFinishedEvent.CAUSE.RECORDING_STOPPED, null
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new RecordFinishedEvent(wrongIds[i], wrongCause[i]);
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
        RecordFinishedEvent.CAUSE[] correctCauses = 
            new RecordFinishedEvent.CAUSE[] {
                RecordFinishedEvent.CAUSE.MAX_RECORDING_DURATION_REACHED,
                RecordFinishedEvent.CAUSE.RECORDING_STOPPED,
                RecordFinishedEvent.CAUSE.MAX_SILENCE_DURATION_REACHED,
                RecordFinishedEvent.CAUSE.SILENCE_DETECTED,
                RecordFinishedEvent.CAUSE.STREAM_DELETED,
                RecordFinishedEvent.CAUSE.STREAM_ABANDONED
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
                    causeIndex++) {
                try {
                    RecordFinishedEvent event = 
                        new RecordFinishedEvent(correctIds[idIndex], 
                                correctCauses[causeIndex]);
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
                    fail("Correct arguments (" + idIndex + ", " + 
                            causeIndex + "): Unexpected exception: " + e);
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
     *     o1 = new RecordFinishedEvent(new Object(), 
     *          CAUSE.RECORDING_STOPPED, null);
     *     o2 = new RecordFinishedEvent(new Object(), 1);
     *     o3 = new RecordFinishedEvent(new Object(), 
     *          CAUSE.STREAM_DELETED);
     * Action: Call equals for all combinations.
     * Result: true for same index, false for different index. 
     * 
     * Compare to self
     * ---------------
     * Description: Control that equals to self gives true. 
     * Condition: obj = new RecordFinishedEvent(new Object(), 
     *                  CAUSE.RECORDING_STOPPED, null)
     * Result: obj.equals(obj) = true 
     * 
     * Compare to null
     * ---------------
     * Description: Control that comparison to null gives false. 
     * Condition: 
     *    obj = new RecordFinishedEvent(new Object(), 
     *          CAUSE.RECORDING_STOPPED, null)
     * Result:  obj.equals(null) = false 
     * 
     * Wrong type
     * ----------
     * Description: Control that comparison with different type gives false.
     * 
     * Condition: obj = new RecordFinishedEvent(id, 0, null)
     * Action:
     *    1. obj.equals(new Integer(0)).
     *    2. obj.equals(new RecordFinishedEvent(id, 0) {}) (subclass)
     * Resultat: false 
     */
    public void testEquals()
    {
        // Common cases
        RecordFinishedEvent[] objList = new RecordFinishedEvent[] {
                new RecordFinishedEvent(new Object(), 
                    RecordFinishedEvent.CAUSE.RECORDING_STOPPED),
                new RecordFinishedEvent(new Object(), 1),
                new RecordFinishedEvent(new Object(), 
                        RecordFinishedEvent.CAUSE.STREAM_DELETED)
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
        RecordFinishedEvent event = new RecordFinishedEvent(new Object(), 
                RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        assertTrue("Compare to self", event.equals(event));
        
        // Compare to null
        assertFalse("Compare to null", event.equals(null));
        
        // Wrong type 1
        Object id = new Object();
        event = new RecordFinishedEvent(id, 0);
        assertFalse("Wrong type 1", event.equals(new Integer(0)));
        // Wrong type 2
        assertFalse("Wrong type 2", 
                event.equals(new RecordFinishedEvent(id, 0){}));
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
     *    1. o1 = new RecordFinishedEvent(new Object(), 
     *       RecordFinishedEvent.CAUSE.RECORDING_STOPPED)
     *    2. o2 = new RecordFinishedEvent(obj1, 
     *       RecordFinishedEvent.CAUSE.RECORDING_STOPPED)
     *    3. o3 = new RecordFinishedEvent(obj1, 
     *       RecordFinishedEvent.CAUSE.STREAM_DELETED)
     *    4. o4 = new RecordFinishedEvent(new Object(), 
     *       RecordFinishedEvent.CAUSE.STREAM_DELETED)
     *    5. o5 = new RecordFinishedEvent(obj1, 1)
     *
     * Action: Test equals on all combinations.
     * Result: If equal, hashCode should be equal for both objects.
     *         If not equal, hashCode should give different values.
     */
    public void testHashCode()
    {
        // Common cases
        Object obj1 = new Object();
        RecordFinishedEvent[] objList = new RecordFinishedEvent[] {
                new RecordFinishedEvent(new Object(), 
                    RecordFinishedEvent.CAUSE.RECORDING_STOPPED),
                new RecordFinishedEvent(obj1, 
                    RecordFinishedEvent.CAUSE.RECORDING_STOPPED),
                new RecordFinishedEvent(obj1, 
                    RecordFinishedEvent.CAUSE.STREAM_DELETED),
                new RecordFinishedEvent(new Object(), 
                    RecordFinishedEvent.CAUSE.STREAM_DELETED),
                new RecordFinishedEvent(obj1, 1)
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