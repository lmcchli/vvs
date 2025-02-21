/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * JUnit tests for {@link MediaObjectIterator}
 *
 * @author Mats Egland
 */
public final class MediaObjectIteratorTest extends TestCase  {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(MediaObjectIteratorTest.class);
    /**
     * The file that is loaded into <code>MediaObject</code>'s.
     */
    private static String file1 = "test/com/mobeon/masp/mediaobject/test.wav";
    /**
     * The size of the file above.
     */
    private static final long FILE_SIZE = 38568;

    /**
     * The <code>File</code> created from the file1 parameter above.
     */
    private File file;
    /**
     * The size of each <code>ByteBuffer</code>, used to map file into memory.
     */
    private static final int BUFFER_SIZE = 8*1024;
    /**
     * The number of byte buffers created.
     */
    private final static int NR_OF_BUFFERS = 5;
    /**
     * The array of ByteBuffers's of size NR_OF_BUFFERS created in the setUp method.
     */
    private ByteBuffer byteBuffers[] = new ByteBuffer[NR_OF_BUFFERS];
    /**
     * Set up for each test. Creates the file, the byte buffers.
     */
    public void setUp()  {
        file = new File(file1);
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.allocateDirect(BUFFER_SIZE);

        }

    }

    /**
     * Sets the file to NULL.
     */
    public void tearDown() {
        file = null;
    }
    /**
     *
     * Tests for the <code>hasNext</code> and next methods.
     *
     * <pre>
     * 1. IllegalStateException
     *    Condition:
     *      A non-null mutable MediaObject is created
     *  Action:
     *      Calling iterator() method
     *  Result:
     *      IllegalStateException
     *
     * 2. Iterator in non-null
     *  Condition:
     *      A non-null immutable MediaObject is created
     *  Action:
     *      Calling iterator() method
     *  Result:
     *      The iterator is non-null
     *
     * 3. Append NR_OF_BUFFERS direct ByteBuffers
     *  Condition:
     *      A non-null MediaObject is created appended with
     *      NR_OF_BUFFERS ByteBuffers
     *  Action:
     *      Calling hasNext and next until hasNext returns false
     *  Result:
     *      Iteration has NR_OF_BUFFERS buffers.
     *
     * 4. Iterator.next() method throws NoSuchElementException if no more elements
     *  Condition:A non-null MediaObject is created
     *  Action:
     *      Calling next method after hasNext() returns false
     *  Result:
     *      Exception NoSuchElementException is thrown
     *
     * 5. next() returns a new read-only copy with same
     *    content and state as original buffer.
     *  Condition:
     *      A non-null MediaObject is created appended with
     *      each buffer in the byteBuffers array.
     *  Action:
     *      Iterate over each buffer with the next method.
     *  Result:
     *      Each buffer returned is a read-only copy of
     *      the original buffer.
     * </pre>
     */
    public void testIterator() throws MediaObjectException {
        // 1
        MediaObject mediaObject =  new MediaObject();
        try {
            IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
        } catch (IllegalStateException e) {/*ok*/}


        // 2
        mediaObject.setImmutable();
        IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
        assertNotNull(iter);

        // 3
        mediaObject =  new MediaObject();
        for (ByteBuffer byteBuffer : byteBuffers) {
            mediaObject.getNativeAccess().append(byteBuffer);
        }
        int nrOfBuffers = 0;
        mediaObject.setImmutable();
        iter = mediaObject.getNativeAccess().iterator();
        while (iter.hasNext()) {
            ByteBuffer byteBuffer =  iter.next();
            nrOfBuffers++;
        }
        assertEquals("Number of buffers in iteration should be " + NR_OF_BUFFERS,
                NR_OF_BUFFERS, nrOfBuffers);

        // 4
        try {
            iter.next();
            fail("next() Should have thrown NoSuchElementException");
        } catch (NoSuchElementException e)  {/*ok*/}



        // 5
        mediaObject =  new MediaObject();

        for (ByteBuffer byteBuffer : byteBuffers) {
            mediaObject.getNativeAccess().append(byteBuffer);
        }
        mediaObject.setImmutable();
        iter = mediaObject.getNativeAccess().iterator();
        nrOfBuffers = 0;
        while (iter.hasNext()) {
            ByteBuffer bb = iter.next();
            assertTrue("ByteBuffer should be read-only", bb.isReadOnly());
            assertNotSame("next should return a new read-only copy",
                    mediaObject.BYTEBUFFER_LIST.get(nrOfBuffers));
            assertEquals("Buffers should have same content",
                    mediaObject.BYTEBUFFER_LIST.get(nrOfBuffers), bb);

            nrOfBuffers++;
        }
        assertEquals("Number of buffers should be " + byteBuffers.length,
                byteBuffers.length, nrOfBuffers);

    }


}
