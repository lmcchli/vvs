/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.mediaobject;

import java.nio.ByteBuffer;

/**
 * Iterator returned from a {@link IMediaObject}. Used to iterate over
 * the {@link java.nio.ByteBuffer}s of a IMediaObject.
 */
public interface IMediaObjectIterator {
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    boolean hasNext();

    /**
     * Returns the next <code>ByteBuffer</code> in the iteration.
     * Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     * <p/>
     * The <code>ByteBuffer</code> returned is a new read-only byte buffer
     * that shares the content of the actual byte buffer in the media object.
     * The new buffer's capacity, limit, position, and mark values will
     * be identical to those of this buffer BUT they are independenent
     * of the original.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *                              If iteration has no more elements.
     * @throws MediaObjectException If the operation fails, i.e. error occurrs when
     *                              fetching next <code>ByteBuffer</code> from IMediaObject.
     */
    ByteBuffer next() throws MediaObjectException;


}
