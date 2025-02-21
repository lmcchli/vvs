/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpResult;

import jakarta.activation.DataSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;

/**
 * Date: 2006-feb-06
 *
 * @author ermmaha
 */
public class ServiceResponse {
    public static final int STATUSCODE_SUCCESS_COMPLETE = 200;
    public static final String STATUSTEXT_SUCCESS_COMPLETE = "Service successfully completed";
    public static final int STATUSCODE_SUCCESS_INIT = 202;
    public static final String STATUSTEXT_SUCCESS_INIT = "Service successfully initiated";
    public static final int STATUSCODE_REQUEST_TIMEOUT = 408;
    public static final String STATUSTEXT_REQUEST_TIMEOUT = "Request timeout";
    public static final int STATUSCODE_SERVICE_NOT_AVAILABLE = 421;
    public static final String STATUSTEXT_SERVICE_NOT_AVAILABLE = "Service not available";
    public static final int STATUSCODE_REQUEST_FAILED = 450;
    public static final String STATUSTEXT_REQUEST_FAILED = "Request failed, try later";
    public static final int STATUSCODE_RESOURCE_LIMIT_EXCEEDED = 502;
    public static final String STATUSTEXT_RESOURCE_LIMIT_EXCEEDED = "Resource limit exceeded";

    private int statusCode;
    private String statusText;
    private int transactionId;
    private String clientId;
    private HashMap<String, Object> parameters;
    private List<DataSource> attachments;

    public ServiceResponse() {
        parameters = new HashMap<String, Object>();
    }

    /**
     * Returns the status code of the XMP response status code.
     * If there are more that one XMP responses we use the status code from the
     * last XMP response.
     *
     * @return the status code; 200 is success other values are failure. What
     *         the error codes are depends on the requested service. Please see
     *         IWD - Extensible Messaging Protocol - 2/155 19-1/HDB 101 02 Uen
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the XMP response status code
     *
     * @param statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the status text of the XMP response.
     * Same condtions as for the status code.
     *
     * @return the status text.
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     * Sets the status text of the XMP response.
     *
     * @param statusText the status text.
     */
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    /**
     * @return the transaction id of this XMP response.
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the transaction id of this XMP response.
     * @param transactionId
     */
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * @return the id of the client this XMP response is sent to.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the id of the client this XMP response is sent to.
     * @param clientId
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Retrieves a list of the names of the parameters currently set for the response.
     *
     * @return list of parameternames
     */
    public String[] getParameterNames() {
        String[] params = new String[parameters.size()];
        int i = 0;
        for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
            params[i++] = it.next();
        }
        return params;
    }

    /**
     * Returns the value of the specified response parameter.
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

    public List<DataSource> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<DataSource> attachments) {
        this.attachments = attachments;
    }
}
