/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.mediaobject;

import java.nio.ByteBuffer;

/**
 * Provides raw access to a {@link IMediaObject}, i.e. exposes
 * methods for writing and reading the {@link java.nio.ByteBuffer}s that
 * constitutes the data of a media object.
 *
 * @author Mats Egland
 */
public interface MediaObjectNativeAccess {
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
     * @return The {@link java.nio.ByteBuffer} that holds the data.
     * @throws IllegalArgumentException If <code>size <= 0</code>.
     * @throws IllegalStateException    If the mediaobject is immutable.
     */
    ByteBuffer append(int size);

    /**
     * Appends the bytebuffer to this media object.
     * <p/>
     * Appends the buffer to the end of the internal
     * buffer list. The buffer must be direct, i.e allocated
     * with the {@link java.nio.ByteBuffer#allocateDirect(int)} method or else
     * the method throws a MediaObjectException.
     * <p/>
     *
     * @param byteBuffer The {@link java.nio.ByteBuffer} that holds
     *                   the data.
     * @throws IllegalArgumentException If the bytebuffer is not direct.
     *                                  A byte buffer appended to a media object must be direct.
     *                                  See {@link java.nio.ByteBuffer}.
     * @throws IllegalStateException    The mediaobject is immutable.
     */
    void append(ByteBuffer byteBuffer);

    /**
     * Returns a {@link IMediaObjectIterator} to iterate over the
     * {@link ByteBuffer}s that holds the media data. The iterator
     * can only be retrieved from a immutable media object, or else
     * IllegalStateException is thrown.
     *
     * @return An iterator to the list of <code>ByteBuffer</code>s that make
     *         up this media objects´ data.
     * @throws IllegalStateException If the MediaObject is mutable.
     */
    IMediaObjectIterator iterator();

}
