/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

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
 * Unit tests for class {@link FileMediaObjectNativeAccessImpl}
 *
 * @author Mats Egland
 */
public class FileMediaObjectNativeAccessImplTest extends TestCase {
     /**
     * MediaObject that is provided access to.
     */
    private FileMediaObject mediaObject;
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
     * The file that is loaded by the <code>FileMediaObject</code>'s.
     *
     */
    private static final String FILE_NAME = "test/com/mobeon/masp/mediaobject/test.wav";
    /**
     * The size of the file above.
     */
    private static final long FILE_SIZE = 38568;
    /**
     * The <code>File</code> created from the FILE_NAME parameter above.
     */
    private File file;
    /**
     * Creates the empty mediaobject member.
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.

        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.allocateDirect(BUFFER_SIZE);

        }
        file = new File(FILE_NAME);
        mediaObject = new FileMediaObject(file, BUFFER_SIZE);
    }

    /**
     * Tests for the constructor
     * {@link FileMediaObjectNativeAccessImpl#FileMediaObjectNativeAccessImpl(FileMediaObject)}.
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
            FileMediaObjectNativeAccessImpl access =
                        new FileMediaObjectNativeAccessImpl(null);
            fail("Should throw IllegalArgumentException if mediaObject is null");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        FileMediaObjectNativeAccessImpl access =
                        new FileMediaObjectNativeAccessImpl(mediaObject);
        assertNotNull("Failed to create FileMediaObjectNativeAccessImpl", access);
    }
    /**
     * Tests for the iterator method.
     * <pre>
     *
     * 1. Iterator is non-null
     *  Condition:
     *      A FileMediaObjectNativeAccessImpl to a FileMediaObject that has
     *      FILE_NAME as source is created.
     *  Action:
     *      Calling iterator() method
     *  Result:
     *      The iterator is non-null
     *
     *
     * 2. The number of buffers returned by the iterator should be equal to
     *      the getNrOfBuffers method
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Calling hasNext and next until hasNext returns false
     *  Result:
     *      The number of buffers returned match the buffers in FileMediaObject
     *
     * 3. Iterator.next() method throws NoSuchElementException if no more elements
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Calling next method after hasNext() returns false
     *  Result:
     *      Exception NoSuchElementException is thrown
     *
     * 4. Read the file and compare content with the content retreived with the next method
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Read each byte from file and compare content
     *  Result:
     *      Content is equal and the bytesread is equal to size of file
     * </pre>
     */
    public void testIterator() {
        // condition
        FileMediaObjectNativeAccessImpl access =
                        new FileMediaObjectNativeAccessImpl(mediaObject);
        try {
            // 1

            IMediaObjectIterator iter = access.iterator();
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
                fail("failed to open filechannel to file " + FILE_NAME);
            }  catch (IOException e2) {
                fail("failed to read size from file" + FILE_NAME);
            }
        } catch (MediaObjectException e3) {
            fail("Failed to create FileMediaObject");
        }
    }

    /**
     * Tests for the method
     * {@link FileMediaObjectNativeAccessImpl#append(java.nio.ByteBuffer)}.
     *
     * <pre>
     * 1. IllegalStateException
     *  Condition:
     *      A FileMediaObjectNativeAccessImpl to a FileMediaObject that has
     *      FILE_NAME as source is created.
     *  Action:
     *      call append with direct buffer.
     *  Result:
     *      IllegalStateException (As FileMediaObject is always immutable)
     * </pre>
     */
    public void testAppend() {
        // condition
        FileMediaObjectNativeAccessImpl access =
                        new FileMediaObjectNativeAccessImpl(mediaObject);
        try {
            access.append(ByteBuffer.allocateDirect(BUFFER_SIZE));    
        } catch (IllegalStateException e) {/*ok*/}
    }
}
