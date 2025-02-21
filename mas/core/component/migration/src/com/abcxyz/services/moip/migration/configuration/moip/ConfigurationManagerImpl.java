/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.configuration.moip;

import com.abcxyz.services.moip.migration.configuration.moip.ConfigurationImpl;
import com.abcxyz.services.moip.migration.configuration.moip.Utilities;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: eperber
 * Date: 2005-okt-05
 * Time: 16:21:33.
 */
public final class ConfigurationManagerImpl implements IConfigurationManager {
    private static final ILogger logger = ILoggerFactory.getILogger(IConfigurationManager.class);
    private final Collection<String> configFiles = new LinkedList<String>();
    private ConfigurationImpl currentConfig = null;
    private boolean reloadConfig = true;
    private IEventDispatcher eventDispatcher = null;

    public void setConfigFile(String... configFiles) {
        this.configFiles.clear();
        for (String s : configFiles) {
            this.configFiles.add(s);
        }
        reloadConfig = true;
        try {
            doReload();
        } catch (ConfigurationException e) {
            logger.error("Configuration load error: " + e.getMessage());
        }
    }

    public void setConfigFile(List<String> configFiles) {
        this.configFiles.clear();
        this.configFiles.addAll(configFiles);
        reloadConfig = true;
        try {
            doReload();
        } catch (ConfigurationException e) {
            logger.error("Configuration load error: " + e.getMessage());
        }

    }

    public void addConfigFile(String configFile) {
        this.configFiles.add(configFile);
        reloadConfig = true;
    }

    public synchronized IConfiguration getConfiguration() {
        if (logger.isDebugEnabled())
            logger.debug("Get configuration. reloadConfig=" + reloadConfig);
        if (reloadConfig) {
            try {
                doReload();
            } catch (ConfigurationException e) {
                logger.error("Configuration load error: " + e.getMessage());
            }
        }
        return currentConfig;
    }

    public void clearConfiguration() {
        configFiles.clear();
        currentConfig = null;
        reloadConfig = true;
    }

    public synchronized boolean reload() throws ConfigurationException {
        reloadConfig = true;
        return doReload();
    }

    private synchronized boolean doReload() throws ConfigurationException {
        if (reloadConfig) {
            logger.debug("Doing reload");

            reloadConfig = false;
            boolean tryBackup = currentConfig == null;

            try {
                currentConfig = new ConfigurationImpl(this, configFiles, tryBackup);
            } catch (ConfigurationException e) {
                if (tryBackup) {
                    logger.error("Unable to load configuration: " + e.getMessage());
                    throw new java.lang.RuntimeException(e);
                }
                currentConfig.setBackupUsed(true);
                return false;
            }

            if (eventDispatcher != null) {
                eventDispatcher.fireGlobalEvent(new ConfigurationChanged(currentConfig));
            }

            if (!currentConfig.getBackupUsed()) {
                for (String configFile : configFiles) {
                    try {
                        logger.debug("Backup file [" + configFile + "]");
                        Utilities.backupFile(configFile);
                    } catch (IOException e) {
                        logger.warn("Unable to create backup of file: " + configFile + " Cause: " + e.getMessage());
                        throw new ConfigurationException("Unable to create backup of file: " + configFile, e);
                    }
                }
            }
        }
        return !getBackupUsed();
    }

    public synchronized boolean getBackupUsed() {
        if (currentConfig != null) {
            return currentConfig.getBackupUsed();
        }
        return false;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
}
