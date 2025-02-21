/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Date: 2006-feb-07
 *
 * @author ermmaha
 */
public class ServiceRequestManagerConfiguration implements IServiceRequestManagerConfiguration {

    private static ILogger log = ILoggerFactory.getILogger(ServiceRequestManagerConfiguration.class);

    public static final String REQUEST_TIMEOUT = "serviceRequestManagerRequestTimeout";
    public static final String REQUEST_RETRIES = "serviceRequestManagerRequestRetries";
    public static final String CLIENT_ID = "serviceRequestManagerClientId";
    public static final String DIAGNOSE_CLIENT_ID = "serviceRequestManagerDiagnoseClientId";


    private IConfiguration configuration;

    // timeout in milliseconds
    private int xmpServiceRequestTimeout = 30000;
    private int xmpServiceRequestRetries = 3;
    private String xmpClientId = "mas1@undefinedhost";

    /**
     * Constructor. The configuration is read.
     *
     * @param configuration To read configuration from.
     */
    ServiceRequestManagerConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
        readConfiguration(false);
    }

    public int getXmpServiceRequestTimeout() {
        return xmpServiceRequestTimeout;
    }

    public int getXmpServiceRequestRetries() {
        return xmpServiceRequestRetries;
    }

    public String getXmpClientId() {
        return xmpClientId;
    }

    public void reload(IConfiguration configuration) {
        if (configuration != null) {
            this.configuration = configuration;
            readConfiguration(true);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to reload, configuration is null.");
            }
        }
    }

    private void readConfiguration(boolean update) {
        IGroup mainGroup = null;
        try {
            mainGroup = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF);
        } catch (ConfigurationException e) {
            if (log.isInfoEnabled())
                log.info("Unable to find configuration group \"servicerequestmanager\"");
            xmpServiceRequestTimeout = 30000;
            xmpServiceRequestRetries = 3;
        }

        if (mainGroup != null) {
            try {
                xmpServiceRequestTimeout = mainGroup.getInteger(REQUEST_TIMEOUT);
            } catch (ConfigurationException e) {
                xmpServiceRequestTimeout = 30000;
                if (log.isInfoEnabled())
                    log.info("Unable to find configuration attribute \"serviceRequestManagerRequestTimeout\", " +
                            "using default value " + xmpServiceRequestTimeout + " ms");
            }

            try {
                xmpServiceRequestRetries = mainGroup.getInteger(REQUEST_RETRIES);
            } catch (ConfigurationException e) {
                xmpServiceRequestRetries = 3;
                if (log.isInfoEnabled())
                    log.info("Unable to find configuration attribute \"serviceRequestManagerRequestRetries\", " +
                            "using default value " + xmpServiceRequestRetries);
            }
            if (!update) {
                try {
                    xmpClientId = mainGroup.getString(CLIENT_ID);
                } catch (ConfigurationException e) {
                    xmpClientId = "mas1@undefinedhost";
                    if (log.isInfoEnabled())
                        log.info("Unable to find configuration attribute \"serviceRequestManagerClientId\", " +
                                "using default value " + xmpClientId);
                }
            }
        }
    }
}
