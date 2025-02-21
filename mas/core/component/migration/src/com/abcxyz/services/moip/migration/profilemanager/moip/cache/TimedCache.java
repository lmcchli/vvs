/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
/**
 * A simple timed cache implementation
 *
 * @author mande
 */
public class TimedCache<K, V> {
    private long timeout;
    Map<K, CacheEntry> cache;

    public TimedCache(long timeout) {
        this.timeout = timeout;
        cache = new HashMap<K, CacheEntry>();
    }

    public V get(K key) {
        if (cache.containsKey(key)) {
            TimedCache<K, V>.CacheEntry cacheEntry = cache.get(key);
            if (System.currentTimeMillis() < cacheEntry.getExpirationTime()) {
                return cacheEntry.getValue();
            } else {
                cache.remove(key);
                return null;
            }
        } else {
            return null;
        }
    }
    public Set<Map.Entry<K, V>>entrySet() {
        Map<K, V> m = new HashMap<K,V>();
        Iterator<Map.Entry <K, CacheEntry>> i = cache.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<K,CacheEntry> n = i.next();
            m.put(n.getKey(), n.getValue().value);
        }
        return m.entrySet();
    }

    public void put(K key, V value) {
        cache.put(key, new CacheEntry(System.currentTimeMillis() + timeout, value));
    }

    private class CacheEntry {
        private long expires;
        private V value;

        private CacheEntry(long expires, V value) {
            this.expires = expires;
            this.value = value;
        }

        private long getExpirationTime() {
            return expires;
        }

        private V getValue() {
            return value;
        }
    }
}
