/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessConfigManagerFactory;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessLoggerFactory;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessProfiler;

/**
 * This interface provides access to the MAS services included in the PlatformAccess plug-in framework.
 */
public interface IPlatformAccessServicesManager {

    /**
     * This method provides access to the logging MAS service. It should be called during plug-in initialization. 
     * 
     * <pre><b>Usage:</b>
     * IPlatformAccessLoggerFactory platformAccessLoggerFactory = platformAccessServicesManager.getPlatformAccessLoggerFactory();
     * IPlatformAccessLogger log = platformAccessLoggerFactory.getPlatformAccessLogger(CLASSNAME.class);
     * log.debug("foo");
     * </pre>
     * @return The PlatformAccess Logger Factory interface to instantiate logger from. 
     */
    public IPlatformAccessLoggerFactory getPlatformAccessLoggerFactory();

    /**
     * This method provides access to the configuration manager MAS service. It should be called during plug-in initialization. 
     * 
     * <pre><b>Usage:</b>
     * IPlatformAccessConfigManagerFactory platformAccessConfigManagerFactory = platformAccessServicesManager.getPlatformAccessConfigManagerFactory();
     * IPlatformAccessConfigManager platformAccessPluginConfigManager = platformAccessConfigManagerFactory.getPlatformAccessConfigManager("/path/to/file");
     * String configParam = platformAccessPluginConfigManager.getParameter("Cm.paramName");
     * </pre>
     * @return The PlatformAccess Configuration Manager Factory to instantiate configuration manager from.
     */
    public IPlatformAccessConfigManagerFactory getPlatformAccessConfigManagerFactory();

    /**
     * This method provides access to the profiler MAS service. It should be called during plug-in initialization. 
     * 
     * <pre><b>Usage:</b>
     * IPlatformAccessProfiler platformAccessProfiler = platformAccessServicesManager.getPlatformAccessProfiler();
     * Object o = null;
     * try {
     *      o = platformAccessProfiler.enterProfilerPoint.("myMethod");
     *      
     *      //do something here
     * }
     * finally {
     *      platformAccessProfiler.exitProfilerPoint(o);
     * }
     * </pre>
     * @return The PlatformAccess Profiler.
     */
    public IPlatformAccessProfiler getPlatformAccessProfiler();

}
