/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.configuration.IConfiguration;

/**
 * Holds the configuration for the Service Request Manager
 *
 * @author mmawi
 */
public interface IServiceRequestManagerConfiguration {
    /**
     * @return The timeout for a service request in milli seconds.
     */
    int getXmpServiceRequestTimeout();

    /**
     * @return Number of retries for a service request.
     */
    int getXmpServiceRequestRetries();

    /**
     * @return The client id used when sending service requests.
     */
    String getXmpClientId();

    /**
     * Reload all reloadable config parameters
     * That is: requesttimeout and requestretries.
     *
     * @param configuration The new configuration
     */
    void reload(IConfiguration configuration);
}
