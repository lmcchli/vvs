/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.services;

import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;

/**
 * This is an Observer for getting notification about an incoming service response.
 * A service response observer is attached to the {@link ServiceResponseReceiver} when
 * sending a {@link ServiceRequest}. When a {@link ServiceResponse} is received by the
 * service response receiver the observer is notified (the response is included in the
 * notification).
 */
public interface ServiceResponseObserver {
    /**
     * The incoming service response notification method.
     * @param serviceResponse a response to a service request.
     */
    public void receiveServiceResponse(ServiceResponse serviceResponse);
}
