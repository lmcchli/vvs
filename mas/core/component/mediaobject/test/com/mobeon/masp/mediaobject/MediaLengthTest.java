/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import junit.framework.TestCase;

/**
 * JUnit tests for the {@link MediaLength} class.
 *
 * @author Mats Egland
 */
public final class MediaLengthTest extends TestCase {

    /**
     * Tests for the constructor
     * {@link MediaLength#MediaLength(MediaLength.LengthUnit, long)}.
     *
     * <pre>
     * 1. IllegalArgumentException
     *  Condition:
     *  Action:
     *      1. Call constructor with null as unit.
     *      2. Call constructor with -1 as value.
     *  Result:
     *      1-2. IllegalArgumentException is thrown.
     *
     * 2. Assert created object is not null
     * 	Condition:
     *  Action:
     *      Create MediaMimeTypes object with arguments:
     *      - unit  = MediaLength.LengthUnit.MILLISECONDS
     *      - lenth = 60*1000 (60 seconds)
     *  Result:
     *      - Created MediaLength object is not null.
     *      - get
     * </pre>
     */
    public void testConstructor() {
        // 1
        try {
            MediaLength mediaLength = new MediaLength(null, 0);
            fail("Constructor should throw IllegalArgumentException" +
                    " if null is passed as unit argument");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            MediaLength mediaLength = new MediaLength(
                    MediaLength.LengthUnit.MILLISECONDS, -1);
            fail("Constructor should throw IllegalArgumentException" +
                    " if -1 is passed as value argument");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        MediaLength mediaLength = new MediaLength(
                MediaLength.LengthUnit.MILLISECONDS, 60*1000);
        assertNotNull("Created MediaLength object is null",
                mediaLength);
        assertEquals("Unit of length should be " +
                MediaLength.LengthUnit.MILLISECONDS,
                MediaLength.LengthUnit.MILLISECONDS, mediaLength.getUnit());
        assertEquals("Value should be 60000",
                60000, mediaLength.getValue());
    }

    /**
     * Tests for the {@link MediaLength#getValue()} method.
     *
     * <pre>
     *
     *
     * 1. Asssert returned value matches the value
     *    passed to constructor.
     * 	Condition:
     * 		Create MediaMimeTypes object with arguments:
     *      	- unit  = MediaLength.LengthUnit.MILLISECONDS
     *      	- lenth = 60*1000 (60 seconds)
     *  Action:
     *      Call getValue
     *  Result:
     *      Returns 60000
     * </pre>
     */
    public void testGetValue() {
        // 1

        MediaLength mediaLength = new MediaLength(
                MediaLength.LengthUnit.MILLISECONDS, 60*1000);
        assertNotNull("Created MediaLength object is null",
                mediaLength);
        assertEquals("Value should be 60000",
                60000, mediaLength.getValue());
    }
    /**
     * Tests for the {@link MediaLength#getUnit()} method.
     *
     * <pre>
     *
     *
     * 1. Asssert returned unit matches the unit
     *    passed to constructor.
     * 	Condition:
     * 		Create MediaMimeTypes object with arguments:
     *      	- unit  = MediaLength.LengthUnit.PAGES
     *      	- length = 200
     *  Action:
     *      Call getUnit
     *  Result:
     *      Returns MediaLength.LengthUnit.PAGES
     * </pre>
     */
    public void testGetUnit() {
        // 1

        MediaLength mediaLength = new MediaLength(
                MediaLength.LengthUnit.PAGES, 200);
        assertNotNull("Created MediaLength object is null",
                mediaLength);
        assertEquals("Unit should be "+
                MediaLength.LengthUnit.PAGES,
                MediaLength.LengthUnit.PAGES,
                mediaLength.getUnit());
    }

/**
     * Tests that toString not crashes.
     * @throws Exception
     */
    public void testToString() throws Exception {

        MediaLength ml = new MediaLength(MediaLength.LengthUnit.MILLISECONDS,9000);
        ml.toString();
    }

}
