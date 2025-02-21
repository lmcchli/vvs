/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import junit.framework.TestCase;

import java.io.*;

/**
 * Test for the {@link MediaObjectIterator} class.
 *
 * @author Mats Egland
 */
public class MediaObjectInputStreamTest extends TestCase {
    /**
     * Logger used to log.
     */
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaObjectInputStream.class);
    /**
     * The file that is loaded by the <code>FileMediaObject</code>'s.
     */
    private static final String FILE_NAME = "test/com/mobeon/masp/mediaobject/test.wav";
    /**
     * The size of the file above.
     */
    private static final long FILE_SIZE = 38568;
    /**
     * The buffer size used in MEdiaObjects.
     */
    private static final int BUFFER_SIZE = 1024;
    /**
     * MediaObject that has above file as source.
     */
    private IMediaObject mediaObject;
    /**
     * Tested object.
     */
    private MediaObjectInputStream moInputStream;
    /**
     * Used to create media-objects to read from.
     */
    private IMediaObjectFactory mediaObjectFactory;

    /**
     * Creates the mediaobject factory, a mediaobject backed up
     * by the test-file and a stream from the mediaObject.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        mediaObjectFactory = new MediaObjectFactory(1024);

        File file = new File(FILE_NAME);
        try {
            mediaObject = mediaObjectFactory.create(file);
        } catch (MediaObjectException e) {
            fail("Failed to create mediaobject from file " + FILE_NAME);
        }
        moInputStream =
                new MediaObjectInputStream(mediaObject);
    }

    /**
     * Tests for the constructor
     * {@link MediaObjectInputStream#MediaObjectInputStream(IMediaObject)}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Create
     *  Condition:
     *      A non-null immutable mediaobject is created.
     *  Action:
     *  Result:
     *      Created MediaObjectInputStream.
     * <p/>
     * 2. Illegal Argument
     *  Condition:
     *  Action:
     *      1) mediaObject = null
     *      2) mediaObject is mutable
     *  Result:
     *      1-2) IllegalArgumentException is thrown.
     * </pre>
     */
    public void testConstructor() {
        IMediaObject mediaObject = null;
        File file = new File(FILE_NAME);
        try {
            mediaObject = mediaObjectFactory.create(file);
        } catch (MediaObjectException e) {
            fail("Failed to create mediaobject from file " + FILE_NAME);
        }

        // 1
        MediaObjectInputStream newMOIS =
                new MediaObjectInputStream(mediaObject);
        assertNotNull("Created MediaObjectInputStream is null", newMOIS);

        // 2
        try {
            newMOIS =
                    new MediaObjectInputStream(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        mediaObject = mediaObjectFactory.create();
        assertNotNull("Failed to create mutable mediaObject", mediaObject);
        try {
            newMOIS =
                    new MediaObjectInputStream(mediaObject);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}


    }

    /**
     * Tests for the method
     * {@link MediaObjectInputStream#read()}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Read all bytes in a file.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *  Result:
     *      The number of bytes read matches the size of the
     *      file "test.wav" (38568 bytes).
     * <p/>
     * 2. Read each file from file and compare
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      Create a FileInputStream, read each byte from that
     *      and from the MediaObjectInputStream.
     *  Result:
     *      Each byte is equal.
     * </pre>
     */
    public void testRead() {

        // 1
        long bytesRead = 0;
        try {

            while (moInputStream.read() != -1) {
                bytesRead++;
            }
        } catch (IOException e) {
            fail("Failed to read from MediaObjectInputStream: " + e.getMessage());
        }
        assertEquals("Number of read bytes do not match size of file",
                FILE_SIZE, bytesRead);

        // 2
        try {
            FileInputStream fis = new FileInputStream(FILE_NAME);
            moInputStream = new MediaObjectInputStream(mediaObject);
            try {
                int value;
                while ((value = fis.read()) != -1) {
                    assertEquals("Bytes should be equal.", value, moInputStream.read());
                }

            } catch (IOException e) {
                fail(e.getMessage());
            }

        } catch (FileNotFoundException e) {
            fail("Failed to create FileInputStream to file:" + FILE_NAME);
        }

    }

    /**
     * Tests for the method
     * {@link MediaObjectInputStream#read(byte[])}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Read all bytes in a file into a buffer.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *  Result:
     *      The number of bytes read matches the size of the
     *      file "test.wav" (38568 bytes).
     * <p/>
     * 2. Read each bytes from and compare content.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      Create a FileInputStream, read each byte from that
     *      and from the MediaObjectInputStream.
     *  Result:
     *      Each byte is equal.
     * </pre>
     */
    public void testReadBytes() {

        // 1
        long bytesRead = 0;
        long totalRead = 0;
        byte[] byteArray = new byte[1024];
        ByteArrayOutputStream byteArrayOS = null;
        try {
            byteArrayOS = new ByteArrayOutputStream();
            while ((bytesRead = moInputStream.read(byteArray)) != -1) {

                byteArrayOS.write(byteArray, 0, (int) bytesRead);
                totalRead += bytesRead;
            }

        } catch (IOException e) {
            fail("Failed to read from MediaObjectInputStream: " + e.getMessage());
        }
        assertEquals("Number of read bytes do not match size of file",
                FILE_SIZE, totalRead);

        // 2  Compare contents
        byteArray = byteArrayOS.toByteArray();
        try {
            FileInputStream fis = new FileInputStream(FILE_NAME);
            byte[] bytesFromFIS = new byte[(int) totalRead];
            byte[] readBytes = byteArrayOS.toByteArray();
            try {
                fis.read(bytesFromFIS, 0, (int) totalRead);
                int value;
                bytesRead = 0;
                for (int i = 0; i < readBytes.length; i++) {
                    assertEquals("Content mismatch",
                            bytesFromFIS[i], readBytes[i]);
                }
            } catch (IOException e) {
                fail(e.getMessage());
            }

        } catch (FileNotFoundException e) {
            fail("Failed to create FileInputStream to file:" + FILE_NAME);
        }

    }

    /**
     * Tests for the method
     * {@link com.mobeon.masp.mediaobject.MediaObjectInputStream#available()}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Bytes returned from available matches size of file.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      Call available on a new MediaObjectInputStream.
     *  Result:
     *      available == FILE_SIZE
     *
     * 2. Read from stream and call available. I.e. check
     *    that available() is updated.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *  Result:
     *      The number of bytes read matches the bytes returned
     *      from available() and the size of the file.
     *      (file "test.wav" (38568 bytes)).
     *
     * 2. Read all bytes and assert available returns 0.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      Read all bytes.
     *  Result:
     *      available returns 0.
     * <p/>
     *
     * </pre>
     */
    public void testAvailable() {
        // 1
        assertEquals("Number of bytes available should match file-size",
                FILE_SIZE, moInputStream.available());


        // 2
        long bytesRead = 0;
        long totalRead = 0;
        byte[] byteArray = new byte[1024];

        try {

            while ((bytesRead = moInputStream.read(byteArray)) != -1) {
                totalRead += bytesRead;
                assertEquals("Number of bytes available should match file-size minus" +
                        " bytes read.",
                    FILE_SIZE-totalRead, moInputStream.available());
            }

        } catch (IOException e) {
            fail("Failed to read from MediaObjectInputStream: " + e.getMessage());
        }
        assertEquals("Number of read bytes do not match size of file",
                FILE_SIZE, totalRead);

        // 3
        assertEquals("Bytes available should be 0", 0, moInputStream.available());

    }
    /**
     * Tests for the method
     * {@link MediaObjectInputStream#reset()}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Reset and check that available returns FILE_SIZE
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      1. call reset.
     *      2. Read half file and call reset.
     *      3. Read entire file and call reset.
     *  Result:
     *      available == FILE_SIZE
     *
     * 2. Read some bytes, reset and read again.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      1) reset, and read all bytes.
     *      2) read all bytes, reset, and read all bytes.
     *
     *  Result:
     *      The number of bytes read matches the size of the
     *      file "test.wav" (38568 bytes).
     * <p/>
     *
     * </pre>
     */
    public void testReset() {
        // 1
        moInputStream.reset();
        int bytesRead = 0;
        assertEquals("Number of bytes available should match file-size",
                FILE_SIZE, moInputStream.available());


        // 2
        moInputStream = new MediaObjectInputStream(mediaObject);
        moInputStream.reset();
        bytesRead = 0;
        assertEquals("Number of bytes available should match file-size",
                FILE_SIZE, moInputStream.available());
        try {

            while (moInputStream.read() != -1) {
                bytesRead++;
            }
        } catch (IOException e) {
            fail("Failed to read from MediaObjectInputStream: " + e.getMessage());
        }
        assertEquals("Number of read bytes do not match size of file",
                FILE_SIZE, bytesRead);
        moInputStream.reset();
        bytesRead = 0;
        assertEquals("Number of bytes available should be " + FILE_SIZE,
                FILE_SIZE, moInputStream.available());
        try {

            while (moInputStream.read() != -1) {
                bytesRead++;
            }
        } catch (IOException e) {
            fail("Failed to read from MediaObjectInputStream: " + e.getMessage());
        }
        assertEquals("Number of read bytes do not match size of file",
                FILE_SIZE, bytesRead);


    }
    /**
     * Tests for the method
     * {@link MediaObjectInputStream#skip(long)}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Skip 0 bytes.
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      n = 0
     *  Result:
     *      available == FILE_SIZE
     *
     * 2. Skip x number of bytes
     *  Condition:
     *      A MediaObjectInputStream is created that has
     *      "test.wav" has source.
     *  Action:
     *      1) Skip FILE_SIZE/2 bytes
     *      2) skip 1 byte
     *      3) skip FILE_SIZE
     *      4) reset and skip FILE_SIZE
     *
     *  Result:
     *      1) skip returns (FILE_SIZE/2) and available returns (FILE_SIZE -(FILE_SIZE/2))
     *      2) skip returns 1 and available returns (FILE_SIZE -(FILE_SIZE/2)-1)
     *      3) skip returns 
     *      4) available returns (0)
     * <p/>
     *
     * </pre>
     */
    public void testSkip() {
        // 1
        try {
            moInputStream.skip(0);
        } catch (IOException e) {
            fail("Failed to skip");
        }
        assertEquals("Number of bytes available should match file-size",
                FILE_SIZE, moInputStream.available());
        // 2
        try {
            long skipped = moInputStream.skip(FILE_SIZE/2);
            long available = moInputStream.available();
            assertEquals("Number of bytes available should be (FILE_SIZE -(FILE_SIZE/2))",
                (FILE_SIZE -(FILE_SIZE/2)), moInputStream.available());
            assertEquals("skip should return FILE_SIZE/2",FILE_SIZE/2, skipped);
            skipped = moInputStream.skip(1);
            assertEquals("Number of bytes available should be (FILE_SIZE -(FILE_SIZE/2))",
                (FILE_SIZE -(FILE_SIZE/2) - 1), moInputStream.available());
            assertEquals("skip should return 1",1, skipped);
            available = moInputStream.available();
            skipped = moInputStream.skip(FILE_SIZE);
            assertEquals("skip should return bytes available if jumping more than available",
                available, skipped);
            assertEquals("Number of bytes available should be 0",
                0, moInputStream.available());
            moInputStream.reset();
            assertEquals("Number of bytes available should be FILE_SIZE",
                FILE_SIZE, moInputStream.available());
            skipped = moInputStream.skip(FILE_SIZE);
            assertEquals("skip should FILE_SIZE",
                FILE_SIZE, skipped);
            assertEquals("Number of bytes available should be 0",
                0, moInputStream.available());
        } catch (IOException e) {
            fail("Failed to skip");
        }
    }
}
