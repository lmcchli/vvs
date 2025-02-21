/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * BasicSqlStatementBuilder Tester.
 *
 * @author qhast
 */
public class BasicSqlStatementBuilderTest extends TestCase {

    private BasicAndCriteria and;

    private BasicValueCriteria bv1;
    private BasicValueCriteria bv2;

    private BasicNotCriteria not;

    private BasicOrCriteria or;

    public void setUp() throws Exception {
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

    public BasicSqlStatementBuilderTest(String name) {
        super(name);
    }

    public void testBuildStatement() throws Exception {
        String sql = BasicSqlStatementBuilder.buildStatement("SELECT FROM x", and);
        assertEquals("SELECT FROM x WHERE (zero='0' AND one='1')", sql);

        sql = BasicSqlStatementBuilder.buildStatement("SELECT FROM x", or);
        assertEquals("SELECT FROM x WHERE (zero='0' OR one='1')", sql);

        sql = BasicSqlStatementBuilder.buildStatement("SELECT FROM x", not);
        assertEquals("SELECT FROM x WHERE NOT (zero='0')", sql);
    }

    public static Test suite() {
        return new TestSuite(BasicSqlStatementBuilderTest.class);
    }
}
