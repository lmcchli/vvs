/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.client.XmpResult;

import java.util.Properties;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class.
 *
 * Date: 2006-feb-08
 * @author ermmaha
 */
public class ServiceUtil {

    /**
     * Create a XMP string from the request, using the specified clientId.
     * @param request   The ServiceRequest
     * @param transId   Transaction Id
     * @param clientId  Client id
     * @return  An XMP string
     */
    public static String makeRequest(ServiceRequest request, int transId, String clientId) {
        Properties properties = new Properties();
        String[] params = request.getParameterNames();
        for (int i = 0; i < params.length; i++) {
            Object o = request.getParameter(params[i]);
            if (o instanceof String) {
                properties.setProperty(params[i], (String) o);
            }
        }

        return XmpProtocol.makeRequest(transId, request.getServiceId(), properties,
                clientId, request.getValidityTime(), request.getResponseRequired());        
    }

    /**
     * @param xmpResult
     * @param clientId The id of the client this response is sent to.
     * @return a new IServiceResponse object
     */
    public static ServiceResponse makeResponse(XmpResult xmpResult, String clientId) {
        ServiceResponse serviceResponse = new ServiceResponse();
        serviceResponse.setStatusCode(xmpResult.getStatusCode());
        serviceResponse.setStatusText(xmpResult.getStatusText());
        serviceResponse.setTransactionId(xmpResult.getTransactionId());
        serviceResponse.setClientId(clientId);

        Properties properties = xmpResult.getProperties();
        if (properties != null) {
            Iterator it = properties.keySet().iterator();
            while (it.hasNext()) {
                String parameterName = (String) it.next();
                Object value = properties.getProperty(parameterName);
                serviceResponse.setParameter(parameterName, value);
            }
        }

        List attachments = xmpResult.getAttachments();
        if (attachments != null) {
            //ToDo how to get rid of this unchecked assignment ?
            serviceResponse.setAttachments(attachments);
        }

        return serviceResponse;
    }
}
