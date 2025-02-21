/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * JUnit tests for {@link FileMediaObjectIterator}
 *
 * @author Mats Egland
 */
public final class FileMediaObjectIteratorTest extends TestCase  {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(FileMediaObjectTest.class);
    /**
     * The file that is loaded by the <code>FileMediaObject</code>'s.
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
    private static final long BUFFER_SIZE = 8*1024;

    /**
     * Set up for each test. Creates the file.
     */
    public void setUp()  {
        file = new File(file1);
    }

    /**
     * Sets the file to NULL.
     */
    public void tearDown() {
        file = null;
    }
    /**
     *
     * Tests for the <code>hasNext</code> and <code>next</code> methods.
     *
     * <pre>
     * 1. Iterator in non-null
     *  Condition:A non-null FileMediaObject is created
     *  Action:
     *      Calling iterator() method
     *  Result:
     *      The iterator is non-null
     *
     * 2. The number of buffers returned should be equal to
     *      the getNrOfBuffers method
     *  Condition: A non-null FileMediaObject is created
     *  Action:
     *      Calling hasNext and next until hasNext returns false
     *  Result:
     *      The number of buffers returned match the buffers in FileMediaObject
     *
     * 3. Iterator.next() method throws NoSuchElementException if no more elements
     *  Condition:A non-null FileMediaObject is created
     *  Action:
     *      Calling next method after hasNext() returns false
     *  Result:
     *      Exception NoSuchElementException is thrown
     *
     * 4. Read the file and compare content with the content retreived with the next method
     *  Condition: A non-null FileMediaObject is created
     *  Action:
     *      Read each byte from file and compare content
     *  Result:
     *      Content is equal and the bytesread is equal to size of file
     *
     * 5. next() returns a new read-only copy with same
     *    content and state as original buffer.
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Iterate over each buffer with the next method.
     *  Result:
     *      Each buffer returned is a read-only copy of
     *      the original buffer.
     *
     * </pre>
     */
    public void testIterator() throws MediaObjectException {
        // 1
        FileMediaObject mediaObject =  new FileMediaObject(file, BUFFER_SIZE);
        IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
        assertNotNull(iter);
        // 2
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        while (iter.hasNext()) {
            ByteBuffer byteBuffer =  iter.next();
            byteBuffers.add(byteBuffer);
        }
        assertEquals(byteBuffers.size(), mediaObject.getNrOfBuffers());

        // 3
        try {
            iter.next();
            fail("next() Should have thrown NoSuchElementException");
        } catch (NoSuchElementException e)  {/*ok*/}

        // 4
        // read the file and compare content
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            FileChannel fileChannel = fileInputStream.getChannel();
            long fileSize = fileChannel.size();
            ByteBuffer fileBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            int bytesRead = 0;
            for (ByteBuffer byteBuffer : byteBuffers) {
                for (int i = 0; i < byteBuffer.capacity(); i++) {
                    assertEquals(byteBuffer.get(), fileBuffer.get());
                    bytesRead++;
                }
            }
            assertEquals(bytesRead, FILE_SIZE);

        } catch (FileNotFoundException e1) {
            fail("failed to open filechannel to file " + file1);
        }  catch (IOException e2) {
            fail("failed to read size from file" + file1);
        }

        // 5
        iter = mediaObject.getNativeAccess().iterator();
        int nrOfBuffers = 0;
        while (iter.hasNext()) {
            ByteBuffer bb = iter.next();
            assertTrue("ByteBuffer should be read-only", bb.isReadOnly());
            assertNotSame("next should return a new read-only copy",
                    mediaObject.BYTEBUFFER_LIST.get(nrOfBuffers));
            assertEquals("Buffers should have same content",
                    mediaObject.BYTEBUFFER_LIST.get(nrOfBuffers), bb);

            nrOfBuffers++;
        }
        assertEquals("Number of buffers should be " + mediaObject.getNrOfBuffers(),
                mediaObject.getNrOfBuffers(), nrOfBuffers);
    }


}
