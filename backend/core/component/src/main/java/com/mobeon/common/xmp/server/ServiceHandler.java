/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

import org.w3c.dom.Document;
import java.util.ArrayList;

/**
 * ServiceHandler specifies a general interface to XMP service handlers,
 * allowing a common generic implementation of the lower XMP levels.
 */
public interface ServiceHandler {

    /**
     * handleRequest is the way a service handler receives requests for its
     * service.
     *@param responseQueue the reciever of responses to this request.
     *@param serviceId the identity of this service (which of course will
     * be known by the service handler already)
     *@param clientId Unique id for the client who is sending the request
     *@param transactionId Unique (per client) id for the transaction of which
     * this request is a part.
     *@param validity - how long this request is valid
     *@param xmpDoc the parsed text of the XMP document.
     *@param attachments any "files" attached to the XMP request.
     */
    void handleRequest(XmpResponseQueue responseQueue, String serviceId,
                       String clientId, Integer transactionId, int validity,
                       Document xmpDoc, ArrayList attachments);

    /**
     * Cancel tells the service handler to cancel a transaction whose validity
     * period has expired.
     *@param clientId id of the client of the cancelled transaction.
     *@param transactionId the clients id for the cancelled transaction.
     */
    void cancelRequest(String clientId, Integer transactionId);
}
