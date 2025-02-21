/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util;

/**
 * The interface to provide access to the factory used to instantiate PlatformAccessLogger objects.
 */
public interface IPlatformAccessLoggerFactory {

    /**
     * Gets the PlatformAccessLogger for the given className
     * @param className The class to instantiate a logger for
     * @return the PlatformAccessLogger for the given className
     */
    public IPlatformAccessLogger getPlatformAccessLogger(Class<?> className);

}
