/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.search;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * AndCriteria Tester.
 *
 * @author MANDE
 * @since <pre>12/19/2006</pre>
 * @version 1.0
 */
public class AndCriteriaTest extends TestCase {
    AndCriteria andCriteria;

    public AndCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        andCriteria = new AndCriteria(new ConfidentialCriteria(false), new UrgentCriteria(false));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClone() throws Exception {
        AndCriteria andCriteria = this.andCriteria.clone();
        assertEquals(this.andCriteria, andCriteria);
        assertEquals(this.andCriteria.hashCode(), andCriteria.hashCode());
    }

    public static Test suite() {
        return new TestSuite(AndCriteriaTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
