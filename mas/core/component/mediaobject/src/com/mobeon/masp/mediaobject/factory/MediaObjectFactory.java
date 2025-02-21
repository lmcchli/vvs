/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject.factory;

import com.mobeon.masp.mediaobject.*;

import jakarta.activation.MimeType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.util.List;
import java.util.ArrayList;

/**
 * Factory that creates {@link IMediaObject}s.
 * <p/>
 * Different types of mediaobjects is created depending on
 * what create method is called.
 * <p/>
 * The the interface IMediaObject is always returned.
 * <p/>
 *
 *
 * @author Mats Egland
 */
public final class MediaObjectFactory implements IMediaObjectFactory {

    /**
     * Default size of direct ByteBuffers used to
     * read file into memory.
     */
    public final static int DEFAULT_BUFFER_SIZE = 8000;
    /**
     * The size of the ByteBuffer's used to read
     * file into memory. Default value is 8K.
     */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * Empty Constructor. Creates a factory with the
     * default ByteBuffer size of 4*1024 bytes.
     */
    public MediaObjectFactory() {

    }

    /**
     * Creates a MediaObject factory with a buffersize
     * as specified.
     *
     * @param bufferSize The size in bytes of the direct ByteBuffers used
     *                   to read a file into memory.
     * @throws IllegalArgumentException If the buffer size is less than 1.
     */
    public MediaObjectFactory(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("BufferSize must be greater than zero");
        }
        this.bufferSize = bufferSize;
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concrete type {@link MediaObject}.
     */
    public final IMediaObject create() {
        return new MediaObject();
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concrete type {@link MediaObject}
     * with the properties as specified.
     */
    public IMediaObject create(MediaProperties mediaProperties) {
        return new MediaObject(mediaProperties);
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concrete type {@link MediaObject}
     * with the data as specified.
     */
    public IMediaObject create(List<ByteBuffer> byteBuffers) {
        return new MediaObject(byteBuffers);
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concreate type {@link MediaObject}
     * with the data and properties as specified.
     */
    public IMediaObject create(List<ByteBuffer> byteBuffers, MediaProperties mediaProperties) {
        return new MediaObject(byteBuffers, mediaProperties);
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concreate type {@link FileMediaObject}
     * from the specified file and
     * with the specified bufferSize and properties.
     */
    public IMediaObject create(File file) throws MediaObjectException {
        return create(file, null);
    }

    /**
     * See interface for details.
     * Creates a {@link FileMediaObject} from the specified file and
     * with the specified bufferSize and properties.
     */
    public IMediaObject create(File file,
                               MediaProperties properties)
            throws MediaObjectException {
        return new FileMediaObject(file, properties, bufferSize);
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concreate type {@link MediaObject}
     * with data read from specified inputstream.
     */
    public final IMediaObject create(InputStream content,
                                     int bufferSize,
                                     MimeType contentType) throws MediaObjectException {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType is null");
        }
        return create(content, bufferSize, new MediaProperties(contentType));
    }

    /**
     * See interface for details.
     * Creates a mediaobject of concrete type {@link MediaObject}
     * with data read from specified inputstream.
     */
    public final IMediaObject create(InputStream content,
                                     int bufferSize,
                                     MediaProperties mediaProperties) throws MediaObjectException {
        try {
            if (bufferSize <= 0) {
                bufferSize = this.bufferSize;
            }else if (content == null) {
                throw new IllegalArgumentException("InputStream is null");
            }
            ReadableByteChannel channel = Channels.newChannel(content);
            if (channel == null) {
                throw new MediaObjectException("Could not create ReadableByteChannel from" +
                        " InputStream=" + content);
            }
            int totalReadBytes = 0;
            MediaObject newMediaObject = new MediaObject(mediaProperties);
            int bufferReadBytes = 0;
            while (bufferReadBytes > -1) { //Not end-of-stream
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);
                int totalBufferReadBytes = 0;
                bufferReadBytes = channel.read(byteBuffer);
                while (bufferReadBytes > 0) { //Not end-of-stream or buffer capacity reached.
                    totalBufferReadBytes += bufferReadBytes;
                    bufferReadBytes = channel.read(byteBuffer);
                }
                if (totalBufferReadBytes > 0) {
                    byteBuffer.limit(totalBufferReadBytes);
                    newMediaObject.getNativeAccess().append(byteBuffer);
                    totalReadBytes += totalBufferReadBytes;
                }
            }
            channel.close();
            //Update Media properties.
            newMediaObject.getMediaProperties().setSize(totalReadBytes);
            newMediaObject.setImmutable();
            return newMediaObject;
        } catch (IOException e) {
            throw new MediaObjectException(e.getMessage());
        }
    }

    public IMediaObject create(String text, MediaProperties mediaProperties) throws MediaObjectException {
        if (text == null) {
            throw new IllegalArgumentException("text argument is null");
        }
        byte[] bytes = new byte[0];
        try {
            bytes = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new MediaObjectException(e.getMessage());
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        List<ByteBuffer> byteBufferList = new ArrayList<ByteBuffer>();
        byteBufferList.add(byteBuffer);
        return create(byteBufferList, mediaProperties);
    }

    /**
     * Returns the default buffersize used when creating mediaobjects.
     *
     * @return The size in bytes of the ByteBuffers used to build up a
     *         mediaobject.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffersize used when creating mediaobjects.
     *
     * @param bufferSize The size in bytes of each buffer used to map a portion of the file
     *                   into memory.
     *
     * @throws IllegalArgumentException If the buffer size is less than 1.
     */
    public final void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("BufferSize must be greater than zero");
        }
        this.bufferSize = bufferSize;
    }

}
