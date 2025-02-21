/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpResultHandler;

/**
 * Proxy class for the <code>com.mobeon.common.xmp.client.XmpClient</code> class that comes from foundation.
 *
 * Date: 2006-feb-06
 * @author ermmaha
 */
public class XMPClient implements IXMPClient {

    private XmpClient client;

    public XMPClient() {
        client = XmpClient.get();
    }

    public boolean sendRequest(int transId, String request, String service,
                               XmpResultHandler resultHandler, IServiceInstance serviceInstance) {
        return client.sendRequestToComponent(transId, request, service, resultHandler, serviceInstance);
    }

    public int nextTransId() {
        return client.nextTransId();
    }

    public void setClientId(String id) {
        client.setClientId(id);
    }

    public String getClientId() {
        return client.getClientId();
    }

    public void setLogger(ILogger logger) {
        client.setLogger(logger);
    }
}

