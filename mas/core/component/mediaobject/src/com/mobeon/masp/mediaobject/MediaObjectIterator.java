/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import java.util.Iterator;
import java.nio.ByteBuffer;

/**
 * Iterator class that implements the {@link IMediaObjectIterator} interface to
 * iterate over the {@link java.nio.ByteBuffer}s of a
 * {@link MediaObject}.
 *
 * @author Mats Egland
 */
public final class MediaObjectIterator implements IMediaObjectIterator {
    /**
     * Iterator that is delegated to. This iterator is fetched directly from
     * the mediaobject's bytebuffer list.
     */
    private final Iterator<ByteBuffer> ITERATOR;

    /**
     * Creates a iterator that iterates over the given MediaObject's list
     * of <code>ByteBuffer</code>s.
     * Only MediaObject's is allowed to create
     * MediaObjectIterator's, so the constructor is package protected.
     *
     * @param mediaObject The MediaObject that holds the <code>ByteBuffer</code>s.
     */
    MediaObjectIterator(MediaObject mediaObject) {
        ITERATOR = mediaObject.BYTEBUFFER_LIST.iterator();
    }
    /* JavaDoc in interface IMediaObjectIterator */
    public final boolean hasNext() {
        return ITERATOR.hasNext();
    }
    /* JavaDoc in interface IMediaObjectIterator */
    public final ByteBuffer next() {
        ByteBuffer byteBuffer = ITERATOR.next();
        ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        readOnlyBuffer.rewind();
        return readOnlyBuffer;

    }
}
