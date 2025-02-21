/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.configuration;

import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * This is a placeholder for the MRCP ASR configuration parameters.
 */
public class SpeechRecognizerConfiguration extends AbstractConfiguration {
    private IServiceInstance service = null;

    public SpeechRecognizerConfiguration(IServiceInstance service) {
        this.service = service;
    }
    public String getHost() {
        return getParameter(IServiceInstance.HOSTNAME, "10.16.2.98");
    }

    public int getPort() {
        return getParameter(IServiceInstance.PORT, 4900);
    }

    public String getProtocol() {
        return getParameter(IServiceInstance.PROTOCOL, "mrcp");
    }

    protected IServiceInstance getService() {
        return service;
    }
}
