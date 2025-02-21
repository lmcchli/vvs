package com.mobeon.masp.execution_engine.externaldocument;

import org.shiftone.cache.Cache;
import org.shiftone.cache.policy.lfu.LfuCacheFactory;

/**
 * Wraps a third party cache library. Contains methods to put/add Resource objects to/from the cache
 * <p/>
 * Uses the shiftone third party cache. See @link http://jocache.sourceforge.net/
 * The LFU cache algoritm is used which means that objects that are "least frequently used" is evicted (removed).
 *
 * @author ermmaha
 */
public class ResourceCache {
    private Cache cache;
    private String name;

    /**
     * Default constructor. Creates a cache with 30 minutes timeout on elements and maxsize 100
     */
    ResourceCache() {
        this("DefaultCache", 30 * 60 * 1000, 100);
    }

    /**
     * Constructor. Creates a cache with 30 minutes timeout on elements and maxsize 100
     *
     * @param name Name on cache
     */
    ResourceCache(String name) {
        this(name, 30 * 60 * 1000, 100);
    }

    /**
     * Constructor.
     *
     * @param name                Name on cache
     * @param timeoutMilliSeconds time before the elements are removed from the cache
     * @param maxSize             max size on cache
     */
    ResourceCache(String name, long timeoutMilliSeconds, int maxSize) {
        this.name = name;
        LfuCacheFactory f = new LfuCacheFactory();
        cache = f.newInstance(name, timeoutMilliSeconds, maxSize);
    }

    /**
     * Puts a resource to the cache.
     *
     * @param key
     * @param externalResource
     */
    void put(String key, Resource externalResource) {
        cache.addObject(key, externalResource);
    }

    /**
     * Retrieves a resource from the cache.
     *
     * @param key
     * @return the resource specified by the key
     */
    Resource get(String key) {
        return (Resource) cache.getObject(key);
    }

    /**
     * Clear the cache.
     */
    void clear() {
        cache.clear();
    }

    /**
     * Retrieves the name for this cache
     *
     * @return the name
     */
    String getName() {
        return name;
    }
}

