/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.IMediaObject;

/**
 *  Represents a cache for {@link com.mobeon.masp.mediaobject.IMediaObject}s.
 *
 *
 * @author Mats Egland
 */
public interface MediaObjectCache {
    

    /**
     * Adds an object to the cache.
     *
     * @param key           The key of the object.
     * @param mediaObject   The object to add.
     */
    void add(Object key, IMediaObject mediaObject);

    /**
     * Gets the <code>IMediaObject</code> stored in the cache by
     * its key.
     * @param key   The key the media object is stored with.
     * @return      The cached mediaobject or null if the key is not found.
     */
    IMediaObject get(Object key);

    /**
     * The number of key/value pares in the cache.
     *
     * @return The current size of cache.
     */
    int size();

    
}
