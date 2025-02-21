/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.io.File;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManagerFactory;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;


public class NotifierConfigManagerFactory implements INotifierConfigManagerFactory {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierConfigManagerFactory.class);
    private static NotifierConfigManagerFactory instance = null;
    
    private NotifierConfigManagerFactory() {
    }
    
    public static NotifierConfigManagerFactory get() {
        if(instance == null) {
            instance = new NotifierConfigManagerFactory();
        }
        return instance;
    }
    
    @Override
    public INotifierConfigManager getConfigManager(String configFilePath) {
        INotifierConfigManager notifierConfigManager = null;
        try {
            ConfigManager configManager = OEManager.getConfigManager(configFilePath, null);
            notifierConfigManager = new NotifierConfigManager(configManager);
            log.debug("Got config manager for " + configFilePath);
        } catch (ConfigurationDataException e) {
            log.error("Unable to instantiate ConfigManager for file path " + configFilePath + ": " + e.getMessage(), e);
        }
        return notifierConfigManager;
    }

    @Override
    public INotifierConfigManager getNtfConfigManager() {
        return getConfigManager(Config.getConfigFileName());
    }

    @Override
    public String getNtfConfigDirectory() {
        File ntfConfigFile = new File(Config.getConfigFileName());
        return ntfConfigFile.getParent();
    }

}
