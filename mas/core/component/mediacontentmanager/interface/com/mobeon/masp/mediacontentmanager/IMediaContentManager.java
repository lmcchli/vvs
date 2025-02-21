/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import java.util.List;

/**
 * This interface provides methods to manage media content resources, i.e.
 * lookup and retreive functions.
 *
 * @see com.mobeon.masp.mediacontentmanager.IMediaContentResource
 */
public interface IMediaContentManager {


    /**
     * Returns a list of {@link IMediaContentResource}s
     * that fulfills the specified {@link MediaContentResourceProperties}.
     * <p>
     * The order in which the Media Content Resources are returned is significant i.e.,
     * the first one in the list has highest priority.
     * <p>
     * If the MediaContentResourceProperties is empty
     * (i.e. null or with no content) all resources will be returned.
     * Similarly if a specific property is not set in the MediaContentResourceProperties
     * that all resources will match in that category.
     * <p>
     * Example: If the type of the resource is of no matter -
     * Don't set the type in the MediaContentResourceProperties.
     *
     * <p>
     * Example: If all prompts in english that support mime type "audio/pcmu" is to be matched but the Variant
     * does not matter, the setLanguage, setType and setMediaProperties methods should be called on the
     * MediaContentResourceProperties.
     * <p>
     * The same applies to the MediaProperties property of the MediaContentResourceProperties.
     *
     *
     * @param contentResourceProperties The MediaContentResourceProperties of type
     *                                  {@link com.mobeon.masp.mediacontentmanager.MediaContentResourceProperties} that is to match
     *                                  the IMediaContentResource.
     * @return Resources that match the contentResourceProperties
     *         (ordered by priority in descending order). If no matching resource
     *         an empty list is returned (never null).
     *
     * @see com.mobeon.masp.mediacontentmanager.MediaContentResourceProperties
     * @see com.mobeon.masp.mediacontentmanager.IMediaContentResource
     */
    List<IMediaContentResource> getMediaContentResource(
            MediaContentResourceProperties contentResourceProperties);
}
