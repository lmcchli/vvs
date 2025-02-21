/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import junit.framework.TestCase;

/**
 * Testclass for class of {@link RecordFailedEvent}.
 * 
 * @author Jörgen Terner
 */
public class RecordFailedEventTest extends TestCase {
    
    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public RecordFailedEventTest(String name)
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
     * Test for constructor RecordFailedEvent(Object, int, String).
     * 
     * Wrong arguments
     * ---------------
     * Action: Call constructor with:
     *    1. id = null, cause = 0.
     *    1. id = new Object(), cause = -1, message = null.
     *    1. id = new Object(), cause = 2, message = null.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = 0 - 1
     *    message = null, "", "Foo"
     * Result: No exceptions are thrown.
     *         getMessage returns the given message.
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
                0, -1, 2
        };
        String[] wrongMessages = new String[] {
                null, null, null
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new RecordFailedEvent(wrongIds[i], wrongCause[i], 
                        wrongMessages[i]);
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
                0, 1
        };
        String[] correctMessages = new String[] {
                null, "", "Foo"
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
                    causeIndex++) {
                for (int messIndex = 0; messIndex < correctMessages.length;
                        messIndex++) {
                    try {
                        RecordFailedEvent event = 
                            new RecordFailedEvent(correctIds[idIndex], 
                                    correctCauses[causeIndex],
                                    correctMessages[messIndex]);
                        assertEquals("Correct arguments (" + idIndex + ", " +
                                causeIndex + ", " + messIndex + 
                                "): unexpected message value.",
                                correctMessages[messIndex], 
                                event.getMessage());
                        assertSame("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex +
                                "): unexpected id value.",
                                correctIds[idIndex], event.getId());
                        assertNotNull("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex + 
                                "): unexpected cause value.", 
                                event.getCause());
                    }
                    catch (Exception e) {
                        fail("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex + 
                                "): Unexpected exception: " + e);
                    }
                }
            }
        }
    }
    
    /**
     * <pre>
     * Test for constructor RecordFailedEvent(Object, CAUSE, String).
     * 
     * Wrong arguments
     * ----------------------------
     * Action: Call constructor with:
     *    1. id = null, cause = EXCEPTION, message = null.
     *    1. id = new Object(), cause = null, message = null.
     * Result: IllegalArgumentException.
     * 
     * ----------------------------
     * Action: Call constructor with all combinations of:
     *    id = new Object()
     *    cause = EXCEPTION, MIN_RECORDING_DURATION
     *    message = null, "", "Foo"
     * Result: No exceptions are thrown.
     *         getMessage returns the given message.
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
        RecordFailedEvent.CAUSE[] wrongCause = new RecordFailedEvent.CAUSE[] {
                RecordFailedEvent.CAUSE.EXCEPTION, null
        };
        String[] wrongMessages = new String[] {
                null, null
        };
        for (int i = 0; i < wrongIds.length; i++) {
            try {
                new RecordFailedEvent(wrongIds[i], wrongCause[i], 
                        wrongMessages[i]);
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
        RecordFailedEvent.CAUSE[] correctCauses = 
            new RecordFailedEvent.CAUSE[] {
                RecordFailedEvent.CAUSE.EXCEPTION,
                RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION
        };
        String[] correctMessages = new String[] {
                null, "", "Foo"
        };
        for (int idIndex = 0; idIndex < correctIds.length; idIndex++) {
            for (int causeIndex = 0; causeIndex < correctCauses.length; 
                    causeIndex++) {
                for (int messIndex = 0; messIndex < correctMessages.length;
                        messIndex++) {
                    try {
                        RecordFailedEvent event = 
                            new RecordFailedEvent(correctIds[idIndex], 
                                    correctCauses[causeIndex],
                                    correctMessages[messIndex]);
                        assertEquals("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex +
                                "): unexpected message value.",
                                correctMessages[messIndex], 
                                event.getMessage());
                        assertSame("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex +
                                "): unexpected id value.",
                                correctIds[idIndex], event.getId());
                        assertSame("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex +
                                "): unexpected cause value.",
                                correctCauses[causeIndex], event.getCause());
                    }
                    catch (Exception e) {
                        fail("Correct arguments (" + idIndex + ", " + 
                                causeIndex + ", " + messIndex +
                                "): Unexpected exception: " + e);
                    }
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
     *     o1 = new RecordFailedEvent(new Object(), CAUSE.EXCEPTION, null);
     *     o2 = new RecordFailedEvent(new Object(), 1, null);
     *     o3 = new RecordFailedEvent(new Object(), 
     *          CAUSE.MIN_RECORDING_DURATION);
     * Action: Call equals for all combinations.
     * Result: true for same index, false for different index. 
     * 
     * Compare to self
     * ---------------
     * Description: Control that equals to self gives true. 
     * Condition: obj = new RecordFailedEvent(new Object(), CAUSE.EXCEPTION, null)
     * Result: obj.equals(obj) = true 
     * 
     * Compare to null
     * ---------------
     * Description: Control that comparison to null gives false. 
     * Condition: 
     *    obj = new RecordFailedEvent(new Object(), CAUSE.EXCEPTION, null)
     * Result:  obj.equals(null) = false 
     * 
     * Wrong type
     * ----------
     * Description: Control that comparison with different type gives false.
     * 
     * Condition: obj = new RecordFailedEvent(id, 0, null)
     * Action:
     *    1. obj.equals(new Integer(0)).
     *    2. obj.equals(new RecordFailedEvent(id, 0, null) {}) (subclass)
     * Resultat: false 
     */
    public void testEquals()
    {
        // Common cases
        RecordFailedEvent[] objList = new RecordFailedEvent[] {
                new RecordFailedEvent(new Object(), 
                    RecordFailedEvent.CAUSE.EXCEPTION, null),
                new RecordFailedEvent(new Object(), 1, null),
                new RecordFailedEvent(new Object(), 
                        RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, null)
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
        RecordFailedEvent event = new RecordFailedEvent(new Object(), 
                RecordFailedEvent.CAUSE.EXCEPTION, null);
        assertTrue("Compare to self", event.equals(event));
        
        // Compare to null
        assertFalse("Compare to null", event.equals(null));
        
        // Wrong type 1
        Object id = new Object();
        event = new RecordFailedEvent(id, 0, null);
        assertFalse("Wrong type 1", event.equals(new Integer(0)));
        // Wrong type 2
        assertFalse("Wrong type 2", 
                event.equals(new RecordFailedEvent(id, 0, null){}));
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
     *    1. o1 = new RecordFailedEvent(new Object(), 
     *       RecordFailedEvent.CAUSE.EXCEPTION, null)
     *    2. o2 = new RecordFailedEvent(obj1, 
     *       RecordFailedEvent.CAUSE.EXCEPTION, null)
     *    3. o3 = new RecordFailedEvent(obj1, 
     *       RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, null)
     *    4. o4 = new RecordFailedEvent(new Object(), 
     *       RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, null)
     *    5. o5 = new RecordFailedEvent(obj1, 1, "")
     *
     * Action: Test equals on all combinations.
     * Result: If equal, hashCode should be equal for both objects.
     *         If not equal, hashCode should give different values.
     */
    public void testHashCode()
    {
        // Common cases
        Object obj1 = new Object();
        RecordFailedEvent[] objList = new RecordFailedEvent[] {
                new RecordFailedEvent(new Object(), 
                    RecordFailedEvent.CAUSE.EXCEPTION, null),
                new RecordFailedEvent(obj1, 
                    RecordFailedEvent.CAUSE.EXCEPTION, null),
                new RecordFailedEvent(obj1, 
                    RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, null),
                new RecordFailedEvent(new Object(), 
                    RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION, null),
                new RecordFailedEvent(obj1, 1, "")
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