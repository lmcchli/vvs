/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.server.XmpResponseQueue;
import com.mobeon.common.xmp.server.IXmpAnswer;
import com.mobeon.common.xmp.server.XmpTransaction;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.xmp.*;
import com.mobeon.masp.operateandmaintainmanager.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.MulticastDispatcher;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.events.ApplicationEnded;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.xerces.dom.DocumentImpl;

import java.util.Properties;

/**
 * Test ServiceRequestManager. Uses the both a mocked version and a stubbed version of the XMP interface.
 *
 * @author ermmaha
 */
public class ServiceRequestManagerTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";

    private static final String CLIENT_ID = "mas@test.com";
    protected Mock jmockServiceLocator;
    protected Mock jmockConfiguration;
    protected Mock jmockXMP;
    protected Mock jmockSupervision;
    protected Mock jmockStatistics;
    protected Mock jmockServiceHandlerFactory;
    protected Mock jmockServiceHandler;
    protected Mock jmockApplicationManagement;
    protected Mock jmockSessionFactory;
    protected XMPClientStub stubXMP;

    /**
     * The ServiceRequestManager tested. Initialized from bean config.
     */
    private IServiceRequestManager serviceRequestManager;
    private IServiceRequestManagerController serviceEnablerOperate;
    private IEventDispatcher eventDispatcher;
    private ApplicationExecutionFactoryStub applicationExecutionFactoryStub;
    private ISessionFactory sessionFactory;

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    public ServiceRequestManagerTest(String name) throws Exception {
        super(name);

        setupMockedServiceLocator();
        setupMockedConfiguration();
        setupMockedXMPClient();
        jmockSupervision = mock(Supervision.class);
        stubXMP = new XMPClientStub();
        sessionFactory = new SessionFactoryStub();
    }

    private void setupServiceRequestManager(
            boolean mockClient,
            boolean mockApplicationManagement,
            boolean mockServiceHandlerFactory) throws Exception {

        IXMPClient client;
        IApplicationManagment appManagement;
        IXMPServiceHandlerFactory shFactory;

        if (mockClient) {
            client = (IXMPClient) jmockXMP.proxy();
        } else {
            client = stubXMP;
        }
        if (mockApplicationManagement) {
            setupMockedApplication();
            appManagement = (IApplicationManagment) jmockApplicationManagement.proxy();
        } else {
            appManagement = new ApplicationManagementStub();
        }
        if (mockServiceHandlerFactory) {
            setupMockedServiceHandler();
            shFactory = (IXMPServiceHandlerFactory) jmockServiceHandlerFactory.proxy();
        } else {
            shFactory = new XMPServiceHandlerFactory();
        }

        assertConfiguration(false);
        serviceRequestManager = new ServiceRequestManager(
                (IConfiguration)jmockConfiguration.proxy(),
                eventDispatcher,
                (ILocateService)jmockServiceLocator.proxy(),
                appManagement,
                shFactory,
                client,
                (Supervision)jmockSupervision.proxy(),
                sessionFactory);

        applicationExecutionFactoryStub.setServiceRequestManager(serviceRequestManager);
        serviceEnablerOperate = ServiceRequestManagerController.getInstance();
        serviceEnablerOperate.updateThreshold(0, 0, 50);
        serviceEnablerOperate.open();
    }

    protected void setUp() throws Exception {
        super.setUp();

        eventDispatcher = new MulticastDispatcher();

        applicationExecutionFactoryStub = new ApplicationExecutionFactoryStub();
        applicationExecutionFactoryStub.setEventDispatcher(eventDispatcher);
    }

    protected void tearDown() throws Exception{
        serviceRequestManager = null;
        eventDispatcher.removeAllEventReceivers();
        eventDispatcher = null;
        serviceEnablerOperate = null;
        applicationExecutionFactoryStub = null;
        super.tearDown();
    }


    // ===== Test cases =====

    /**
     * Test method {@link ServiceRequestManager#handleRequest}
     *
     * <pre>
     * <p/>
     * 1. Test locked state.
     *  Condition:
     *      A ServiceRequestManager is created.
     *      A stubbed application is created. I will wait at least 4 secs
     *      before a response is sent back.
     *      A response queue is created.
     *      A XMP service request is created.
     *  Action:
     *      Set admin state to locked with the serviceEnablerOperate
     *      Invoke handleRequest with a service request.
     *  Result:
     *      The reponse 421 is added to the response queue.
     * <p/>
     * 2. Test unlocked, successful request.
     *  Condition:
     *      As in 1.
     *  Action:
     *      Set admin state to unlocked with the serviceEnablerOperate
     *      Invoke handleRequest with a service request.
     *  Result:
     *      The result 200 is added to the response queue.
     * <p/>
     * 3. Test timeout.
     *  Condition:
     *      As in 1.
     *  Action:
     *      Set admin state to unlocked with the serviceEnablerOperate
     *      Invoke handleRequest with a service request.
     *  Result:
     *      The result 200 is added to the response queue.
     * </pre>
     *
     * @throws Exception
     */
    public void testHandleRequest() throws Exception {
        setupServiceRequestManager(false, false, false);
        String clientId = "TestClient";

        // 1 Test locked
        int transactionId = 1;
        int validity = 30;

        XmpResponseQueue responseQueue = new XmpResponseQueue(clientId, null);
        responseQueue.setLogger(new XMPLogger());
        Document doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        serviceEnablerOperate.close(false);
        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);
        IXmpAnswer answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 421", 421, answer.getStatusCode());


        // 2 Test successful
        transactionId = 2;
        doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));
        serviceEnablerOperate.open();
        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);
        answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 200", 200, answer.getStatusCode());

        // 3 Test timeout
        transactionId = 3;
        validity = 2;
        doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));
        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);
        answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 408", 408, answer.getStatusCode());
    }

    /**
     * Test thread safety of {@link ServiceRequestManager#handleRequest}
     *
     * <pre>
     * <p/>
     * 1. Test multiple simultaneous requests.
     *  Condition:
     *      A service request manager is set up and is ready to handle requests.
     *      10 clients are created.
     *  Action:
     *      Each client sends 9 requests with transaction ids 1 to 10 to the
     *      SRM.
     *  Result:
     *      All of the requests are successful and the transaction id in the
     *      results matches the transaction id in the requests.
     * </pre>
     *
     * @throws Exception
     */
    public void testMultipleHandleRequest() throws Exception {
        setupServiceRequestManager(false, false, false);
        ServiceRequestManagerClient[] clients = new ServiceRequestManagerClient[10];

        for (int i=0; i<clients.length; ++i) {
            clients[i] = new ServiceRequestManagerClient(serviceRequestManager, "client" + i);
        }

        for (ServiceRequestManagerClient client : clients) {
            client.start();
        }

        for (ServiceRequestManagerClient client : clients) {
            client.join();
            assertTrue(client.getErrorMessage(), client.isSuccessful());
        }
    }

    /**
     * Client sending requests to the service request manager.
     * Sends 9 requests, each of them should be successful.
     */
    private class ServiceRequestManagerClient extends Thread {
        private IServiceRequestManager serviceRequestManager;
        private String clientId;
        private int validity = 30;
        private XmpResponseQueue responseQueue;
        private boolean successful = true;
        private String errorMessage;

        public ServiceRequestManagerClient(IServiceRequestManager serviceRequestManager,
                                           String clientId) {
            this.serviceRequestManager = serviceRequestManager;
            this.clientId = clientId;
            responseQueue = new XmpResponseQueue(clientId, null);
            responseQueue.setLogger(new XMPLogger());
        }

        /**
         * Get the result of the requests.
         *
         * @return <code>true</code> if all requests are successful,
         * <code>false</code> otherwise.
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * If <code>isSuccessful()</code> returns false the error message
         * can be fetched with this method.
         *
         * @return The error message from a non-successful request.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        public void run() {
            for (int i=1; i<10; ++i) {
                try {
                    Document doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, i);
                    responseQueue.addTransaction(new XmpTransaction(validity, i, serviceRequestManager));
                    serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                            i, validity, doc, null);
                    IXmpAnswer answer = responseQueue.getAnswer();
                    assertNotNull("No answer in queue.", answer);
                    assertEquals("TransactionId should be " + Integer.valueOf(i),
                            Integer.valueOf(i), answer.getTransactionId());
                    assertEquals("StatusCode should be 200", 200, answer.getStatusCode());
                } catch (Throwable t) {
                    successful = false;
                    errorMessage = t.getMessage();
                    break;
                }
            }
        }
    }

    public void testDiagnoseServiceRequest() throws Exception {
        setupServiceRequestManager(false, false, false);
        String clientId = "diagnoseservicetest@localhost";

        int transactionId = 1;
        int validity = 30;

        XmpResponseQueue responseQueue = new XmpResponseQueue(clientId, null);
        responseQueue.setLogger(new XMPLogger());
        Document doc = createDocument(clientId, IServiceName.DIAGNOSE_SERVICE, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        serviceRequestManager.handleRequest(responseQueue, IServiceName.DIAGNOSE_SERVICE, clientId,
                transactionId, validity, doc, null);
        IXmpAnswer answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 200", 200, answer.getStatusCode());


        //Test when locked.
        transactionId = 2;
        doc = createDocument(clientId, IServiceName.DIAGNOSE_SERVICE, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        serviceEnablerOperate.close(false);
        serviceRequestManager.handleRequest(responseQueue, IServiceName.DIAGNOSE_SERVICE, clientId,
                transactionId, validity, doc, null);
        answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 200", 200, answer.getStatusCode());

    }

    /**
     * Test the sendRequest method. Uses the stubbed XMP
     *
     * <pre>
     * <p/>
     * 1. Test with component register
     *  Condition:
     *      A SRM with mocked service locator, configuration and a stub XMP
     *      client is created.
     *  Action:
     *      Invoke sendRequest with a service request 5 times.
     *  Result:
     *      5 successful responses are received.
     * <p/>
     * 2. Test with host only specified
     * Condition:
     *      A SRM with mocked service locator, configuration and a stub XMP
     *      client is created.
     *  Action:
     *      Invoke sendRequest with a service request 5 times, with the host
     *      "brage".
     *  Result:
     *      5 successful responses are received.
     * <p/>
     * 3. Test with host and port specified
     * Condition:
     *      A SRM with mocked service locator, configuration and a stub XMP
     *      client is created.
     *  Action:
     *      Invoke sendRequest with a service request 5 times, with the host
     *      "brage" and port "8080".
     *  Result:
     *      5 successful responses are received.
     * </pre>
     * @throws Exception
     */
    public void testSendRequest() throws Exception {
        // conditions...
        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        serviceRequest.setParameter("language", "en");
        serviceRequest.setParameter("codec", "G.711/u-law");
        serviceRequest.setParameter("charset", "us-acsii");

        // 1, preferred host
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest);
            assertEquals(200, response.getStatusCode());
            assertEquals("Success", response.getStatusText());
            assertNull(response.getParameter("noparam"));
            assertEquals(CLIENT_ID, response.getClientId());
            assertTrue("TransactionId should be greater than 0",
                    response.getTransactionId() > 0);
        }

        // 2, specified host
        String host = "brage";
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest, host);
            assertNotNull("No response.", response);
            assertEquals(200, response.getStatusCode());
            assertEquals("Success", response.getStatusText());
            assertNull(response.getParameter("noparam"));
            assertEquals(CLIENT_ID, response.getClientId());
            assertTrue("TransactionId should be greater than 0",
                    response.getTransactionId() > 0);
        }

        // 3, specified host and port
        int port = 8080;
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest, host, port);
            assertNotNull("No response.", response);
            assertEquals(200, response.getStatusCode());
            assertEquals("Success", response.getStatusText());
            assertNull(response.getParameter("noparam"));
            assertEquals(CLIENT_ID, response.getClientId());
            assertTrue("TransactionId should be greater than 0",
                    response.getTransactionId() > 0);
        }
    }

    /**
     * Test method sendRequestAsync
     *
     * <pre>
     * <p/>
     * 1. Test sendRequestAsync with preferred host.
     *  Condition:
     *      A SRM with mocked service locator and configuration and a stubbed
     *      XMP client is set up.
     *  Action:
     *      Invoke sendRequestAsync with a service request.
     *      Check with isTransactionCompleted until it is done.
     *      Invoke receiveResponse.
     *  Result:
     *      A transaction id > 0 is returned from sendRequestAsync.
     *      isTransactionCompleted returns true after a while.
     *      A "200 sucessful" response is returned from receiveResponse.
     * <p/>
     * 2. Test sendRequestAsync with a host
     *  Condition:
     *     A SRM with mocked service locator and configuration and a stubbed
     *      XMP client is set up.
     *  Action:
     *      Invoke sendRequestAsync with a service request and the host "brage".
     *      Check with isTransactionCompleted until it is done.
     *      Invoke receiveResponse.
     *  Result:
     *      A transaction id > 0 is returned from sendRequestAsync.
     *      isTransactionCompleted returns true after a while.
     *      A "200 sucessful" response is returned from receiveResponse.
     * <p/>
     * 3. Test sendRequestAsync with a host and port.
     *  Condition:
     *     A SRM with mocked service locator and configuration and a stubbed
     *      XMP client is set up.
     *  Action:
     *      Invoke sendRequestAsync with a service request and the host "brage".
     *      Check with isTransactionCompleted until it is done.
     *      Invoke receiveResponse.
     *  Result:
     *      A transaction id > 0 is returned from sendRequestAsync.
     *      isTransactionCompleted returns true after a while.
     *      A "200 sucessful" response is returned from receiveResponse.
     * </pre>
     *
     * @throws Exception
     */
    public void testSendRequestAsync() throws Exception {
        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        serviceRequest.setParameter("language", "en");
        serviceRequest.setParameter("codec", "G.711/u-law");
        serviceRequest.setParameter("charset", "us-acsii");

        ServiceResponse response;
        int transactionId;

        // 1, preferred host
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest);
        assertTrue("TransactionId should be greater than 0", transactionId > 0);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getStatusText());
        assertNull(response.getParameter("noparam"));

        // 2, specified host
        String host = "brage";
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest, host);
        assertTrue("TransactionId should be greater than 0", transactionId > 0);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getStatusText());
        assertNull(response.getParameter("noparam"));

        // 3, specified host and port
        int port = 8080;
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest, host, port);
        assertTrue("TransactionId should be greater than 0", transactionId > 0);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getStatusText());
        assertNull(response.getParameter("noparam"));
    }

    /**
     * Test retry at failure: communications error in sendRequest and
     * sendRequestAsync.
     *
     * <pre>
     * <p/>
     * 1. Test sendRequest with timeout.
     *  Condition:
     *      A SRM is set up with mocked service locator and config. It also
     *      has a mocked XMP client that will always return comm error on
     *      sendRequest.
     *  Action:
     *      Invoke sendRequest on SRM with a service request.
     *  Result:
     *      sendRequest on the client is called 3 times and then the SRM
     *      returns a 421 response.
     * <p/>
     * 2. Test sendRequestAsync with timeout.
     *  Condition:
     *      A SRM is set up with mocked service locator and config. It also
     *      has a mocked XMP client that will always return comm error on
     *      sendRequest.
     *  Action:
     *      Invoke sendRequestAsync on SRM with a service request.
     *  Result:
     *      sendRequest on the client is called 3 times and then the SRM
     *      returns the transaction id.
     *      A response 421 should be created.
     * </pre>
     *
     * @throws Exception
     */
    public void testSendRequestCommErr() throws Exception {
        // 1
        jmockXMP.expects(once()).method("nextTransId")
                .withNoArguments()
                .will(returnValue(1));

        setupServiceRequestManager(true, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        serviceRequest.setParameter("language", "en");
        serviceRequest.setParameter("codec", "G.711/u-law");
        serviceRequest.setParameter("charset", "us-acsii");

        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(1),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));
        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(1),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));
        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(1),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));

        ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest);
        assertNotNull("No response", response);
        assertEquals(421, response.getStatusCode());
        assertEquals("Service not available", response.getStatusText());
        assertNull(response.getParameter("noparam"));

        // 2
        jmockXMP.expects(once()).method("nextTransId")
                .withNoArguments()
                .will(returnValue(2));

        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(2),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));
        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(2),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));
        jmockXMP.expects(once()).method("sendRequest")
                .with(new Constraint[] {
                        eq(2),
                        isA(String.class),
                        isA(String.class),
                        isA(XmpResultHandler.class),
                        isA(IServiceInstance.class)})
                .will(returnValue(false));

        int transactionId = serviceRequestManager.sendRequestAsync(serviceRequest);
        assertTrue("Transaction id should be greater than 0", transactionId > 0);
        assertTrue("isTransactionCompleted should return true",
                serviceRequestManager.isTransactionCompleted(transactionId));
        response = serviceRequestManager.receiveResponse(transactionId);
        assertNotNull("Response is null", response);
        assertEquals(421, response.getStatusCode());
        assertEquals("Service not available", response.getStatusText());
        assertNull(response.getParameter("noparam"));
    }

    /**
     * Test retry at failure: Status code 421, 502 and 450 on sendRequest.
     *
     * <pre>
     * <p/>
     * 1. Test status code 421.
     *  Condition:
     *      A SRM is set up with a mocked service locator and config. It uses
     *      a stubbed XMP client that will return status code 421 for requests
     *      with service id "TestService421".
     *  Action:
     *      Invoke sendRequest on SRM with a service request on service
     *      "TestService421".
     *  Result:
     *      reportServiceError is called 3 times on the service locator.
     *      A 421 service response is returned from SRM.
     * <p/>
     * 2. Test status code 502.
     *  Condition:
     *      A SRM is set up with a mocked service locator and config. It uses
     *      a stubbed XMP client that will return status code 502 for requests
     *      with service id "TestService502".
     *  Action:
     *      Invoke sendRequest on SRM with a service request on service
     *      "TestService502".
     *  Result:
     *      reportServiceError is called 3 times on the service locator.
     *      A 502 service response is returned from SRM.
     * <p/>
     * 3. Test status code 450.
     *  Condition:
     *      A SRM is set up with a mocked service locator and config. It uses
     *      a stubbed XMP client that will return status code 450 for requests
     *      with service id "TestService450".
     *  Action:
     *      Invoke sendRequest on SRM with a service request on service
     *      "TestService450".
     *  Result:
     *      getAnotherService is called 3 times on the service locator.
     *      A 450 service response is returned from SRM.
     * </pre>
     *
     * @throws Exception
     */
    public void testSendRequestErrorCodes() throws Exception {
        Mock otherServiceInstance = mock(IServiceInstance.class);
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("buell.mvas.lab.mobeon.com"));
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("8080"));
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.LOGICALZONE)).will(returnValue("UNDEFINED"));

        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId("TestService421");
        serviceRequest.setValidityTime(30);
        serviceRequest.setParameter("mailbox-id", "ermmaha@mobeon.com");
        serviceRequest.setParameter("called-number", "161074");
        serviceRequest.setParameter("called-type-of-number", 3);

        // Test 421
        // expect reportServiceError 3 times
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest);
        assertNotNull("No response", response);
        assertEquals(421, response.getStatusCode());
        assertEquals("Service not available", response.getStatusText());
        assertNull(response.getParameter("noparam"));

        // Test 502
        serviceRequest.setServiceId("TestService502");
        // expect reportServiceError 3 times
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        jmockServiceLocator.expects(once()).method("reportServiceError")
                .withAnyArguments()
                .isVoid();
        response = serviceRequestManager.sendRequest(serviceRequest);
        assertNotNull("No response", response);
        assertEquals(502, response.getStatusCode());
        assertEquals("Resource limit exceeded", response.getStatusText());
        assertNull(response.getParameter("noparam"));

        // Test 450
        serviceRequest.setServiceId("TestService450");
        // expect getAnotherService 2 times, (the first time locateService is called).
        jmockServiceLocator.expects(once())
                .method("getAnotherService")
                .withAnyArguments()
                .will(returnValue(otherServiceInstance.proxy()));
        jmockServiceLocator.expects(once())
                .method("getAnotherService")
                .withAnyArguments()
                .will(returnValue(otherServiceInstance.proxy()));
        response = serviceRequestManager.sendRequest(serviceRequest);
        assertNotNull("No response", response);
        assertEquals(450, response.getStatusCode());
        assertEquals("Request failed, try again", response.getStatusText());
        assertNull(response.getParameter("noparam"));
    }

    /**
     * Test timeout with success.
     *
     * <pre>
     * <p/>
     * 1. Test sendRequest with request timeout.
     *  Condition:
     *      A SRM with mocked service locator and config is set up. It uses a
     *      stubbed XMP client that will timeout for a service request to
     *      service id "TimeoutService" on the host "brage".
     *  Action:
     *      Invoke sendRequest with a "TimeoutService" request.
     *  Result:
     *      "brage" will timeout.
     *      getAnotherService is called once on the service locator.
     *      A 200 Success response from the other host is returned.
     * </pre>
     *
     * @throws Exception
     */
    public void testTimeOut() throws Exception {
        Mock otherServiceInstance = mock(IServiceInstance.class);
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("buell.mvas.lab.mobeon.com"));
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("8080"));
        otherServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.LOGICALZONE)).will(returnValue("UNDEFINED"));

        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId("TimeoutService");
        serviceRequest.setValidityTime(30);
        serviceRequest.setParameter("mailbox-id", "ermmaha@mobeon.com");
        serviceRequest.setParameter("called-number", "161074");
        serviceRequest.setParameter("called-type-of-number", 3);

        // 1.
        // The first request should timeout, the retry will succeed.
        // expect getAnotherService once
        jmockServiceLocator.expects(once()).method("getAnotherService")
                .with(isA(IServiceInstance.class))
                .will(returnValue(otherServiceInstance.proxy()));

        ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest);
        assertNotNull("No response", response);
        assertEquals(200, response.getStatusCode());
        assertNull(response.getParameter("noparam"));
    }

    /**
     * Verify that no response is received when sending a request
     * with response required = false.
     * @throws Exception
     */
    public void testSendRequestNoResponseRequired() throws Exception {
        // conditions...
        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        serviceRequest.setParameter("language", "en");
        serviceRequest.setParameter("codec", "G.711/u-law");
        serviceRequest.setParameter("charset", "us-acsii");
        serviceRequest.setResponseRequired(false);

        // 1, preferred host
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest);
            assertNull(response);
        }

        // 2, specified host
        String host = "brage";
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest, host);
            assertNull(response);
        }

        // 3, specified host and port
        int port = 8080;
        for (int i = 0; i < 5; i++) {
            ServiceResponse response = serviceRequestManager.sendRequest(serviceRequest, host, port);
            assertNull(response);
        }
    }

    public void testSendRequestAsyncNoResponseRequired() throws Exception {
        setupServiceRequestManager(false, false, false);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        serviceRequest.setParameter("language", "en");
        serviceRequest.setParameter("codec", "G.711/u-law");
        serviceRequest.setParameter("charset", "us-acsii");
        serviceRequest.setResponseRequired(false);

        ServiceResponse response;
        int transactionId;

        // 1, preferred host
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest);
        assertEquals("TransactionId should be -1", -1, transactionId);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertNull(response);

        // 2, specified host
        String host = "brage";
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest, host);
        assertEquals("TransactionId should be -1", -1, transactionId);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertNull(response);

        // 3, specified host and port
        int port = 8080;
        transactionId = serviceRequestManager.sendRequestAsync(serviceRequest, host, port);
        assertEquals("TransactionId should be -1", -1, transactionId);

        while (!serviceRequestManager.isTransactionCompleted(transactionId)) {
            // do nothing
            synchronized(this) {
                wait(200);
            }
        }

        response = serviceRequestManager.receiveResponse(transactionId);
        assertNull(response);
    }

    public void testEvents() throws Exception {
        setupServiceRequestManager(false, true, false);

        int nrOfReceivers = eventDispatcher.getNumReceivers();
        assertEquals("Number of event receivers should be 1", 1, nrOfReceivers);

        // Configuration Changed
        assertConfiguration(true);
        Event event = new ConfigurationChanged((IConfiguration)jmockConfiguration.proxy());
        eventDispatcher.fireGlobalEvent(event);

        // Start application
        String clientId = "testEvents";
        int transactionId = 1;
        int validity = 30;

        XmpResponseQueue responseQueue = new XmpResponseQueue(clientId, null);
        responseQueue.setLogger(new XMPLogger());
        Document doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);

        // Application Ended
        event = new ApplicationEnded("session_1");
        eventDispatcher.fireEvent(event);

        //Expect a 421 answer.
        IXmpAnswer answer = responseQueue.getAnswer();
        assertNotNull("answer is null", answer);
        assertEquals(421, answer.getStatusCode());
    }

    /**
     * Test load regulation
     *
     * <pre>
     * <p/>
     * 1. Initialize
     *  Condition:
     *      A SRM with a ServiceEnablerOperate (SEO) is set up.
     *      A mocked ServiceEnablerInfo (SEI) is set up.
     *  Action:
     *      Initialize the SEO with the mocked SEI.
     *  Result:
     *      setMaxConnections in the SEI is called once with the default
     *      value 50.
     * <p/>
     * 2. Test max sessions reached.
     *  Condition:
     *      As in 1.
     *  Action:
     *      Invoke updateThreshold on SEO with 0.
     *      Invoke handleRequest on SRM with a service request.
     *  Result:
     *      setMaxConnection in the SEO is called once with the value 0.
     *      A 502 response is returned.
     * <p/>
     * 3. Test successful request.
     *  Condition:
     *      As in 1.
     *  Action:
     *      Invoke updateThreshold on SEO with 3.
     *      Invoke handleRequest on SRM with a service request.
     *  Result:
     *      setMaxConnection in the SEO is called once with the value 3.
     *      incrementCurrentConnections on the SEI is called once.
     *      decrementCurrentConnections on the SEI is called once.
     *      A 200 response is returned.
     * <p/>     
     * </pre>
     * @throws Exception
     */
    public void testLoadRegulation() throws Exception {
        setupServiceRequestManager(false, false, false);
        setupMockedStatistics();
        String clientId = "TestClient";
        int transactionId = 1;
        int validity = 30;
        XmpResponseQueue responseQueue;
        IXmpAnswer answer;
        Document doc;

        // 1
        // Initialize, default threshold is 50
        jmockStatistics.expects(once())
                .method("setMaxConnections")
                .with(eq(50));
        serviceEnablerOperate.setServiceEnablerInfo((ServiceEnablerInfo)jmockStatistics.proxy());

        // 2
        // Change threshold to 0
        jmockStatistics.expects(once())
                .method("setMaxConnections")
                .with(eq(0));
        serviceEnablerOperate.updateThreshold(0, 0, 0);

        // Test send one request, expect 502
        responseQueue = new XmpResponseQueue(clientId, null);
        responseQueue.setLogger(new XMPLogger());
        doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        jmockStatistics.expects(once())
                .method("incrementNumberOfConnections")
                .with(eq(CallType.SERVICE_REQUEST), eq(CallResult.FAILED), eq(CallDirection.INBOUND));

        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);
        answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 502", 502, answer.getStatusCode());

        // 3
        // Change threshold to 3
        jmockStatistics.expects(once())
                .method("setMaxConnections")
                .with(eq(3));
        serviceEnablerOperate.updateThreshold(0, 0, 3);

        // Test send one request, expect 200
        jmockStatistics.expects(once())
                .method("incrementCurrentConnections")
                .with(eq(CallType.SERVICE_REQUEST), eq(CallDirection.INBOUND));
        jmockStatistics.expects(once())
                .method("incrementNumberOfConnections")
                .with(eq(CallType.SERVICE_REQUEST), eq(CallResult.CONNECTED), eq(CallDirection.INBOUND));
        jmockStatistics.expects(once())
                .method("decrementCurrentConnections")
                .with(eq(CallType.SERVICE_REQUEST), eq(CallDirection.INBOUND));
        transactionId = 2;
        responseQueue = new XmpResponseQueue(clientId, null);
        responseQueue.setLogger(new XMPLogger());
        doc = createDocument(clientId, IServiceName.OUT_DIAL_NOTIFICATION, validity, transactionId);
        responseQueue.addTransaction(new XmpTransaction(validity, transactionId, serviceRequestManager));

        serviceRequestManager.handleRequest(responseQueue, IServiceName.OUT_DIAL_NOTIFICATION, clientId,
                transactionId, validity, doc, null);
        answer = responseQueue.getAnswer();
        assertNotNull("No answer in queue", answer);
        assertEquals("StatusCode should be 200", 200, answer.getStatusCode());
    }

    // ===== End of test cases =====


    private void setupMockedStatistics() throws Exception {
        jmockStatistics = mock(ServiceEnablerInfo.class);

        jmockStatistics.stubs()
                .method("setProtocol")
                .with(isA(String.class))
                .isVoid();
    }

    private void setupMockedConfiguration() throws Exception {
        jmockConfiguration = mock(IConfiguration.class);
    }

    private void assertConfiguration(boolean reload) throws Exception {
        Mock jmockConfigSrm = mock(IGroup.class);
        jmockConfigSrm.expects(once()).method("getInteger")
                .with(eq("requesttimeout"))
                .will(returnValue(30000));
        jmockConfigSrm.expects(once()).method("getInteger")
                .with(eq("requestretries"))
                .will(returnValue(3));
        if (!reload) {
            jmockConfigSrm.expects(once()).method("getString")
                    .with(eq("clientid"))
                    .will(returnValue(CLIENT_ID));
        }

        jmockConfiguration.expects(once()).method("getGroup")
                .with(eq("servicerequestmanager")).will(returnValue(jmockConfigSrm.proxy()));
    }

    private void setupMockedServiceLocator() {
        Mock serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("brage"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("8899"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.LOGICALZONE)).will(returnValue("UNDEFINED"));

        jmockServiceLocator = mock(ILocateService.class);
        jmockServiceLocator.stubs()
                .method("locateService")
                .withAnyArguments()
                .will(returnValue(serviceInstance.proxy()));

        jmockServiceLocator.stubs()
                .method("reportServiceError")
                .withAnyArguments()
                .isVoid();
    }

    private void setupMockedXMPClient() {
        jmockXMP = mock(IXMPClient.class);
        jmockXMP.stubs()
                .method("setClientId")
                .with(isA(String.class))
                .isVoid();
        jmockXMP.stubs()
                .method("setLogger")
                .with(isA(XMPLogger.class))
                .isVoid();
    }

    private void setupMockedServiceHandler() {
        jmockServiceHandler = mock(IXMPServiceHandler.class);
        jmockServiceHandler.stubs().method("handleRequest")
                .with(new Constraint[] {
                        isA(ISession.class),
                        isA(ServiceRequest.class),
                        isA(String.class),
                        eq(1),
                        isA(IXMPResponseQueue.class),
                        isA(IApplicationExecution.class)});

        jmockServiceHandlerFactory = mock(IXMPServiceHandlerFactory.class);
        jmockServiceHandlerFactory.stubs().method("create")
                .withNoArguments()
                .will(returnValue(jmockServiceHandler.proxy()));
    }

    private void setupMockedApplication() {
        Mock jmockApplicationExecution = mock(IApplicationExecution.class);
        jmockApplicationExecution.stubs()
                .method("start")
                .withNoArguments()
                .isVoid();
        jmockApplicationExecution.expects(once())
                .method("setSession")
                .isVoid();

        jmockApplicationManagement = mock(IApplicationManagment.class);
        jmockApplicationManagement.stubs()
                .method("load")
                .with(eq("OutdialNotification"))
                .will(returnValue(jmockApplicationExecution.proxy()));
    }

    public static Test suite() {
        return new TestSuite(ServiceRequestManagerTest.class);
    }


    private Document createDocument(String clientId,
                                    String serviceId,
                                    int validity,
                                    int transactionId) {
        Document doc = new DocumentImpl();
        Element root = doc.createElement("xmp-message");
        root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
        Element service = doc.createElement("xmp-service-request");
        service.setAttribute("service-id", serviceId);
        service.setAttribute("client-id", clientId);
        service.setAttribute("transaction-id", String.valueOf(transactionId));

        addElement(doc, service, "validity", String.valueOf(validity));
        addElement(doc, service, "message-report", "true");
        addParameterElement(doc, service, "mailbox-id", "9905");
        addParameterElement(doc, service, "number", "9905");

        root.appendChild( service );
        doc.appendChild( root );

        return doc;
    }

    private void addParameterElement(Document doc,
                                     Element service,
                                     String attributeName,
                                     String textValue){
        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("name",  attributeName);
        parameter.appendChild( doc.createTextNode(textValue) );
        service.appendChild( parameter );
    }

    private void addElement(Document doc,
                            Element service,
                            String attributeName,
                            String textValue){
        Element parameter = doc.createElement(attributeName);
        parameter.appendChild( doc.createTextNode(textValue) );
        service.appendChild( parameter );
    }
}

class SessionFactoryStub implements ISessionFactory {
    private int sessionCounter;

    public SessionFactoryStub() {
        this.sessionCounter = 0;
    }

    public ISession create() {
        return new SessionStub(++sessionCounter);
    }

    private class SessionStub implements ISession {

        private int sessionId;
        private Object data;

        public SessionStub(int i) {
            sessionId = i;
        }

        public String getId() {
            return "session_" + sessionId;
        }

        public String getUnprefixedId() {
            return "" + sessionId;
        }

        public void setId(Id<ISession> id) {
        }

        public void setMdcItems(SessionMdcItems sessionMdcItems) {
        }

        public void dispose() {
        }

        public void setData(String string, Object object) {
            this.data = object;
        }

        public Object getData(String string) {
            return this.data;
        }

        public void setSessionLogData(String string, Object object) {
        }

        public void registerSessionInLogger() {
        }

        public Id<ISession> getIdentity() {
            return null;
        }
    }
}

class XMPClientStub implements IXMPClient {

    private final com.mobeon.common.logging.ILogger log = ILoggerFactory.getILogger(getClass());
    //"response" related parameters
    int transId = 1;
    int timeOut = 5;
    boolean sendOk = true;
    int statusCode = 200;
    String statusText = "Success";
    Properties responseProperties = new Properties();

    //"expected" varaibles used for asserting
    String expectedClientId;

    public boolean sendRequest(int transId, String request, String service, final XmpResultHandler resultHandler) {

        if (!request.toLowerCase().contains("<message-report>false</message-report>")) {
            log.debug("sendRequest1.");
            final XmpResult xmpResult = new XmpResult(transId, statusCode, statusText, responseProperties);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(timeOut * 1000);
                        resultHandler.handleResult(xmpResult);
                    }
                    catch (InterruptedException ix) {
                    }
                }
            }).start();
        }
        return sendOk;
    }

    public boolean sendRequest(int transId, String request, String service, final XmpResultHandler resultHandler, IServiceInstance instance) {
    	String host = instance.getProperty(IServiceInstance.HOSTNAME);
        if (request.toLowerCase().indexOf("<message-report>false</message-report>") == -1) {
            log.debug("sendRequest2: " + request);
            final XmpResult xmpResult;

            if (service.equals("TestService421")) {
                xmpResult = new XmpResult(transId, 421, "Service not available", responseProperties);
            } else if (service.equals("TestService450")) {
                xmpResult = new XmpResult(transId, 450, "Request failed, try again", responseProperties);
            } else if (service.equals("TestService502")) {
                xmpResult = new XmpResult(transId, 502, "Resource limit exceeded", responseProperties);
            } else if (service.equals("TimeoutService") && "brage".equals(host)) {
                xmpResult = new XmpResult(transId, 408, "Request timeout", responseProperties);
            } else {
                xmpResult = new XmpResult(transId, statusCode, statusText, responseProperties);
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (xmpResult.getStatusCode() == 408) {
                            Thread.sleep(35000);
                        } else {
                            Thread.sleep(timeOut * 1000);
                        }
                        resultHandler.handleResult(xmpResult);
                    }
                    catch (InterruptedException ix) {
                    }
                }
            }).start();
        }

        return sendOk;
    }

    public int nextTransId() {
        return transId++;
    }

    public void setClientId(String id) {
        expectedClientId = id;
    }

    public String getClientId() {
        return expectedClientId;
    }

    public void setLogger(ILogger logger) {

    }
}
