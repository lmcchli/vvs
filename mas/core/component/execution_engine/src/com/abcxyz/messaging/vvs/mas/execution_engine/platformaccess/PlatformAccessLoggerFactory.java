/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessLogger;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessLoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class PlatformAccessLoggerFactory implements IPlatformAccessLoggerFactory {

    private static PlatformAccessLoggerFactory instance = null;

    private PlatformAccessLoggerFactory() {        
    }

    public static PlatformAccessLoggerFactory get() {
        if(instance == null) {
            instance = new PlatformAccessLoggerFactory();
        }
        return instance;
    }

    @Override
    public IPlatformAccessLogger getPlatformAccessLogger(Class<?> theClass) {
        ILogger log = ILoggerFactory.getILogger(theClass);
        return new PlatformAccessLogger(log);
    }

}
