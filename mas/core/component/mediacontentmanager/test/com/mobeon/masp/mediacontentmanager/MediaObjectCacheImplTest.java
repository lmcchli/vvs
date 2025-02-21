/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Unit tests for the class {@link MediaObjectCacheImpl}.
 *
 * @author Mats Egland
 */
public class MediaObjectCacheImplTest extends TestCase {
    /**
     * Tested cache.
     */
    private MediaObjectCacheImpl mediaObjectCache;
    /**
     * Used to create mediaobjects.
     */
    private IMediaObjectFactory mediaObjectFactory;
    /**
     * Number of media objects created and cached.
     */
    private int NR_OF_MEDIA_OBJECTS = 1000;
    /**
     * 5 Sec element timeout
     */
    private long ELEMENT_TIMEOUT_MS = 1000;
    /**
     * Max numbers of objects in cache.
     */
    private int MAX_SIZE = 100;

    private IMediaObject[] mediaObjectArray = new
            IMediaObject[NR_OF_MEDIA_OBJECTS];

    /**
     * Creates the tested MediaContentManager from
     * spring factory.
     *
     * @throws Exception If error occurrs.
     */
    protected void setUp() throws Exception {
        super.setUp();

        mediaObjectFactory = new MediaObjectFactory(8000);
        mediaObjectCache = new MediaObjectCacheImpl(MediaObjectCacheImpl.POLICY.LFU,
                MAX_SIZE, ELEMENT_TIMEOUT_MS, false);

        for (int i = 0; i < mediaObjectArray.length; i++) {
            mediaObjectArray[i] = mediaObjectFactory.create();

        }

        assertNotNull(mediaObjectCache);
    }

    /**
     * Unit test for constructor
     * {@link MediaObjectCacheImpl#MediaObjectCacheImpl(com.mobeon.masp.mediacontentmanager.MediaObjectCacheImpl.POLICY, int, long, boolean)}.
     *
     * 1. IllegalArgument
     *  Condition:
     *  Action:
     *      1. policy = null
     *      2. maxSize = 0
     *      3. elementTimeout = 0
     *
     *  Result:
     *      1-3 IllegalArgumentException
     *
     * 1. Create cache
     *  Condition:
     *  Action:
     *      policy=LFU, maxSize=100, elementTimeout=10000
     *
     *  Result:
     *      Created cache
     */
    public void testConstructor() {
        // 1
        MediaObjectCacheImpl cache;
        try{
            cache = new MediaObjectCacheImpl(null, 1, 1, false);
        } catch (IllegalArgumentException e) {/*ok*/}
        try{
            cache = new MediaObjectCacheImpl(MediaObjectCacheImpl.POLICY.LFU, 0, 1, false);
        } catch (IllegalArgumentException e) {/*ok*/}
        try{
            cache = new MediaObjectCacheImpl(MediaObjectCacheImpl.POLICY.LFU, 100, 0, false);
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        cache = new MediaObjectCacheImpl(MediaObjectCacheImpl.POLICY.LFU, 100, 10000, false);
        assertNotNull("Failed to create cache", cache);
    }
    /**
     * Tests a cache with LFU policy.
     *
     *
     * <p/>
     * <pre>
     * 1. Add MAX_SIZE number of media objects.
     *  Condition:
     *      A LFU cache is created.
     *  Action:
     *      Add MAX_SIZE number of media objects.
     *  Result:
     *      All media objects are in the cache and is
     *      associated with the key they are added with.
     *
     * <p/>
     * 2. Overfill cache
     *  Condition:
     *      A LFU cache is prefilled with MAX_SIZE number
     *      of media objects.
     *  Action:
     *      1)  Request all but the last mediaobject, then
     *          add a new mediaObject.
     *      2)  First, request all but the last mediaobject ten times over.
     *          Then, request the last mediaobject.
     *          Last, add a new mediaObject
     *  Result:
     *      1-2) All added mediaobjects added but the last one is present
     *           (and the newly added is also in cache).
     *
     * </pre>
     */
    public void testLFU_Policy() {
        // 1
        for (int i = 0; i < MAX_SIZE; i++) {
            mediaObjectCache.add("" + i, mediaObjectArray[i]);
        }
        for (int i = 0; i < MAX_SIZE; i++) {
            IMediaObject iMediaObject =
                    mediaObjectCache.get("" + i);
            assertNotNull("MediaObject with key " + i + " should be in cache");
            assertSame("Wrong MediaObject is returned",
                    iMediaObject, mediaObjectArray[i]);

        }

        // 2  (1)
        for (int i = 0; i < MAX_SIZE - 1; i++) {
            mediaObjectCache.get("" + i);
        }
        for (int i = 0; i < MAX_SIZE - 1; i++) {
            IMediaObject iMediaObject =
                    mediaObjectCache.get("" + i);
            assertNotNull("MediaObject with key " + i + " should be in cache");
            assertSame("Wrong MediaObject is returned",
                    iMediaObject, mediaObjectArray[i]);
        }
        mediaObjectCache.add("new", mediaObjectFactory.create());
        assertEquals("Size of cache should be " + MAX_SIZE,
                    MAX_SIZE, mediaObjectCache.size());
        assertNotNull(mediaObjectCache.get("new"));
        IMediaObject iMediaObject =
                mediaObjectCache.get("" + (MAX_SIZE - 1));
        assertNull("MediaObject with key " + (MAX_SIZE - 1) + " should not be in cache",
                iMediaObject);
        iMediaObject =
                mediaObjectCache.get("new");
        assertNotNull("MediaObject with key new should be in cache",
                iMediaObject);
        // 2 (2)
        mediaObjectCache.clear();
        for (int i = 0; i < MAX_SIZE; i++) {
            mediaObjectCache.add("" + i, mediaObjectArray[i]);
        }
        for (int requests = 0; requests < 2; requests++) {
            for (int i = 0; i < MAX_SIZE-1; i++) {
                iMediaObject =
                        mediaObjectCache.get("" + i);
                assertNotNull("MediaObject with key " + i + " should be in cache");
                assertSame("Wrong MediaObject is returned",
                        iMediaObject, mediaObjectArray[i]);
            }
        }
        iMediaObject = mediaObjectCache.get("" + (MAX_SIZE - 1));
        assertNotNull("MediaObject with key " + (MAX_SIZE - 1) + " should be in cache",
                iMediaObject);
        assertSame("Wrong MediaObject is returned",
                        iMediaObject, mediaObjectArray[MAX_SIZE - 1]);
        mediaObjectCache.add("new", mediaObjectFactory.create());
        iMediaObject =
                mediaObjectCache.get("" + (MAX_SIZE - 1));
        assertNull("MediaObject with key " + (MAX_SIZE - 1) + " should not be in cache",
                iMediaObject);
        iMediaObject =
                mediaObjectCache.get("new");
        assertNotNull("MediaObject with key new should be in cache",
                iMediaObject);
        for (int i = 0; i < MAX_SIZE-1; i++) {
            iMediaObject =
                    mediaObjectCache.get("" + i);
            assertNotNull("MediaObject with key " + i + " should be in cache");
            assertSame("Wrong MediaObject is returned",
                    iMediaObject, mediaObjectArray[i]);
        }


    }

    /**
     * Tests that the timeout works in the cache.
     *
     * <pre>
     * 1. Add MAX_SIZE of elements and wait ELEMENT_TIMEOUT_MS milliseconds, then
     *  Condition:
     *      A cache with element timout of 500 ms is created and
     *      fed with MAX_SIZE number of media objects.
     *  Action:
     *      Wait 1 second.
     *  Result:
     *      Cache is empty.
     *
     *
     * </pre>
     */
    public void testTimeout() throws InterruptedException {
        // 1
        for (int i = 0; i < MAX_SIZE; i++) {
            mediaObjectCache.add("" + i, mediaObjectArray[i]);
        }
        synchronized (this) {
            wait(ELEMENT_TIMEOUT_MS*2);
        }
         
        for (int i = 0; i < MAX_SIZE; i++) {
            IMediaObject iMediaObject =
                    mediaObjectCache.get("" + i);
            assertNull("MediaObject with key " + i + " should not be in cache",
                    iMediaObject);
        }
    }
}
