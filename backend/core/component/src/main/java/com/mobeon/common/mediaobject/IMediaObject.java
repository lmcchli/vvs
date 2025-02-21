/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.mediaobject;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Interface to a media object. A media object is a media data
 * container that represent a media file. An <code>IMediaObject</code>
 * has data in a list of {@link ByteBuffer}s. The list must be sequential
 * meaning that the ByteBuffers are ordered.
 * <p/>
 * <p/>
 * A IMediaObject always represent a specific fileformat,
 * "MOV", "WAV" etc, and keeps its data in that format.
 * <p/>
 * That an IMediaObject has its data conformant to its given
 * fileformat does not mean that it actually represent a physical
 * file on the file system. It may have read its content from
 * a physical file but it does not represent the file in the sense
 * that a update to the data
 * will affect the underlying file.
 * <p/>
 * That a IMediaObject always has its data formatted as to represent
 * a specific file format brings that whoever injects data into it
 * MUST have formatted the data so it conforms to that file format.
 * <p/>
 * <b>
 * Reading from a media object:
 * </b>
 * There are two ways to read data from a media object, either data is read
 * as a stream of bytes from the <code>InputStream</code> returned from
 * the {@link com.mobeon.common.mediaobject.IMediaObject#getInputStream()}
 * method, or in a more "raw" fashion by iterating over the
 * <code>ByteBuffer</code>s that each holds a chunk of data of the
 * media object. The latter method should only be used if really
 * neccessary, for example when the <code>ByteBuffer</code> objects
 * is themself is needed as is the case when reading a media object
 * over JNI from the native side. The byte buffer iterator is
 * provided by the {@link MediaObjectNativeAccess} interface retrieved
 * with the {@link com.mobeon.common.mediaobject.IMediaObject#getNativeAccess()}
 * method.
 * <p/>
 * <p/>
 * <b>Writing to a media object:</b>
 * The only way to write data to a <code>IMediaObject</code> is
 * via the {@link MediaObjectNativeAccess#append(java.nio.ByteBuffer)} method
 * of the {@link MediaObjectNativeAccess} interface provided with
 * the {{@link com.mobeon.common.mediaobject.IMediaObject#getNativeAccess()} method.
 * Writing data to a IMediaObject is strongly connected to the
 * {@link ByteBuffer} class. Data is added as one or many <code>ByteBuffer</code>s
 * with the {@link MediaObjectNativeAccess#append(java.nio.ByteBuffer)} method.
 * <p/>
 * <p/>
 * A media object is either mutable or immutable.
 * If immutable it can't be modified, i.e.
 * no more data can be appended to it BUT its' properties may
 * be updated.
 * An iterator can only be retrieved from an immutable media object.
 * It is only possible to append data to an mutable media object.
 *
 * @author Mats Egland
 * @see ByteBuffer
 */
public interface IMediaObject {
    /**
     * Return the {@link MediaProperties} for this media. A URLMediaObject
     * may not have any properties. In this case the returned MediaProperties is empty.
     * The method never returns null. An IMediaObject instance guarantees that
     * is has a MediaProperties object.
     *
     * @return The properties for this media.
     */
    MediaProperties getMediaProperties();

    /**
     * Returns a {@link java.io.InputStream} that reads
     * from this media-object.
     *
     * @return The InputStream that reads from this mediaobject.
     * @throws IllegalStateException If this mediaObject is not immutable.
     *                               A MediaObject must be set to immutable
     *                               before reading from it is allowed.
     */
    InputStream getInputStream();

    /**
     * Returns a "native" interface to this media object, i.e.
     * an interface that provides access to the <code>ByteBuffer</code>s
     * that holds the data of a media object. The word native derives
     * from the fact that it is native access over JNI that requires
     * the exposure of the <code>ByteBuffer</code>s.
     *
     * @return The interface that provides access to the
     *         <code>ByteBuffer</code>s that holds the
     *         media data.
     */
    MediaObjectNativeAccess getNativeAccess();

    /**
     * Sets the media data as immutable, i.e. no more
     * data can be appended to it.
     * <p/>
     * If a media object is immutable it is not possible
     * to append more data to it, but it is legal to
     * update its properties.
     * <p/>
     * The size of the mediaobject will be calculated and updated to
     * the properties.
     */
    void setImmutable();

    /**
     * Returns whether this mediaobject is immutable or not.
     * <p/>
     * If a media object is immutable it is not possible
     * to append more data to it, but it is legal to
     * update its properties.
     *
     * @return whether or not this media object is immutable
     */
    boolean isImmutable();

    /**
     * Returns the total size in bytes of this MediaObject's data.
     *
     * @return The total size in bytes of the data.
     */
    long getSize();
}
