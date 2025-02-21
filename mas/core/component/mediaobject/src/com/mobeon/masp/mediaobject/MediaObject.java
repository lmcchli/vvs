/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implementation of the {@link IMediaObject} interface that
 * takes a list of {@link java.nio.ByteBuffer}s as input parameter
 * in the constructor.
 * <br>
 * This class uses the {@link MediaObjectIterator} class to iterate over the bytebuffers.
 * <br>
 * @see MediaObjectIterator
 *
 * @author Mats Egland
 */
public final class MediaObject extends AbstractMediaObject {
    /**
     * Empty constructor. Resulting MediaObject is not
     * immutable.
     */
    public MediaObject() {
        super();
    }
    /**
     * Constructor that takes the properties of the media data.
     * Note that no media data is set in the resulting object, data
     * must be appended with the append method.
     *
     * @param mediaProperties the properties of the data.
     *                        Can be null, in which case the
     *                        internal mediaProperties are untouched,
     *                        otherwise the mimetypes is copied to the
     *                        internal mediaproperties.
     *
     */
    public MediaObject(MediaProperties mediaProperties) {
        super(mediaProperties);

    }
    /**
     * Constructs a MediaObject with the bytebuffers given in the bytebuffers
     * parameter and the given fileformat.
     * The resulting MediaObject is set to be immutable, no
     * more ByteBuffers can be appended.
     *
     * @param bytebuffers bytebuffers The data
     *
     * @throws IllegalArgumentException If:
     * <ul>
     *  <li> The list is null.</li>
     *  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *       A direct buffer is allocated with the method
     *      {@link ByteBuffer#allocateDirect(int)}.</li>
     * </ul>
     */
    public MediaObject(List<ByteBuffer> bytebuffers)  {
        this(bytebuffers, null);
    }

    /**
     * Constructs a MediaObject with the bytebuffers given in the bytebuffers parameter,
     * and the given properties,
     * The resulting MediaObject is set to be immutable, no
     * more ByteBuffers can be appended.
     *
     * @param bytebuffers       The data
     * @param mediaProperties   The properties of the data. Can be null, in which case the
     *                          internal mediaProperties are untouched,
     *                          otherwise the internal media properties member will set to
     *                          the passed properties object
     *
     * @throws IllegalArgumentException If:
     * <ul>
     *  <li> The list of byteBuffer is null or any of the contained
     *       ByteBuffers in the list is null.</li>
     *  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *       A direct buffer is allocated with the method
     *      {@link ByteBuffer#allocateDirect(int)}.</li>
     * </ul>
     */
    public MediaObject(List<ByteBuffer> bytebuffers,
                       MediaProperties mediaProperties) {

        super(bytebuffers, mediaProperties);
    }

    /**
     * Returns native access for this media object.
     *
     * @return Interface that provides access to the
     *         internal byte buffers.
     */
    public MediaObjectNativeAccess getNativeAccess() {
        return new MediaObjectNativeAccessImpl(this);
    }
}
