/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Unit tests for class {@link MediaObjectNativeAccessImpl}
 *
 * @author Mats Egland
 */
public class MediaObjectNativeAccessImplTest extends TestCase {
     /**
     * MediaObject that is provided access to.
     */
    private MediaObject mediaObject;
    /**
     * The number of byte buffers created.
     */
    private final static int NR_OF_BUFFERS = 5;
    /**
     * The size of each ByteBuffer created.
     */
    private final static int BUFFER_SIZE = 1024;
    /**
     * The array of ByteBuffers's of size NR_OF_BUFFERS created in the setUp method.
     */
    private ByteBuffer byteBuffers[] = new ByteBuffer[NR_OF_BUFFERS];

    /**
     * Creates the empty mediaobject member.
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        mediaObject = new MediaObject();
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.allocateDirect(BUFFER_SIZE);

        }
    }

    /**
     * Tests for the constructor
     * {@link MediaObjectNativeAccessImpl#MediaObjectNativeAccessImpl(MediaObject)}.
     *
     * <pre>
     * 1. IllegalArgument
     *  Condition:
     *  Action:
     *      mediaObject = null
     *  Result:
     *      IllegalArgumentException
     *
     * 2. Create with non-null MediaObject.
     *  Condition:
     *      A non-null media object is created.
     *  Action:
     *      Call constructor with non-null mediaobject.
     *  Result:
     *      MediaObjectNatvieAccessImpl is created.
     * </pre>
     */
    public void testConstructor() {
        // 1
        try {
            MediaObjectNativeAccessImpl access =
                        new MediaObjectNativeAccessImpl(null);
            fail("Should throw IllegalArgumentException if mediaObject is null");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        MediaObjectNativeAccessImpl access =
                        new MediaObjectNativeAccessImpl(mediaObject);
        assertNotNull("Failed to create MediaObjectNativeAccessImpl", access);
    }
    /**
     * Tests for the append method and that the Iterator returned from the
     * iterator method conforms to the appended content.
     * <pre>
     * 1. Exception is not thrown when calling
     *    iterator-method on immutable MediaObject
     *  Condition:
     *      A MediaObjectNativeAccessImpl object that provides access to
     *      a MediaObject that is immutable.
     * Action:
     *    1. call iterator() method
     *    2. set media object to immutable and then call iterator() method
     *  Result:
     *    1. IllegalStateException is thrown
     *    2. IllegalStateException is NOT thrown
     * <p/>
     * 2. Iterate over the content of a MediaObject fed with the append method
     *  Condition:
     *      A MediaObject is created and appended with the content of the
     *      byte buffers array
     *  Action:
     *      Iterate over each ByteBuffer in the MediaObject and count them.
     *  Result:
     *      1. The index of each ByteBuffer corresponds to its position
     *         in the iteration.
     *      2. The length of the iteration is equal to the number of buffers appended.
     * <p/>
     * 3. Iterate over the content of a MediaObject fed with the constructor
     *  Condition:
     *      A MediaObject is created with a list of ByteBuffersy
     *  Action:
     *    Iterate over each ByteBuffer in the MediaObject and count them.
     *  Result:
     *      1. The index of each ByteBuffer corresponds to its position
     *         in the iteration.
     *      2. The length of the iteration is equal to the number of buffers appended.
     * <p/>
     * </pre>
     */
    public void testIterator() {
        // condition
        MediaObjectNativeAccessImpl access =
                        new MediaObjectNativeAccessImpl(mediaObject);
        // 1
        IMediaObjectIterator iter = null;
        try {
            iter = access.iterator();
            fail("iterator method should throw IllegalStateException when called on " +
                    " a mutable MediaObject");
        } catch (IllegalStateException e) {/*ok*/}
        mediaObject.setImmutable();
        try {
            iter = access.iterator();
        } catch (IllegalStateException e) {
            fail("Should not throw IllegalStateException if immutable");
        }

        // 2
        mediaObject = new MediaObject();
        access = new MediaObjectNativeAccessImpl(mediaObject);
        for (ByteBuffer byteBuffer : byteBuffers) {
            access.append(byteBuffer);
        }

        mediaObject.setImmutable();
        iter = access.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ByteBuffer currentBuffer = null;
            try {
                currentBuffer = iter.next();
            } catch (MediaObjectException e) {
                fail("Failed to call next on MediaObjectIterator");
            }

            i++;
        }
        assertEquals(
                "The number of ByteBuffers in iteration " +
                        "should be " + byteBuffers.length +
                        ", it is " + i, byteBuffers.length, i);

        // 3
        try {
            mediaObject = new MediaObject(Arrays.asList(byteBuffers), null);
            mediaObject.setImmutable();
            iter = access.iterator();
            i = 0;
            while (iter.hasNext()) {
                ByteBuffer currentBuffer = iter.next();
                i++;
            }
            assertEquals(i, byteBuffers.length);
        } catch (MediaObjectException e) {
            fail("Failed to create mediaobject");
        }
    }

    /**
     * Tests for the method
     * {@link MediaObjectNativeAccessImpl#append(java.nio.ByteBuffer)}.
     *
     * <pre>
     * 2. MediaObjectException
     *  Condition:
     *      A MediaObjectNativeAccessImpl object that provides access to
     *      a MediaObject that is mutable.
     *  Action:
     *      Call append with a
     *  Result:
     *      IllegalStateException
     *
     * 2. IllegalStateException
     *  Condition:
     *      A MediaObjectNativeAccessImpl object that provides access to
     *      a MediaObject that is immutable.
     *  Action:
     *      Call append with direct buffer
     *  Result:
     *      IllegalStateException
     *
     * 3. IllegalArgumentException
     *  Condition:
     *      A MediaObjectNativeAccessImpl object that provides access to
     *      a MediaObject that is mutable.
     *  Action:
     *      Call append with non-direct buffer
     *  Result:
     *      IllegalArgumentException
     *
     * 4. Append direct buffers on mutable mediaobject
     *  Condition:
     *      A MediaObjectNativeAccessImpl object that provides access to
     *      a MediaObject that is mutable.
     *  Action:
     *      Call append with NR_OF_BUFFERS direct buffers
     *  Result:
     *      The iterator has NR_OF_BUFFERS.
     * </pre>
     */
    public void testAppend() {
        // condition
        MediaObjectNativeAccessImpl access =
                        new MediaObjectNativeAccessImpl(mediaObject);

        // 2
        mediaObject.setImmutable();
        try {
            access.append(ByteBuffer.allocateDirect(BUFFER_SIZE));
            fail("IllegalStateException should be thrown if mediaobject is immutable");
        } catch (IllegalStateException e) {/*ok*/}

        // 3
        mediaObject = new MediaObject();
        access = new MediaObjectNativeAccessImpl(mediaObject);
        try {
            access.append(ByteBuffer.allocate(BUFFER_SIZE));
            fail("IllegalStateException should be thrown if mediaobject is immutable");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 4
        mediaObject = new MediaObject();
        access = new MediaObjectNativeAccessImpl(mediaObject);
        for (ByteBuffer byteBuffer : byteBuffers) {
            access.append(byteBuffer);
        }
        mediaObject.setImmutable();
        IMediaObjectIterator iter = access.iterator();
        int nrOfBuffers = 0;
        while (iter.hasNext()) {
            try {
                iter.next();
            } catch (MediaObjectException e) {
                fail("Failed to retreive next ByteBuffer");
            }
            nrOfBuffers++;
        }
        assertEquals("Number of buffers should be " + NR_OF_BUFFERS,
                NR_OF_BUFFERS, nrOfBuffers);
    }
}
