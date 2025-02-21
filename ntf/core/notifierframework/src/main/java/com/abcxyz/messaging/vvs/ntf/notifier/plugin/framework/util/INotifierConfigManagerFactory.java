/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * The INotifierConfigManagerFactory interface defines the methods that the Notifier plug-in can invoke to instantiate 
 * INotifierConfigManager objects for configuration files.
 * <p> 
 * To obtain a INotifierConfigManager object for a configuration file, the configuration file must be valid 
 * against a schema that follows a standard MiO configuration schema format.
 * Please refer to the sample code provided in the VVS NTF Notifier SDK.
 * <p>
 * By convention, the configuration file has the .conf extension and the schema file has the .xsd extension.
 * If there is no extension provided in the configuration file name that is passed to get a INotifierConfigManager object, 
 * the .conf extension is assumed and added.
 */
public interface INotifierConfigManagerFactory {

    /**
     * Gets a {@link INotifierConfigManager} for the specified configuration file.
     * @param configFilePath the full file path and name of the configuration file
     * @return a INotifierConfigManager object for the specified configuration file or null if the file cannot be read
     */
    public INotifierConfigManager getConfigManager(String configFilePath);

    /**
     * Gets a {@link INotifierConfigManager} for the NTF component notification.conf configuration file.
     * @return a INotifierConfigManager object for the NTF component configuration.
     */
    public INotifierConfigManager getNtfConfigManager();

    /**
     * Gets the full path for the NTF component configuration directory.
     * <p>
     * This method can be useful if the Notifier plug-in configuration is located the NTF component configuration directory
     * (avoids hard-coding the directory path).
     * @return the full path for the NTF component configuration directory
     */
    public String getNtfConfigDirectory();
    
}
