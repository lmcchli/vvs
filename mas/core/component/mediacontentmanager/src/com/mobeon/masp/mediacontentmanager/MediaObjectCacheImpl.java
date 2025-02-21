/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import org.shiftone.cache.Cache;
import org.shiftone.cache.CacheFactory;
import org.shiftone.cache.decorator.sync.SyncCacheFactory;
import org.shiftone.cache.decorator.soft.SoftCacheFactory;
import org.shiftone.cache.policy.fifo.FifoCacheFactory;
import org.shiftone.cache.policy.lfu.LfuCacheFactory;
import org.shiftone.cache.policy.lru.LruCacheFactory;

import java.util.MissingResourceException;

/**
 * Implementation of the interface {@link MediaObjectCache} that uses
 * the Open Source cache-implementation ShiftOne Java Object Cache.
 * http://jocache.sourceforge.net/index.html.
 * <p/>
 * The cache is synchronized.
 * <p/>
 * The cache have the following properties:
 * <ul>
 * <li>
 * ElementTimeout: Maximum time that the elements are considered valid.
 * No element will ever be returned that exceeds this time limit.
 * This ensures a predictable data freshness.
 * </li>
 * <li>
 * MaxSize: Hard limit on the number of elements the cache will contain.
 * When this limit is exceeded, the least valuable element is evicted.
 * This happens immediately, on the same thread.
 * This prevents the cache from growing uncontrollably.
 * </li>
 * Policy: The caching policy, can be FIFO, LFU (least frequently used )
 * and LRU (least recently used).
 * <li>
 * MemorySensitive: If memory sensitive the objects in the cache is referenced
 * with weak referenced and so are available for garbage
 * collection.
 * </li>
 * </ul>
 *
 * @author Mats Egland
 */
public class MediaObjectCacheImpl implements MediaObjectCache {
    /**
     * Caching policies. Fifo is a first in first out policy.
     * LFU is Least Frequently used. LRU is Least Recently used.
     */
    public enum POLICY {
        FIFO, LFU, LRU}

    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaObjectCacheImpl.class);
    /**
     * The element timeout in milleseconds.
     */
    private long elementTimeout;
    /**
     * The max number of MediaObjects in the cache.
     */
    private int maxSize;

    /**
     * The cache delegate.
     */
    private Cache cache;
    /**
     * The caching policy.
     */
    private POLICY policy;
    /**
     * Whether the cache should be memory sensitive or not.
     */
    private boolean memorySensitive = false;

    /**
     * Creates a <code>MediaObjectCacheImpl</code> with the specified properties.
     *
     * @param policy            The cache policy.
     * @param maxSize           The maximum number of elements in the cache.
     * @param elementTimeout    The element timeout in the cache (milliseconds).
     * @param memorySensitive   If memory sensitive the elements in the cache is
     *                          garbage-collectable.
     *
     * @throws IllegalArgumentException If:
     * <ul>
     * <li>policy is null.</li>
     * <li>maxSize is less than or equal to zero.</li>
     * <li>elementTimeout is less than or equal to zero.</li>
     * </ul>
     */
    public MediaObjectCacheImpl(
            POLICY policy,
            int maxSize,
            long elementTimeout,
            boolean memorySensitive) {
        if (policy == null) {
            throw new IllegalArgumentException("policy is null");
        } else if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0");
        } else if (elementTimeout <= 0) {
            throw new IllegalArgumentException("elementTimeout must be greater than 0");
        }

        this.policy = policy;
        this.maxSize = maxSize;
        this.elementTimeout = elementTimeout;
        this.memorySensitive = memorySensitive;
        init();
    }

    public MediaObjectCacheImpl() {}

    /**
     * Init method, creates the {@link Cache}.
     *
     * @throws MissingResourceException If any of the required properties
     *                                  timeout or maxSize is not set.
     */
    private void init() {
        CacheFactory policyFactory = null;

        switch (policy) {
            case LFU:
                policyFactory = new LfuCacheFactory();
                break;
            case FIFO:
                policyFactory = new FifoCacheFactory();
                break;
            case LRU:
                policyFactory = new LruCacheFactory();

        }

        // Synchronize it
        SyncCacheFactory syncFactory = new SyncCacheFactory();
        syncFactory.setDelegate(policyFactory);

        // and last create a memory sensitive if configured
        if (memorySensitive) {
            SoftCacheFactory softFactory = new SoftCacheFactory();
            softFactory.setDelegate(syncFactory);
            cache = softFactory.newInstance("MediaObjectCache", elementTimeout, maxSize);
        } else {
            cache = syncFactory.newInstance("MediaObjectCache", elementTimeout, maxSize);
        }
    }

    public void add(Object key, IMediaObject mediaObject) {
        cache.addObject(key, mediaObject);
    }

    public IMediaObject get(Object key) {
        return (IMediaObject) cache.getObject(key);
    }

    public int size() {
        return cache.size();
    }

    /**
     * Removes ALL keys and values from the cache.
     */
    public void clear() {
        cache.clear();

    }


    /**
     * Sets the element timout in milliseconds for the cache.
     * each cache has a maximum time that it's elements are considered valid.
     * No element will be returned that exceeds this time limit.
     *
     * @param elementTimeout Timout in milliseconds.
     */
    public void setElementTimeout(long elementTimeout) {
        this.elementTimeout = elementTimeout;
    }

    /**
     * Sets the maximum number of MediaObjects in cache.
     * A cache has a hard limit on the number of elements it will contain.
     * When this limit is exceeded, the least valuable element is evicted.
     *
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Sets this cache to be memory sensitive. If memory sensitive
     * the objects in this cache are available for garbage collection.
     *
     * @param soft If true this cache will use wead references.
     */
    public void setMemorySensitive(boolean soft) {
        this.memorySensitive = soft;

    }

    /**
     * Sets the policy for the cache. Legal strings are
     * "FIFO", "LFU" or "LRU".
     *
     * @param policy The policy for the cache.
     * @throws IllegalArgumentException If policy is not any of above.
     */
    public void setPolicy(String policy) {
        this.policy = POLICY.valueOf(policy);
    }

}
