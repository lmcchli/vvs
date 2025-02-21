/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerException;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISession;

/**
 * Handles a single XMP service request. An XMP Service Handler keeps a
 * response queue for a client. When the answer is ready, the response is
 * sent back to the client using the XMP Service Handler for the client.
 *
 * @author mmawi
 */
public interface IXMPServiceHandler {
    /**
     * Initiate a new request.
     *
     * @param session The session.
     * @param serviceRequest The service request
     * @param clientId The id of the requesting client
     * @param transactionId The id of the transaction
     * @param responseQueue The response queue for the client
     * @param applicationExecution The <code>IApplicationExecution</code>
     * that will execute the service
     */
    void handleRequest(ISession session,
                       ServiceRequest serviceRequest,
                       String clientId,
                       int transactionId,
                       IXMPResponseQueue responseQueue,
                       IApplicationExecution applicationExecution);

    /**
     * Add a response in the response queue.
     *
     * @throws ServiceRequestManagerException if the response cannot be added
     * to the queue.
     *
     * @param serviceResponse The service response.
     */
    void sendResponse(ServiceResponse serviceResponse)
            throws ServiceRequestManagerException;

    /**
     * Cancel a request due to timeout.
     * The service handler will terminate the application.
     *
     * @throws ServiceRequestManagerException if no application exists
     * in this service handler.
     */
    void cancelRequest()
            throws ServiceRequestManagerException;

    /**
     * Terminate a request. The application will be terminated and
     * a service response 450 (Request Failed) is returned to the
     * client.
     */
    void terminate();

    /**
     * Get the id of the session this service handler works for
     *
     * @return the session id
     */
    String getSessionId();

    /**
     * Get the name of the service this service handler is processing a
     * request for.
     *
     * @return The service id.
     */
    String getServiceId();
    /**
     * Used to find a service handler using a client id and a transaction id
     *
     * @param clientId The client id
     * @param transactionId The transaction id
     * @return <code>true</code> if this service handler handles the specified
     * client and transaction. <code>false</code>otherwise.
     */
    boolean equals(String clientId, Integer transactionId);
}
