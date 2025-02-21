/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.List;


/**
 * Interface to request Media Content by specifying a identity and a number of
 * qualifiers. A Media Content Resource has an identity and a priority.
 * <p/>
 * The properties of a resource is held by a {@link IMediaContentResourceProperties} object.
 *
 * @see MediaContentResourceProperties
 */
public interface IMediaContentResource extends Comparable<IMediaContentResource> {
    /**
     * Returns the identity of this resource.
     * @return The id as a String.
     */
    public String getID();

    /**
     * Returns the priority of the resource. Can be
     * used to select between similar resources.
     * Highest priority is 1 and lowest is
     * <code>Integer.MAX_VALUE</code>.
     *
     * @return Priority for this resource.
     */
    public int getPriority();
    /**
     * Returns the properties for this resource.
     *
     * @return The properties for this resource.
     */
    IMediaContentResourceProperties getMediaContentResourceProperties();

    /**
     * Returns a list of IDs for all media contents that
     * matches the passed list of qualifiers. If qualifiers
     * is null the IDs for all media contents in the resource
     * is returned.
     * The list is sorted on Id.
     *
     * @param qualifiers The qualifiers. If null all contents
     *                   is returned.
     * @return List of matching content ids.
     *
     * @throws MediaContentManagerException If a condition for a
     *                                      specific message failed to be interpreted.
     */
    List<String> getMediaContentIDs(IMediaQualifier[] qualifiers)
            throws MediaContentManagerException;

    /**
     * Retrieves media content with the specified id
     * as a list of {@link IMediaObject}s.
     *
     * The optional <code>qualifiers</code> argument has two purposes:
     * <ul>
     * <li>To select only the media content,
     * which condition matches the qualifier list</li>
     * <li>If the content has qualifiers: To be converted to their
     * Media Object representation and be part of the returned list.</li>
     * </ul>select only
     *
     * @param mediaContentId The id of the content.
     * @param qualifiers     Optional list of qualifiers for the
     *                       content. (i.e. parameter can be null).
     * @return List of {@link IMediaObject}s that represents
     *         the specified id, and matches the qualifiers.
     *         Empty list if no match, never returns null.
     *
     * @throws MediaContentManagerException If error occurs when creating the
     *                                      media objects or if a condition for a
     *                                      specific message failed to be interpreted.
     *
     * @throws IllegalArgumentException     If id is null or if the qualifiers specified
     *                                      is not valid input to the content.
     *                                      The number of qualifiers specified must match
     *                                      the number of qualifiers of the content and
     *                                      also be of correct types.
     *                                      This exception is also thrown if the requested
     *                                      id is not defined in the media content package.
     *
     */
    IMediaObject[] getMediaContent(String mediaContentId, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException, IllegalArgumentException;

    /**
     * Return a list of all Media Content Ids for this resource.
     * Can be used when the client is unaware of which Media Content
     * is included.
     * The list is sorted on Id.
     *
     * @return List of all Media Content for this resource.
     */
    List<String> getAllMediaContentIDs();
}
