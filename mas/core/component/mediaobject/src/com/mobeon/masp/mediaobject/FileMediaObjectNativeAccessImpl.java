/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

/**
 * A {@link MediaObjectNativeAccess} implementation that
 * provides access to the concrete media object class
 * {@link FileMediaObject}.
 *
 * @author Mats Egland
 */
public class FileMediaObjectNativeAccessImpl extends AbstractMediaObjectNativeAccessImpl {

    /**
     * The media object to provide access to.
     */
    private FileMediaObject mediaObject;
    /**
     * Creates a <code>MediaObjectNativeAccessImpl</code> that
     * provides access to the specified media object.
     *
     * @param mediaObject
     *
     * @throws IllegalArgumentException If mediaObject is null.
     */
    public FileMediaObjectNativeAccessImpl(FileMediaObject mediaObject) {
        if (mediaObject == null) {
            throw new IllegalArgumentException("mediaObject is null");
        }
        this.mediaObject = mediaObject;
    }

    // javadoc in interface
    public IMediaObjectIterator iterator() {
        return new FileMediaObjectIterator(mediaObject);
    }

    protected AbstractMediaObject getMediaObject() {
        return mediaObject;
    }
}
