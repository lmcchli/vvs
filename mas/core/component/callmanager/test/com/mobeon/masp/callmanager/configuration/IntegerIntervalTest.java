/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.TestCase;

import java.util.Collection;

/**
 * IntegerInterval Tester.
 *
 * @author Malin Flodin
 */
public class IntegerIntervalTest extends TestCase
{
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that an integer interval is written as "<start>-<stop>".
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        IntegerInterval interval = new IntegerInterval(1, 2);
        assertEquals("1-2", interval.toString());
    }

    /**
     * Verifies that when parsing illegally formatted intervals, an
     * IllegalArgumentException is thrown.
     * @throws Exception if test case fails.
     */
    public void testParseIntegerIntervalsWithIllegalFormat() throws Exception {
        // Verify for empty string
        try {
            IntegerInterval.parseIntegerIntervals("");
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }

        // Verify for: ","
        try {
            IntegerInterval.parseIntegerIntervals(",");
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }

        // Verify for: "1-a"
        try {
            IntegerInterval.parseIntegerIntervals("1-a");
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }

        // Verify for: "1-2-3"
        try {
            IntegerInterval.parseIntegerIntervals("1-2-3");
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that when parsing correctly formatted intervals, a
     * collection of intervals is returned.
     * @throws Exception if test case fails.
     */
    public void testParseIntegerIntervals() throws Exception {
        // Verify for string with spaces
        Collection<IntegerInterval> intervals =
                IntegerInterval.parseIntegerIntervals(" 401 - 415 , 501 , 601 - 603 ");

        assertEquals(3, intervals.size());
        IntegerInterval[] result = new IntegerInterval[3];
        result = intervals.toArray(result);
        assertEquals(401, result[0].getStart());
        assertEquals(415, result[0].getEnd());
        assertEquals(501, result[1].getStart());
        assertEquals(501, result[1].getEnd());
        assertEquals(601, result[2].getStart());
        assertEquals(603, result[2].getEnd());

        // Verify for string without spaces
        intervals = IntegerInterval.
                parseIntegerIntervals("401-415,501,601-603");

        assertEquals(3, intervals.size());
        result = intervals.toArray(result);
        assertEquals(401, result[0].getStart());
        assertEquals(415, result[0].getEnd());
        assertEquals(501, result[1].getStart());
        assertEquals(501, result[1].getEnd());
        assertEquals(601, result[2].getStart());
        assertEquals(603, result[2].getEnd());
    }
}

