/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.services;

import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;

/**
 * The service response receiver is responsible for receiving service request responses.
 * When a {@link ServiceRequest} is issued through the {@link IServiceRequestManager} the
 * Service Response Receiver is responsible for pending on {@link ServiceResponse}.
 */
public class ServiceResponseReceiver extends Thread {
    ServiceResponseObserver observer = null;
    IServiceRequestManager serviceRequestManager = null;
    int transactionId = -1;

    public ServiceResponseReceiver(ServiceResponseObserver observer) {
        this.observer = observer;
    }

    /**
     * This initalization medthod ensures that the fields contains proper values.
     */
    void init() {
        if (observer == null) throw new IllegalStateException("Undefined observer");
        if (serviceRequestManager == null) {
            throw new IllegalStateException("Undefined service requste manager");
        }
        if (transactionId < 0) throw new IllegalThreadStateException("Undefined transaction id");
    }

    /**
     * Starting the thread after the field values have been verified.
     */
    public void start() {
        init();
        super.start();
    }

    /**
     * 
     */
    public void run() {
        ServiceResponse response = serviceRequestManager.receiveResponse(transactionId);
        observer.receiveServiceResponse(response);
    }

    /**
     * A setter for the service request manager reference.
     * @param serviceRequestManager a service request manager.
     */
    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    /**
     * A setter for the transaction id.
     * @param transactionId a transaction id.
     */
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * A setter for the service response observer ({@link ServiceResponseObserver}).
     * @param observer
     */
    public void setObserver(ServiceResponseObserver observer) {
        this.observer = observer;
    }

}
