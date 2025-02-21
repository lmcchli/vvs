/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework;

/**
 * The APlatformAccessPlugin abstract class defines some of the PlatformAccess plug-in methods that the MAS component will invoke.
 * This includes methods which initialize the PlatformAccess plug-in and allow the PlatformAccess plug-in to handle configuration refresh.
 * Methods specific to the features of a given PlatformAccess plug-in are only defined in the plug-in's subclass of APlatformAccessPlugin.
 * <p>
 * Hence, the PlatformAccess plug-in must create a concrete class that extends this abstract class and override the methods as needed.
 * <p>
 * In order for PlatformAccess plug-in to be loaded by the MAS component, the concrete class MUST have the following package and class name:<p>
 * <code>com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.custom.PlatformAccessPlugin</code>
 */
public class APlatformAccessPlugin {

    /**
     * This method is invoked by MAS component to allow the initialization of the plug-in.
     * It MUST throw an exception in case of failing to initialize.
     * @param platformAccessServicesManager The IPlatformAccessServicesManager which provides access to the MAS services available to this plug-in
     * @throws PlatformAccessPluginException if initialization fails
     */
    @SuppressWarnings("unused")
    public void initialize(IPlatformAccessServicesManager platformAccessServicesManager) throws PlatformAccessPluginException {
        return;
    }

    /**
     * This method notifies the plug-in of a configuration refresh.
     * This method is invoked by the MAS component when a configuration refresh has been triggered.
     * <p>
     * This plug-in should retrieve new IPlatformAccessConfigManager objects and then update any configuration values stored in memory.  
     * 
     * @return boolean true if successful, false otherwise.
     */
    public boolean refreshConfig() {
        return true;
    }

}
