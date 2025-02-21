/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import junit.framework.TestCase;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.List;

/**
 * JUnit tests for {@link MediaProperties}.
 *
 * @author Mats Egland
 */
public class MediaPropertiesTest extends TestCase {
    /**
     * Number of concurrent threads to consume a
     * <code>MediaProperties<code> in the concurrent test.
     */
    private static final int CONSUMERS = 10;

    /**
     * Tests for the emtpy constructor
     * {@link MediaProperties#MediaProperties()}.
     * <p/>
     * <pre>
     * <p/>
     * 1.Assert object is created.
     *  Condition:
     *  Action:
     *      new MediaProperties()
     *  Result:
     *      Created MediaProperties is non-null and has
     *      no lengths and size of 0.
     * </pre>
     */
    public void testConstructor() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();
        assertNotNull(
                "Failed to create MediaProperties with empty constructor",
                mediaProperties);

        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());
    }

    /**
     * Tests for the constructor
     * {@link MediaProperties#MediaProperties(jakarta.activation.MimeType)}.
     * <p/>
     * <pre>
     * 1. Null argument
     *  Condition:
     *  Action:
     *      - contenttype = null
     *  Result:
     *      Created object with null content-type, null fileextension,
     *      no lengths and size of 0.
     * <p/>
     * 2.Assert object is created.
     *  Condition:
     *      A content-type with mime audio/pcmu.
     *  Action:
     *      new MediaProperties(audio/pcmu)
     *  Result:
     *      Created object with same content-type as passed, null fileextension,
     *      no lengths and size of 0.
     * </pre>
     */
    public void testConstructor1() throws MimeTypeParseException {
        MimeType AUDIO_PCMU = new MimeType("audio/pcmu");
        // 1
        MediaProperties mediaProperties = null;
        mediaProperties = new MediaProperties((MimeType) null);
        assertNotNull("Failed to create MediaProperties", mediaProperties);
        assertNull("Content-type should be null", mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());

        // 2
        mediaProperties = new MediaProperties(AUDIO_PCMU);
        assertNotNull(
                "Failed to create MediaProperties",
                mediaProperties);
        assertNotNull("Failed to create MediaProperties", mediaProperties);
        assertSame("Content-type should same as passed ",
                AUDIO_PCMU, mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());
    }
    /**
     * Tests for the constructor
     * {@link MediaProperties#MediaProperties(jakarta.activation.MimeType)}.
     * <pre>
     * <p/>
     * 1. Null arguments
     *  Condition:
     *  Action:
     *      content type = null
     *  Result:
     *      Created object with null content-type, null fileextension,
     *      no lengths and size of 0.
     *
     * 2. Non-null arguments
     *  Condition:
     *  Action:
     *      content-type = audio/pcmu
     *
     * <p/>
     *  Result:
     *      Successfully created MediaProperties object with properties as above.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testConstructor2() throws MimeTypeParseException {
        MimeType AUDIO_PCMU = new MimeType("audio/pcmu");

        // 1
        MediaProperties mediaProperties =
                new MediaProperties(null);
        assertNotNull("Failed to create MediaProperties with null arguments", mediaProperties);
        assertNull("Content-type should be null", mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());


        // 2
        mediaProperties =
                new MediaProperties(AUDIO_PCMU);
        assertNotNull("Failed to create MediaProperties",
                mediaProperties);
        assertSame("Content-type should be same as passed",
                AUDIO_PCMU, mediaProperties.getContentType());

        assertEquals("size should be 0",
                0, mediaProperties.getSize());

        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
    }
    /**
     * Tests for the constructor
     * {@link MediaProperties#MediaProperties(jakarta.activation.MimeType, String)}.
     * <pre>
     * <p/>
     * 1. Null arguments
     *  Condition:
     *  Action:
     *      content type = null
     *      file extension = null
     *
     * <p/>
     *  Result:
     *      Created object with null content-type, null fileextension,
     *      no lengths and size of 0.
     *
     * 2. Non-null arguments
     *  Condition:
     *  Action:
     *      content-type = audio/pcmu
     *      extension    = wav
     *      size         = 999
     * <p/>
     *  Result:
     *      Successfully created MediaProperties object with properties as above.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testConstructor3() throws MimeTypeParseException {
        MimeType AUDIO_PCMU = new MimeType("audio/pcmu");

        // 1
        MediaProperties mediaProperties =
                new MediaProperties(null, null);
        assertNotNull("Failed to create MediaProperties with null arguments", mediaProperties);
        assertNull("Content-type should be null", mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());



        // 2
        mediaProperties =
                new MediaProperties(AUDIO_PCMU,
                        "wav");
        assertNotNull("Failed to create MediaProperties",
                mediaProperties);
        assertSame("Content-type should be same as passed",
                AUDIO_PCMU, mediaProperties.getContentType());
        assertEquals("File-extension should be equal to the passed",
                "wav", mediaProperties.getFileExtension());

        assertEquals("size should be 0",
                0, mediaProperties.getSize());

        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
    }
    /**
     * Tests for the constructor
     * {@link MediaProperties#MediaProperties(jakarta.activation.MimeType, String, long)}.
     * <pre>
     * <p/>
     * 1. Null arguments
     *  Condition:
     *  Action:
     *      content type = null
     *      file extension = null
     *      size = 0
     * <p/>
     *  Result:
     *      Created object with null content-type, null fileextension,
     *      no lengths and size of 0.
     * <p/>
     * 2.Illegal arguments
     *  Condition:
     *  Action:
     *      size = -1
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 3. Non-null arguments
     *  Condition:
     *  Action:
     *      content-type = audio/pcmu
     *      extension    = wav
     *      size         = 999
     * <p/>
     *  Result:
     *      Successfully created MediaProperties object with properties as above.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testConstructor4() throws MimeTypeParseException {
        MimeType AUDIO_PCMU = new MimeType("audio/pcmu");

        // 1
        MediaProperties mediaProperties =
                new MediaProperties(null, null, 0);
        assertNotNull("Failed to create MediaProperties with null arguments", mediaProperties);
        assertNull("Content-type should be null", mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());

        // 2
        try {
            mediaProperties =
                    new MediaProperties(null, null, -1);
            fail("size -1 should result in IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 3
        mediaProperties =
                new MediaProperties(AUDIO_PCMU,
                        "wav",
                        999);
        assertNotNull("Failed to create MediaProperties",
                mediaProperties);
        assertSame("Content-type should be same as passed",
                AUDIO_PCMU, mediaProperties.getContentType());
        assertEquals("File-extension should be equal to the passed",
                "wav", mediaProperties.getFileExtension());

        assertEquals("size should be 999",
                999, mediaProperties.getSize());

        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
    }


    /**
     * Tests for the constructor
     * {@link MediaProperties#MediaProperties(jakarta.activation.MimeType, String, long, MediaLength...)}.
     * <pre>
     * <p/>
     * 1. Null arguments
     *  Condition:
     *  Action:
     *      content type = null
     *      file extension = null
     *      size = 0
     *      lengts = null
     *  Result:
     *      Created object with null content-type, null fileextension,
     *      no lengths and size of 0.
     * <p/>
     * 2.Illegal arguments
     *  Condition:
     *  Action:
     *      size = -1
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 3. Non-null arguments
     *  Condition:
     *  Action:
     *      content-type = audio/pcmu
     *      extension    = wav
     *      size         = 999
     *      lengts       = (100 PAGES, 1000 MILLISECONDS)
     *  Result:
     *      Successfully created MediaProperties object with properties as above.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testConstructor5() throws MimeTypeParseException {
        MimeType AUDIO_PCMU = new MimeType("audio/pcmu");
        MediaLength PAGES_100 =
                new MediaLength(MediaLength.LengthUnit.PAGES, 100);
        MediaLength MILLISECONDS_1000 =
                new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 1000);

        // 1
        MediaProperties mediaProperties =
                new MediaProperties(null, null, 0, (MediaLength[])null);
        assertNotNull("Failed to create MediaProperties with null arguments", mediaProperties);
        assertNull("Content-type should be null", mediaProperties.getContentType());
        assertNull("File-extension should be null", mediaProperties.getFileExtension());
        for (MediaLength.LengthUnit unit : MediaLength.LengthUnit.values()) {
            assertFalse("Should have no lenghts",
                    mediaProperties.hasLengthInUnit(unit));
        }
        assertEquals("Default size should be 0",
                0, mediaProperties.getSize());

        // 2
        try {
            mediaProperties =
                    new MediaProperties(null, null, -1, (MediaLength[])null);
            fail("size -1 should result in IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 3
        mediaProperties =
                new MediaProperties(AUDIO_PCMU,
                        "wav",
                        999,
                        PAGES_100, MILLISECONDS_1000);


        assertNotNull("Failed to create MediaProperties",
                mediaProperties);
        assertSame("Content-type should be same as passed",
                AUDIO_PCMU, mediaProperties.getContentType());
        assertEquals("File-extension should be equal to the passed",
                "wav", mediaProperties.getFileExtension());
        assertEquals("Should be 100 pages",
                100, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.PAGES));
        assertEquals("Should be 1000 milliseconds",
                1000, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertEquals("size should be 999",
                999, mediaProperties.getSize());


    }

    /**
     * Tests for the method
     * {@link MediaProperties#addLength(MediaLength)}.
     * <p/>
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Calling method with null.
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 2. Assert length is added
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Call addLength with new MediaLength with content:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Result:
     *      The length is added, i.e. call to getLengthInUnit(MILLISECONDS)
     *      returns 20.
     * <p/>
     * 3. Add duplicate, i.e. a second length with same unit.
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Call addLength with new MediaLength with same unit.
     *  Result:
     *      The new length overwrites the old.
     * <p/>
     * 4. Add a length in unit PAGES
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Action:
     *      Call addMimeType with new MediaLength with unit PAGES.
     *  Result:
     *      Both lengths added is present.
     * <p/>
     * </pre>
     */
    public void testAddLength() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();
        try {
            mediaProperties.addLength(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        MediaLength mediaLength_MS_20 = new MediaLength(
                MediaLength.LengthUnit.MILLISECONDS,
                20);
        mediaProperties.addLength(mediaLength_MS_20);
        assertEquals("Value of length does not match length passed to addLength method",
                20, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // 3 Add duplicate, i.e. a second length with same unit.
        MediaLength mediaLength_MS_30 = new MediaLength(
                MediaLength.LengthUnit.MILLISECONDS,
                30);
        mediaProperties.addLength(mediaLength_MS_30);
        assertEquals("Value of length does not match length passed to addLength method",
                30, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // 4 Add a length in unit PAGES
        MediaLength mediaLength_PAGES_1 = new MediaLength(
                MediaLength.LengthUnit.PAGES,
                1);
        mediaProperties.addLength(mediaLength_PAGES_1);
        assertEquals("Value of length does not match length passed to addLength method",
                1, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.PAGES));
    }

    /**
     * Tests for the method
     * {@link MediaProperties#addLengthInUnit(com.mobeon.masp.mediaobject.MediaLength.LengthUnit, long)}.
     * <p/>
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      1. unit   = null
     *      2. length = null
     *  Result:
     *      1-2. IllegalArgumentException
     * <p/>
     * 2. Assert length is added
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Call addLengthInUnit with :
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Result:
     *      The length is added, i.e. the getLengthInUnit(unit) method
     *      returns 20.
     * <p/>
     * 3. New length with same unit overwrites old length.
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Action:
     *      Call addLengthInUnit with
     *          - unit   = MILLISECONDS
     *          - length = 30
     *  Result:
     *      The new length length in unit MILLISECONDS is 30.
     * <p/>
     * 4. Add a length in unit PAGES
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Action:
     *      Call addMimeType with new length as:
     *         - unit   = PAGES
     *         - length = 2
     *  Result:
     *      Both lengths is present in object.
     * <p/>
     * </pre>
     */
    public void testAddLengthInUnit() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();

        try {
            mediaProperties.addLengthInUnit(
                    (MediaLength.LengthUnit) null, 20);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            mediaProperties.addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, -1);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2. Assert length is added
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS, 20);
        assertEquals("Value of length does not match length passed to addLengthInUnit method",
                20, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // 3 Add duplicate, i.e. a second length with same unit.
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS, 30);
        assertEquals("Value of length does not match length passed to addLengthInUnit method",
                30, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // 4
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.PAGES, 2);
        assertEquals("Value of length does not match length passed to addLengthInUnit method",
                2, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.PAGES));
        assertEquals("Value of length does not match length passed to addLengthInUnit method",
                30, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
    }

    /**
     * Tests for the method
     * {@link MediaProperties#removeLengthInUnit(com.mobeon.masp.mediaobject.MediaLength.LengthUnit)}.
     * <p/>
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *      A non-null empty (no lengths added) MediaProperties object is created.
     *  Action:
     *      Calling method with null.
     * <p/>
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 2. Assert length is removed
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Action:
     *      Call removeLengthInUnit with unit MILLISECONDS
     *  Result:
     *      The length is removed, i.e. the getLengthInUnit(unit) method
     *      returns the length object added, and its value is
     *      20.
     * <p/>
     * 3. Remove one of two added lengths.
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS, length = 20
     *          - unit   = PAGES       , length = 1
     *  Action:
     *      Call removeLengthInUnit with unit MILLISECONDS,
     *  Result:
     *      The length with unit MILLISECONDS is removed.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testRemoveLengthInUnit() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();
        try {
            mediaProperties.removeLengthInUnit(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2 Assert length is removed
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS,
                20);
        assertTrue("Condition for test failed as " +
                "a length with unit MILLISECONDS is not added",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        mediaProperties.removeLengthInUnit(MediaLength.LengthUnit.MILLISECONDS);
        assertFalse("Failed to remove length in unit",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // 3 Remove one of two added lengths.
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS,
                20);
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.PAGES,
                1);
        mediaProperties.removeLengthInUnit(MediaLength.LengthUnit.MILLISECONDS);
        assertFalse("Did not remove length in specified unit",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertTrue("Did not remove length in specified unit",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
    }

    /**
     * Tests for the method
     * {@link MediaProperties#hasLengthInUnit(com.mobeon.masp.mediaobject.MediaLength.LengthUnit)}.
     * <p/>
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *      A non-null empty (no lengths added) MediaProperties object is created.
     *  Action:
     *      Calling method with null.
     * <p/>
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 2. Return false if length in unit is not present
     *  Condition:
     *      A non-null MediaProperties object is created with following
     *      length added:
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Action:
     *      Call hasLengthInUnit with unit PAGES
     *  Result:
     *      Returns false.
     * <p/>
     * 3. Always return false if no lengths added
     *  Condition:
     *      A non-null MediaProperties object is created with
     *      no lengths (empty),
     *  Action:
     *      1 Call hasLengthInUnit with unit MILLISECONDS.
     *      2 Call hasLengthInUnit with unit PAGES.
     *  Result:
     *      1-2. Returns false.
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testHasLengthInUnit() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();
        try {
            mediaProperties.hasLengthInUnit(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2 Return false if length in unit is not present
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS,
                20);
        assertTrue("Condition for test failed as " +
                "a length with unit MILLISECONDS is not added",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertFalse("Failed to remove length in unit",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));

        // 3 Always return false if no lengths added

        mediaProperties.removeLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS);
        assertFalse("hasLengthInUnit should always return false" +
                " if no lengths are added",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertFalse("hasLengthInUnit should always return false" +
                " if no lengths are added",
                mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
    }

    /**
     * Tests for the methods
     * {@link MediaProperties#getSize()} and
     * {@link MediaProperties#setSize(long)}.
     * <p/>
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Calling setSize with -1
     *  Result:
     *      IllegalArgumentException
     * <p/>
     * 2. setSize succeeds
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Calling setSize with -1024
     *  Result:
     *      getSize method returns 1024.
     * <p/>
     * </pre>
     */
    public void testGetAndSetSize() {
        // Condition
        MediaProperties mediaProperties = new MediaProperties();

        try {
            mediaProperties.setSize(-1);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        mediaProperties.setSize(1024);
        assertEquals("Size should be 1024",
                1024, mediaProperties.getSize());
    }

    /**
     * Tests for the method
     * {@link MediaProperties#getAllMediaLengths()}.
     * <p/>
     * <pre>
     * <p/>
     * 1. Assert that empty list is returned on empty
     *   Condition:
     *      A non-null empty MediaProperties object is created.
     *  Action:
     * <p/>
     *  Result:
     *      Empty list.
     * <p/>
     * 2. Assert added length is added
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Call addLengthInUnit with :
     *          - unit   = MILLISECONDS
     *          - length = 20
     *  Result:
     *      Method returns a list of 1 MediaLength with data as:
     *           - unit   = MILLISECONDS
     *          - length = 20
     * <p/>
     * 2. Assert multiple added lengths is returned
     *  Condition:
     *      A non-null MediaProperties object is created.
     *  Action:
     *      Call addLengthInUnit with :
     *          1) 20 MS
     *           2) 1 PAGES
     *  Result:
     *      Method returns a list of 2 MediaLength with data as above:
     *           1) 20 MS
     *           2) 1 PAGES
     * <p/>
     * </pre>
     */
    public void testGetAllMediaLengths() {
        // 1
        MediaProperties mediaProperties = new MediaProperties();
        assertNotNull("List of MediaLengths must never be null",
                mediaProperties.getAllMediaLengths());

        // 2. Assert length is added
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS, 20);
        List<MediaLength> list = mediaProperties.getAllMediaLengths();
        assertEquals("Length of list should be 1",
                1, list.size());

        // 3
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.PAGES, 1);
        list = mediaProperties.getAllMediaLengths();
        assertEquals("Length of list should be 2",
                2, list.size());
        boolean foundMS = false;
        boolean foundPages = false;
        for (MediaLength mediaLength : list) {
            if (mediaLength.getUnit() == MediaLength.LengthUnit.PAGES) {
                assertEquals("pages should be 1", 1, mediaLength.getValue());
                foundPages = true;
            } else if (mediaLength.getUnit() == MediaLength.LengthUnit.MILLISECONDS) {
                foundMS = true;
                assertEquals("pages should be 20", 20, mediaLength.getValue());

            }
        }
        assertTrue("Length in unit MS not found", foundMS);
        assertTrue("Length in unit PAGES not found", foundPages);

    }

    /**
     * Tests for the methods
     * {@link MediaProperties#setContentType(jakarta.activation.MimeType)} and
     * {@link MediaProperties#getContentType()}.
     * <p/>
     * <p/>
     * 1. Set/Get content-type
     * Condition:
     * <p/>
     * Action:
     * Create an empty MediaProperties and set
     * content-type to audio/pcmu.
     * Result:
     * getContentType returns same as passed to set method.
     */
    public void testSetContentType() {
        // setup MimeTypes
        MimeType audioPcmu = null;
        MimeType videoMpeg = null;
        try {
            audioPcmu = new MimeType("audio", "pcmu");
            videoMpeg = new MimeType("video", "mpeg");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }

        MediaProperties mediaProperties = new MediaProperties();
        mediaProperties.setContentType(audioPcmu);
        assertSame("Content-Type is not set",
                audioPcmu, mediaProperties.getContentType());

    }

    /**
     * Tests for the methods
     * {@link MediaProperties#setFileExtension(String)} and
     * {@link MediaProperties#getFileExtension()}.
     * <p/>
     * 1. Set/Get File-Extension.
     * Condition:
     * <p/>
     * Action:
     * Create an empty MediaProperties and set
     * file-extension to audio/pcmu.
     * Result:
     * getFileExtension returns same as passed to set method.
     */
    public void testSetFileExtentsion() {
        final String EXT = "wav";
        MediaProperties mediaProperties = new MediaProperties();
        mediaProperties.setFileExtension(EXT);
        assertEquals("Extension is not set",
                EXT, mediaProperties.getFileExtension());

    }


    /**
     * Tests that toString not crashes.
     * @throws Exception
     */
    public void testToString() throws Exception {

        MediaProperties mp = new MediaProperties();
        mp.toString();
        mp.setContentType(null);
        mp.toString();
        mp.setContentType(new MimeType("plain/text"));
        mp.toString();
        mp.setFileExtension(null);
        mp.toString();
        mp.setFileExtension("txt");
        mp.toString();
        mp.setSize(123);
        mp.toString();
        mp.addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS,9000);
        mp.toString();
        mp.addLengthInUnit(MediaLength.LengthUnit.PAGES,5);
        mp.toString();
    }

    /**
     * Tests the thread-safete of MediaProperties
     */
    public void testConcurrent() {
        // todo
    }

    private class MediaPropertiesClient extends Thread {

        private MediaProperties mediaProperties;

        public MediaPropertiesClient(MediaProperties mediaProperties) {
            this.mediaProperties = mediaProperties;
        }

        public void run() {
            // todo
        }
    }

}
