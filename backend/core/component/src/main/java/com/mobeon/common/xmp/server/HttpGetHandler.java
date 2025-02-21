/**
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved
 */

package com.mobeon.common.xmp.server;

/**
 * Interface for a class that handles HTTP get requests
 */
public interface HttpGetHandler {
    /**
     * Handle  HTTP get request.
     *@param request String with the request information.
     *@param responder the object that forwards the respose to the client.
     */
    public void handleGet(String request, HttpResponder responder);
}
