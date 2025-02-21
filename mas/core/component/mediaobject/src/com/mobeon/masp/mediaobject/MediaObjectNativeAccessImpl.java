/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

/**
 * A {@link MediaObjectNativeAccess} implementation that
 * provides access to the concrete media object class
 * {@link MediaObject}.
 *
 * @author Mats Egland
 */
public class MediaObjectNativeAccessImpl extends AbstractMediaObjectNativeAccessImpl {

    /**
     * The media object to provide access to.
     */
    private MediaObject mediaObject;
    /**
     * Creates a <code>MediaObjectNativeAccessImpl</code> that
     * provides access to the specified media object.
     *
     * @param mediaObject
     *
     * @throws IllegalArgumentException If mediaObject is null.
     */
    public MediaObjectNativeAccessImpl(MediaObject mediaObject) {
        if (mediaObject == null) {
            throw new IllegalArgumentException("mediaObject is null");
        }
        this.mediaObject = mediaObject;
    }

    /**
     * Returns a iterator for the list of media buffers that make up the data
     * for this media object. The method will only return an iterator if the
     * MediaObject is set to immutable, or else will throw MediaObjectException
     *
     * @return Iterator for the media data
     *
     * @throws IllegalStateException If the MediaObject is mutable.
     */
    public IMediaObjectIterator iterator() {
        if (mediaObject.isImmutable()) {
            return new MediaObjectIterator(mediaObject);
        } else {
            throw new IllegalStateException("Can not fetch iterator from a mutable MediaObject, " +
                    " must be set to immutable first");
        }

    }
    // javadoc in superclass
    protected MediaObject getMediaObject() {
        return mediaObject;
    }
}
