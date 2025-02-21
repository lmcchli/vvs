/**
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved
 */

package com.mobeon.common.xmp.server;

/**
 * Interface for a class that handles HTTP get requests
 */
public interface HttpResponder {
    /**
     * Responds to HTTP requests by adding the content-length to the body and
     * returning it to the client.
     *@param header - the headers of the response, so far.
     *@param body - the body of the response
     */
    public void respond(String header, String body);
}
