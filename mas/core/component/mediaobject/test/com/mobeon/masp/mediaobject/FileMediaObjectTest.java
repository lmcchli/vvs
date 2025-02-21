/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * JUnit tests for {@link FileMediaObject}
 *
 * @author Mats Egland
 */
public class FileMediaObjectTest extends TestCase {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(FileMediaObjectTest.class);
    /**
     * The file that is loaded by the <code>FileMediaObject</code>'s.
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
     * Number of concurrent threads to consume a <code>FileMediaObject<code> in the concurrent test.
     */
    private static final int CONSUMERS = 10;
    /**
     * Number of <code>FileMediaObject</code>s created for the tests.
     */
    private static final int NR_MEDIAOBJECTS = 10;
    /**
     * The size of each <code>ByteBuffer</code>, used to map file into memory.
     */
    private static final long BUFFER_SIZE = 8 * 1024;
    /**
     * Array of <code>NR_MEDIAOBJECTS</code> <code>FileMEdiaObject</code>s created in the setUp method for the tests to use.
     */
    private static FileMediaObject[] mediaObjects = new FileMediaObject[NR_MEDIAOBJECTS];
    /**
     * Time values, used for clocking in performance tests.
     */
    private long starttime;
    private long stoptime;
    private long time;

    /**
     *  This static block is just to initialize classloader, filesystem and logger
     */
    static {
        FileMediaObject FileMediaObject;
        File file = new File(FILE_NAME);

        try {
            IMediaObject mediaObject1 = new FileMediaObject(file, BUFFER_SIZE);
            try {
                FileInputStream fis = new FileInputStream(FILE_NAME);
            } catch (FileNotFoundException e) {
                fail("File:" + FILE_NAME + " cannot be found");
            }
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject from ile:" + FILE_NAME);
        }
    }

    /**
     * Set up for each test. Creates the file, the array of mediaobjects from the file.
     */
    public void setUp() {
        file = new File(FILE_NAME);
        // DIRECT BUFFER
        starttime = System.currentTimeMillis();

        for (int i = 0; i < mediaObjects.length; i++) {
            try {
                mediaObjects[i] = new FileMediaObject(file, BUFFER_SIZE);

            } catch (MediaObjectException e) {
                e.printStackTrace();
                fail("Failed to create Direct MediaObjects");
            }
        }
        stoptime = System.currentTimeMillis();
        time = stoptime - starttime;
        //LOGGER.debug("Created "+ mediaObjects.length+
        //        " Direct MediaObjects in:" + time +" ms");
    }

    /**
     * Tear down after each test. Sets all FileMediaObject's to null in mediaObjects array.
     */
    public void tearDown() {
        file = null;
        for (int i = 0; i < mediaObjects.length; i++) {
            mediaObjects[i] = null;
        }
    }

    /**
     * Tests for constructor {@link FileMediaObject#FileMediaObject(java.io.File, long)}.
     * <pre>
     * <p/>
     * 1. Basic creation
     *  Condition:
     *  Action:
     *      1. Call create method with good non-null arguments
     *  Result:
     *      Non -null media object is created that has
     *      non-null media properties
     * <p/>
     * <p/>
     * 2. Wrong Arguments
     *  Condition:
     *  Action:
     *    1. file=null
     *    2. passing a file that don't exist
     *    3. bufferSize=0
     *    4. bufferSize=-1
     *  Result:
     *      1-4 IllegalArgumentException.
     * <p/>
     * <p/>
     * 3. Created FileMediaObject should be immutable and the size should match the size of the file.
     *  Condition:
     *  Action:
     *      Call the isImmutable method after creation.
     *  Result:
     *      The FileMediaObject should not be immutable and it's size should match
     *      the size of the file.
     * </pre>
     */
    public void testConstructor() {
        FileMediaObject mediaObject;

        // 1
        try {
            mediaObject = new FileMediaObject(file, BUFFER_SIZE);
            assertNotNull(mediaObject.getMediaProperties());
        } catch (IllegalArgumentException e1) {
            fail("test of constructor with good arguments failed");
        } catch (MediaObjectException moe) {
            fail("test of constructor with good arguments failed");
        }

        // 2
        try {
            mediaObject = new FileMediaObject(null, BUFFER_SIZE);
            fail("IllegalArgumentException should have been thrown when passing null as file");
        } catch (IllegalArgumentException e1) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing null as file");
        }

        try {
            mediaObject = new FileMediaObject(new File("notexisting.file"), BUFFER_SIZE);
            fail("IllegalArgumentException should have been thrown when passing a non-existing file");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing a non-existing file");
        }
        try {
            mediaObject = new FileMediaObject(file, 0);
            fail("IllegalArgumentException should have been thrown when passing 0 as BufferSize");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing 0 as BufferSize");
        }
        try {
            mediaObject = new FileMediaObject(file, -1);
            fail("IllegalArgumentException should have been thrown when passing a negative buffer size");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing a negative buffer size");
        }

        // 3
        try {
            mediaObject = new FileMediaObject(file, BUFFER_SIZE);
            long size = mediaObject.getSize();
            FileChannel channel = new FileInputStream(file).getChannel();
            assertEquals(size, channel.size());
            assertTrue(mediaObject.isImmutable());
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject");
        } catch (FileNotFoundException e) {
            fail("Failed to create FileChannel to file:" + file);
        } catch (IOException e) {
            fail("Failed to read size from FileChannel:" + file);
        }
    }

    /**
     * Tests for constructor
     * {@link FileMediaObject#FileMediaObject(java.io.File, MediaProperties, long)}
     * <p/>
     * <pre>
     * <p/>
     * 1. Wrong Arguments
     *  Condition:
     *  Action:
     *    1. file=null<br>
     *    2. passing a file that don't exist
     *    3. bufferSize=0
     *    4. bufferSize=-1
     *  Result:
     *      1-4 IllegalArgumentException.
     * <p/>
     * <p/>
     * 2. Passing null as MediaProperties
     *  Condition:
     *  Action:
     *      Calling constructor with null as MediaProperties
     *  Result:
     *      The FileMediaObject should have a non-null MediaProperties
     * <p/>
     * 3. Passing non-null MediaProperties
     *  Condition:
     *  Action:
     *      Calling constructor with a non-null MediaProperties
     *  Result:
     *      The mediaobject's properties should be same as properties passed.
     * <p/>
     * 4. Created FileMediaObject should be immutable and the size should match the size of the file.
     *  Condition:
     *  Action:
     *      Call the isImmutable method after creation.
     *  Result:
     *      The FileMediaObject should not be immutable and it's size should match
     *      the size of the file.
     * <p/>
     * </pre>
     */
    public void testConstructor2() {
        FileMediaObject mediaObject;
        MediaProperties mediaProperties = new MediaProperties();

        // 1
        try {
            mediaObject = new FileMediaObject(null, mediaProperties, BUFFER_SIZE);
            fail("IllegalArgumentException should have been thrown when passing null as file");
        } catch (IllegalArgumentException e1) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing null as file");
        }

        try {
            mediaObject = new FileMediaObject(new File("notexisting.file"),
                    mediaProperties, BUFFER_SIZE);
            fail("IllegalArgumentException should have been " +
                    "thrown when passing a non-existing file");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been " +
                    "thrown when passing a non-existing file");
        }
        try {
            mediaObject = new FileMediaObject(file, mediaProperties, 0);
            fail("IllegalArgumentException should have " +
                    "been thrown when passing 0 as BufferSize");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been thrown when passing 0 as BufferSize");
        }
        try {
            mediaObject = new FileMediaObject(file, mediaProperties, -1);
            fail("IllegalArgumentException should have " +
                    "been thrown when passing a negative buffer size");
        } catch (IllegalArgumentException e2) {/*ok*/
        } catch (MediaObjectException moe) {
            fail("IllegalArgumentException should have been " +
                    "thrown when passing a negative buffer size");
        }

        // 2
        try {
            mediaObject = new FileMediaObject(file, null, BUFFER_SIZE);
            assertNotNull(mediaObject.getMediaProperties());
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject");
        } catch (IllegalArgumentException e2) {
            fail("Failed to create FileMediaObject");
        }
        // 3
        try {
            mediaObject = new FileMediaObject(file, mediaProperties, BUFFER_SIZE);
            assertNotNull(mediaObject.getMediaProperties());
            assertSame(mediaProperties, mediaObject.getMediaProperties());
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject");
        } catch (IllegalArgumentException e2) {
            fail("Failed to create FileMediaObject");
        }

        // 4
        try {
            mediaObject = new FileMediaObject(file, BUFFER_SIZE);
            long size = mediaObject.getSize();
            FileChannel channel = new FileInputStream(file).getChannel();
            assertEquals(size, channel.size());
            assertTrue(mediaObject.isImmutable());
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject");
        } catch (FileNotFoundException e) {
            fail("Failed to create FileChannel to file:" + file);
        } catch (IOException e) {
            fail("Failed to read size from FileChannel:" + file);
        }
    }

    /**
     * Tests for the iterator method.
     * <pre>
     * <p/>
     * 1. Iterator is non-null
     *  Condition: A non-null FileMediaObject is created
     *  Action:
     *      Calling iterator() method
     *  Result:
     *      The iterator is non-null
     * <p/>
     * 2. The number of buffers returned should be equal to
     *      the getNrOfBuffers method
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Calling hasNext and next until hasNext returns false
     *  Result:
     *      The number of buffers returned match the buffers in FileMediaObject
     * <p/>
     * 3. Iterator.next() method throws NoSuchElementException if no more elements
     *  Condition:
     *      A non-null FileMediaObject is created
     *  Action:
     *      Calling next method after hasNext() returns false
     *  Result:
     *      Exception NoSuchElementException is thrown
     * <p/>
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
        try {
            // 1
            FileMediaObject mediaObject = new FileMediaObject(file, BUFFER_SIZE);
            IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
            assertNotNull(iter);

            // 2
            List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
            while (iter.hasNext()) {
                ByteBuffer byteBuffer = iter.next();
                byteBuffers.add(byteBuffer);
            }
            assertEquals(byteBuffers.size(), mediaObject.getNrOfBuffers());

            // 3
            try {
                iter.next();
                fail("next() Should have thrown NoSuchElementException");
            } catch (NoSuchElementException e) {/*ok*/}

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
            } catch (IOException e2) {
                fail("failed to read size from file" + FILE_NAME);
            }
        } catch (MediaObjectException e3) {
            fail("Failed to create FileMediaObject");
        }
    }

    /**
     * Tests for the <code>getSize</code> method.
     * <p/>
     * <pre>
     * <p/>
     * 1. Returned size should match size of file.
     *  Condition:
     *      A non-null FileMediaObject is created and a FileChannel for the file.
     *  Action:
     *      Compare value from getSize with the size of the file (retreived with the FileChannel)
     *  Result:
     *      Size is equal to size of file
     * <p/>
     * </pre>
     */
    public void testSize() {
        try {
            // 1
            FileMediaObject mediaObject = new FileMediaObject(file, BUFFER_SIZE);
            long size = mediaObject.getSize();
            FileChannel channel = new FileInputStream(file).getChannel();
            assertEquals(size, channel.size());
            IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
            assertEquals(size, mediaObject.getSize());
        } catch (MediaObjectException e) {
            fail("Failed to create FileMediaObject");
        } catch (FileNotFoundException e) {
            fail("Failed to create FileChannel to file:" + file);
        } catch (IOException e) {
            fail("Failed to read size from FileChannel:" + file);
        }
    }

    /**
     * Tests the performance of FileMediaObject.
     * <p/>
     * This test makes no assertions, it just takes the time of doing different operations
     * on a FileMediaObject and prints the result to stdout.
     * <p/>
     * <pre>
     * The following measurements is done:
     * <p/>
     * 1. Time to map the file into a FileMediaObject without reading the content.
     *    I.e. force the FileMediaObject to map the file by calling the hasNext and next methods
     *    on the iterator. As it is the first the content is requested the file is actually mapped.
     * 2. As test 1 except each byte is also read.
     * 3. As test 2, except this is the SECOND time the FileMediaObject is read and so the
     *    file is already mapped into memory
     * 4. Benchmark - Time to read the file, byte for byte from a FileInputStream.
     * 5. Benchmark - Time to read the file, byte for byte from a BufferedInputStream.
     * <p/>
     * </pre>
     */
    public void testPerformance() {
        assertNotNull(mediaObjects[0]);

        ByteBuffer byteBuffer;
        try {
            // 1
            IMediaObjectIterator bufferIter1 = mediaObjects[1].getNativeAccess().iterator();
            starttime = System.currentTimeMillis();
            int i;
            while (bufferIter1.hasNext()) {
                byteBuffer = bufferIter1.next();
            }
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to map file into a FileMediaObject with buffersize:"
                    + BUFFER_SIZE + ", time=" + time + " ms");

            // 2
            IMediaObjectIterator bufferIter = mediaObjects[0].getNativeAccess().iterator();
            starttime = System.currentTimeMillis();
            int readBytes = 0;
            while (bufferIter.hasNext()) {
                byteBuffer = bufferIter.next();

                for (i = 0; i < byteBuffer.capacity(); i++) {
                    byte b = byteBuffer.get(i);
                    readBytes++;
                }
            }
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to read " + readBytes +
                    " nrofbytes from MediaObject FIRST TIME:" + time + " ms");

            // 3
            bufferIter = mediaObjects[0].getNativeAccess().iterator();
            starttime = System.currentTimeMillis();
            readBytes = 0;
            while (bufferIter.hasNext()) {
                byteBuffer = bufferIter.next();
                for (i = 0; i < byteBuffer.capacity(); i++) {
                    byte b = byteBuffer.get(i);
                    readBytes++;
                }
            }
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to read " + readBytes +
                    " nrofbytes from MediaObject SECOND TIME:" + time + " ms");

            // 4 Stream
            starttime = System.currentTimeMillis();
            FileInputStream inputStream;
            inputStream = new FileInputStream(FILE_NAME);
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to create FileInputStream:" + time);
            starttime = System.currentTimeMillis();
            int tmp;
            try {
                for (i = 0; (tmp = inputStream.read()) != -1; i++)
                    ;
            } catch (IOException e) {
                fail("Failed to read from FileInputStream");
            }
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to read " + readBytes + " nrofbytes from FileInputStream:" + time);

            // 5 BufferedStream
            starttime = System.currentTimeMillis();
            FileInputStream inputStream1 =
                    new FileInputStream(FILE_NAME);
            BufferedInputStream bis = new BufferedInputStream(inputStream1);
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to create buffered stream:" + time);
            starttime = System.currentTimeMillis();
            try {
                for (i = 0; (tmp = bis.read()) != -1; i++)
                    ;
            } catch (IOException e) {
                fail("Failed to read from BufferedInputStream");
            }
            stoptime = System.currentTimeMillis();
            time = stoptime - starttime;
            LOGGER.debug("Time to read " + readBytes + " nrofbytes from BufferedInputStream:" + time);
        } catch (FileNotFoundException e) {
            fail("Failed to create FileInputStream to " + FILE_NAME);
        } catch (MediaObjectException e1) {
            fail("Failed to iterate over FileMediaObject for file: " + FILE_NAME);
        }
    }

    /**
     * Test concurrent access of a common MediaObject.
     * <p/>
     * This test makes no assertions, it just makes a concurrent test of
     * FileMediaObject by creating <code>CONSUMERS</code> number of concurrent threads
     * that each will consume on the FileMediaObject.
     * <p/>
     * <p/>
     * Each Consumer created is of type <code>MediaObjectConsumer</code>.
     *
     * @throws InterruptedException If any of the created threads is interrupted.
     */
    public void testConcurrentMediaObject() throws InterruptedException {
        // The consumers
        MediaObjectConsumer[] consumers = new MediaObjectConsumer[CONSUMERS];
        FileMediaObject theMediaObject = null;
        try {
            theMediaObject = new FileMediaObject(file, BUFFER_SIZE);
        } catch (MediaObjectException e) {
            fail("Failed to create FileMEdiaObject from file:" + FILE_NAME);
        }
        FileMediaObjectIterator bufferIter = (FileMediaObjectIterator) mediaObjects[0].
                getNativeAccess().iterator();
        while (bufferIter.hasNext()) {
            try {
                bufferIter.next();
            } catch (MediaObjectException e) {
                fail("Failed to call next on FileMediaObjectIterator");
            }
        }
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new MediaObjectConsumer(theMediaObject, "MediaConsumer" + i);
        }
        // start consumers
        for (MediaObjectConsumer mediaObjectConsumer : consumers) {
            mediaObjectConsumer.start();

        }
        for (MediaObjectConsumer mediaObjectConsumer : consumers) {
            mediaObjectConsumer.join();
        }

        // The readers
        FileMediaObjectReader[] readers = new FileMediaObjectReader[CONSUMERS];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new FileMediaObjectReader(theMediaObject, "FileMediaObjectReader" + i,
                    (int) FILE_SIZE);
        }
        // start readers
        for (FileMediaObjectReader reader : readers) {
            reader.start();

        }
        for (FileMediaObjectReader reader : readers) {
            reader.join();
        }
    }

    /**
     * Thread that iterates over the given mediaobect and reads every byte in every
     * <code>ByteBuffer</code> acquired.
     */
    private class MediaObjectConsumer extends Thread {
        private FileMediaObject mediaObject;
        private boolean done = false;

        public MediaObjectConsumer(FileMediaObject mediaObject, String name) {
            super(name);
            this.mediaObject = mediaObject;
        }

        public void setDone(boolean d) {
            this.done = d;
        }

        public void run() {
            // Read from InputStream and compare content from file
            InputStream is = mediaObject.getInputStream();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(FILE_NAME);
            } catch (FileNotFoundException e) {
                fail("Failed to create FileInputStream");
            }
            int readByte;
            int readBytes = 0;
            try {
                while ((readByte = is.read()) != -1) {
//                    LOGGER.debug("Thread:" + Thread.currentThread().getName() + " read byte:"
//                            + readByte);
                    assertEquals("ContentMismatch when reading from inputstream",
                            readByte, fis.read());
                    readBytes++;
                }
            } catch (IOException e) {
                fail(e.getMessage());
            }
            assertEquals("Readbytes from InputStream should be FILE_SIZE",
                    FILE_SIZE, readBytes);

            // Read with two iterators and compare content
            for (int nrOfIterations = 0; nrOfIterations < 100; nrOfIterations++) {
                FileMediaObjectIterator bufferIter =
                        (FileMediaObjectIterator) mediaObject.getNativeAccess().iterator();
                FileMediaObjectIterator bufferIter2 =
                        (FileMediaObjectIterator) mediaObjects[0].getNativeAccess().iterator();

                ByteBuffer byteBuffer;
                ByteBuffer byteBuffer2;
                long starttime = System.currentTimeMillis();
                long stoptime = System.currentTimeMillis();

                int i = 0;
                readBytes = 0;
                int totalBytes = 0;
                int bufferIndex = 0;
                while (bufferIter.hasNext()) {
                    //LOGGER.debug(""+getName()+" will try to read buffer "+bufferIter.getPosition());
                    if (bufferIndex != bufferIter.getPosition()) {
                        LOGGER.debug("DOOOOOOOO");
                        throw new RuntimeException("Position of FileMediaObjectIterator is wrong");
                    }
                    mediaObject.getSize();
                    mediaObject.getNrOfBuffers();
                    starttime = System.currentTimeMillis();
                    try {
                        byteBuffer = bufferIter.next();
                        byteBuffer2 = bufferIter2.next();
                        readBytes = 0;
                        for (i = 0; i < byteBuffer.capacity(); i++) {
                            if (byteBuffer.get(i) != byteBuffer2.get(i)) {
                                LOGGER.debug("DOOOOOOOO");
                                throw new RuntimeException("Compare of content in bytebuffers in MediaObjectConsumer.run is no equal");
                            }
                            readBytes++;
                            totalBytes++;
                        }
                        stoptime = System.currentTimeMillis();
                        time = stoptime - starttime;
                        //                      LOGGER.debug(""+getName()+" read buffer "+(bufferIter.getPosition()-1)
                        //                            +", nr of read bytes=" + readBytes +
                        //                            ", total="+totalBytes+(totalBytes==FILE_SIZE?", FINISHED":""));

                        bufferIndex++;
                    } catch (MediaObjectException e) {
                        fail("Failed to call next on FileMediaObjectIterator");
                    }
                }
                assertEquals("Number of bytes read should be equal to size of file",
                        FILE_SIZE, totalBytes);
            }
        }
    }

    /**
     * Thread that reads all bytes from the specified FileMediaObject.
     * Throws RuntimeException if number of bytes read does not match
     * the specifiec expected bytes.
     */
    private class FileMediaObjectReader extends Thread {
        private FileMediaObject mediaObject;
        private boolean done = false;
        private int expectedNrOfBytes;

        public FileMediaObjectReader(FileMediaObject mediaObject,
                                     String name,
                                     int expectedNumberOfBytes) {
            super(name);
            this.mediaObject = mediaObject;
            this.expectedNrOfBytes = expectedNumberOfBytes;
        }

        /**
         * reads all bytes from the mediaobject in this class.
         * First reads with iterator of native interface and
         * then with InputStream. Will throw RuntimeException
         * if bytes read does not match expectedNrOfBytes.
         */
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
                    throw new RuntimeException(e.getMessage());
                }
            }
            if (bytesRead != expectedNrOfBytes) {
                throw new RuntimeException("Number of bytes read from " +
                        "native access iterator is=" + bytesRead +
                        ", should be " + expectedNrOfBytes);
            }
            InputStream is = mediaObject.getInputStream();
            bytesRead = 0;
            int readByte;
            try {
                while ((readByte = is.read()) != -1) {
                    bytesRead++;
                }
                if (expectedNrOfBytes != bytesRead) {
                    throw new RuntimeException(
                            "Should have read " + expectedNrOfBytes +
                                    ", but only read " + bytesRead + " from mediaobject with size: " +
                                    mediaObject.getSize());
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
            LOGGER.debug("Thread " + Thread.currentThread().getName()
                    + " read " + bytesRead + " from FileMediaObject with InputStream");
        }
    }
}
