/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.PlatformAccessPluginException;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.generic.PlatformAccessPlugin;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class PlatformAccessPluginLoader {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private static final String PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME = "com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.custom.PlatformAccessPlugin";
    private static final String PLATFORMACCESS_GENERIC_PLUGIN_CLASS_NAME = "com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.generic.PlatformAccessPlugin";

    private static PlatformAccessPluginLoader instance = null;    
    private APlatformAccessPlugin platformAccessPlugin = null;
    private boolean isFirstPluginLoadingAttempt = true;

    private PlatformAccessPluginLoader() {        
    }

    public static PlatformAccessPluginLoader get() {
        if (instance == null) {
            instance = new PlatformAccessPluginLoader();
        }
        return instance;
    }

    public APlatformAccessPlugin getPlugin() {
        if (platformAccessPlugin == null && isFirstPluginLoadingAttempt) {
            try {
                Class<?> pluginClass = Class.forName(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME);
                platformAccessPlugin = (APlatformAccessPlugin) pluginClass.newInstance();
                platformAccessPlugin.initialize(PlatformAccessServicesManager.get());
                log.debug(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " class loaded and initialized.");
            } catch (ClassNotFoundException e) {
                log.debug(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " - customized platform access plugin - not loaded as not found - probably normal. ");
            } catch (InstantiationException e) {
                log.error(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " class could not be instantiated (InstantiationException): " + e.getMessage(), e);                
            } catch (IllegalAccessException e) {
                log.error(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " class could not be instantiated (IllegalAccessException): " + e.getMessage(), e);                
            } catch (PlatformAccessPluginException e) {
                platformAccessPlugin = null;
                log.error(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " class could not be initialized: " + e.getMessage(), e);  
            } catch (Throwable e) {
                platformAccessPlugin = null;
                log.error(PLATFORMACCESS_CUSTOM_PLUGIN_CLASS_NAME + " class could not be initialized: " + e.getMessage(), e);  
            }

            if (platformAccessPlugin == null) {
                try {
                    platformAccessPlugin = new PlatformAccessPlugin();
                    platformAccessPlugin.initialize(PlatformAccessServicesManager.get());
                    log.debug(PLATFORMACCESS_GENERIC_PLUGIN_CLASS_NAME + " class loaded and initialized.");
                } catch (PlatformAccessPluginException e) {
                    platformAccessPlugin = null;
                    log.error("Default PlatformAccessPlugin class could not be initialized: " + e.getMessage(), e);  
                } catch (Throwable e) {
                    platformAccessPlugin = null;
                    log.error(PLATFORMACCESS_GENERIC_PLUGIN_CLASS_NAME + " class could not be initialized: " + e.getMessage(), e);  
                }
            }

            isFirstPluginLoadingAttempt = false;            
        }
        return platformAccessPlugin;
    }

}
