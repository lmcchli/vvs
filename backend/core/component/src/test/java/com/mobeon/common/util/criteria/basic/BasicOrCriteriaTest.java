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
public class BasicOrCriteriaTest extends MockObjectTestCase {
    private Mock visitor = mock(BasicCriteriaVisitor.class);
    private BasicOrCriteria or;
    private BasicValueCriteria bv1;
    private BasicValueCriteria bv2;


    public BasicOrCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        bv1 = new BasicValueCriteria("zero", 0);
        bv2 = new BasicValueCriteria("one", 1);

        or = new BasicOrCriteria(
                bv1,
                bv2
        );

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructWithIllegaArguments() throws Exception {

        try {
            new BasicOrCriteria(bv1, null);
            fail("Constructing with null argument should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            new BasicOrCriteria(bv1);
            fail("Constructing with fewer than one argument should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

    }

    public void testAccept() throws Exception {
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv1));
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv2));
        visitor.expects(once()).method("visitOrCriteria").with(eq(or));
        or.accept((BasicCriteriaVisitor) visitor.proxy());
    }

    /* Tests that criterias that are equal on class and children are equal.
     * @throws Exception
     */
    public void testEquals() throws Exception {
        BasicOrCriteria other = new BasicOrCriteria(
                new BasicValueCriteria("zero", 0),
                new BasicValueCriteria("one", 1)
        );
        assertTrue("Criteria " + other + " should be equal to " + or, other.equals(or));

        BasicOrCriteria other2 = new BasicOrCriteria(
                new BasicValueCriteria("one", 1),
                new BasicValueCriteria("zero", 0)
        );
        assertTrue("Criteria " + other2 + " should be equal to " + or, other2.equals(or));


    }

    /**
     * Tests that criterias that differ on children are NOT equal.
     *
     * @throws Exception
     */
    public void testNotEquals() throws Exception {
        BasicOrCriteria other = new BasicOrCriteria(
                new BasicValueCriteria("zero", 1),
                new BasicValueCriteria("one", 1)
        );
        assertFalse("Criteria " + other + " should NOT be equal to " + or, other.equals(or));

        BasicOrCriteria other2 = new BasicOrCriteria(
                new BasicValueCriteria("one", 0),
                new BasicValueCriteria("zero", 1)
        );
        assertFalse("Criteria " + other2 + " should NOT be equal to " + or, other2.equals(or));

        assertFalse("Criteria " + or + " should NOT be equal to null", or.equals(null));
    }

    /**
     * Tests that clone returns an object that are equal to the cloned object.
     *
     * @throws Exception
     */
    public void testClone() throws Exception {
        BasicOrCriteria cloned = or.clone();
        assertTrue("Criteria " + cloned + " should be equal to " + or, cloned.equals(or));
    }

    public void testGetCriterias() throws Exception {
        assertEquals(2, or.getCriterias().length);
    }


    public static Test suite() {
        return new TestSuite(BasicOrCriteriaTest.class);
    }

}
