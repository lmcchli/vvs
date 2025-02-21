/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import java.nio.ByteBuffer;

/**
 * Default implementation of the interface
 * {@link MediaObjectNativeAccess} that provides access
 * to the concrete mediaobject class {@link AbstractMediaObject}.
 *
 * @author Mats Egland
 *
 */
public abstract class AbstractMediaObjectNativeAccessImpl implements MediaObjectNativeAccess {

    // javadoc in interface
    public void append(ByteBuffer byteBuffer) {
        getMediaObject().append(byteBuffer);
    }
    
    // javadoc in interface
    public ByteBuffer append(int size) {
        return getMediaObject().append(size);
    }

    // javadoc in interface
    public abstract IMediaObjectIterator iterator();

    /**
     * Implemented by subclasses to return the concrete
     * media object.
     *
     * @return The media object to provide access to.
     */
    protected abstract AbstractMediaObject getMediaObject();
}
