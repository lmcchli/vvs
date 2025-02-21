/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: 2006-feb-06
 *
 * @author ermmaha
 */
public class ServiceRequest {

    private String serviceId;
    private int validityTime;
    private boolean responseRequired = true;
    private HashMap<String, Object> parameters;

    public ServiceRequest() {
        parameters = new HashMap<String, Object>();
    }

    /**
     * Retrieves the service id for this request.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service id for this request.
     *
     * @param serviceId
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Retrieves the validity time for this request.
     *
     * @return the validityTime
     */
    public int getValidityTime() {
        return validityTime;
    }

    /**
     * Sets the validity time for this request.
     *
     * @param validityTime
     */
    public void setValidityTime(int validityTime) {
        this.validityTime = validityTime;
    }

    /**
     * Indicates if a response is required for the request.
     *
     * @return true if required, false if not.
     */
    public boolean getResponseRequired() {
        return responseRequired;
    }

    /**
     * Indicates if a response is required for the request. Default is true.
     *
     * @param responseRequired true if required, false if not.
     */
    public void setResponseRequired(boolean responseRequired) {
        this.responseRequired = responseRequired;
    }

    /**
     * Retrieves a list of the names of the parameters currently set for the request.
     *
     * @return list of parameternames
     */
    public String[] getParameterNames() {
        String[] params = new String[parameters.size()];
        int i = 0;
        for (Iterator<String> it = parameters.keySet().iterator();it.hasNext();) {
            params[i++] = it.next();
        }
        return params;
    }

    /**
     * Returns the value of the specified request parameter.
     *
     * @param parameterName the name
     * @return the          value
     */
    public Object getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     * Set the specified parameter to the given value.
     *
     * @param parameterName  the name
     * @param parameterValue the value
     */
    public void setParameter(String parameterName, Object parameterValue) {
        parameters.put(parameterName, parameterValue);
    }
}
