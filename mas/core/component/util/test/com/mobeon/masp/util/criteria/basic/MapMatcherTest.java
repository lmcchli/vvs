/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Hashtable;

/**
 * MapMatcher Tester.
 *
 * @author qhast
 */
public class MapMatcherTest extends TestCase
{
    private BasicAndCriteria and;

    private BasicValueCriteria bv1;
    private BasicValueCriteria bv2;

    private BasicNotCriteria not;

    private BasicOrCriteria or;


    public MapMatcherTest(String name)
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

        or = new BasicOrCriteria(
                bv1,
                bv2
        );

        not = new BasicNotCriteria(bv1);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

   public void testBasicAndMatch() throws Exception
    {
        Map<String,Object> matchingObject = new Hashtable<String,Object>();
        matchingObject.put("zero",0);
        matchingObject.put("one",1);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertTrue(matchingObject+" should match "+and,MapMatcher.match(and,matchingObject));

        Map<String,Object> nonMatchingObject1 = new Hashtable<String,Object>();
        nonMatchingObject1.put("zero",0);
        nonMatchingObject1.put("one",2);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertFalse(nonMatchingObject1+" should NOT match "+and,MapMatcher.match(and,nonMatchingObject1));

        Map<String,Object> nonMatchingObject2 = new Hashtable<String,Object>();
        nonMatchingObject2.put("zero",45);
        nonMatchingObject2.put("one",2);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertFalse(nonMatchingObject2+" should NOT match "+and,MapMatcher.match(and,nonMatchingObject2));

    }

    public void testBasicNotMatch() throws Exception
    {
        Map<String,Object> matchingObject = new Hashtable<String,Object>();
        matchingObject.put("zero",3);
        matchingObject.put("one",1);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertTrue(matchingObject+" should match "+not,MapMatcher.match(not,matchingObject));

        Map<String,Object> nonMatchingObject = new Hashtable<String,Object>();
        nonMatchingObject.put("zero",0);
        nonMatchingObject.put("one",2);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertFalse(nonMatchingObject+" should NOT match "+not,MapMatcher.match(not,nonMatchingObject));


    }

    public void testBasicOrMatch() throws Exception
        {
            Map<String,Object> matchingObject = new Hashtable<String,Object>();
            matchingObject.put("zero",0);
            matchingObject.put("one",1);
            matchingObject.put("zerow",0);
            matchingObject.put("onew",1);
            assertTrue(matchingObject+" should match "+or,MapMatcher.match(or,matchingObject));

            Map<String,Object> matchingObject1 = new Hashtable<String,Object>();
            matchingObject1.put("zero",0);
            matchingObject1.put("one",2);
            matchingObject.put("zerow",0);
            matchingObject.put("onew",1);
            assertTrue(matchingObject1+" should match "+or,MapMatcher.match(or,matchingObject1));

            Map<String,Object> matchingObject2 = new Hashtable<String,Object>();
            matchingObject2.put("zero",5);
            matchingObject2.put("one",1);
            matchingObject.put("zerow",0);
            matchingObject.put("onew",1);
            assertTrue(matchingObject2+" should match "+or,MapMatcher.match(or,matchingObject2));

            Map<String,Object> nonMatchingObject = new Hashtable<String,Object>();
            nonMatchingObject.put("zero",45);
            nonMatchingObject.put("one",2);
            matchingObject.put("zerow",0);
            matchingObject.put("onew",1);
            assertFalse(nonMatchingObject+" should NOT match "+or,MapMatcher.match(or,nonMatchingObject));

        }

    public void testBasicValueMatch() throws Exception
    {
        Map<String,Object> matchingObject = new Hashtable<String,Object>();
        matchingObject.put("zero",0);
        matchingObject.put("one",1);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertTrue(matchingObject+" should match "+bv1,MapMatcher.match(bv1,matchingObject));

        Map<String,Object> nonMatchingObject = new Hashtable<String,Object>();
        nonMatchingObject.put("zero",22);
        nonMatchingObject.put("one",1);
        matchingObject.put("zerow",0);
        matchingObject.put("onew",1);
        assertFalse(nonMatchingObject+" should NOT match "+bv1,MapMatcher.match(bv1,nonMatchingObject));

    }


    public static Test suite()
    {
        return new TestSuite(MapMatcherTest.class);
    }
}
