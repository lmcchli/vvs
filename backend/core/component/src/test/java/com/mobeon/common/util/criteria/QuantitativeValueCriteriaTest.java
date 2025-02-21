/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria;

import static com.mobeon.common.util.criteria.QuantitativeValueCriteria.Comparison.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.List;

/**
 * QuantitativeValueCriteria Tester.
 *
 * @author qhast
 */
public class QuantitativeValueCriteriaTest extends MockObjectTestCase {

    Mock list100;
    Mock list80;
    Mock list50;
    Mock list20;

    private ListSizeCriteria eq100;
    private ListSizeCriteria eq80;
    private ListSizeCriteria lt50;
    private ListSizeCriteria gt50;
    private ListSizeCriteria le50;
    private ListSizeCriteria ge50;
    private ListSizeCriteria ne50;

    public QuantitativeValueCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        list100 = mock(List.class);
        list100.stubs().method("size").will(returnValue(100));

        list80 = mock(List.class);
        list80.stubs().method("size").will(returnValue(80));

        list50 = mock(List.class);
        list50.stubs().method("size").will(returnValue(50));

        list20 = mock(List.class);
        list20.stubs().method("size").will(returnValue(20));

        eq100 = new ListSizeCriteria(100);
        eq80 = new ListSizeCriteria(80, EQ);
        lt50 = new ListSizeCriteria(50, LT);
        gt50 = new ListSizeCriteria(50, GT);
        le50 = new ListSizeCriteria(50, LE);
        ge50 = new ListSizeCriteria(50, GE);
        ne50 = new ListSizeCriteria(50, NE);
    }


    /**
     * Test that criteria has the same comparision as set in constuctor.
     *
     * @throws Exception
     */
    public void testGetComparison() throws Exception {
        assertEquals("Comparision should be " + EQ, EQ, eq100.getComparison());
        assertEquals("Comparision should be " + EQ, EQ, eq80.getComparison());
        assertEquals("Comparision should be " + LT, LT, lt50.getComparison());
        assertEquals("Comparision should be " + GT, GT, gt50.getComparison());
        assertEquals("Comparision should be " + LE, LE, le50.getComparison());
        assertEquals("Comparision should be " + GE, GE, ge50.getComparison());
        assertEquals("Comparision should be " + NE, NE, ne50.getComparison());
    }

    /**
     * Tests that criterias that are equal on name,value,class and comparision are equal.
     *
     * @throws Exception
     */
    public void testEqual() throws Exception {
        ListSizeCriteria other100 = new ListSizeCriteria(100);
        assertTrue("Criteria " + other100 + " should be equal to " + eq100, other100.equals(eq100));

        ListSizeCriteria other80 = new ListSizeCriteria(80);
        assertTrue("Criteria " + other80 + " should be equal to " + eq80, other80.equals(eq80));

        ListSizeCriteria otherLt50 = new ListSizeCriteria(50, LT);
        assertTrue("Criteria " + otherLt50 + " should be equal to " + lt50, otherLt50.equals(lt50));

        ListSizeCriteria otherGt50 = new ListSizeCriteria(50, GT);
        assertTrue("Criteria " + otherGt50 + " should be equal to " + gt50, otherGt50.equals(gt50));

        ListSizeCriteria otherLe50 = new ListSizeCriteria(50, LE);
        assertTrue("Criteria " + otherLe50 + " should be equal to " + le50, otherLe50.equals(le50));

        ListSizeCriteria otherGe50 = new ListSizeCriteria(50, GE);
        assertTrue("Criteria " + otherGe50 + " should be equal to " + ge50, otherGe50.equals(ge50));

        ListSizeCriteria otherNe50 = new ListSizeCriteria(50, NE);
        assertTrue("Criteria " + otherNe50 + " should be equal to " + ne50, otherNe50.equals(ne50));

    }

    /**
     * Tests that criterias that differ value or comparision are NOT equal.
     *
     * @throws Exception
     */
    public void testNotEqual() throws Exception {
        ListSizeCriteria different = new ListSizeCriteria(99);
        assertFalse("Criteria " + different + " should be equal to " + eq100, different.equals(eq100));

        ListSizeCriteria different2 = new ListSizeCriteria(50, LE);
        assertFalse("Criteria " + different2 + " should be equal to " + lt50, different2.equals(lt50));

        ListSizeCriteria different3 = new ListSizeCriteria(49, GT);
        assertFalse("Criteria " + different3 + " should be equal to " + gt50, different3.equals(gt50));

        ListEmptyCriteria different4 = new ListEmptyCriteria(false);
        assertFalse("Criteria " + different4 + " should be equal to " + gt50, different4.equals(gt50));

    }

    public void testMatch() throws Exception {
        assertTrue(eq100.matchValue(100));
        assertFalse(eq100.matchValue(101));

        assertTrue(lt50.matchValue(40));
        assertFalse(lt50.matchValue(50));
        assertFalse(lt50.matchValue(60));

        assertTrue(gt50.matchValue(52));
        assertFalse(gt50.matchValue(50));
        assertFalse(gt50.matchValue(42));

        assertTrue(le50.matchValue(50));
        assertTrue(le50.matchValue(48));
        assertFalse(le50.matchValue(52));

        assertTrue(ge50.matchValue(50));
        assertTrue(ge50.matchValue(52));
        assertFalse(ge50.matchValue(45));

        assertTrue(ne50.matchValue(52));
        assertTrue(ne50.matchValue(4));
        assertTrue(ne50.matchValue(null));
        assertFalse(ne50.matchValue(50));

    }

    /**
     * Tests that hashcode method not crashes.
     *
     * @throws Exception
     */
    public void testGenerateHashCode() throws Exception {
        int hashCode = ne50.hashCode();
    }

    /**
     * Tests that class string representation is <i>name</i>&lt;symbol&gt;<i>value</i>.
     *
     * @throws Exception
     */
    public void testToString() throws Exception {
        assertEquals("size=100", eq100.toString());
        assertEquals("size=80", eq80.toString());
        assertEquals("size<50", lt50.toString());
        assertEquals("size>50", gt50.toString());
        assertEquals("size<=50", le50.toString());
        assertEquals("size>=50", ge50.toString());
        assertEquals("size!=50", ne50.toString());
    }

    public static Test suite() {
        return new TestSuite(QuantitativeValueCriteriaTest.class);
    }


    /**
     * Simple test data class.
     *
     * @author qhast
     */
    public static class ListSizeCriteria extends QuantitativeValueCriteria<Integer, CriteriaVisitor> {

        public ListSizeCriteria(Integer value, Comparison c) {
            super("size", value, c);
        }

        public ListSizeCriteria(Integer value) {
            super("size", value);
        }

        public void accept(CriteriaVisitor visitor) {

        }

        public ListSizeCriteria clone() {
            return new ListSizeCriteria(getValue(), getComparison());
        }

    }

    /**
     * Simple test data class.
     *
     * @author qhast
     */
    public static class ListEmptyCriteria extends ValueCriteria<Boolean, CriteriaVisitor> {


        public ListEmptyCriteria(Boolean value) {
            super("empty", value);
        }

        public void accept(CriteriaVisitor visitor) {

        }

        public ListEmptyCriteria clone() {
            return new ListEmptyCriteria(getValue());
        }

    }


}
