/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject.factory;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;

import jakarta.activation.MimeType;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Factory that creates {@link com.mobeon.masp.mediaobject.IMediaObject}s. 
 *
 * @author Mats Egland
 */
public interface IMediaObjectFactory {

    /**
     * Creates an empty IMediaObject, i.e. no data is appended to it.
     *
     * @return Empty IMediaObject
     */
    public IMediaObject create();

    /**
     * Creates a IMediaObject that takes the fileformat and the properties of the media data.
     * Note that no media data is set in the resulting object, data
     * must be appended with the append method.
     *
     * @param mediaProperties The properties of the data. Can be null, in which case the
     *                        internal mediaProperties are untouched.
     *
     * @return The created object of type IMediaObject
     *
     */
    public IMediaObject create(MediaProperties mediaProperties);

    /**
     * Creates a media object with the given data (bytebuffers).
     * The resulting MediaObject is set to be immutable, no
     * more ByteBuffers can be appended.
     *
     * @param byteBuffers The data
     *
     * @return The created object of type IMediaObject
     *
     *
     * @throws IllegalArgumentException if:
     *                                  <ul>
     *                                  <li> The list of byteBuffers is null, or
     *                                  if any of the ByteBuffers in the list is null.</li>
     *                                  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *                                  A direct buffer is allocated with the method
     *                                  {@link ByteBuffer#allocateDirect(int)}</li>
     *                                  </ul>
     *
     */
    public IMediaObject create(List<ByteBuffer> byteBuffers);


    /**
     * Creates a media object with the given data (bytebuffers)
     * and the given properties.
     * The resulting MediaObject is set to be immutable, no
     * more ByteBuffers can be appended.
     *
     * @param byteBuffers       The data
     *
     * @param mediaProperties   The properties of the data
     *
     * @return The created object of type IMediaObject
     *
     *
     * @throws IllegalArgumentException if:
     *                                  <ul>
     *                                  <li> The list of byteBuffers is null, of zero length or
     *                                  if any of the ByteBuffers in the list is null.</li>
     *                                  <li> Any of the passed <code>ByteBuffer</code>s is non-direct.
     *                                  A direct buffer is allocated with the method
     *                                  {@link ByteBuffer#allocateDirect(int)}</li>
     *                                  </ul>
     */
    public IMediaObject create(List<ByteBuffer> byteBuffers,
                               MediaProperties mediaProperties);


    /**
     * Creates a {@link IMediaObject} that loads its data from the specified file.
     * The returned IMediaObject will be set to be immutable, i.e. no more ByteBuffer's can
     * be appended to it.
     * <p>
     * The file will be read into a number of buffers. The number will depend on
     * the parameter bufferSize.
     * <p>
     *
     * @param file       The File to load data from.
     *
     * @return The created object of type IMediaObject.
     *
     * @throws MediaObjectException If fails to read from file.
     *
     * @throws IllegalArgumentException If the file does not exist or is null.
     *
     */
    public IMediaObject create(File file) throws MediaObjectException;
    /**
     * Creates a {@link IMediaObject}, that loads its data from the specified file,
     * with the specified properties.
     *
     * The returned IMediaObject will be set to be immutable, i.e. no more ByteBuffer's can
     * be appended to it.
     * <p>
     * The file will be read into a number of buffers. The number will depend on
     * the parameter bufferSize.
     * <p>
     *
     * @param file          The File to load data from
     * @param properties    The properties that will be set on
     *                      the created media-object.
     *
     * @return The created object of type IMediaObject
     *
     * @throws MediaObjectException If fails to read from file.
     *
     * @throws IllegalArgumentException If the file does not exist
     *                                  or is null.
     */
    public IMediaObject create(File file, MediaProperties properties)
            throws MediaObjectException;

    /**
     * Creates a <code>IMediaObject</code> that will read its' content from
     * the specified <code>InputStream</code> into <code>bufferSize</code> big
     * <code>ByteBuffer</code>s. The inputstream will be read at creation-time
     * with the until end-of-stream is reached.
     * <p/>
     * The returned mediaobject will be immutable, i.e. no more data can be
     * written to it.
     *
     * @param content       The InputStream that is read from.
     * @param bufferSize    The size in bytes of each ByteBuffer
     *                      that the content is read into.
     *                      If 0 the default buffersize of the factory will be used.
     * @param contentType   The content-type of the data.
     *
     * @return  The created IMediaObjected.
     *
     * @throws MediaObjectException If error occurs when reading from the
     *                              stream. Can be if the source do not exist.
     *
     * @throws IllegalArgumentException If content is null or if contentType is null.
     */
    public IMediaObject create(InputStream content,
                               int bufferSize,
                               MimeType contentType) throws MediaObjectException;
    /**
     * Creates a <code>IMediaObject</code> that will read its' content from
     * the specified <code>InputStream</code> into <code>bufferSize</code> big
     * <code>ByteBuffer</code>s. The inputstream will be read at creation-time
     * with the until end-of-stream is reached.
     * <p/>
     * The returned mediaobject will be immutable, i.e. no more data can be
     * written to it.
     *
     * @param content           The InputStream that is read from.
     * @param bufferSize        The size in bytes of each ByteBuffer
     *                          that the content is read into. If 0 the
     *                          default buffersize of the factory will be used.
     * @param mediaProperties   The properties of the MediaObject.
     *
     * @return  The created IMediaObjected.
     *
     * @throws IllegalArgumentException If content is null.
     *
     * @throws MediaObjectException If I/O error occurs when reading from the
     *                              stream.
     */
    public IMediaObject create(InputStream content,
                               int bufferSize,
                               MediaProperties mediaProperties) throws MediaObjectException;

    /**
     * Creates a <code>IMediaObject</code> that will contain the given text.
     *
     * <p/>
     * The returned mediaobject will be immutable, i.e. no more data can be
     * written to it.
     * @param text              The text that the media object will contain.
     * @param mediaProperties   The properties of the media object.
     *
     * @return                  The created IMediaObjected.
     *
     * @throws IllegalArgumentException If text is null.
     */
    public IMediaObject create(String text, MediaProperties mediaProperties) throws MediaObjectException;

    /**
     * Sets the buffersize of the ByteBuffers which are
     * used to read files to memory.
     *
     * @param bufferSize ByteBuffer size.
     * @throws IllegalArgumentException if the buffer size is less than or equal to zero.
     */
    public void setBufferSize(int bufferSize);


}
