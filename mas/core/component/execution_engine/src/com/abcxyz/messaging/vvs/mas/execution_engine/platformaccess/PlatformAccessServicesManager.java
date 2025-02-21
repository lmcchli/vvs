/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.IPlatformAccessServicesManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessConfigManagerFactory;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessLoggerFactory;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessProfiler;

public class PlatformAccessServicesManager implements IPlatformAccessServicesManager {

    private static PlatformAccessServicesManager instance;

    private PlatformAccessServicesManager() {
    }

    public static PlatformAccessServicesManager get() {
        if (instance == null) {
            instance = new PlatformAccessServicesManager();
        }
        return instance;
    }

    @Override
    public IPlatformAccessLoggerFactory getPlatformAccessLoggerFactory() {
        return PlatformAccessLoggerFactory.get();
    }

    @Override
    public IPlatformAccessConfigManagerFactory getPlatformAccessConfigManagerFactory() {
        return PlatformAccessConfigManagerFactory.get();
    }

    @Override
    public IPlatformAccessProfiler getPlatformAccessProfiler() {
        return PlatformAccessProfiler.get();
    }

}
