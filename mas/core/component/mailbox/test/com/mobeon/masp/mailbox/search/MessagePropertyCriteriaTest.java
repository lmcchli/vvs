/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.Criteria;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * MessagePropertyCriteria Tester.
 *
 * @author qhast
 * @see Criteria 
 */
public class MessagePropertyCriteriaTest extends TestCase
{
    Criteria<MessagePropertyCriteriaVisitor> criteria;
    Criteria<MessagePropertyCriteriaVisitor> criteria1;

    public MessagePropertyCriteriaTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        criteria1 = new UrgentCriteria(true);
        criteria = new OrCriteria(
                new ConfidentialCriteria(true),
                criteria1
                );
        criteria = new NotCriteria(criteria);

        criteria = new OrCriteria(criteria,criteria1);

    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetParent() throws Exception
    {
        assertNull(criteria.getParent());
    }

    public void testSetParent() throws Exception
    {
        /*
        try {
            criteria1.setParent(criteria);
            fail("Should not go here!");
        } catch(IllegalStateException e) {
            //Should go here
        }
        */
    }

    public void testHashCode() throws Exception
    {
        criteria.hashCode();
    }

    public void testToString() throws Exception
    {
        criteria.toString();
    }

    public void testClone() throws Exception
    {
        Criteria<MessagePropertyCriteriaVisitor> clone = criteria.clone();
        assertEquals(criteria,clone);
    }


    public static Test suite()
    {
        return new TestSuite(MessagePropertyCriteriaTest.class);
    }
}
