/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.xmp.server.ServiceHandler;
import com.mobeon.common.xmp.server.XmpResponseQueue;
import com.mobeon.common.xmp.server.XmpAnswer;
import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Thread safe Service Handler stub.
 *
 * @author mmawi
 */
public class ServiceHandlerStub implements ServiceHandler {
    /**
     * handleRequest is the way a service handler receives requests for its
     * service.
     *
     * @param responseQueue the reciever of responses to this request.
     * @param serviceId     the identity of this service (which of course will
     *                      be known by the service handler already)
     * @param clientId      Unique id for the client who is sending the request
     * @param transactionId Unique (per client) id for the transaction of which
     *                      this request is a part.
     * @param validity      - how long this request is valid
     * @param xmpDoc        the parsed text of the XMP document.
     * @param attachments   any "files" attached to the XMP request.
     */
    public void handleRequest(XmpResponseQueue responseQueue,
                              String serviceId,
                              String clientId,
                              Integer transactionId,
                              int validity,
                              Document xmpDoc,
                              ArrayList attachments) {
        XmpAnswer answer = new XmpAnswer();
        answer.setStatusCode(ServiceResponse.STATUSCODE_SUCCESS_COMPLETE);
        answer.setStatusText(ServiceResponse.STATUSTEXT_SUCCESS_COMPLETE);
        answer.setTransactionId(transactionId);
        
        responseQueue.addResponse(answer);
    }

    /**
     * Cancel tells the service handler to cancel a transaction whose validity
     * period has expired.
     *
     * @param clientId      id of the client of the cancelled transaction.
     * @param transactionId the clients id for the cancelled transaction.
     */
    public void cancelRequest(String clientId, Integer transactionId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
