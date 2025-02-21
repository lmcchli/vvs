/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * BasicAndCriteria Tester.
 *
 * @author qhast
 */
public class BasicAndCriteriaTest extends MockObjectTestCase
{
    private Mock visitor = mock(BasicCriteriaVisitor.class);
    private BasicAndCriteria and;
    private BasicValueCriteria bv1;
    private BasicValueCriteria bv2;


    public BasicAndCriteriaTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        bv1 = new BasicValueCriteria("zero", 0);
        bv2 = new BasicValueCriteria("one", 1);

        and = new BasicAndCriteria(
                bv1,
                bv2
        );

    }

    public void testConstructWithIllegaArguments() throws Exception {

        try {
            new BasicAndCriteria(bv1,null);
            fail("Constructing with null argument should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            new BasicAndCriteria(bv1);
            fail("Constructing with fewer than one argument should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

    }

    /**
     * Tests that and-criteria propagates accept call to all childen.
     * @throws Exception
     */
    public void testAccept() throws Exception {
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv1));
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv2));
        visitor.expects(once()).method("visitAndCriteria").with(eq(and));
        and.accept((BasicCriteriaVisitor)visitor.proxy());
    }


    /**
     * Tests that criterias that are equal on class and children are equal.
     * @throws Exception
     */
    public void testEquals() throws Exception {
        BasicAndCriteria other = new BasicAndCriteria(
                new BasicValueCriteria("zero", 0),
                new BasicValueCriteria("one", 1)
        );
        assertTrue("Criteria "+other+" should be equal to "+and,other.equals(and));

        BasicAndCriteria other2 = new BasicAndCriteria(
                new BasicValueCriteria("one", 1),
                new BasicValueCriteria("zero", 0)
        );
        assertTrue("Criteria "+other2+" should be equal to "+and,other2.equals(and));


    }

    /**
     * Tests that criterias that differ on children are NOT equal.
     * @throws Exception
     */
    public void testNotEquals() throws Exception {
        BasicAndCriteria other = new BasicAndCriteria(
                new BasicValueCriteria("zero", 1),
                new BasicValueCriteria("one", 1)
        );
        assertFalse("Criteria "+other+" should NOT be equal to "+and,other.equals(and));

        BasicAndCriteria other2 = new BasicAndCriteria(
                new BasicValueCriteria("one", 0),
                new BasicValueCriteria("zero", 1)
        );
        assertFalse("Criteria "+other2+" should NOT be equal to "+and,other2.equals(and));

        assertFalse("Criteria "+and+" should NOT be equal to null",and.equals(null));
    }

    /**
     * Tests that clone returns an object that are equal to the cloned object.
     * @throws Exception
     */
    public void testClone() throws Exception
    {
        BasicAndCriteria cloned = and.clone();
        assertTrue("Criteria "+cloned+" should be equal to "+and,cloned.equals(and));
    }

    public void testGetCriterias() throws Exception {
        assertEquals(2,and.getCriterias().length);
    }


    public static Test suite()
    {
        return new TestSuite(BasicAndCriteriaTest.class);
    }

}
