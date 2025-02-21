/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.executor;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The ExecutorServiceManager handles requests for ExecutorServices through a configurable mapping of ExecutorServices, typically
 * threadpools, to categories, typically class/packagenames. It is implemented as a Singleton.
 * An ExecutorService is mapped to a java packagename, or classname.
 * Once a client requests an ExecutorService in order to execute a Runnable interface, it provides its classname.
 * The classname is compared to the currently registered packagenames/classnames, and the corresponding ExecutorService
 * is returned.
 * <br>
 * If there is no registered ExecutorService for the requested class, the package is used as search key. If still no
 * match can be made, the parent packagename is used to search, then its parent packagename etc. If no ExecutorService can be
 * found using the classname/packagenames, the default ExecutorService corresponding to the key "root" is returned.
 * <br>
 * By default, an unbound cached executor is mapped to the "root" category. This mapping can be overwritten by
 * mapping a different ExecutorService to the category "root".
 * <br> <br>
 * Example of use:<br>
 * <code>
 * ExecutorServiceManager.getInstance().getExecutorService(MulticastDispatcher.class).execute( <br>
 * &nbsp;new Runnable() <br>
 * &nbsp;{ <br>
 * &nbsp;&nbsp;public void run() <br>
 * &nbsp;&nbsp;{   if( subscription_list != null ) <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;subscription_list.doEvent( e ); <br>
 * &nbsp;&nbsp;} <br>
 * &nbsp;} <br>
 * ); <br>
 * </code>
 * <br><br>
 * The class is threadsafe
 * <br>
 *
 * @see ExecutorService
 * @see Executors
 * @see Runnable
 */
public class ExecutorServiceManager {
    private static ILogger logger = ILoggerFactory.getILogger(ExecutorServiceManager.class);

    public static class ContextThreadFactory implements ThreadFactory {
        private ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        private static Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                logger.warn("Pool thread " + t + " died because of an uncaught exception !", e);
            }
        };

        public Thread newThread(Runnable r) {
            Thread t = defaultFactory.newThread(r);
            t.setContextClassLoader(ClassLoader.getSystemClassLoader());
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        }
    }

    private static ExecutorServiceManager singletonInstance = null;
    private final ConcurrentHashMap<String, ExecutorService> executors = new ConcurrentHashMap<String, ExecutorService>();
    private ArrayList<String> keys = new ArrayList<String>();
    private static Object lock = new Object();

    /**
     * Return the singleton instance of ExecutorServiceManager
     *
     * @return the singleton object instance
     */
    public static ExecutorServiceManager getInstance() {
        if (singletonInstance == null) {
            synchronized (lock) {
                if (singletonInstance == null) {
                    singletonInstance = new ExecutorServiceManager();
                }
            }
        }
        return singletonInstance;
    }

    /**
     * Add an onbound cached executor as the ExecutorService to the category "root".
     */
    private ExecutorServiceManager() {
        if (logger.isInfoEnabled())
            logger.info("The 'singleton' constructor");
        initialize();
    }

    /**
     * Adds a new category/ExecutorService mapping, by overwrinting any mapping existing for the specified category.
     * In order to make the finding of a ExecutorService a bit faster, an index sorted by reverse is updated
     * whenever a category/ExecutorService mapping is added.
     * <br>
     * Adding of category/ExecutorService mappings is typically performed as a configuration at system startup.
     * How evere, it is possible to add/modify mappings at a later during runtime.
     *
     * @param category - The classname/packagename to use as category
     * @param exec     - The mapping ExecutorService
     * @see ExecutorService
     */
    public synchronized void addExecutor(String category, ExecutorService exec) {
        executors.put(category, exec);

        Enumeration<String> keysEnum = executors.keys();
        keys = Collections.list(keysEnum);
        Collections.sort(keys);
        Collections.reverse(keys);
    }

    /**
     * Add a map of category/Executor mappings using the @link{E}
     *
     * @param map
     */
    public void setExecutors(Map<String, ExecutorService> map) {
        for (String cat : map.keySet()) {
            addExecutor(cat, map.get(cat));
        }
    }

    /**
     * A helper method used to add a bound executor, with a limited maximum number of concurrently executing threads
     * to the category/ExecutorService mapping.
     *
     * @param category - The classname/packagename to use as category
     * @param idlesize - The minimum number of idle threads to keep in the executor
     * @param maxsize  - The maximum number of concurrently executing threads.
     * @see ThreadPoolExecutor
     */
    public void addCachedThreadPool(String category, int idlesize, int maxsize) {
        addExecutor(category, new ThreadPoolExecutorService(idlesize, maxsize, category));
    }


    /**
     * Iterate the list of categories comparing them to the supplied classname. If a match is found, the
     * corresponding ExecutorService is returned. If no match can be made, return the "root" ExecutorService.
     *
     * @param clazz - The classname used to find a match for
     * @return the corresponding ExecutorService
     * @see ExecutorService
     *      TODO: Is there a better way of making the lookup threadsafe?
     */
    public synchronized ExecutorService getExecutorService(String clazz) {
        if (logger.isDebugEnabled())
            logger.debug("Attempting to retrieve ExecutorService: [" + clazz + "]");

        for (String s : keys) {
            if ((clazz.startsWith(s) &&
                    clazz.length() > s.length() &&
                    clazz.charAt(s.length()) == '.') ||
                    clazz.equals(s)) {
                if (logger.isDebugEnabled())
                    logger.debug("Found ExecutorService: [" + s + "]");
                return executors.get(s);
            }
        }
        logger.info("Returning fallback ExecutorService (requested '" + clazz + "')");
        return executors.get("root");
    }

    /**
     * Iterate the list of categories comparing them to the supplied classname. If a match is found, the
     * corresponding ExecutorService is returned. If no match can be made, return the "root" ExecutorService.
     *
     * @param clazz - The classname used to find a match for
     * @return the corresponding ExecutorService
     * @see ExecutorService
     */
    public ExecutorService getExecutorService(Class clazz) {
        return getExecutorService(clazz.getName());
    }

    /**
     * Remove all thread pools.
     * The purpose of this method is to provide means to remove thread pools in order
     * simplify the different test cases.
     */
    void clear() {
        if (logger.isDebugEnabled())
            logger.debug("Clear all executors");
        keys.clear();
        executors.clear();
        initialize();
    }

    /**
     * Creating default pool.
     * There should always be an available pool. If no pool is found the default pool will
     * be uses (returned to the client).
     */
    private void initialize() {
        if (logger.isDebugEnabled())
            logger.debug("Initializing the thread pool pool.");
        // Setting up the default thread pool
        this.addCachedThreadPool("root", 1024, 1024);
    }

    // TODO: It might be possible to hide the calls to the ExecutorServices returned by the getExecutorService(...) calls
    // TODO: in order to be able to handle shutdown/lock/unlock in a similar fashion as e.g. NtfThread.
}
