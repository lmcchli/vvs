/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * ValueCriteria Tester.
 *
 * @author qhast
 */
public class ValueCriteriaTest extends TestCase {

    public ValueCriteriaTest(String name) {
        super(name);
    }

    /**
     * Test that criteria has the same value as set in constuctor.
     *
     * @throws Exception
     */
    public void testGetValue() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        assertEquals("Criteria value should be set to \"error\"", "error", c.getValue());
    }

    /**
     * Test that criteria has the same name as set in constuctor.
     *
     * @throws Exception
     */
    public void testGetName() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        assertEquals("Criteria name should be set to \"error\"", "message", c.getName());
    }


    /**
     * Tests that criterias that are equal on name,value and class are equal.
     *
     * @throws Exception
     */
    public void testEquals() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        ExceptionMessageCriteria other = new ExceptionMessageCriteria("message", "error");
        assertTrue("Criteria " + other + " should be equal to " + c, other.equals(c));
    }

    /**
     * Tests that criterias that differs on name or value or class are NOT equal.
     *
     * @throws Exception
     */
    public void testNotEqual() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");

        ExceptionMessageCriteria different = new ExceptionMessageCriteria("message", "not-error");
        assertFalse("Criteria " + different + " should NOT be equal to " + c, different.equals(c));

        ExceptionMessageCriteria different2 = new ExceptionMessageCriteria("text", "error");
        assertFalse("Criteria " + different2 + " should NOT be equal to " + c, different2.equals(c));

        ExceptionCauseCriteria different3 = new ExceptionCauseCriteria(new Exception());
        assertFalse("Criteria " + different3 + " should NOT be equal to " + c, different3.equals(c));

    }


    public void testMatch() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        ExceptionMessageCriteria c2 = new ExceptionMessageCriteria("message", null);

        assertTrue(c.matchValue("error"));
        assertFalse(c.matchValue("erroooor"));
        assertFalse(c.matchValue(null));

        assertFalse(c2.matchValue("error"));
        assertFalse(c2.matchValue("erroooor"));
        assertTrue(c2.matchValue(null));

    }


    /**
     * Tests that hashcode method not crashes.
     *
     * @throws Exception
     */
    public void testGenerateHashCode() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        int hashCode = c.hashCode();
    }


    /**
     * Tests that class string representation is <i>name</i>=<i>value</i>.
     *
     * @throws Exception
     */
    public void testToString() throws Exception {
        ExceptionMessageCriteria c = new ExceptionMessageCriteria("message", "error");
        assertEquals("message=error", c.toString());
    }

    public static Test suite() {
        return new TestSuite(ValueCriteriaTest.class);
    }


    /**
     * Simple test data class.
     */
    public static class ExceptionMessageCriteria extends ValueCriteria<String, CriteriaVisitor> {

        public ExceptionMessageCriteria(String name, String value) {
            super(name, value);
        }

        public void accept(CriteriaVisitor visitor) {

        }

        public ExceptionMessageCriteria clone() {
            return new ExceptionMessageCriteria(getName(), getValue());
        }

    }

    /**
     * Simple test data class.
     */
    public static class ExceptionCauseCriteria extends ValueCriteria<Throwable, CriteriaVisitor> {

        public ExceptionCauseCriteria(Throwable value) {
            super(value);
        }

        public void accept(CriteriaVisitor visitor) {

        }

        public ExceptionCauseCriteria clone() {
            return new ExceptionCauseCriteria(getValue());
        }

    }

}
