/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerException;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.xmp.server.XmpAnswer;
import com.mobeon.common.xmp.XmpAttachment;

import jakarta.activation.DataSource;
import java.util.List;

/**
 * Implementation of the <code>IXMPServiceHandler</code> interface.
 *
 * @author mmawi
 */
public class XMPServiceHandler implements IXMPServiceHandler {
    private static com.mobeon.common.logging.ILogger log = ILoggerFactory.getILogger(XMPServiceHandler.class);

    private IXMPResponseQueue responseQueue;
    private int transactionId;
    private String clientId;
    private String serviceId;
    private ISession session = null;
    private IApplicationExecution applicationExecution;

    public void handleRequest(ISession session,
                              ServiceRequest serviceRequest,
                              String clientId,
                              int transactionId,
                              IXMPResponseQueue responseQueue,
                              IApplicationExecution applicationExecution) {

        this.session = session;
        this.responseQueue = responseQueue;
        this.clientId = clientId;
        this.transactionId = transactionId;
        this.applicationExecution = applicationExecution;
        this.serviceId = serviceRequest.getServiceId();

        if(log.isDebugEnabled()) {
            log.debug("Starting application for " + clientId +
                    ", transactionId: " + transactionId);
        }

        applicationExecution.setSession(session);
        applicationExecution.start();
    }

    public void sendResponse(ServiceResponse serviceResponse)
            throws ServiceRequestManagerException {
        XmpAnswer answer = new XmpAnswer();
        answer.setTransactionId(transactionId);
        answer.setStatusCode(serviceResponse.getStatusCode());
        answer.setStatusText(serviceResponse.getStatusText());

        for (String parameterName : serviceResponse.getParameterNames()) {
            answer.addParameter(parameterName, (String)serviceResponse.getParameter(parameterName));
        }

        List<DataSource> attachments = serviceResponse.getAttachments();
        if( attachments != null) {
            for (DataSource attachment : attachments) {
                answer.addAttachment((XmpAttachment) attachment);
            }
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding response to queue, clientId: " + responseQueue.getClientId()
                        + ", Status code: " + serviceResponse.getStatusCode());
            }
            responseQueue.addResponse(answer);
        } catch (Exception e) {
            throw new ServiceRequestManagerException("Failed to add service response to queue.", e);
        }
    }

    public void cancelRequest()
            throws ServiceRequestManagerException {
        if (applicationExecution != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cancelling request." +
                        " clientId: " + clientId + ", transactionId: " + transactionId);
            }
            applicationExecution.terminate();
        } else {
            throw new ServiceRequestManagerException("No application started.");
        }
    }

    public void terminate() {
        if (log.isDebugEnabled())
            log.debug("Terminating request, a 450 response is returned." +
                    " clientId: " + clientId + ", transactionId: " + transactionId);

        XmpAnswer answer = new XmpAnswer();
        answer.setTransactionId(transactionId);
        answer.setStatusCode(ServiceResponse.STATUSCODE_REQUEST_FAILED);
        answer.setStatusText(ServiceResponse.STATUSTEXT_REQUEST_FAILED);
        responseQueue.addResponse(answer);

        if (applicationExecution != null) {
            applicationExecution.terminate();
        }
    }

    public String getSessionId() {
        return session.getId();
    }

    /**
     * Get the name of the service this service handler is processing a
     * request for.
     *
     * @return The service id.
     */
    public String getServiceId() {
        return serviceId;
    }

    public boolean equals(String clientId, Integer transactionId) {
        return clientId.equals(this.clientId) && transactionId.equals(this.transactionId);
    }
}
