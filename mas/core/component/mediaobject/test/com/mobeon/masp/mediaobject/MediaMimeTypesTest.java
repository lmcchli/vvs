/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit-tests for the {@link MediaMimeTypes} class.
 *
 * @author Mats Egland
 */
public class MediaMimeTypesTest extends TestCase {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(MediaMimeTypesTest.class);
    /**
     * Number of concurrent threads to consume
     * a <code>MediaMimeTypes<code> in the concurrent test.
     */
    private static final int CONSUMERS = 10;


    /**
     * Tests for the empty constructor
     * {@link MediaMimeTypes#MediaMimeTypes()}.
     * 
     * <pre>
     * 1. Assert not null
     *  Condition:
     *  Action:
     *      Create MediaMimeTypes object with constructor
     *  Result:
     *      Created MediaMimeTypes object is not null
     *
     * 2. Assert that list of mime-types returned is non-null
     * 	Condition:
     *  Action:
     *      Create MediaMimeTypes object with constructor
     *  Result:
     *      List returned from getAllMimeTypes is not null
     * </pre>
     */
    public void testConstructor() {
        // 1
        MediaMimeTypes mimeTypes = new MediaMimeTypes();
        assertNotNull(mimeTypes);
        // 2
        assertNotNull(mimeTypes.getAllMimeTypes());
    }
    
    /**
     * Tests for the constructor
     * {@link MediaMimeTypes#MediaMimeTypes(List)}.
     * <pre>
     * 1. IllegalArgumentException
     *  Condition:
     *  Action:
     *      1. List is null
     *      2. One element in the list is null
     *  Result:
     *      1-2 IllegalArgumentException is thrown
     *
     * 2. Pass a zero length list.
     *  Condition:
     *  Action:
     *      Passing a list of zero mime-types
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 0.
     *      
     * 3. Pass a list with two non-matching mime-types.
     *  Condition:
     *  Action:
     *      Passing a list of two non-matching mime-types
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 2. 
     * 
     * 4. Pass a list with two matching mime-types.
     *  Condition:
     *  Action:
     *      Passing a list of two mime-types, both with
     *      type "audio/pcmu"
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 1. Only one of the passed mime-types 
     *      should be added as they have same type.
     *      
     * 5. Pass a list of size 2 with the same mime-type object in
     *    both position.
     *  Condition:
     *  Action:
     *      Passing a list of two mime-types, but both
     *      is the same object. 
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 1. Only one of the passed mime-types 
     *      should be added as they are same object.
     *
     * </pre>
     * 
     */
    public void testConstructor1() {
    	// conditions and setup
    	MimeType audioPcmu = null;
    	MimeType audioPcmu1= null;
    	MimeType videoQuicktime = null;
		try {
			audioPcmu = new MimeType("audio/pcmu");
			audioPcmu1 = new MimeType("audio/pcmu");
			videoQuicktime = new MimeType("video/quicktime");
		} catch (MimeTypeParseException e1) {
			fail("Failed to create MimeType objects");
		}
		List<MimeType> list = new ArrayList<MimeType>();
		
        // 1
    	try {
    		MediaMimeTypes mimeTypes = new MediaMimeTypes((List<MimeType>)null);
    		fail("IllegalArgumentException should be thrown if " +
    				"passing null to constructor");
		} catch (IllegalArgumentException e) {/*ok*/}
		list.add(audioPcmu);
		list.add(null);
		try {
    		MediaMimeTypes mimeTypes = new MediaMimeTypes(list);
    		fail("IllegalArgumentException should be thrown if " +
    				"one of the elements in list is null");
		} catch (IllegalArgumentException e) {/*ok*/}
		
		// 2
		list.clear();
		MediaMimeTypes mimeTypes = new MediaMimeTypes(list);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 0",
				0, mimeTypes.getNumberOfMimeTypes());
		
        // 3
		list = new ArrayList<MimeType>();
		list.add(audioPcmu);
		list.add(videoQuicktime);
		mimeTypes = new MediaMimeTypes(list);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 2",
				2, mimeTypes.getNumberOfMimeTypes());
		
		// 4
		list = new ArrayList<MimeType>();
		list.add(audioPcmu);
		list.add(audioPcmu1);
		mimeTypes = new MediaMimeTypes(list);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 1",
				1, mimeTypes.getNumberOfMimeTypes());
        
		// 5
		list = new ArrayList<MimeType>();
		list.add(audioPcmu);
		list.add(audioPcmu);
		mimeTypes = new MediaMimeTypes(list);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 1",
				1, mimeTypes.getNumberOfMimeTypes());
    }
    
    /**
     * Tests for the constructor
     * {@link MediaMimeTypes#MediaMimeTypes(jakarta.activation.MimeType...)}.
     * <pre>
     * 
     * 1. IllegalArgumentException
     *  Condition:
     *  Action:
     *      1. Array is null
     *      2. One element in the array is null
     *  Result:
     *		1-2 IllegalArgumentException is thrown
     *
     * 2. Pass a zero length array.
     *  Condition:
     *  Action:
     *      Passing a array of zero mime-types
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 0.
     *      
     * 3. Pass a list with two non-matching mime-types.
     *  Condition:
     *  Action:
     *      Passing a list of two non-matching mime-types
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 2. 
     * 
     * 4. Pass a list with two matching mime-types.
     *  Condition:
     *  Action:
     *      Passing a list of two mime-types, both with
     *      type "audio/pcmu"
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 1. Only one of the passed mime-types 
     *      should be added as they have same type.
     * 
     * 5. Pass an array of size 2 with the same mime-type object in
     *    both position.
     *  Condition:
     *  Action:
     *      Passing an array of two mime-types, but both
     *      is the same object. 
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 1. Only one of the passed mime-types 
     *      should be added as they are same object.
     *
     * </pre>
     * 
     */
    public void testConstructor2() {
    	// conditions and setup
    	MimeType audioPcmu = null;
    	MimeType audioPcmu1= null;
    	MimeType videoQuicktime = null;
		try {
			audioPcmu = new MimeType("audio/pcmu");
			audioPcmu1 = new MimeType("audio/pcmu");
			videoQuicktime = new MimeType("video/quicktime");
		} catch (MimeTypeParseException e1) {
			fail("Failed to create MimeType objects");
		}
		
        
		// 1
    	try {
    		MediaMimeTypes mimeTypes = new MediaMimeTypes((MimeType[])null);
    		fail("IllegalArgumentException should be thrown if " +
    				"passing null to constructor");
		} catch (IllegalArgumentException e) {/*ok*/}
		try {
    		MediaMimeTypes mimeTypes = 
    			new MediaMimeTypes(new MimeType[] {audioPcmu, null});
    		fail("IllegalArgumentException should be thrown if " +
    				"one of MimeTypes is null in array");
		} catch (IllegalArgumentException e) {/*ok*/}
		
		
		// 2
		MimeType[] mimeTypeArray = new MimeType[] {};
		MediaMimeTypes mimeTypes = 
			new MediaMimeTypes(mimeTypeArray);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 0",
				0, mimeTypes.getNumberOfMimeTypes());
		
        // 3
		mimeTypeArray = new MimeType[] {audioPcmu, videoQuicktime};
		mimeTypes = new MediaMimeTypes(mimeTypeArray);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 2",
				2, mimeTypes.getNumberOfMimeTypes());
		
		// 4
		mimeTypeArray = new MimeType[] {audioPcmu, audioPcmu1};
		mimeTypes = new MediaMimeTypes(mimeTypeArray);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 1",
				1, mimeTypes.getNumberOfMimeTypes());
        
		// 4
		mimeTypeArray = new MimeType[] {audioPcmu, audioPcmu};
		mimeTypes = new MediaMimeTypes(mimeTypeArray);
		assertNotNull("Failed to create MediaMimeType",
				mimeTypes);
		assertNotNull(mimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 1",
				1, mimeTypes.getNumberOfMimeTypes());
    }
    
    /**
     * Tests for the constructor
     * {@link MediaMimeTypes#MediaMimeTypes(MediaMimeTypes)}.
     * <pre>
     * 
     * 1. IllegalArgumentException
     *  Condition:
     *  Action:
     *      argument is null
     *  Result:
     *		IllegalArgumentException is thrown
     *
     * 2. Pass a MediaMimeTypes with no MimeTypes.
     *  Condition:
     *  Action:
     *  Result:
     *      MediaMimeTypes object created and the list of 
     *      mimetypes is non-null and the number of mime-types
     *      is 0. 
     * 
     * 3. Pass a MediaMimeTypes with MimeTypes 
     * 	  "audio/pcmu" and "video/quicktime"
     *  Condition:
     *  Action:
     *  Result:
     *      - MediaMimeTypes object created.
     *      - The list of mimetypes is non-null.
     *      - The number of mime-types is 2.
     *       
     * 4. Adding a MimeType to a MediaMimeTypes used as argument
     *    when creating a second MediaMimeTypes, does not affect the
     *    second MediaMimeType.
     *  Condition:
     *  	A MediaMimeTypes object with mime-types "audio/pcmu"
     *  	and "video/quictime" is used as argument to create
     *  	a second MediaMimeTypes object.
     *  Action:
     *  	Add a new mime-type "audio/amr" to the first MediaMimeTypes.
     *  Result:
     *      - Number of mime-types in first MediaMimeTypes object is 3
     *      - Number of mime-types in second MediaMimeTypes object is
     *        still 2.
     * </pre>
     * 
     */
    public void testConstructor3() {
    	// conditions and setup
    	MimeType audioPcmu = null;
    	MimeType audioPcmu1= null;
    	MimeType videoQuicktime = null;
		MimeType audioAmr = null;
    	try {
			audioPcmu = new MimeType("audio/pcmu");
			audioPcmu1 = new MimeType("audio/pcmu");
			audioAmr = new MimeType("audio/amr");
			videoQuicktime = new MimeType("video/quicktime");
		} catch (MimeTypeParseException e1) {
			fail("Failed to create MimeType objects");
		}
		        
		// 1
    	try {
    		MediaMimeTypes mimeTypes = new MediaMimeTypes((MediaMimeTypes)null);
    		fail("IllegalArgumentException should be thrown if " +
    				"passing null to constructor");
		} catch (IllegalArgumentException e) {/*ok*/}
				
		// 2
		MediaMimeTypes prototype = new MediaMimeTypes();
		MediaMimeTypes mediaMimeTypes = 
			new MediaMimeTypes(prototype);
		assertNotNull("Failed to create MediaMimeType",
				mediaMimeTypes);
		assertNotNull(mediaMimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 0",
				0, mediaMimeTypes.getNumberOfMimeTypes());
		
        // 3
		prototype = new MediaMimeTypes(audioPcmu, videoQuicktime);
		mediaMimeTypes = new MediaMimeTypes(prototype);
		assertNotNull("Failed to create MediaMimeType",
				mediaMimeTypes);
		assertNotNull(mediaMimeTypes.getAllMimeTypes());
		assertEquals("Number of mime-types should be 2",
				2, mediaMimeTypes.getNumberOfMimeTypes());
		
		// 4
		prototype.addMimeType(audioAmr); 
		assertEquals("Number of mimetypes should be three",
				3, prototype.getNumberOfMimeTypes());
		assertEquals("Number of mimetypes should still be 2 ",
				2, mediaMimeTypes.getNumberOfMimeTypes());		
		
        
	 
    }
    /**
     * Tests for the {@link MediaMimeTypes#addMimeType(MimeType)} method.
     * 
     * <pre>
     * 1. Null argument
     *  Condition:
     *      A non-null MediaMimeTypes object is created
     *  Action:
     *      Calling method with null.
     *  Result:
     *      IllegalArgumentException
     * 
     * 2. Add a MimeType and check content
     *  Condition:
     *      A non-null MediaMimeTypes object is created.
     *      A non-null MimeType's is created with type audio/pcmu
     *  Action:
     *          Call addMimeType
     *  Result:
     *          - The mime-type is added - i.e. the method getNumberOfMimeTypes
     *            returns 1.
     *          - The method hasMimeType returns true when
     *            added MimeType is passed as argument
     * 
     * 2. Add duplicate MimeType
     *  Condition:
     *      - A non-null MediaMimeTypes added with MimeType object with type audio/pcmu.
     *      - A second non-null MimeType's is created with same audio/pcmu
     *  Action:
     *      Call addMimeType with second MimeType
     *  Result:
     *          - The mime-type is not added, i.e. the number
     *            of mime-types should still be 1 and the MediaMimeTypes object
     *            only contains the first MimeType added
     *          - There should be a matching mime-type for the duplicate
     *            in the MediaMimeTypes object.
     * 
     * 3. Add another non-duplicate mime-type
     *  Condition:
     *      - A non-null MediaMimeTypes added with MimeType object with type audio/pcmu.
     *      - A second non-null MimeType's is created with same video/mpeg.
     *  Action:
     *      Call addMimeType with second MimeType
     *  Result:
     *          - The second mime-type is added, i.e. the number
     *            of mime-types should be 2.
     *          - There should be a matching mime-type for both
     *            the first added and the second added MimeTypes
     *            in the MediaMimeTypes object.
     * </pre>
     */
    public void testAddMimeType() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        // 1
        try {
            mediaMimeTypes.addMimeType(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}


        try {
            // 2
            MimeType mimeType1 = new MimeType("audio", "pcmu");
            mediaMimeTypes.addMimeType(mimeType1);
            assertEquals("Number of mimetypes returned should be 1",
                    1, mediaMimeTypes.getNumberOfMimeTypes());
            assertTrue("The MediaMimeType should have the added mime type",
                    mediaMimeTypes.hasMatchingMimeType(mimeType1));

            // 3
            MimeType mimeType2 = new MimeType("audio", "pcmu");
            mediaMimeTypes.addMimeType(mimeType2);
            assertEquals(
                    "Number of mimetypes returned should be 1 when a duplicate is added",
                    1, mediaMimeTypes.getNumberOfMimeTypes());
            assertTrue(
                    "The first added mimetype is the one still in MediaMimeTypes " +
                            "object after duplicate added",
                    mediaMimeTypes.getAllMimeTypes().contains(mimeType1));
            assertTrue(
                    "There should be a matching mime-type to the mime-type rejected be addMimeType method",
                    mediaMimeTypes.hasMatchingMimeType(mimeType2));

            // 4
            MimeType mimeType3 = new MimeType("video", "mpeg");
            mediaMimeTypes = new MediaMimeTypes();
            mediaMimeTypes.addMimeType(mimeType1);
            mediaMimeTypes.addMimeType(mimeType3);
            assertEquals(
                    "Number of mimetypes returned should be 2",
                    2, mediaMimeTypes.getNumberOfMimeTypes());
            assertTrue(
                    "First added mimetype should be in MimeTypes object",
                    mediaMimeTypes.getAllMimeTypes().contains(mimeType1));
            assertTrue(
                    "Second added mimetype should be in MimeTypes object",
                    mediaMimeTypes.getAllMimeTypes().contains(mimeType3));

            assertTrue(
                    "There should be a matching mime-type for the first added MimeType",
                    mediaMimeTypes.hasMatchingMimeType(mimeType1));
            assertTrue(
                    "There should be a matching mime-type for the second added MimeType",
                    mediaMimeTypes.hasMatchingMimeType(mimeType3));
        } catch (MimeTypeParseException e) {
            fail("Failed to create a MimeType object used in addMimeType test");
        }

    }

    /**
     * Tests for the {@link MediaMimeTypes#removeMimeType(MimeType)} method.
     * 
     * <pre>
     * 1. Wrong arguments
     *  Condition:
     *     A non-null MediaMimeTypes object is created.
     * Action:
     *      Calling method with null.
     * Result:
     *      IllegalArgumentException
     * 
     * 2. Remove added MimeType
     *  Condition:
     *      A non-null MediaMimeTypes object is created and a MimeType with
     *      subtype "audio/pcmu" is added.
     *  Action:
     *      Call method with the MimeType object added
     * 
     *  Result:
     *      The mime-type is removed, and getNumberOfMimeTypes returns 0.
     * 
     * 3. Remove a different MimeType object that has same subtype as the
     *    mime-type added.
     *  Condition:
     *      A non-null MediaMimeTypes object is created and a MimeType with
     *      subtype "audio/pcmu" is added.
     *  Action:
     *      Call method with a another MimeType-object that has also has
     *      subtype "audio/pcmu".
     *  Result:
     *      The mime-type is NOT removed, and getNumberOfMimeTypes returns 1.
     * 
     *  4. Remove a MimeType thas is first retreived with the
     *     getMatchingMimeType method.
     *  Condition:
     *      A non-null MediaMimeTypes object is created and a MimeType with
     *      subtype "audio/pcmu" is added.
     *  Action:
     *      1) Retreive the MimeType to be removed by calling
     *         getMatchingMimeType with a MimeType with subtype "audio/pcmu"
     * 
     *      2) Call remove with the retreived MimeType
     *  Result:
     *      The mime-type is removed, and getNumberOfMimeTypes returns 0.
     * 
     * </pre>
     */
    public void testRemoveMimeType() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();

        // 1
        try {
            mediaMimeTypes.removeMimeType(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        MimeType mimeType1 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            assertNotNull("Failed to create MimeType", mimeType1);
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        mediaMimeTypes.addMimeType(mimeType1);
        assertEquals("Number of MimeTypes should be 1",
                1, mediaMimeTypes.getNumberOfMimeTypes());
        mediaMimeTypes.removeMimeType(mimeType1);
        assertEquals("Number of MimeTypes should be 0",
                0, mediaMimeTypes.getNumberOfMimeTypes());

        // 3
        MimeType mimeType2 = null;
        try {
            mimeType2 = new MimeType("audio", "pcmu");
            assertNotNull("Failed to create MimeType", mimeType2);
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        mediaMimeTypes.addMimeType(mimeType1);
        mediaMimeTypes.removeMimeType(mimeType2);
        assertEquals("Number of mime-types should be 1 after a remove" +
                " with different object but same mime-type",
                1, mediaMimeTypes.getNumberOfMimeTypes());

        // 4
        assertEquals("Pre-condition for remove test: Number of MimeTypes should be 1",
                1, mediaMimeTypes.getNumberOfMimeTypes());

        try {
            assertTrue("Pre-condition for remove test: There should be " +
                    "a MimeType with subtype audio/pcmu",
                    mediaMimeTypes.hasMatchingMimeType(new MimeType("audio", "pcmu")));
            MimeType mimeType3 = mediaMimeTypes.getMatchingMimeType(
                    new MimeType("audio", "pcmu"));
            mediaMimeTypes.removeMimeType(mimeType3);
        } catch (MimeTypeParseException e) {
            fail("Failed to retrive MimeType with getMatchingMimeType method" +
                    ", even though mime-type with same subtype do exist");
        }
        assertEquals("Number of mime-types should be 0 after a remove" +
                " with MimeType retrieved with getMatchingMimeType",
                0, mediaMimeTypes.getNumberOfMimeTypes());


    }

    /**
     * Tests for the {@link MediaMimeTypes#getAllMimeTypes()} method.
     * 
     * <pre>
     * 1. Assert that number of elements in received list
     *    matches the number added, and that the MimeTypes
     *    added is part of that list.
     *  Condition:
     *      A non-null MediaMimeTypes object is created,
     *      added with two MimeTypes with subtypes
     *      "audio/pcmu" respective "video/mpeg".
     *  Action:
     *      Retreive list of mime-type with the
     *      getMimeTypes method.
     *  Result:
     *      - The number of MimeTypes in MediaMimeTypes object
     *        is 2.
     *      - Both added MimeTypes is part of returned list
     * 
     * 2. Assert that received list is copy.
     *  Condition:
     *      A non-null MediaMimeTypes object is created,
     *      added with two MimeTypes with subtypes
     *      "audio/pcmu" respective "video/mpeg".
     *  Action:
     *      1) Retreive list of mime-type with the
     *         getMimeTypes method and add a new MimeType to
     *         it.
     *      2) Remove all elements in the list
     *  Result:
     *      1) The number of MimeTypes in MediaMimeTypes object
     *         is still 2.
     * 
     * </pre>
     */
    public void testGetAllMimeTypes() {
        // Conditions
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        mediaMimeTypes.addMimeType(mimeType1);
        mediaMimeTypes.addMimeType(mimeType2);

        // 1
        List<MimeType> list = mediaMimeTypes.getAllMimeTypes();
        assertEquals("Number of mime-types in list returned with " +
                "getMimeTypes does not match number of MimeTypes added",
                2, mediaMimeTypes.getNumberOfMimeTypes());
        assertTrue("MimeType added is not part of list returned with getMimeTypes",
                list.contains(mimeType1));
        assertTrue("MimeType added is not part of list returned with getMimeTypes",
                list.contains(mimeType2));

        // 2
        list.add(new MimeType());
        assertEquals("Number of mime-types in MediaMimeTypes object " +
                "should be unaffected by operating on the list " +
                "retrieved with getMimeTypes",
                2, mediaMimeTypes.getNumberOfMimeTypes());
        list.clear();
        assertEquals("Number of mime-types in MediaMimeTypes object " +
                "should be unaffected by operating on the list " +
                "retrieved with getMimeTypes",
                2, mediaMimeTypes.getNumberOfMimeTypes());

    }

    /**
     * Tests for the method {@link MediaMimeTypes#addAll(MediaMimeTypes)}.
     * 
     * <pre>
     * 
     * 1. IllegalArgumentException
     *  Condition:
     *      A non-null MimeTypes-object is created.
     *  Action:
     *      Call addAll method with null as argument
     *  Result:
     *      IllegalArgumentException is thrown
     * 
     * 
     * 2. addAll on a empty MediaMimeTypes-object
     *  Condition:
     *      Two non-null MediaMimeTypes-objects is created,
     *      one with multiple mime-types
     *      and the other empty.
     *  Action:
     *      Calling addAll method on the empty
     *      MediaMimeTypes with the other as argument
     *  Result:
     *      All mimetypes in the passed MediaMimeTypes-object is present in the
     *      other MediaMimeTypes-object, and the number of mime-types match.
     * 
     * 3. addAll on a non-empty MediaMimeTypes-object
     *  Condition:
     *      Two non-null MediaMimeTypes-objects object is created,
     *      both with two mime-types added. One of the Mime-Types in the
     *      MediaMimeTypes-objects has same subtype as:
     * 
     *      MediaMimeType1 Content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     * 
     *      MediaMimeType2 Content:
     *          - MimeType with subtype "audio/pcmu"  (here's the conflict)
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Calling addAll method on MediaMimeType1 with
     *      MediaMimeType2 as argument
     *  Result:
     *      The resulting content of MediaMimeType1:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     * </pre>
     */
    public void testAddAll() {
        // Conditions
        MediaMimeTypes mediaMimeTypes1 = new MediaMimeTypes();
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        MimeType mimeType3 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes2 = new MediaMimeTypes(mimeType1, mimeType2);

        // 1
        try {
            mediaMimeTypes1.addMimeType(null);
            fail("addMimeType should throw IllegalArgumentException when " +
                    "argument is null");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        mediaMimeTypes1.addAll(mediaMimeTypes2);
        assertEquals("The number of mime-types should be 2 after addAll " +
                "is called with a MimeTypes object with two mime-types",
                2, mediaMimeTypes1.getNumberOfMimeTypes());
        List<MimeType> list = mediaMimeTypes1.getAllMimeTypes();
        assertTrue("All added MimeType to a MimeTypes-object should be part of " +
                "another MimeTypes-object after a addAll call is made on it",
                list.contains(mimeType1));
        assertTrue("All added MimeType to a MimeTypes-object should be part of " +
                "another MimeTypes-object after a addAll call is made on it",
                list.contains(mimeType2));

        // 3
        mediaMimeTypes1 = new MediaMimeTypes(mimeType1, mimeType2);
        mediaMimeTypes2 = new MediaMimeTypes(mimeType1, mimeType3);
        mediaMimeTypes1.addAll(mediaMimeTypes2);
        assertEquals("Number of mime-types in MimeTypes-object with 2 mime-types " +
                "should be 3 after a addAll call on it with a MimeTypes-object with " +
                "2 mime-types, where one has same subtype as one of the mime-types in " +
                "the first MimeTypes-object",
                3, mediaMimeTypes1.getNumberOfMimeTypes());
        list = mediaMimeTypes1.getAllMimeTypes();
        assertTrue("MimeType not part of MimeTypes-object after addAll call",
                list.contains(mimeType1));
        assertTrue("MimeType not part of MimeTypes-object after addAll call",
                list.contains(mimeType2));
        assertTrue("MimeType not part of MimeTypes-object after addAll call",
                list.contains(mimeType3));

    }

    /**
     * Tests for the method {@link MediaMimeTypes#getNumberOfMimeTypes()}.
     * <pre>
     * 
     * 1. Get number of mime-types
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call getNumberOfMimeTypes
     *  Result:
     *      Should return 3
     * 
     * </pre>
     */
    public void testGetNumberOfMimeTypes() {
        // Condition
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        MimeType mimeType3 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(
                mimeType1, mimeType2, mimeType3);

        // 1
        assertEquals("Number of mime-types should be 3",
                3, mediaMimeTypes.getNumberOfMimeTypes());
    }

    /**
     * Tests for the method 
     * {@link MediaMimeTypes#hasMatchingMimeType(MimeType)}.     
     *  
     * <pre>
     * 
     * 1. IllegalArgument
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call hasMimeType with null as argument
     *  Result:
     *      IllegalArgumentException is thrown
     * 
     * 2. Argument-object same as added.
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call hasMatchingMimeType with same MimeType as added.
     *  Result:
     *      Returns true
     * 
     * 3. Argument-object has same subtype (i.e. should match) as added
     *    but not same object.
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call hasMatchingMimeType with a MimeType with same subtype as one of
     *      the added MimeTypes.
     *  Result:
     *      Returns true
     * 
     * 4. Non-match
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call hasMatchingMimeType with a MimeType with
     *      subtype  "audio/amr"
     * 
     *  Result:
     *      Returns false
     * </pre>
     */
    public void testHasMatchingMimeType() {
        // Condition
        MimeType mimeType1 = null;
        MimeType mimeType11 = null;
        MimeType mimeType2 = null;
        MimeType mimeType22 = null;
        MimeType mimeType3 = null;
        MimeType mimeType33 = null;
        MimeType mimeType4 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType11 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType22 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
            mimeType33 = new MimeType("video", "h263");
            mimeType4 = new MimeType("audio", "amr");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(
                mimeType1, mimeType2, mimeType3);

        // 1
        try {
            mediaMimeTypes.hasMatchingMimeType(null);
            fail("IllegalArgumentException should be thrown when " +
                    "calling hasMatchingMimeType with null");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has been added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType1));
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has been added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType2));
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has been added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType3));

        // 3
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has same subtype as one of the added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType11));
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has same subtype as one of the added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType22));
        assertTrue("Calling hasMatchingMimeType with an MimeType " +
                "that has same subtype as one of the added should return true",
                mediaMimeTypes.hasMatchingMimeType(mimeType33));

        // 4
        assertFalse("Calling hasMatchingMimeType with an MimeType " +
                "with a subtype that no-one of the added, should return false",
                mediaMimeTypes.hasMatchingMimeType(mimeType4));
    }
    /**
     * Tests for the method 
     * {@link MediaMimeTypes#getMatchingMimeType(MimeType)}.
     *  
     * <pre>
     * 
     * 1. IllegalArgument
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call getMatchingMimeType with null as argument
     *  Result:
     *      IllegalArgumentException is thrown
     * 
     * 2. Argument-object is one of the added.
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType1 with subtype "audio/pcmu"
     *          - MimeType2 with subtype "video/mpeg"
     *          - MimeType3 with subtype "video/h263"
     *  Action:
     *      Call getMatchingMimeType with MimeType1
     *      
     *  Result:
     *      - Returns MimeType1 object 
     *        
     * 
     * 3. Argument-object has same subtype (i.e. should match) as added
     *    but not same object.
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType1 with type "audio/pcmu"
     *          - MimeType2 with type "video/mpeg"
     *          - MimeType3 with type "video/h263"
     *  Action:
     *      Call getMatchingMimeType with a new MimeType object with 
     *      type "video/mpeg".
     *  Result:
     *      Returns MimeType2.
     * 
     * 4. Non-match
     *  Condition:
     *      A non-null MediaMimeTypes-object is created with content:
     *          - MimeType with subtype "audio/pcmu"
     *          - MimeType with subtype "video/mpeg"
     *          - MimeType with subtype "video/h263"
     *  Action:
     *      Call getMatchingMimeType with a MimeType with
     *      subtype  "audio/amr"
     * 
     *  Result:
     *      Returns null.
     * </pre>
     */
    public void testGetMatchingMimeType() {
        // Condition
        MimeType mimeType1 = null;
        MimeType mimeType11 = null;
        MimeType mimeType2 = null;
        MimeType mimeType22 = null;
        MimeType mimeType3 = null;
        MimeType mimeType33 = null;
        MimeType mimeType4 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType11 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType22 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
            mimeType33 = new MimeType("video", "h263");
            mimeType4 = new MimeType("audio", "amr");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(
                mimeType1, mimeType2, mimeType3);

        // 1
        try {
            mediaMimeTypes.getMatchingMimeType(null);
            fail("IllegalArgumentException should be thrown when " +
                    "calling getMatchingMimeType with null");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        MimeType result = mediaMimeTypes.getMatchingMimeType(mimeType1);
        assertSame("The returned object should be same as argument " +
        		"if passing one of previoulsy added MimeTypes",
        		mimeType1, result);

        // 3
        result = mediaMimeTypes.getMatchingMimeType(mimeType22);
        assertSame("The returned object should be not be same as argument, " +
        		"but the object added with the searched mime-type",
        		mimeType2, result);
        

        // 4
        assertNull("Calling hasMatchingMimeType with an MimeType " +
                "with a subtype that no-one of the added, should return false",
                mediaMimeTypes.getMatchingMimeType(mimeType4));
    }
    /**
     * Tests for the method 
     * {@link com.mobeon.masp.mediaobject.MediaMimeTypes#clearMimeTypes()}.
     * <pre>
     * 1. Clear added mime-types
     *  Condition:
     *      A non-null MediaMimeTypes object is created with
     *      three MimeTypes added.
     *  Action:
     *      Call clear method
     *  Result:
     *      All mimetypes is removed
     * </pre>
     */
    public void testClearMimeTypes() {
        // Condition
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        MimeType mimeType3 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(
                mimeType1, mimeType2, mimeType3);

        // 1
        mediaMimeTypes.clearMimeTypes();
        assertEquals("The number of MimeTypes should be 0 after a call to clear",
                0, mediaMimeTypes.getNumberOfMimeTypes());
    }

    /**
     * Test for the method
     * compareTo()
     * <pre>
     * 1. Compare "audio/pcmu video/h263" to "video/h263 audio/pcmu".
     *  Condition:
     *      Two non-null MediaMimeTypes object are created with two MimeTypes
     *      added.
     *  Action:
     *      Call compareTo method with the two MimeMediaTypes.
     *  Result:
     *      The method should return true.
     *
     * 2. Compare "audio/pcmu video/h263" to "audio/pcmu".
     *  Condition:
     *      One MediaMimeTypes object with one MimeType is created,
     *      and one MediaMimeTypes object with two MimeTypes is created.
     *  Action:
     *      Call compareTo method with the two MimeMediaTypes.
     *  Result:
     *      The method should return false.
     *
     * 3. Compare "audio/pcmu video/h263" to "audio/pcmu video/mpeg".
     *  Condition:
     *      Two MediaMimeTypes object are created with two MimeTypes added.
     *  Action:
     *      Call compareTo method with the two MimeMediaTypes.
     *  Result:
     *      The method should return false.
     *
     * </pre>
     */
    public void testCompareTo() {
        // Conditions
        MimeType mimeType1 = null;
        MimeType mimeType2 = null;
        MimeType mimeType3 = null;
        try {
            mimeType1 = new MimeType("audio", "pcmu");
            mimeType2 = new MimeType("video", "mpeg");
            mimeType3 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            fail("Failed to create MimeType");
        }
        MediaMimeTypes mediaMimeTypes1 = new MediaMimeTypes(
                mimeType1, mimeType3);
        MediaMimeTypes mediaMimeTypes2 = new MediaMimeTypes(
                mimeType3, mimeType1);
        MediaMimeTypes mediaMimeTypes3 = new MediaMimeTypes(
                mimeType1);
        MediaMimeTypes mediaMimeTypes4 = new MediaMimeTypes(
                mimeType1, mimeType2);

        // 1
        assertTrue("audio/pcmu, video/h263 should be equal to video/h263, audio/pcmu",
                mediaMimeTypes1.compareTo(mediaMimeTypes2));

        // 2
        assertFalse("audio/pcmu, video/h263 should not be equal to audio/pcmu",
                mediaMimeTypes1.compareTo(mediaMimeTypes3));

        // 3
        assertFalse("audio/pcmu, video/h263 should not be equal to audio/pcmu, video/mpeg",
                mediaMimeTypes1.compareTo(mediaMimeTypes4));
    }

    /**
     * Test concurrent access of a common MediaMimeTypes object.
     * <p/>
     * This test makes no assertions, it just makes a concurrent test of a
     * MediaMimeTypes object by creating <code>CONSUMERS</code> number
     * of concurrent threads that each will consume on the MediaMimeTypes object.
     * <p/>
     * Each Consumer created is of type <code>MediaMimeTypesClient</code>.
     *
     * @throws InterruptedException If any of the created threads is interrupted.
     */
    public void testConcurrent() throws InterruptedException {
        MediaMimeTypesClient[] consumers = new MediaMimeTypesClient[CONSUMERS];
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();

        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new MediaMimeTypesClient(
                    "MediaMimeTypesClient " + i,
                    mediaMimeTypes);
        }
        // start consumers
        for (MediaMimeTypesClient consumer : consumers) {
            consumer.start();

        }
        for (MediaMimeTypesClient consumer : consumers) {
            consumer.join();
        }
    }

    /**
     * Thread that consumes a MediaMimeTypes object
     */
    private class MediaMimeTypesClient extends Thread {
        /**
         * The MediaMimeTypes that this thread consumes
         */
        private MediaMimeTypes mediaMimeTypes;

        public MediaMimeTypesClient(String name, MediaMimeTypes mp) {
            super(name);
            this.mediaMimeTypes = mp;
        }

        public void run() {
            MimeType mimeType = null;
            for (int i = 0; i < 1000; i++) {
                try {
                    mimeType = new MimeType("audio", "h26" + i);

                } catch (MimeTypeParseException e) {
                    fail("MediaMimeTypesClient-Thread with name:" +
                            Thread.currentThread().getName() +
                            " failed to create mime-type with name audio/h26" + i);
                }
                mediaMimeTypes.addMimeType(mimeType);
                mediaMimeTypes.getNumberOfMimeTypes();
                List<MimeType> list = mediaMimeTypes.getAllMimeTypes();
                for (MimeType type : list) {
                    type.getSubType();
                }
                mediaMimeTypes.removeMimeType(mimeType);

            }
        }
    }
    
}
