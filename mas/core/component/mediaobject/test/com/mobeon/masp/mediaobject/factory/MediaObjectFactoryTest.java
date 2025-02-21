/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject.factory;

import com.mobeon.masp.mediaobject.*;
import junit.framework.TestCase;

import jakarta.activation.MimeType;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * JUnit tests for the {@link MediaObjectFactory} class.
 *
 * @author Mats Egland
 */
public class MediaObjectFactoryTest extends TestCase {

    /**
     * The file used in the tests.
     */
    private static String file1 = "test/com/mobeon/masp/mediaobject/gillty.wav";
    /**
     * File created from the file1 parameter.
     */
    private File file;

    /**
     * Tests for the method
     * {@link MediaObjectFactory#create(com.mobeon.masp.mediaobject.MediaProperties)}.
     * <pre>
     * 1. Null MediaProperties
     *  Condition:
     *  Action:
     *      mediaProperties=null
     *  Result:
     *      Non-null media object is created that has
     *      non-null media properties
     * <p/>
     * 2. Copy MediaProperties
     *  Condition:
     *  Action:
     *      mediaProperties=non-empty
     *  Result:
     *      Non -null media object is created that has
     *      same mediaproperties
     * <p/>
     * <p/>
     * </pre>
     */
    public void testCreate1() throws MediaObjectException {
        MediaObjectFactory factory = new MediaObjectFactory();
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        byteBuffers.add(ByteBuffer.allocateDirect(0));
        MediaProperties mp = new MediaProperties();

        // 1
        IMediaObject mediaObject = factory.create((MediaProperties) null);
        assertNotNull("Failed to create media object", mediaObject);
        assertNotNull(mediaObject.getMediaProperties());

        // 2
        mediaObject = factory.create(mp);
        assertNotNull("Failed to create media object", mediaObject);
        assertNotNull(mediaObject.getMediaProperties());
        assertSame(mediaObject.getMediaProperties(), mp);
    }

    /**
     * <pre>
     * Tests for the method
     * {@link MediaObjectFactory#create(java.util.List<java.nio.ByteBuffer>)}.
     * <p/>
     * 1. Basic creation
     *  Condition:
     *  Action:
     *      1. Call create method with good non-null arguments
     *  Result:
     *      Non -null media object is created that has
     *      non-null media properties
     * <p/>
     * 2. Illegal arbuments
     *  Condition:
     *  Action:
     *      1. byteBuffers=null
     *      2. byteBuffers empty list
     *      3. a bytebuffer is non-direct
     *  Result:
     *      1-3. IllegalArgumentException thrown.
     * </pre>
     */
    public void testCreate2() {
        MediaObjectFactory factory = new MediaObjectFactory();
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        byteBuffers.add(ByteBuffer.allocateDirect(0));

        // 1
        IMediaObject mediaObject = null;
        try {
            mediaObject = factory.create(byteBuffers);
            assertNotNull("Failed to create media object", mediaObject);
            assertNotNull(mediaObject.getMediaProperties());
        } catch (Throwable e) {
            fail("Failed to create MediaObject");
        }
        // 2

        try {
            mediaObject = factory.create((List<ByteBuffer>) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            byteBuffers.clear();
            mediaObject = factory.create(byteBuffers);

        } catch (IllegalArgumentException e) {
            fail("Should not throw IllegalArgumentException if empty list.");
        }
        byteBuffers.add(ByteBuffer.allocateDirect(0));
        byteBuffers.add(ByteBuffer.allocate(0));
        try {
            mediaObject = factory.create(byteBuffers);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
    }

    /**
     *
     * Tests for the method
     * {@link MediaObjectFactory#create(java.util.List<java.nio.ByteBuffer>, com.mobeon.masp.mediaobject.MediaProperties)}.
     * <pre>
     * 1. Basic creation
     *  Condition:
     *  Action:
     *      1. Call create method with good non-null arguments
     *      2. mediaProperties=null
     *  Result:
     *      1-2. Non-null media object
     *
     * 2. Illegal argument
     *  Condition:
     *  Action:
     *      1. Fileformat=null
     *      2. Fileformat is only whitespze
     *      3. FileFormat is empty
     *      4. byteBuffers=null
     *      5. byteBuffers empty list
     *      6. a bytebuffer is non-direct
     *  Result:
     *      1-6. IllegalArgumentException thrown.
     * </pre>
     */
    public void testCreate3() {
        MediaObjectFactory factory = new MediaObjectFactory();
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        byteBuffers.add(ByteBuffer.allocateDirect(0));
        MediaProperties mediaProperties = new MediaProperties();

        // 1
        IMediaObject mediaObject = factory.create(byteBuffers, mediaProperties);
        assertNotNull("Failed to create media object", mediaObject);
        mediaObject = factory.create(byteBuffers, null);
        assertNotNull("Failed to create media object", mediaObject);

        // 2
        try {
            mediaObject = factory.create((List<ByteBuffer>)null, mediaProperties);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            byteBuffers.clear();
            mediaObject = factory.create(byteBuffers, mediaProperties);
        } catch (IllegalArgumentException e) {
            fail("Should not have thrown IllegalArgumentException");
        }
        byteBuffers.add(ByteBuffer.allocateDirect(0));
        byteBuffers.add(ByteBuffer.allocate(0));
        try {
            mediaObject = factory.create(byteBuffers, mediaProperties);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
    }

    /**
     *
     * Tests for the method
     * {@link MediaObjectFactory#create(java.io.File)}.
     *
     * <pre>
     *
     * 1. Create
     *  Condition:
     *  Action:
     *      Call create method
     *  Result:
     *      Non -null media object
     *
     * 2. Illegal argument
     *  Condition:
     *  Action:
     *      1. File is null
     *      2. File does not exist
     *  Result:
     *      1-2. IllegalArgumentException thrown.
     * </pre>
     */
    public void testCreate4() {
        MediaObjectFactory factory = new MediaObjectFactory();

        // 1
        try {
            file = new File(file1);
            IMediaObject mediaObject = factory.create(file);
            assertNotNull("Failed to create media object", mediaObject);
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from file:" + file);
        }

        // 2
        try {
            file = new File("finnsej.ajda");
            IMediaObject mediaObject = factory.create(file);
            fail("Should have thrown IllegalArgumentException if non-existing file.");
        } catch (MediaObjectException e) {
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            IMediaObject mediaObject = factory.create((File) null);
            fail("Should have thrown IllegalArgumentException if file is null.");
        } catch (MediaObjectException e) {
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
    }

    /**
     * <pre>
     * Tests for
     * {@link MediaObjectFactory#create(java.io.File, com.mobeon.masp.mediaobject.MediaProperties)}.
     * <p/>
     * 1. non-null mediaProperties
     *  Condition:
     *  Action:
     *      Call create method with non-null mediaProperties
     *  Result:
     *      Non -null media object is created and
     *      the mediaProperties is same as passed.
     * <p/>
     * 2. null mediaProperties
     *  Condition:
     *  Action:
     *      Call create method with null mediaProperties
     *  Result:
     *      Non-null media object is created and
     *      the mediaProperties is not null.
     * <p/>
     * 3. Illegal argument
     *  Condition:
     *  Action:
     *      1. File is null
     *      2. File does not exist
     *  Result:
     *      1-2. IllegalArgumentException thrown.
     * </pre>
     */
    public void testCreate5() {
        MediaObjectFactory factory = new MediaObjectFactory();
        MediaProperties mediaProperties = new MediaProperties();
        // 1
        try {
            file = new File(file1);
            IMediaObject mediaObject = factory.create(file, mediaProperties);
            assertNotNull("Failed to create media object", mediaObject);
            assertSame("Should be same mediaproperties",
                    mediaProperties, mediaObject.getMediaProperties());
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from file:" + file);
        }

        // 2
        try {
            file = new File(file1);
            IMediaObject mediaObject = factory.create(file, null);
            assertNotNull("Failed to create media object", mediaObject);
            assertNotNull("MediaProperties must not be null",
                    mediaObject.getMediaProperties());
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from file:" + file);
        } catch (IllegalArgumentException e) {
            fail("Failed to create MediaObject from file:" + file);
        }

        // 3
        try {
            file = new File("finnsej.ajda");
            IMediaObject mediaObject = factory.create(file, mediaProperties);
            fail("Should have thrown IllegalArgumentException");
        } catch (MediaObjectException e) {
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            IMediaObject mediaObject = factory.create((File) null, mediaProperties);
            fail("Should have thrown IllegalArgumentException");
        } catch (MediaObjectException e) {
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {/*ok*/}
    }

    /**
     * Tests for the method
     * {@link MediaObjectFactory#create(java.io.InputStream, int, com.mobeon.masp.mediaobject.MediaProperties)}.
     *
     * <pre>
     * 1. IllegalArgument
     *  Condition:
     *  Action:
     *      1) inputstream = null
     *
     *  Result:
     *      1. IllegalArgumentException.
     *
     *
     * 2. mediaProperties is non-null
     *  Condition:
     *  Action:
     *      pass a non-null mediaProperties object.
     *  Result:
     *      Successfully created MediaObject which MediaProperties is
     *      same as passed in.
     *
     * 3. mediaProperties is null
     *  Condition:
     *  Action:
     *      pass a null mediaProperties object.
     *  Result:
     *      Successfully created MediaObject which MediaProperties is
     *      not null.
     *
     * 4. Created mediaobject should be immutable
     *  Condition:
     *  Action:
     *  Result:
     *      Successfully created MediaObject which MediaProperties is
     *      immutable.
     *
     * 5. Compare content.
     *  Condition:
     *  Action:
     *      Retrieve content from mediaobject and compare with original
     *      source.
     *  Result:
     *      Content is same.
     * </pre>
     *
     *
     * @throws Exception
     */
    public void testCreate6() throws Exception {
        final MimeType AUDIO_PCMU = new MimeType("audio/pcmu");

        MediaObjectFactory factory = new MediaObjectFactory();
        MediaProperties mediaProperties = new MediaProperties();

        int length = 10000;
        ByteArrayOutputStream source = new ByteArrayOutputStream(length);
        for (int i = 0; i < length; i++) {
            source.write(i);
        }

        // 1
        IMediaObject mo = null;
        try {
            mo = factory.create(null, length / 5, AUDIO_PCMU);
            fail("Should throw IllegalArgumentException if contentType is null");
        } catch (IllegalArgumentException e) {/*ok*/}


        // 2
        try {
            mo = factory.create(
                    new ByteArrayInputStream(source.toByteArray()),
                        0, mediaProperties);
        } catch (Throwable e) {
            fail("Should not throw exception if mediaproperties is 0");
        }
        assertNotNull("Failed to create mediaobject", mo);
        IMediaObjectIterator iter = mo.getNativeAccess().iterator();
        assertSame("Should be same MediaProperties as passed in.",
                mediaProperties, mo.getMediaProperties());

        // 3
        try {
            mo = factory.create(
                    new ByteArrayInputStream(source.toByteArray()),
                        0, mediaProperties);
        } catch (Throwable e) {
            fail("Should not throw exception if mediaproperties is 0");
        }
        assertNotNull("Failed to create mediaobject", mo);

        assertNotNull("MediaProperties should never be null.",
                mo.getMediaProperties());

        // 4
        assertTrue("Created MediaObject should be immutable", mo.isImmutable());

        // 5
        mo = factory.create(
                new ByteArrayInputStream(source.toByteArray()), length / 5, AUDIO_PCMU);
        IMediaObjectIterator iterator = mo.getNativeAccess().iterator();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (iterator.hasNext()) {
            ByteBuffer byteBuffer = iterator.next();
            byteBuffer.rewind();
            while (byteBuffer.hasRemaining()) {
                result.write(byteBuffer.get());
            }
        }
        assertEquals("Size should be " + length, length, mo.getMediaProperties().getSize());
        assertTrue("Source and result should be equal!",
                Arrays.equals(source.toByteArray(), result.toByteArray()));
    }

    /**
     * Tests for the method
     * {@link MediaObjectFactory#create(java.io.InputStream, int, jakarta.activation.MimeType)}.
     *
     * <pre>
     * 1. IllegalArgument
     *  Condition:
     *  Action:
     *      1) contentType = null
     *      2) inputstream = null
     *
     *  Result:
     *      1-2. IllegalArgumentException.
     *
     * 2. Buffersize = 0
     *  Condition:
     *  Action:
     *      bufferSize = 0
     *  Result:
     *      Created non-null MediaObject with buffers with size equal to
     *      factory size.
     *
     * 3. Compare content.
     *  Condition:
     *  Action:
     *      Retrieve content from mediaobject and compare with original
     *      source.
     *  Result:
     *      Content is same.
     * </pre>
     *
     *
     * @throws Exception
     */
    public void testCreate7() throws Exception {
        final MimeType AUDIO_PCMU = new MimeType("audio/pcmu");

        MediaObjectFactory factory = new MediaObjectFactory();
        MediaProperties mediaProperties = new MediaProperties();

        int length = 10000;
        ByteArrayOutputStream source = new ByteArrayOutputStream(length);
        for (int i = 0; i < length; i++) {
            source.write(i);
        }

        // 1
        IMediaObject mo = null;
        try {
            mo = factory.create(
                    new ByteArrayInputStream(source.toByteArray()), length / 5, (MimeType)null);
            fail("Should throw IllegalArgumentException if contentType is null");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            mo = factory.create(null, length / 5, AUDIO_PCMU);
            fail("Should throw IllegalArgumentException if contentType is null");
        } catch (IllegalArgumentException e) {/*ok*/}


        // 2
        try {
            mo = factory.create(
                    new ByteArrayInputStream(source.toByteArray()), 0, AUDIO_PCMU);
        } catch (Throwable e) {
            fail("Should not throw exception if bufferSize is 0");
        }
        assertNotNull("Failed to create mediaobject", mo);
        IMediaObjectIterator iter = mo.getNativeAccess().iterator();
        ByteBuffer bb = iter.next();
        assertEquals("The size of the byteBuffer should be as default buffer size in" +
                " mediaobject-factory", factory.getBufferSize(), bb.capacity());
        // 3
        assertTrue("Created MediaObject should be immutable", mo.isImmutable());

        // 4
        mo = factory.create(
                new ByteArrayInputStream(source.toByteArray()), length / 5, AUDIO_PCMU);
        IMediaObjectIterator iterator = mo.getNativeAccess().iterator();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (iterator.hasNext()) {
            ByteBuffer byteBuffer = iterator.next();
            byteBuffer.rewind();
            while (byteBuffer.hasRemaining()) {
                result.write(byteBuffer.get());
            }
        }
        assertEquals("Size should be " + length, length, mo.getMediaProperties().getSize());
        assertTrue("Source and result should be equal!",
                Arrays.equals(source.toByteArray(), result.toByteArray()));
    }
    /**
     *
     * Tests for the method
     * {@link MediaObjectFactory#create(String, com.mobeon.masp.mediaobject.MediaProperties)}.
     * <pre>
     * 1. Basic creation
     *  Condition:
     *  Action:
     *      1. text = "aBc"
     *      2. text = "aBc", mediaProperties = null
     *
     *  Result:
     *      1-2. Non-null media object that contains char sequance "aBc"
     *
     * 2. Illegal argument
     *  Condition:
     *  Action:
     *      1. text=null
     *
     *  Result:
     *      1. IllegalArgumentException thrown.
     * </pre>
     */
    public void testCreate8() throws IOException {
        String text = "ö aBc deaf aasf lasdfj Ã¶ ADSF adk ";
        byte[] bytes = text.getBytes("UTF-8");
        byte[] buffer = new byte[bytes.length];
        MediaObjectFactory factory = new MediaObjectFactory();
        MediaProperties mediaProperties = new MediaProperties();

        // 1
        IMediaObject mediaObject = null;
        try {
            mediaObject = factory.create(text, mediaProperties);
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from text:" + text);
        }
        assertNotNull("Failed to create media object", mediaObject);
        InputStream is = mediaObject.getInputStream();
        is.read(buffer);
        for (int i = 0; i < bytes.length; i++) {

            assertEquals("Text in mediaobject is wrong",
                    bytes[i], buffer[i]);
        }
        try {
            mediaObject = factory.create(text, null);
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from text:" + text);
        }
        assertNotNull("Failed to create media object", mediaObject);
        is = mediaObject.getInputStream();
        is.read(buffer);
        for (int i = 0; i < bytes.length; i++) {

            assertEquals("Text in mediaobject is wrong",
                    bytes[i], buffer[i]);
        }
        // 2
        try {
            mediaObject = factory.create((String)null, mediaProperties);
            fail("Should have thrown IllegalArgumentException");
        } catch (MediaObjectException e) {
            fail("Failed to create MediaObject from text:" + text);
        } catch (IllegalArgumentException e) {/*ok*/}

    }

    /**
     * Tests for the methods
     * {@link com.mobeon.masp.mediaobject.factory.MediaObjectFactory#getBufferSize()} and
     * {@link MediaObjectFactory#setBufferSize(int)}.
     *
     * <pre>
     * 1. getBufferSize returns {@link MediaObjectFactory#DEFAULT_BUFFER_SIZE}.
     *  Condition:
     *  Action:
     *      Call getBufferSize without setting the size first.
     *  Result:
     *      Returns {@link MediaObjectFactory#DEFAULT_BUFFER_SIZE}
     *
     * 2. Create a MediaObject without specifying buffersize.
     *  Condition:
     *  Action:
     *      Use factory method create(File) to create a
     *      MediaObject.
     *  Result:
     *      The size of the first buffer in the created MediaObject
     *      should be equal to the default buffersize in the factory.
     *
     * 3. setSize
     *  Condition:
     *  Action:
     *      set the buffer size to 1000 bytes.
     *  Result:
     *      getBufferSize returns 1000.
     *
     * 4. Create MediaObject with new buffersize.
     *  Condition:
     *  Action:
     *      Use factory method create(File) to create a
     *      MediaObject.
     *  Result:
     *      The size of the first buffer in the created MediaObject
     *      should be equal 1000.
     *
     * </pre>
     *
     */
    public void testSize() throws MediaObjectException {
        MediaObjectFactory factory = new MediaObjectFactory();
        file = new File(file1);
        
        // 1
        assertEquals("BufferSize should be Default buffer size",
                factory.getBufferSize(), MediaObjectFactory.DEFAULT_BUFFER_SIZE);

        // 2
        IMediaObject mo = factory.create(file);
        ByteBuffer firstBuffer = mo.getNativeAccess().iterator().next();
        assertEquals("Size of first buffer in MediaObject should be " +
                "as default size in factory", factory.getBufferSize(), firstBuffer.capacity());

        // 3
        factory.setBufferSize(1000);
        assertEquals("getBufferSize should return 1000",
                1000, factory.getBufferSize());
        // 4
        mo = factory.create(file);
        firstBuffer = mo.getNativeAccess().iterator().next();
        assertEquals("Size of first buffer in MediaObject should be 1000",
                1000, firstBuffer.capacity());
    }
}
