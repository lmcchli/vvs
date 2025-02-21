/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

/**
 * BasicValueCriteria Tester.
 *
 * @author qhast
 */
public class BasicValueCriteriaTest extends MockObjectTestCase
{
    private Mock visitor = mock(BasicCriteriaVisitor.class);
    private BasicValueCriteria bv1;


    public BasicValueCriteriaTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        bv1 = new BasicValueCriteria("zero", 0);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testAccept() throws Exception
    {
        visitor.expects(once()).method("visitBasicValueCriteria").with(eq(bv1));
        bv1.accept((BasicCriteriaVisitor)visitor.proxy());
    }

    public void testClone() throws Exception
    {
        BasicValueCriteria cloned = bv1.clone();
        assertEquals(bv1,cloned);
    }

    public static Test suite()
    {
        return new TestSuite(BasicValueCriteriaTest.class);
    }
}
