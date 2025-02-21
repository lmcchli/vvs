/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.util.logging.ILogger;

/**
 * Defines the XMP interface used to communicate with a XMP server.
 * <p/>
 * Date: 2006-feb-06
 *
 * @author ermmaha
 */
public interface IXMPClient {

    /**
     * Send a request on the specified service using the specified
     * host.
     *
     * @param transId       an unique id, use getNextTransId to get the id.
     * @param request       the XmpRequest
     * @param service       the service to send the reqeuest to.
     * @param resultHandler where to send result to.
     * @param instance     Instead of using the preferred host, use this one.
     * @return              true if the request was sent ok.
     */
    public boolean sendRequest(int transId, String request, String service, XmpResultHandler resultHandler, IServiceInstance instance);

    public int nextTransId();

    public void setClientId(String id);

    public String getClientId();

    public void setLogger(ILogger logger);
}
