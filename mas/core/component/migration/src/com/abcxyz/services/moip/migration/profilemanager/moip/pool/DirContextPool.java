/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.abcxyz.services.moip.migration.profilemanager.moip.pool;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InterruptedNamingException;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Collections;

/**
 * Implements a connection pool with DirContext objects.
 *
 * @author ermmaha
 */
public class DirContextPool {
    /**
     * Logger
     */
    private static ILogger log = ILoggerFactory.getILogger(DirContextPool.class);
    /**
     * Free context's not used.
     */
    private List<PooledDirContext> freeContexts = Collections.synchronizedList(new ArrayList<PooledDirContext>());
    /**
     * context's in use.
     */
    private List<PooledDirContext> usedContexts = Collections.synchronizedList(new ArrayList<PooledDirContext>());
    /**
     * url to where this pool has context's
     */
    private String providerUrl;

    /**
     * Constructor.
     *
     * @param providerUrl
     */
    DirContextPool(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * Retrieves a context from the pool.
     *
     * @param env
     * @return a PooledDirContext object free to use.
     * @throws NamingException
     */
    public PooledDirContext getDirContext(Hashtable<String, String> env) throws NamingException {
        PooledDirContext context = null;
        boolean createdNew = false; //use this variable to be able to connect
        //the context outside the synchronized block
        long tStart = System.currentTimeMillis();

        synchronized(this){
            while (context == null) {
                context = removeContext();
                if (context == null) {
                    context = obtainNew();
                    if (context == null) {
                        // timeLeft throws TimeoutException.
                        if (log.isDebugEnabled()) log.debug("Waiting for a free context...");
                        try {
                            wait(timeLeft(tStart));
                        } catch (InterruptedException e) {
                            String errmsg = "Wait for free context interrupted";
                            if (log.isDebugEnabled()) log.debug(errmsg);
                            throw new InterruptedNamingException(errmsg);
                        }
                        if (log.isDebugEnabled()) log.debug("...done waiting");
                    } else {
                        createdNew = true;
                    }
                }
            }
            usedContexts.add(context);
        }
        if(createdNew){
            InitialDirContext initialDirContext = null;
            try {
                initialDirContext = new InitialDirContext(env);
            } catch (NamingException e){
                usedContexts.remove(context);
                throw e;
            } catch(Exception e){ // we want to catch every exception but not
                // change method signature to "catch Exception"
                usedContexts.remove(context);
                throw new NamingException(e.getMessage());
            }
            context.setContext(initialDirContext);
        }
        return context;
    }

    /**
     * Returns a context back to the pool.
     *
     * @param context to put back to the pool.
     * @throws NamingException
     */
    public void returnDirContext(Context context) throws NamingException {
        returnDirContext(context, false);
    }

    /**
     * Returns a context back to the pool.
     *
     * @param context to put back to the pool.
     * @param release if context should be released from pool
     * @throws NamingException
     *
     * @logs.warning "Returned unknown PooledDircontext: &lt;dirContext&gt;" - An unknown PooledDirContext has
     * been returned to the pool. This should not occur and indicates an implementation error.
     * @logs.error "Error in returnDirContext: java.lang.IllegalMonitorStateException &lt;errormessage&gt;" -
     * The current thread is not the owner of this object's monitor. This should not occur and indicates an
     * implementation error.
     */

    public void returnDirContext(Context context, boolean release) throws NamingException {
        PooledDirContext pooledDirContext = (PooledDirContext)context;
        boolean doDisconnectContext = false;
        synchronized(this){
            logMethodEntry("returnDirContext");
            if (!usedContexts.remove(pooledDirContext)) {
                log.warn("Returned unknown PooledDirContext: " + pooledDirContext);
            }

            if (release || pooledDirContext.getSinceCreateTime() >= DirContextPoolManager.getInstance().getForcedReleaseContextLimit()) {
                doDisconnectContext = true;
            } else {
                freeContexts.add(pooledDirContext);
            }

            try {
                // Release lock for the threads that are waiting for a free context
                notify();
            } catch (IllegalMonitorStateException e) {
                log.error("Error in returnDirContext " + e);
                throw e;
            }
        }
        if(doDisconnectContext) releaseContext(pooledDirContext);
    }

    /**
     * @return int Number of context's in pool.
     */
    public int getSize() {
        return usedContexts.size() + freeContexts.size();
    }

    int getUsedContexts() {
        return usedContexts.size();
    }

    private PooledDirContext obtainNew() {
        if (getSize() >= DirContextPoolManager.getInstance().getMaxSize()) {
            if (log.isDebugEnabled()) log.debug("Max Poolsize (" +
                DirContextPoolManager.getInstance().getMaxSize() +
                ") reached, no more context's can be added");
            return null;
        }

        if (log.isDebugEnabled()) log.debug("Creating context for " + providerUrl);

        return new PooledDirContext(providerUrl);
    }

    private long timeLeft(long tStart) throws NamingException {
        long result = DirContextPoolManager.getInstance().getTimeoutLimit() -
            (System.currentTimeMillis() - tStart);
        if (log.isDebugEnabled()) log.debug("Time left to wait: " + result);

        if (result > 0) {
            return result;
        } else {
            throw new NamingException("Timeout waiting for free context on " + providerUrl);
        }
    }

    private synchronized PooledDirContext removeContext() {
        if (freeContexts.isEmpty()) {
            return null;
        }
        return freeContexts.remove(0);
    }

    private void releaseContext(PooledDirContext context) throws NamingException {
        if (log.isDebugEnabled()) log.debug("Releasing a context for " + providerUrl);
        context.release();
    }

    private void logMethodEntry(String methodName) {
        if (log.isDebugEnabled()) log.debug("In "+methodName+
                ": usedConnections.size="
                + usedContexts.size() +
                ", freeContexts.size="
                + freeContexts.size());
    }
}


