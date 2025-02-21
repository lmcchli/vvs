/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerException;
import com.mobeon.common.xmp.server.XmpAnswer;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * JUnit tests for {@link XMPServiceHandler} class.
 *
 * @author mmawi
 */
public class XMPServiceHandlerTest extends MockObjectTestCase {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(XMPServiceHandlerTest.class);

    private IXMPServiceHandler serviceHandler;
    private Mock jmockApplicationExecution;
    private Mock jmockSession;

    private static String sessionId = "session_1";

    /**
     * Set up common basic conditions.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        serviceHandler = new XMPServiceHandler();
    }

    /**
     * Test method {@link XMPServiceHandler#handleRequest}
     *
     * <pre>
     * <p/>
     * 1. Test one request.
     *  Condition:
     *      A service handler is created. A mocked application and a mocked
     *      response queue are created.
     *  Action:
     *      Invoke handleRequest with a ServiceRequest.
     *  Result:
     *      A session is fetched from the application.
     * <p/>
     * 2. Test the service handler's properties with equals()
     *  Condition:
     *      As in #1.
     *      A resuest is sent to the service handler. Client id is
     *      "testHandleRequest" and transaction id is 1.
     *  Action:
     *      2.1) Invoke equals("testHandleRequest", 1).
     *      2.2) Invoke equals with an invalid client id and transaction id.
     *  Result:
     *      2.1) Equals returns true.
     *      2.2) Equals returns false.
     * </pre>
     * @throws Exception
     */
    public void testHandleRequest() throws Exception {
        String clientId = "testHandleRequest";
        int tId = 1;

        Mock jmockQueue = mock(IXMPResponseQueue.class);
        jmockQueue.stubs()
                .method("addResponse")
                .with(isA(XmpAnswer.class))
                .isVoid();

        ServiceRequest request = new ServiceRequest();
        request.setServiceId(IServiceName.OUT_DIAL_NOTIFICATION);
        request.setValidityTime(30);

        setupMockedApplication();

        // 1
        serviceHandler.handleRequest((ISession)jmockSession.proxy(),
                request,
                clientId,
                tId,
                (IXMPResponseQueue)jmockQueue.proxy(),
                (IApplicationExecution)jmockApplicationExecution.proxy());

        // 2.1
        assertTrue("ServiceHandler should be equal to " + clientId + ", " + tId,
                serviceHandler.equals(clientId, tId));
        // 2.2
        assertFalse("ServiceHandler should not be equal to invalidClientId, -1",
                serviceHandler.equals("invalidClientId", -1));
    }

    /**
     * Test method
     * {@link XMPServiceHandler#sendResponse(com.mobeon.masp.servicerequestmanager.ServiceResponse)}
     *
     * <pre>
     * <p/>
     * 1. Test error handling
     *  Condition:
     *      A service handler is created and no request is sent to it, no
     *      application is started.
     *  Action:
     *      Invoke sendResponse.
     *  Result:
     *      A ServiceRequestManagerException is thrown.
     * <p/>
     * 2. Test response for a processed request.
     *  Condition:
     *      A service handler is created. A mocked response queue is set up.
     *  Action:
     *      Invoke handleRequest with a ServiceRequest.
     *      Invoke sendResponse with a ServiceResponse.
     *  Result:
     *      The response is added to the mocked response queue.
     * </pre>
     *
     * @throws Exception
     */
    public void testSendResponse() throws Exception {
        ServiceResponse response = new ServiceResponse();
        response.setStatusCode(ServiceResponse.STATUSCODE_SUCCESS_COMPLETE);
        response.setStatusText(ServiceResponse.STATUSTEXT_SUCCESS_COMPLETE);

        String clientId = "testSendResponse";
        int tId = 1;
        ServiceRequest request = new ServiceRequest();
        request.setServiceId(IServiceName.OUT_DIAL_NOTIFICATION);
        request.setValidityTime(30);

        // 1
        try {
            serviceHandler.sendResponse(response);
            fail("handleResult is not called, should throw exception.");
        } catch (ServiceRequestManagerException e) {
            //ok
        }

        // 2
        setupMockedApplication();

        Mock jmockQueue = mock(IXMPResponseQueue.class);
        jmockQueue.expects(once())
                .method("addResponse")
                .with(isA(XmpAnswer.class))
                .isVoid();
        jmockQueue.stubs()
                .method("getClientId")
                .withNoArguments()
                .will(returnValue(clientId));

//        String sessionId = serviceHandler.handleRequest(
//                request,
//                clientId,
//                tId,
//                (IXMPResponseQueue)jmockQueue.proxy(),
//                (IApplicationExecution)jmockApplicationExecution.proxy());
//        sessionMdcItems.setLogData("clientid", clientId);
//        sessionMdcItems.setLogData("transactionid", tId);
//        Id<ISession> sessionId = sessionIdGenerator.generateId();
//        LOGGER.registerSessionInfo("session", sessionId.toString());
//        sessionMdcItems.registerMdcItemsInLogger();

        serviceHandler.handleRequest((ISession)jmockSession.proxy(),
                request,
                clientId,
                tId,
                (IXMPResponseQueue)jmockQueue.proxy(),
                (IApplicationExecution)jmockApplicationExecution.proxy());

        try {
            serviceHandler.sendResponse(response);
        } catch (ServiceRequestManagerException e) {
            fail("ServiceRequestManagerException thrown in sendResponse, " + e.getMessage());
        }

    }

    /**
     * Test method
     * {@link com.mobeon.masp.servicerequestmanager.xmp.XMPServiceHandler#cancelRequest()}
     * <pre>
     * <p/>
     * 1. Test error handling
     *  Condition:
     *      A service handler is created. No request is sent to it, so no
     *      application is started.
     *  Action:
     *      Invoke cancelRequest.
     *  Result:
     *      A ServiceRequestManagerException is thrown.
     * <p/>
     * 2. Test cancel of started application
     *  Condition:
     *      A service handler is created. A mocked application and response
     *      queue are set up.
     *  Action:
     *      Invoke handleRequest so the application starts.
     *      Invoke cancelRequest.
     *  Result:
     *      The application is terminated.
     * </pre>
     *
     * @throws Exception
     */
    public void testCancelRequest() throws Exception {
        String clientId = "testSendResponse";
        int tId = 1;
        ServiceRequest request = new ServiceRequest();
        request.setServiceId(IServiceName.OUT_DIAL_NOTIFICATION);
        request.setValidityTime(30);

        // 1
        try {
            serviceHandler.cancelRequest();
            fail("cancelRequest should throw exception if no application is started.");
        } catch (ServiceRequestManagerException e) {
            //ok
        }

        // 2
        setupMockedApplication();
        Mock jmockQueue = mock(IXMPResponseQueue.class);

//        String sessionId = serviceHandler.handleRequest(
//                request,
//                clientId,
//                tId,
//                (IXMPResponseQueue)jmockQueue.proxy(),
//                (IApplicationExecution)jmockApplicationExecution.proxy());
//        sessionMdcItems.setLogData("clientid", clientId);
//        sessionMdcItems.setLogData("transactionid", tId);
//        Id<ISession> sessionId = sessionIdGenerator.generateId();
//        LOGGER.registerSessionInfo("session", sessionId.toString());
//        sessionMdcItems.registerMdcItemsInLogger();

        serviceHandler.handleRequest((ISession)jmockSession.proxy(),
                request,
                clientId,
                tId,
                (IXMPResponseQueue)jmockQueue.proxy(),
                (IApplicationExecution)jmockApplicationExecution.proxy());

        jmockApplicationExecution.expects(once())
                .method("terminate")
                .withNoArguments()
                .isVoid();
        try {
            serviceHandler.cancelRequest();
        } catch (ServiceRequestManagerException e) {
            fail("cancelRequest should not throw exception.");
        }
    }

    private void setupMockedApplication() throws Exception {
        jmockSession = mock(ISession.class);
        jmockSession.stubs()
                .method("setData")
                .with(isA(String.class), isA(Object.class))
                .isVoid();
        jmockSession.stubs()
                .method("getId")
                .withNoArguments()
                .will(returnValue("session_1"));

        jmockApplicationExecution = mock(IApplicationExecution.class);
        jmockApplicationExecution.expects(once())
                .method("setSession");
        jmockApplicationExecution.expects(once())
                .method("start");        
    }
}
