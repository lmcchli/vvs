/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.pool;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.naming.directory.DirContext;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class.
 *
 * @author ermmaha
 */
public class DirContextPoolManager {
    private static ILogger log = ILoggerFactory.getILogger(DirContextPoolManager.class);

    /**
     * The max number of allowed contexts
     */
    private int maxSize = 5;
    /**
     * The timelimit (ms) to wait for a free context if the pool is full.
     */
    private int timeoutLimit = 10 * 1000;
    /**
     * The timelimit (ms) to use the context before it is closed.
     */
    private int forcedReleaseContextLimit = 5 * 60 * 1000;
    /**
     * A map of DirContextPool objects. Contains a pool to a specific directory
     */
    private ConcurrentHashMap<String, DirContextPool> dirContextPools = new ConcurrentHashMap<String, DirContextPool>(5);

    private ConcurrentHashMap<DirContext, DirContextPool> dirContextPoolMap = new ConcurrentHashMap<DirContext, DirContextPool>();

    /**
     * Singleton instance.
     */
    private static DirContextPoolManager instance = new DirContextPoolManager();

    private DirContextPoolManager() {
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance.
     */
    public static DirContextPoolManager getInstance() {
        return instance;
    }

    /**
     * Returns the max number of allowed contexts
     *
     * @return maxSize
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the max number of allowed contexts
     *
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Returns timelimit (ms) to wait for a free context if the pool is full.
     *
     * @return timelimit (ms)
     */
    public int getTimeoutLimit() {
        return timeoutLimit;
    }

    /**
     * Sets timelimit (ms) to wait for a free context if the pool is full.
     *
     * @param timeoutLimit (ms)
     */
    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    /**
     * Sets the timelimit (ms) to use the context before it is closed.
     *
     * @return forcedReleaseContextLimit (ms)
     */
    public int getForcedReleaseContextLimit() {
        return forcedReleaseContextLimit;
    }

    /**
     * Sets the timelimit (ms) to use the context before it is closed.
     *
     * @param forcedReleaseContextLimit (ms)
     */
    public void setForcedReleaseContextLimit(int forcedReleaseContextLimit) {
        this.forcedReleaseContextLimit = forcedReleaseContextLimit;
    }

    /**
     * Retrieves a DirContextPool keyed by providerUrl
     *
     * @param providerUrl key to get the DirContextPool
     * @return the DirContextPool object
     */
    public synchronized DirContextPool getDirContextPool(String providerUrl) {
        DirContextPool pool = dirContextPools.get(providerUrl);
        if (pool == null) {
            pool = new DirContextPool(providerUrl);
            dirContextPools.put(providerUrl, pool);
        }
        return pool;
    }

    /**
     * Removes a DirContextPool keyed by providerUrl
     *
     * @param providerUrl key
     */
    public void removeDirContextPool(String providerUrl) {
        dirContextPools.remove(providerUrl);
    }

    /**
     * Retrieves a DirContext from the DirContext pool
     * @param env
     * @return a DirContext from the DirContext pool
     * @throws NamingException
     * @logs.warning "Already retrieved Dircontext: &lt;dirContext&gt;" - The DirContextPool has returned
     * an already used DirContext. This should not occur and indicates an implementation error.
     */
    public DirContext getDirContext(Hashtable<String, String> env) throws NamingException {
        String providerUrl = env.get(Context.PROVIDER_URL);
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(providerUrl);
        PooledDirContext dirContext = pool.getDirContext(env);
        if (dirContextPoolMap.put(dirContext, pool) != null) {
            log.warn("Already retrieved DirContext: " + dirContext);
        }
        return dirContext;
    }

    public void returnDirContext(DirContext dirContext) throws NamingException {
        returnDirContext(dirContext, false);
    }

    /**
     * Returns a DirContext to the DirContext pool
     * @param dirContext the DirContext to return
     * @param release if context should be released from pool
     * @throws NamingException
     * 
     * @logs.warning "Returned unknown Dircontext: &lt;dirContext&gt;" - An unknown DirContext has
     * been returned to the pool. This should not occur and indicates an implementation error.
     */
    public void returnDirContext(DirContext dirContext, boolean release) throws NamingException {
        DirContextPool pool = dirContextPoolMap.remove(dirContext);
        if (pool != null) {
            pool.returnDirContext(dirContext, release);
        } else {
            log.warn("Returned unknown DirContext: " + dirContext);
        }
    }
}

