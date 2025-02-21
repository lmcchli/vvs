/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.MockObjectTestCase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit tests for class {@link MediaObject} and its superclass {@link AbstractMediaObject}.
 *
 * @author Mats Egland
 */
public class MediaObjectTest extends MockObjectTestCase {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(MediaObjectTest.class);
    /**
     * The file that read in soume tests.
     */
    private static final String FILE_NAME = "test/com/mobeon/masp/mediaobject/test.wav";
    /**
     * Size in bytes of file above.
     */
    private static final int FILE_SIZE = 38568;
    /**
     * The number of byte buffers created.
     */
    private final static int NR_OF_BUFFERS = 2;
    /**
     * The size of each ByteBuffer created.
     */
    private final static int BUFFER_SIZE = 1024;
    /**
     * The number of concurrent clients of type
     * <code>MediaObjectClient</code> that is created
     * in the testConcurrent test.
     */
    private final static int NR_OF_CLIENTS = 5;

    /**
     * Bytes appended to common mediaobject in concurrent test.
     */
    private final AtomicInteger concurrentBytesAppended =
            new AtomicInteger(0);

    /**
     * The array of ByteBuffers's of size NR_OF_BUFFERS created in the setUp method.
     */
    private ByteBuffer byteBuffers[] = new ByteBuffer[NR_OF_BUFFERS];
    /**
     * List of ByteBuffers. Constains same bytebuffers as array byteBuffers.
     * Used to test constructor that takes list of bytebuffers as list. Created in the setUp method.
     */
    private List<ByteBuffer> byteBufferList;
    /**
     *  This static block is just to initialize classloader, filesystem and logger
     */

    /**
     * Fills the arrays byte buffers and creates the list byteBufferList from the
     * content of byteBuffers.
     */
    public void setUp() {
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.allocateDirect(BUFFER_SIZE);

        }
        byteBufferList = new ArrayList<ByteBuffer>(asList(byteBuffers));
    }

    /**
     * Test for the constructor {@link MediaObject#MediaObject()} and in turn
     * {@link AbstractMediaObject#AbstractMediaObject()}
     * <p/>
     * <pre>
     * <p/>
     * 1. Creation
     *  Condition:
     *  Action:
     *      new MediaObject()
     *  Result:
     *      Created MediaObject is not null and it has a non-null
     *      MediaProperties member.
     * <p/>
     * </pre>
     */
    public void testConstructor() {
        // 1
        MediaObject mediaObject = new MediaObject();
        assertNotNull(mediaObject);
        assertNotNull(mediaObject.getMediaProperties());

    }

    /**
     * Tests for constructor {@link MediaObject#AbstractMediaObject(MediaProperties)}  and
     * {@link AbstractMediaObject#AbstractMediaObject(MediaProperties)}.
     * <p/>
     * <pre>
     * <p/>
     * 1. Passing a non-null MediaProperties as argument.
     *  Condition:
     *  Action:
     *  Result:
     *      Non-null mediaobject created with the media properties same
     *      as passed mediaproperties.
     * <p/>
     * <p/>
     * 2. Created MediaObject should not be immutable.
     *  Condition:
     *  Action:
     *      Call the isImmutable method after creation.
     *  Result:
     *      The MediaObject should not be immutable
     * <p/>
     * </pre>
     */
    public void testConstructor1() {
        MediaProperties mediaProperties = new MediaProperties();

        // 1
        MediaObject mediaObject = new MediaObject(mediaProperties);
        assertNotNull(mediaObject);
        assertSame(mediaProperties, mediaObject.getMediaProperties());

        // 3
        assertFalse(mediaObject.isImmutable());
    }

    /**
     * Tests for constructors {@link MediaObject#MediaObject(java.util.List<java.nio.ByteBuffer>)}  and
     * {@link AbstractMediaObject#AbstractMediaObject(java.util.List<java.nio.ByteBuffer>)}.
     * <p/>
     * <p/>
     * <pre>
     * <p/>
     * 1. Illegal arguments
     *  Condition:
     *  Action:
     *      1. byteBuffers=null.
     *      2. one of the buffers in list is null.
     *      3. a bytebuffer is non-direct.
     *  Result:
     *      1-3. IllegalArgumentException thrown.
     * <p/>
     * 2. Assert that created MediaObject has a non-null MediaProperties object.
     *  Condition:
     *  Action:
     *      Call the getMediaProperties method after creation of MediaObject
     *  Result:
     *      The getMediaProperties method return a non-null MediaProperties object and
     *      it should contain zero mime-types
     * <p/>
     * 3. Assert that created MediaObject is immutable.
     *  Condition:
     *  Action:
     *      Call the isImmutable method after creation
     *  Result:
     *      The isImmutable method returns true
     * <p/>
     * </pre>
     */
    public void testConstructor2() {
        //MediaObject(List<ByteBuffer> bytebuffers, String fileFormat) throws MediaObjectException
        MediaObject mediaObject;
        String fileFormat = "wav";
        // 1
        try {
            mediaObject = new MediaObject((List<ByteBuffer>) null);
        } catch (IllegalArgumentException e1) {/*ok*/}
        byteBufferList.clear();
        byteBufferList.add(ByteBuffer.allocateDirect(1024));
        byteBufferList.add(null);
        try {
            mediaObject = new MediaObject(byteBufferList);
        } catch (IllegalArgumentException e1) {/*ok*/}
        byteBufferList.clear();
        byteBufferList.add(ByteBuffer.allocate(1024));
        try {
            mediaObject = new MediaObject(byteBufferList);
        } catch (IllegalArgumentException e1) {/*ok*/}
        byteBufferList.clear();
        byteBufferList.add(ByteBuffer.allocateDirect(1024));

        // 2
        mediaObject = new MediaObject(byteBufferList);
        assertNotNull(mediaObject);
        assertNotNull(mediaObject.getMediaProperties());

        // 3
        assertTrue(mediaObject.isImmutable());

    }

    /**
     * Tests for constructor {@link MediaObject#AbstractMediaObject(java.util.List<java.nio.ByteBuffer>, com.mobeon.masp.mediaobject.MediaProperties)}
     * and {@link AbstractMediaObject#AbstractMediaObject(java.util.List<java.nio.ByteBuffer>, MediaProperties)}.
     * <p/>
     * <pre>
     * 1. Illegal arguments
     *  Condition:
     *  Action:
     *      1. byteBuffers=null.
     *      2. one of buffers in list is null.
     *      3. a bytebuffer is non-direct.
     *  Result:
     *      1-3. IllegalArgumentException thrown.
     * <p/>
     * 2. Same MediaProperties
     *  Condition:
     *  Action:
     *      Call the getMediaProperties method after creation of MediaObject
     *  Result:
     *      The getMediaProperties method returns the same MediaProperties
     *      object that is passed.
     * <p/>
     * <p/>
     * 4. Assert that created MediaObject is immutable
     *  Condition:
     *  Action:
     *      Call the isImmutable method after creation
     *  Result:
     *      The isImmutable method returns true
     * <p/>
     * </pre>
     */
    public void testConstructor3() {
        MediaProperties mediaProperties = new MediaProperties();
        MediaObject mediaObject;

        // 1
        try {
            mediaObject = new MediaObject((List<ByteBuffer>) null, mediaProperties);
        } catch (IllegalArgumentException e1) {/*ok*/}
        byteBufferList.clear();
        byteBufferList.add(ByteBuffer.allocateDirect(1024));
        byteBufferList.add(null);
        try {
            mediaObject = new MediaObject(byteBufferList, mediaProperties);
        } catch (IllegalArgumentException e1) {/*ok*/}
        byteBufferList.add(ByteBuffer.allocate(1024));
        try {
            mediaObject = new MediaObject(byteBufferList, mediaProperties);
        } catch (IllegalArgumentException e1) {/*ok*/}

        // 2
        byteBufferList.clear();
        byteBufferList.add(ByteBuffer.allocateDirect(1024));
        mediaObject = new MediaObject(byteBufferList, mediaProperties);
        assertSame(mediaProperties, mediaObject.getMediaProperties());

        // 4
        assertTrue(mediaObject.isImmutable());
    }

    /**
     * Tests for the append method.
     * <p/>
     * <pre>
     * <p/>
     * 1. Append ByteBuffer to immutable Mediaobject
     *  Condition:
     *      A immutable mediaObject is created
     *  Action:
     *      Call the append method on a Immutable mediaObject
     *  Result:
     *      IllegalStateException is thrown.
     * <p/>
     * 2. Call append with a non-direct ByteBuffer.
     *  Condition:
     *      A mutable mediaObject is created.
     *  Action:
     *      Call the append method on a mutable
     *      MediaObject with a non-direct ByteBuffer
     *  Result:
     *      IllegalArgumentException is thrown.
     * <p/>
     * 3. Append good content on mutable MediaObject.
     *  Condition:
     *      A mutable mediaObject is created with the fileformat set.
     *  Action:
     *      Call the append method on a mutable MediaObject with a direct ByteBuffer
     *  Result:
     *      No Exception is thrown and number of bytebuffers in mediaobject
     *      corresponds to the number of appends made.
     * <p/>
     * </pre>
     */
    public void testAppend() throws MediaObjectException {
        MediaObject mediaObject = new MediaObject(byteBufferList, null);

        // 1
        assertTrue("The MediaObject should be immutable", mediaObject.isImmutable());
        try {
            mediaObject.append(ByteBuffer.allocateDirect(1024));
            fail("Buffer should be immutable");
        } catch (IllegalStateException e) {
            /*ok should not be able to append to a immutable object*/
        }

        // 2
        mediaObject = new MediaObject();
        assertFalse("MediaObject should be mutable", mediaObject.isImmutable());
        try {
            mediaObject.append(ByteBuffer.allocate(1024));
            fail("Should not be possible to append non-direct ByteBuffer");
        } catch (IllegalArgumentException e) {
            /*ok*/
        } catch (IllegalStateException e) {
            fail("MediaObjectException should have been thrown");
        }

        // 3
        mediaObject = new MediaObject();
        try {
            for (ByteBuffer byteBuffer : byteBuffers) {
                mediaObject.append(byteBuffer);
            }
        } catch (Exception e) {
            fail("Failed to append bytebuffer");
        }
        int nrOfBuffers = 0;
        mediaObject.setImmutable();
        IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
        while (iter.hasNext()) {
            iter.next();
            nrOfBuffers++;
        }
        assertEquals("The number of appended ByteBuffers is incorrect",
                byteBuffers.length, nrOfBuffers);
    }

    /**
     * Tests for the append method and that the Iterator returned from the
     * iterator method conforms to the appended content.
     * <pre>
     * 1. Exception is not thrown when calling
     *    iterator-method on immutable MediaObject
     *  Condition:
     *      A MediaObject is created.
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
        MediaObject mediaObject = new MediaObject();

        // 1
        IMediaObjectIterator iter = null;
        try {
            iter = mediaObject.getNativeAccess().iterator();
            fail("iterator method should throw IllegalStateException when called on " +
                    " a mutable MediaObject");
        } catch (IllegalStateException e) {/*ok*/}
        mediaObject.setImmutable();
        try {
            iter = mediaObject.getNativeAccess().iterator();
        } catch (IllegalStateException e) {
            fail("Should not throw IllegalStateException if immutable");
        }

        mediaObject = new MediaObject();
        // 2
        for (ByteBuffer byteBuffer : byteBuffers) {
            mediaObject.append(byteBuffer);
        }
        mediaObject.setImmutable();
        iter = mediaObject.getNativeAccess().iterator();
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
            mediaObject = new MediaObject(byteBufferList, null);
            mediaObject.setImmutable();
            iter = mediaObject.getNativeAccess().iterator();
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
     * Tests for the getSize method.
     * <p/>
     * <pre>
     * <p/>
     * 1.Check size
     *  Condition:
     *      A non-null MediaObject is created
     *  Action:
     *    1. Append a zero length bytebuffer
     *    2. Append a 1024 byte bytebuffer
     *  Result:
     *      1. getSize returns 0
     *      2. getSize returns 1024
     * </pre>
     */
    public void testGetSize() {
        // 1
        MediaObject mediaObject = new MediaObject();
        mediaObject.append(ByteBuffer.allocateDirect(0));
        assertEquals(mediaObject.getSize(), 0);
        mediaObject.append(ByteBuffer.allocateDirect(1024));
        assertEquals(mediaObject.getSize(), 1024);
    }

    /**
     * Tests for the method
     * {@link AbstractMediaObject#getInputStream()}.
     * <p/>
     * <pre>
     * 1. IllegalStateException
     *  Condition:
     *      A non-null, empty and mutable MediObject is created.
     *  Action:
     *      getInputStream()
     *  Result:
     *      IllegalStateException as MediaObject not immutable.
     * <p/>
     * 2. Assert returned inputstream is non-null.
     *  Condition:
     *      A non-null, empty and immutable MediObject is created.
     *  Action:
     *      getInputStream()
     *  Result:
     *      Returned InputStream is non-null and has no bytes.
     * <p/>
     * 3. Compare content
     *  Condition:
     *      - A non-null MediaObject with data from file FILE_NAME is
     *        created.
     *      - A FileInputStream to the file FILE_NAME is created.
     *  Action:
     *      Read each byte from the FileInputStream and
     *      MediaObjectInputStream and compare content.
     *  Result:
     *      Each byte from the  FileInputStream is same
     *      in MediaObjectInputStream.
     * <p/>
     * <p/>
     * </pre>
     */
    public void testGetInputStream() throws IOException, MediaObjectException {
        MediaObject mediaObject = new MediaObject();

        // 1
        try {
            InputStream is = mediaObject.getInputStream();
            fail("IllegalStateException should be thrown if not immutable");
        } catch (IllegalStateException e) {/*ok*/}

        // 2
        mediaObject.setImmutable();
        InputStream is = mediaObject.getInputStream();
        assertNotNull("Returned inputstream is null", is);
        int readBytes = 0;
        while (is.read() != -1) {
            readBytes++;
        }
        assertEquals("There should be 0 bytes in stream",
                0, readBytes);

        // 3
        mediaObject = new MediaObject();
        FileInputStream fis = new FileInputStream(FILE_NAME);
        FileChannel channel = fis.getChannel();
        long fileSize = channel.size();
        ByteBuffer bb = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        mediaObject.append(bb);
        mediaObject.setImmutable();
        fis = new FileInputStream(FILE_NAME);
        int nrOfBytes = 0;
        int readByte;
        InputStream moIS = mediaObject.getInputStream();
        while ((readByte = fis.read()) != -1) {
            assertEquals("Content mismatch", readByte, moIS.read());
            nrOfBytes++;
        }
        assertEquals("Number of bytes read should be equal to FILE_SIZE",
                FILE_SIZE, nrOfBytes);

    }

    /**
     * Tests for the method
     * {@link MediaObject#getNativeAccess()}.
     * <p/>
     * <pre>
     * 1. Assert returned MediaObjectNativeAccess is not null
     *  Condition:
     *      A non-null empty MediaObject is created.
     *  Action:
     *      Call getNativeAccess()
     *  Result:
     *      Non-null MediaObjectNativeAccess is returned.
     * <p/>
     * 1. Append data
     *  Condition:
     *      A non-null empty MediaObject is created.
     *      ByteBuffers is appended to it via native access method.
     *  Action:
     *      Check that the  MediaObject is updated accordingly.
     *  Result:
     *      The MediaObject contains the ByteBuffers added.
     * </pre>
     */
    public void testGetNativeAccess() throws MediaObjectException {
        // 1
        final MediaObject mediaObject = new MediaObject();
        MediaObjectNativeAccess access = mediaObject.getNativeAccess();
        assertNotNull("getNativeAccess returns null", access);

        // 2
        for (ByteBuffer byteBuffer : byteBuffers) {
            access.append(byteBuffer);
        }
        mediaObject.setImmutable();
        IMediaObjectIterator iter = access.iterator();
        int nrOfBuffers = 0;
        while (iter.hasNext()) {
            ByteBuffer bb = iter.next();
            assertNotSame("Iterator should not return same byte buffer",
                    byteBuffers[nrOfBuffers], bb);
            assertEquals("Buffers should have same content",
                    byteBuffers[nrOfBuffers], bb);
            nrOfBuffers++;
        }
        assertEquals("Number of buffers in MediaObject should be " + NR_OF_BUFFERS,
                NR_OF_BUFFERS, nrOfBuffers);
    }

    /**
     * Tests the thread-safety of the MediaObject class.
     * Creates <code>NR_OF_CLIENTS</code> number of
     * <code>MediaObjectAppender</code>s that each will appends 100K bytes
     * to the common mediaobject. The member <code>concurrentBytesAppended</code>
     * is updated with the total number of bytes appended.
     * <p/>
     * Then <code>NR_OF_CLIENTS</code> number of <code>MediaObjectReader</code>s
     * is created that each will read every byte in the MediaObject.
     */
    public void testConcurrent() throws InterruptedException {
        final MediaObject mediaObject = new MediaObject();
        final MediaObjectAppender[] appenders = new MediaObjectAppender[NR_OF_CLIENTS];
        final MediaObjectReader[] readers = new MediaObjectReader[NR_OF_CLIENTS];

        // start the appenders
        for (int i = 0; i < appenders.length; i++) {
            appenders[i] = new MediaObjectAppender("MediaObjectAppender:" + i, mediaObject);
            appenders[i].start();
        }
        // join each appender
        for (MediaObjectAppender mediaObjectAppender : appenders) {
            mediaObjectAppender.join();
        }
        assertEquals("The number of bytes added should be " + (NR_OF_CLIENTS * 100000),
                NR_OF_CLIENTS * 100000, concurrentBytesAppended.get());
        assertEquals("The size of the MediaObject does not match bytes added.",
                concurrentBytesAppended.get(), mediaObject.getSize());

        // start the readers
        mediaObject.setImmutable();
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new MediaObjectReader(
                    "MediaObjectReader:" + i,
                    mediaObject,
                    concurrentBytesAppended.get());
            readers[i].start();
        }
        // join each Reader
        for (MediaObjectReader mediaObjectReader : readers) {
            mediaObjectReader.join();
        }

    }

     /**
     * Tests that toString not crashes.
     * @throws Exception
     */
    public void testToString() throws Exception {

         MediaObject mo = new MediaObject(new MediaProperties());
         mo.toString();

         List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
         byteBuffers.add(ByteBuffer.allocateDirect(123));
         byteBuffers.add(ByteBuffer.allocateDirect(12));
         mo = new MediaObject(byteBuffers);
         mo.toString();
         mo = new MediaObject(byteBuffers,null);
         mo.toString();

    }

    /**
     * Thread that appends 100K of bytes to the specified
     * <code>MediaObject</code>.
     */
    private class MediaObjectAppender extends Thread {
        private MediaObject mediaObject;

        public MediaObjectAppender(String name, MediaObject mediaObject) {
            super(name);
            this.mediaObject = mediaObject;
        }

        public void run() {

            for (int i = 0; i < 10000; i++) {
                mediaObject.getNativeAccess().append(ByteBuffer.allocateDirect(10));
                //LOGGER.debug("Thread:"+Thread.currentThread().getName()+" append");
                concurrentBytesAppended.addAndGet(10);
                mediaObject.getSize();
            }

        }
    }

    /**
     * Thread that reads all bytes from a <code>MediaObject</code>.
     * First it will read with an ByteBuffer iterator, and then
     * with the InputStream.
     */
    private class MediaObjectReader extends Thread {
        private MediaObject mediaObject;
        private int expectedMOSize;

        /**
         * Creates a reader that reads all bytes from the specified
         * mediaobject.
         *
         * @param name         The name of the thread.
         * @param mediaObject  The mediaobject to read from
         * @param expectedSize The expected number of bytes in the mediaObject.
         */
        public MediaObjectReader(String name, MediaObject mediaObject, int expectedSize) {
            super(name);
            this.mediaObject = mediaObject;
            this.expectedMOSize = expectedSize;
        }

        public void run() {

            IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
            int bytesRead = 0;
            while (iter.hasNext()) {
                try {
                    ByteBuffer bb = iter.next();
                    while (bb.hasRemaining()) {
                        bb.get();
                        bytesRead++;
                    }
                } catch (MediaObjectException e) {
                    throw new RuntimeException("Failed to get next ByteBuffer from MediaObject");
                }
            }
//            LOGGER.debug("Thread " + Thread.currentThread().getName()
//                    + " read " + bytesRead + " from MediaObject");
            if (expectedMOSize != bytesRead) {
                throw new RuntimeException(
                        "Should have read " + concurrentBytesAppended.get() +
                                ", but only read " + bytesRead + " from mediaobject with size: " +
                                mediaObject.getSize());
            }
            InputStream is = mediaObject.getInputStream();
            bytesRead = 0;
            int readByte;
            try {
                while ((readByte = is.read()) != -1) {
                    bytesRead++;
                }
                if (expectedMOSize != bytesRead) {
                throw new RuntimeException(
                        "Should have read " + concurrentBytesAppended.get() +
                                ", but only read " + bytesRead + " from mediaobject with size: " +
                                mediaObject.getSize());
            }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

        }
    }


}
