/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * BasicAndCriteria Tester.
 *
 * @author qhast
 */
public class BasicNotCriteriaTest extends MockObjectTestCase {
    private Mock visitor = mock(BasicCriteriaVisitor.class);
    private BasicNotCriteria not;
    private BasicValueCriteria bv1;

    public BasicNotCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        bv1 = new BasicValueCriteria("zero", 0);

        not = new BasicNotCriteria(bv1);

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructWithIllegaArguments() throws Exception {

        try {
            new BasicNotCriteria(null);
            fail("Constructing with null argument should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    public void testAccept() throws Exception {
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv1));
        visitor.expects(once()).method("visitNotCriteria").with(eq(not));
        not.accept((BasicCriteriaVisitor) visitor.proxy());
    }

    /* Tests that criterias that are equal on class and children are equal.
     * @throws Exception
     */
    public void testEquals() throws Exception {
        BasicNotCriteria other = new BasicNotCriteria(
                new BasicValueCriteria("zero", 0)
        );
        assertTrue("Criteria " + other + " should be equal to " + not, other.equals(not));
    }

    /**
     * Tests that criterias that differ on children are NOT equal.
     *
     * @throws Exception
     */
    public void testNotEquals() throws Exception {
        BasicNotCriteria other = new BasicNotCriteria(
                new BasicValueCriteria("zero", 1)
        );
        assertFalse("Criteria " + other + " should NOT be equal to " + not, other.equals(not));

        BasicNotCriteria other2 = new BasicNotCriteria(
                new BasicValueCriteria("one", 0)
        );
        assertFalse("Criteria " + other2 + " should NOT be equal to " + not, other2.equals(not));

        assertFalse("Criteria " + not + " should NOT be equal to null", not.equals(null));
    }

    /**
     * Tests that clone returns an object that are equal to the cloned object.
     *
     * @throws Exception
     */
    public void testClone() throws Exception {
        BasicNotCriteria cloned = not.clone();
        assertTrue("Criteria " + cloned + " should be equal to " + not, cloned.equals(not));
    }

    public void testGetCriteria() throws Exception {
        assertEquals(bv1, not.getCriteria());
    }


    public static Test suite() {
        return new TestSuite(BasicNotCriteriaTest.class);
    }

}
