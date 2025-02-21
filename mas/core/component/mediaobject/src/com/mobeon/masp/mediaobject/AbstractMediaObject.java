/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract implementation if {@link IMediaObject} that provides common
 * basic functionality for media objects.
 *
 * @author Mats Egland
 */
public abstract class AbstractMediaObject implements IMediaObject {

    /**
     * Lock object used for synchronization. This lock is used to
     * synchronize to list of media buffers.
     */
    protected final Object LOCK = new Object();
    /**
     * The list of <code>ByteBuffer</code>s. Package protected to allow access
     * from the iterator, {@link MediaObjectIterator}
     */
    protected final List<ByteBuffer> BYTEBUFFER_LIST =
            new ArrayList<ByteBuffer>();

    /**
     * Controls whether this media object is immutable or not.
     * Default is false (not immutable).
     * <p/>
     * volatile to be thread-safe.
     */
    private volatile boolean immutable = false;

    /**
     * The current size in bytes.
     */
    private long size = 0;

    /**
     * Empty constructor. Resulting media object is not
     * immutable.
     */
    public AbstractMediaObject() {
    }

    /**
     * The {@link MediaProperties} for the represented audio.
     */
    private final AtomicReference<MediaProperties> mediaProperties =
            new AtomicReference<MediaProperties>(new MediaProperties());

    /**
     * Creates an <code>AbstractMediaObject</code> with the
     * specified properties.
     * <p/>
     * Note that no media data is set in the resulting object, data
     * must be appended with the append method.
     *
     * @param mediaProperties The properties of the data. Can be null, in which case the
     *                        internal mediaProperties are untouched,
     *                        otherwise the internal media properties member will set to
     *                        the passed properties object.
     */
    protected AbstractMediaObject(MediaProperties mediaProperties) {
        if (mediaProperties != null) {
            this.mediaProperties.set(mediaProperties);
        }
    }

    /**
     * Constructor that creates a media object with the given data (bytebuffers)
     * given in the bytebuffers parameter.
     * The resulting MediaObject is set to be immutable, i.e. no more ByteBuffers
     * can be appended.
     *
     * @param bytebuffers The data
     * @throws IllegalArgumentException If:
     *                                  <ul>
     *                                  <li> The list of byteBuffer is null or of zero length</li>
     *                                  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *                                  A direct buffer is allocated with the method
     *                                  {@link ByteBuffer#allocateDirect(int)}</li>
     *                                  </ul>
     */
    protected AbstractMediaObject(List<ByteBuffer> bytebuffers) {

        this(bytebuffers, null);
    }

    /**
     * Creates a media object with the data (bytebuffers) given in the bytebuffers parameter,
     * and the given mediaproperties.
     * <p/>
     * The resulting MediaObject is set to be immutable, no
     * more ByteBuffers can be appended.
     *
     * @param bytebuffers     The data
     * @param mediaProperties The properties of the data. Can be null, in which case the
     *                        internal mediaProperties are untouched,
     *                        otherwise the internal media properties member will set to
     *                        the passed properties object
     * @throws IllegalArgumentException If:
     *                                  <ul>
     *                                  <li> The list of byteBuffer is null, or
     *                                  if any of the ByteBuffers in the list is null.</li>
     *                                  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *                                  A direct buffer is allocated with the method
     *                                  {@link ByteBuffer#allocateDirect(int)}</li>
     *                                  </ul>
     */
    protected AbstractMediaObject(List<ByteBuffer> bytebuffers,
                                  MediaProperties mediaProperties) {

        this(mediaProperties);

        if (bytebuffers == null) {
            throw new IllegalArgumentException(
                    "Passed list of ByteBuffers is null");
        }
        for (ByteBuffer byteBuffer : bytebuffers) {
            if (byteBuffer == null) {
                throw new IllegalArgumentException("A ByteBuffer in list of buffers was null");
            } else if (!byteBuffer.isDirect()) {
                throw new IllegalArgumentException("A ByteBuffer in list of buffers was not direct.");
            }

            BYTEBUFFER_LIST.size();
            BYTEBUFFER_LIST.add(byteBuffer);
        }
        this.setImmutable();
    }

    /**
     * Appends the specified bytebuffer to this media object.
     * <p/>
     * Appends the buffer to the end of the internal
     * buffer list. The buffer must be direct, i.e allocated
     * with the {@link java.nio.ByteBuffer#allocateDirect(int)} method or else
     * the method throws a MediaObjectException.
     * <p/>
     * <b>This method is thread-safe.</b>
     *
     * @param byteBuffer The {@link java.nio.ByteBuffer} that holds
     *                   the data.
     * @throws IllegalArgumentException  If the bytebuffer is not direct.
     *                                   A byte buffer appended to a media object must be direct.
     *                                   See {@link java.nio.ByteBuffer}.
     * @throws IllegalStateException If the mediaobject is immutable.
     */
    final void append(ByteBuffer byteBuffer) {
        if (!byteBuffer.isDirect()) {
            throw new IllegalArgumentException("The buffer to append must be direct.");

        }
        synchronized (LOCK) {
            if (immutable) {
                throw new IllegalStateException(
                        "Illegal to make append on an immutable MediaObject.");
            }
            BYTEBUFFER_LIST.add(byteBuffer);

            // Update size and set size in properties.
            size += byteBuffer.limit();
            getMediaProperties().setSize(size);

        }
    }
    
    /**
     * Appends a bytebuffer with the requested size to this media object.
     * <p/>
     * Appends the buffer to the end of the internal buffer list. The buffer 
     * will be direct, i.e allocated with the 
     * {@link java.nio.ByteBuffer#allocateDirect(int)} method.
     * <p/>
     * <b>This method is thread-safe.</b>
     *
     * @param size Buffersize.
     * 
     * @return The {@link java.nio.ByteBuffer} that holds the data.
     * 
     * @throws IllegalArgumentException  If <code>size <= 0</code>.
     * @throws IllegalStateException     If the mediaobject is immutable.
     */
    final ByteBuffer append(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Buffersize must be greater than zero.");
        }
        synchronized (LOCK) {
            if (immutable) {
                throw new IllegalStateException(
                        "Illegal to make append on an immutable MediaObject.");
            }
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(size);
            BYTEBUFFER_LIST.add(newBuffer);
            // Update size and set size in properties.
            size += newBuffer.limit();
            getMediaProperties().setSize(size);
            return newBuffer;
        }
    }

    public final void setImmutable() {
        // Acquire lock so that it is impossible to set to
        // immutable at the same time as an append
        synchronized (LOCK) {
	    if (this.immutable == false) {
		this.immutable = true;

		size = 0;
		for (ByteBuffer byteBuffer : BYTEBUFFER_LIST) {
		    size += byteBuffer.limit();
		}
		mediaProperties.get().setSize(size);
	    }
        }
    }

    public final boolean isImmutable() {
        return immutable;
    }

    public final MediaProperties getMediaProperties() {
        return mediaProperties.get();
    }

    // Default implementation
    public long getSize() {
        return mediaProperties.get().getSize();
    }

    /**
     * Returns a {@link MediaObjectInputStream} that reads
     * from this media-object.
     *
     * @return The InputStream that reads from this mediaobject.
     * @throws IllegalStateException If the mediaObject is not immutable.
     */
    public final InputStream getInputStream() {
        if (!this.isImmutable()) {
            throw new IllegalStateException("Illegal to open inputstream from " +
                    "a mutable MediaObject. Must be set to immutable first");
        }
        return new MediaObjectInputStream(this);
    }


    /**
     * Returns a string representation of the object. In general, the
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("object=").append(super.toString());
        sb.append(",mediaProperties=").append(mediaProperties);
        sb.append(",immutable=").append(immutable);
        sb.append("}");
        return sb.toString();
    }


}
