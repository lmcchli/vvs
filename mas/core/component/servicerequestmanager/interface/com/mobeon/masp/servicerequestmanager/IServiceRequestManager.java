/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.masp.servicerequestmanager.xmp.IXMP;
import com.mobeon.masp.execution_engine.ServiceEnabler;
import com.mobeon.common.eventnotifier.IEventReceiver;

/**
 * The XMP Service Request Manager.
 * The Service Request Manager is responsible for providing both XMP server and client capabilities.
 *
 * The sendRequest methods are blocking/synchronous and will wait for a response before returning.
 * 
 * @author ermmaha
 */
public interface IServiceRequestManager extends IXMP, ServiceEnabler, IEventReceiver {
    /**
     * Sends a service request to an available host that supports the requested service.
     *
     * @param request   The service request
     * @return A service response
     */
    ServiceResponse sendRequest(ServiceRequest request);

    /**
     * Sends a service request to the specified host.
     *
     * @param request   The service request
     * @param hostName  The name of the host to use
     * @return A service response.
     */
    ServiceResponse sendRequest(ServiceRequest request, String hostName);

    /**
     * Sends a service request to the specified host, and the specified port.
     *
     * @param request       The service request
     * @param hostName      The name of the host to use
     * @param portNumber    The port number
     * @return A service response
     */
    ServiceResponse sendRequest(ServiceRequest request, String hostName, int portNumber);

    /**
     * Sends a service request to an available host that supports the requested service.
     * The request is sent asynchronously.
     *
     * @param request   The service request
     * @return A transaction id that can be used to fetch the response later.
     */
    int sendRequestAsync(ServiceRequest request);

    /**
     * Sends a service request to an available host that supports the requested service
     * and matches the given hostname.
     * The request is sent asynchronously.
     *
     * @param request   The service request
     * @param hostName  The name of the host to use
     * @return A transaction id that can be used to fetch the response later.
     */
    int sendRequestAsync(ServiceRequest request, String hostName);

    /**
     * Sends a service request to an available host that supports the requested service
     * and matches the given hostname and port number.
     * The request is sent asynchronously.
     *
     * @param request       The service request
     * @param hostName      The name of the host to use
     * @param portNumber    The port that shall be used
     * @return A transaction id that can be used to fetch the response later.     
     */
    int sendRequestAsync(ServiceRequest request, String hostName, int portNumber);

    /**
     * Check if a result is ready for an asynchronous request.
     *
     * @param transactionId The transaction id for the asynchronous request.
     * @return <code>true</code> if a result is ready, <code>false</code>
     * otherwise.
     */
    boolean isTransactionCompleted(int transactionId);

    /**
     * Receives a response to an asynchronous service request.
     * The call is blocking/synchronous and will return a response corresponding
     * to the given transaction. With other words this method will return a response
     * as soon as the transaction has completed. In the case where multiple XMP responses
     * are received these responses are appended into one {@link ServiceResponse}.
     *
     * @param transactionId a transaction from which a response is required.
     * @return the required response.
     */
    ServiceResponse receiveResponse(int transactionId);

    /**
     * Sends a service response back to a service handler.
     *
     * @throws ServiceRequestManagerException if the response could not
     * be sent.
     *
     * @param sessionId The id of the session created during the request.
     * @param response The response to send.
     */
    void sendResponse(String sessionId, ServiceResponse response)
            throws ServiceRequestManagerException;
}
