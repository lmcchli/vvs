/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.ServiceUtil;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceHandlerStub;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.ServiceInstanceImpl;
import com.mobeon.common.xmp.server.XmpHandler;
import com.mobeon.common.xmp.client.XmpResult;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.MockObjectTestCase;

/**
 * @author mmawi
 */
public class IXMPClientTest extends MockObjectTestCase {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(IXMPServerImplTest.class);

    private static int SERVERPORT = 8080;
    private static String HOST = "localhost";
    private static String serviceId = "TeztService";
    private IXMPServer xmpServer;
    private IServiceInstance instance;

    /**
     * The client that is being tested.
     */
    private IXMPClient xmpClient;


    /**
     * Create the tested client and set up a server.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        xmpClient = new XMPClient();
        xmpClient.setLogger(new XMPLogger());
        xmpClient.setClientId("TestClient");
        instance = new ServiceInstanceImpl("testService");
        instance.setProperty(IServiceInstance.HOSTNAME, HOST);
        instance.setProperty(IServiceInstance.PORT, Integer.toString(SERVERPORT));

        xmpServer = IXMPServerImpl.getInstance();
        XmpHandler.setServiceHandler(serviceId, new ServiceHandlerStub());
        xmpServer.start(HOST, SERVERPORT);
    }

    public void testSendRequest() throws Exception {
        boolean sendOk;
        int transactionId;
        XMPResultHandler resultHandler;
        XmpResult xmpResult;
        ServiceRequest request = new ServiceRequest();
        request.setServiceId(serviceId);
        request.setValidityTime(10);

        // 1, preferred host
//        transactionId = xmpClient.nextTransId();
//        resultHandler = new XMPResultHandler();
//        sendOk = xmpClient.sendRequest(transactionId,
//                ServiceUtil.makeRequest(request, transactionId), serviceId, resultHandler);
//        assertTrue("SendRequest failed.", sendOk);
//        resultHandler.waitForResult(10000);
//        xmpResult = resultHandler.getXmpResult();
//        assertNotNull("No result.", xmpResult);
//        assertEquals("Transaction id is not " + transactionId,
//                transactionId, xmpResult.getTransactionId());
//        assertEquals("Status code is not 200",
//                200, xmpResult.getStatusCode());

        // 2, specified host
        transactionId = xmpClient.nextTransId();
        resultHandler = new XMPResultHandler();
        sendOk = xmpClient.sendRequest(transactionId,
                ServiceUtil.makeRequest(request, transactionId, xmpClient.getClientId()), serviceId, resultHandler, instance);
        assertTrue("SendRequest failed.", sendOk);
        resultHandler.waitForResult(10000);
        xmpResult = resultHandler.getXmpResult();
        assertNotNull("No result.", xmpResult);
        assertEquals("Transaction id is not " + transactionId,
                transactionId, xmpResult.getTransactionId());
        assertEquals("Status code is not 200",
                200, xmpResult.getStatusCode());
    }        
}
