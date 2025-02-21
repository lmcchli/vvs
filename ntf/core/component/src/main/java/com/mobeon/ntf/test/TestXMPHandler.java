/*
 * TestXMPHandler.java
 *
 * Created on den 26 februari 2007, 12:54
 */

package com.mobeon.ntf.test;

import com.mobeon.common.xmp.server.*;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.ServiceInstanceImpl;

import org.w3c.dom.Document;
import java.util.*;

/**
 *
 * @author  mnify
 */
public class TestXMPHandler implements ServiceHandler {
    
    private int statusCode = 200;
    private String statusText = "Status text";
    private int handledRequests = 0;


    private Properties props = new Properties();
    
    /** Creates a new instance of TestXMPHandler */
    public TestXMPHandler() {
    }
    
    public TestXMPHandler(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }
    
    public void addParameter( String key, String value) {
        props.put(key, value);
    }
    
    public void clearParameters() {
        props.clear();
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    
    public void cancelRequest(String clientId, Integer transactionId) {
    }
    
    public void handleRequest(XmpResponseQueue responseQueue, String serviceId, String clientId, Integer transactionId, int validity, Document xmpDoc, ArrayList attachments) {
        handledRequests++;
        XmpAnswer answer = new XmpAnswer(statusCode, statusText);
        Enumeration keys = props.keys();
        while( keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = props.getProperty(key);
            answer.addParameter(key, value);
        }
        answer.setTransactionId(transactionId);
        responseQueue.addResponse(answer);
    }

    public IServiceInstance getServiceInstance(String serviceName, String port) {
    	IServiceInstance instance = new ServiceInstanceImpl(serviceName);
    	instance.setProperty(IServiceInstance.HOSTNAME, "localhost");
    	instance.setProperty(IServiceInstance.PORT, port);
        return instance;
    }

    public int getHandledRequests() {
        return handledRequests;
    }
}
