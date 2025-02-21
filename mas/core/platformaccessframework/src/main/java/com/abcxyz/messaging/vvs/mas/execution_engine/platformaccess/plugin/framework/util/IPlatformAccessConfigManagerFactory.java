/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util;

/**
 * The IPlatformAccessConfigManagerFactory interface defines the methods that the PlatfromAccess plug-in can invoke to instantiate 
 * IPlatformAccessConfigManager objects for configuration files.
 * <p> 
 * To obtain a IPlatformAccessConfigManager object for a configuration file, the configuration file must be valid 
 * against a schema that follows a standard MiO configuration schema format.
 * Please refer to the sample code provided in the VVS MAS PlatformAccess SDK.
 * <p>
 * By convention, the configuration file has the .conf extension and the schema file has the .xsd extension.
 * If there is no extension provided in the configuration file name that is passed to get a IPlatformAccessConfigManager object, 
 * the .conf extension is assumed and added.
 */
public interface IPlatformAccessConfigManagerFactory {

    /**
     * Gets a {@link IPlatformAccessConfigManager} for the specified configuration file.
     * @param configFilePath the full file path and name of the configuration file
     * @return a IPlatformAccessConfigManager object for the specified configuration file or null if the file cannot be read
     */
    public IPlatformAccessConfigManager getPlatformAccessConfigManager(String configFilePath);

}
