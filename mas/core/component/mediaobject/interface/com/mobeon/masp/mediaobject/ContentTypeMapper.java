/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import jakarta.activation.MimeType;

/**
 * Maps codecs to a content-type and content-type to file-extension.
 * <p/>
 * A media with a specific content-type
 * may contain media with multiple and different codecs. This
 * interface maps codecs to one content-type.
 * <p/>
 * Both codecs and content-types is represented
 * as MIME-types with the class {@link jakarta.activation.MimeType}.
 *
 *
 * @author Mats Egland
 */
public interface ContentTypeMapper {
    /**
     * Maps a collection of codecs to a
     * content type.
     *
     * @param codecs The input codecs.
     *
     * @return The resulting content-type or null if no match is found.
     *
     * @throws IllegalArgumentException If argument is null.
     */
    MimeType mapToContentType(MediaMimeTypes codecs);

    /**
     * Maps a collection of codecs to a
     * file extension.
     *
     * @param codecs The input codecs.
     *
     * @return The resulting file-extension or null if no match is found.
     *
     * @throws IllegalArgumentException If argument is null.
     */
    String mapToFileExtension(MediaMimeTypes codecs);
    
    /**
     * Maps a file extension as "wav" to a specific
     * content type, such as "audio/wav".
     *
     * @param fileExtension The file extension as a string.
     *
     * @return The respective content type as a MIME type or null
     *         if no matching MIME type is found.
     *
     * @throws IllegalArgumentException If argument is null or empty.
     *
     */
    MimeType mapToContentType(String fileExtension);
}
