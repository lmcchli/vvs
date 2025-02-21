/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;

/**
 * Singleton class that contains configuration variables for the Provisioning Manager.
 *
 * @author ermmaha
 */
public class ProvisioningConfiguration {
    private static final String PROVISIONMANAGER_RULE_GROUP = "provisionmanager";
    private static final String CONNECTIONPOOLSIZE = "connectionpoolsize";
    private static final String CONNECTIONTIMEOUT = "connectiontimeout";
    private static final String CONNECTIONIDLETIMEOUT = "connectionidletimeout";
    private static final String COMMANDSENDRETRIES = "commandsendretries";
    private static final String DEFAULTMAILHOST = "defaultmailhost";

    private int connectionPoolSize = 5;
    private int connectionTimeout = 10 * 1000;
    private int connectionIdleTimeout = 60 * 1000;
    private int commandSendRetries = 1;
    private String defaultMailhost;

    private IConfiguration configuration;

    private static ProvisioningConfiguration instance = new ProvisioningConfiguration();

    private ProvisioningConfiguration() {
    }

    /**
     * Retrieves the singleton instance of this class
     *
     * @return the singleton instance
     */
    static ProvisioningConfiguration getInstance() {
        return instance;
    }

    /**
     * Sets the configuration. This method should only be called once when the Provisoning Manager is initiated.
     *
     * @param config The configuration instance.
     * @throws IllegalArgumentException If <code>config</code> is <code>null</code>.
     */
    void setConfiguration(IConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Parameter config is null");
        }
        configuration = config;
    }

    /**
     * Reads configuration parameters.
     *
     * @throws ConfigurationException if configuration could not be read.
     */
    void update() throws ConfigurationException {
        IGroup provisionmanager = configuration.getGroup(PROVISIONMANAGER_RULE_GROUP);
        connectionPoolSize = provisionmanager.getInteger(CONNECTIONPOOLSIZE);

        connectionTimeout = provisionmanager.getInteger(CONNECTIONTIMEOUT);

        connectionIdleTimeout = provisionmanager.getInteger(CONNECTIONIDLETIMEOUT);

        commandSendRetries = provisionmanager.getInteger(COMMANDSENDRETRIES);

        defaultMailhost = provisionmanager.getString(DEFAULTMAILHOST);
    }

    /**
     * Return max size of connectionpool
     *
     * @return max size of connectionpool
     */
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * Return idle connection timeout
     *
     * @return idle connection timeout in seconds
     */
    public int getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }

    /**
     * Return connection timeout
     *
     * @return connection timeout in seconds
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Return number of send command retries
     *
     * @return number of send command retries
     */
    public int getCommandSendRetries() {
        return commandSendRetries;
    }

    /**
     * Returns default mailhost
     *
     * @return default mailhost to use
     */
    public String getDefaultMailhost() {
        return defaultMailhost;
    }

    /**
     * For testing purposes only. Sets the default mailhost
     * @param defaultMailhost
     */
    void setDefaultMailhost(String defaultMailhost) {
        this.defaultMailhost = defaultMailhost;
    }
}
