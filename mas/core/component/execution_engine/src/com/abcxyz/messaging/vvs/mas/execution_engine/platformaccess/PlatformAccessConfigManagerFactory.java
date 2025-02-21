/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessConfigManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessConfigManagerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class PlatformAccessConfigManagerFactory implements IPlatformAccessConfigManagerFactory {

    ILogger log = ILoggerFactory.getILogger(getClass());
    private static PlatformAccessConfigManagerFactory instance = null;

    private PlatformAccessConfigManagerFactory() {
    }

    public static PlatformAccessConfigManagerFactory get() {
        if(instance == null) {
            instance = new PlatformAccessConfigManagerFactory();
        }
        return instance;
    }

    @Override
    public IPlatformAccessConfigManager getPlatformAccessConfigManager(String configFilePath) {
        IPlatformAccessConfigManager platformAccessConfigManager = null;
        try {
            ConfigManager configManager = OEManager.getConfigManager(configFilePath, null);
            platformAccessConfigManager = new PlatformAccessConfigManager(configManager);
            log.debug("Got config manager for " + configFilePath);
        } catch (ConfigurationDataException e) {
            log.error("Unable to instantiate ConfigManager for file path " + configFilePath + ": " + e.getMessage(), e);
        }

        return platformAccessConfigManager;
    }

}
