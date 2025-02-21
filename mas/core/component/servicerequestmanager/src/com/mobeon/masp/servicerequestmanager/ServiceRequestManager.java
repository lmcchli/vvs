/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.server.XmpResponseQueue;
import com.mobeon.common.xmp.server.XmpAnswer;
import com.mobeon.common.xmp.server.XmpHandler;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.externalcomponentregister.ServiceInstanceImpl;
import com.mobeon.masp.servicerequestmanager.xmp.*;
import com.mobeon.masp.servicerequestmanager.events.ServiceClosed;
import com.mobeon.masp.servicerequestmanager.states.OpenedState;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.execution_engine.events.ApplicationEnded;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.operateandmaintainmanager.*;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.common.logging.HostedServiceLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// How the file is organized:
//
// Public methods for:
//  * Initialization
//  * IServiceRequestManager
//  * ServiceHandler
//  * ServiceEnabler
//  * IEventReceiver
//
// Private methods for:
//  * Initialization
//  * IServiceRequestManager
//  * IEventReceiver

/**
 * The ServiceRequestManager is the XMP entry to MAS. It receives XMP requests
 * from other components and starts an application that processes the request.
 * ServiceRequestManager is also used by other MAS-components to send outbound
 * XMP requests to other components in the system.
 *
 * @author ermmaha, mmawi
 */
public class ServiceRequestManager implements IServiceRequestManager {
    private static com.mobeon.common.logging.ILogger log = ILoggerFactory.getILogger(ServiceRequestManager.class);
    private static HostedServiceLogger hostedServiceLog =
            new HostedServiceLogger(ILoggerFactory.getILogger(ServiceRequestManager.class));

    private ServiceRequestManagerConfiguration configurationHolder;
    private IConfiguration configuration;
    private IEventDispatcher eventDispatcher;
    private ILocateService locateService;
    private Supervision supervision;
    private IXMPClient xmpClient;
    private IXMPServer xmpServer;
    private IApplicationManagment applicationManagement;
    private IXMPServiceHandlerFactory serviceHandlerFactory;
    private ISessionFactory sessionFactory;

    private static final Map<String, IXMPServiceHandler> serviceHandlers
            = new HashMap<String, IXMPServiceHandler>();
    private static final Map<Integer, XMPResultHandler> resultHandlers
            = new HashMap<Integer, XMPResultHandler>();

    private AtomicBoolean initialized = new AtomicBoolean(false);

    //========== Initialization methods =======================================

    public ServiceRequestManager() {
    }

    /**
     * Constructor for mtest.
     *
     * @param locateService  to find the services from the externalcomponentregister
     * @param iConfiguration to access config parameters
     */
    public ServiceRequestManager(ILocateService locateService, IConfiguration iConfiguration) {
        setServiceLocator(locateService);
        setConfiguration(iConfiguration);

        xmpClient = new XMPClient();
        setupXMPClient();
    }

    /**
     * Constructor for testing purposes. Can be used when a XMP server socket
     * is not needed.
     *
     * @param configuration
     * @param eventDispatcher
     * @param locateService
     * @param applicationManagement
     * @param serviceHandlerFactory
     */
    public ServiceRequestManager(IConfiguration configuration,
                                 IEventDispatcher eventDispatcher,
                                 ILocateService locateService,
                                 IApplicationManagment applicationManagement,
                                 IXMPServiceHandlerFactory serviceHandlerFactory,
                                 IXMPClient xmpClient,
                                 Supervision supervision,
                                 ISessionFactory sessionFactory) {

        setConfiguration(configuration);
        setEventDispatcher(eventDispatcher);
        setServiceLocator(locateService);
        setApplicationManagement(applicationManagement);
        setServiceHandlerFactory(serviceHandlerFactory);
        setSupervision(supervision);
        setSessionFactory(sessionFactory);

        verifyResources();
        if (xmpClient == null) {
            this.xmpClient = new XMPClient();
        } else {
            this.xmpClient = xmpClient;
        }
        setupXMPClient();
        this.eventDispatcher.addEventReceiver(this);
        initialized.set(true);
    }

    /**
     * Injects the required <code>IApplicationManagment</code>.
     *
     * @param applicationManagement The application management.
     */
    public void setApplicationManagement(IApplicationManagment applicationManagement) {
        this.applicationManagement = applicationManagement;
    }

    /**
     * Inject the service handler factory.
     *
     * @param serviceHandlerFactory
     */
    public void setServiceHandlerFactory(IXMPServiceHandlerFactory serviceHandlerFactory) {
        this.serviceHandlerFactory = serviceHandlerFactory;
    }

    /**
     * Inject configuration
     *
     * @param configuration The configuration
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
        configurationHolder = new ServiceRequestManagerConfiguration(configuration);
    }

    /**
     * Inject the event dispatcher.
     *
     * @param eventDispatcher The event dispatcher.
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        ServiceRequestManagerController.getInstance().setEventDispatcher(this.eventDispatcher);
    }

    /**
     * Inject O&M manager
     *
     * @param supervision The Supervision
     */
    public void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    /**
     * Inject service locator
     *
     * @param locateService The service locator
     */
    public void setServiceLocator(ILocateService locateService) {
        this.locateService = locateService;
    }

    /**
     * Inject session factory.
     * @param sessionFactory
     */
    public void setSessionFactory(ISessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void init() {
        verifyResources();
        xmpClient = new XMPClient();
        setupXMPClient();
        eventDispatcher.addEventReceiver(this);
    }

    //========== End of Initialization methods ================================


    //========== IServiceRequestManager methods ===============================

    /**
     * Sends a service request to an available host that supports the requested service.
     *
     * @param request The service request
     * @return A service response, null if no response is required.
     */
    public ServiceResponse sendRequest(ServiceRequest request) {
        return doSendRequest(request);
    }

    /**
     * Sends a service request to the specified host.
     *
     * @logs.warning "The host (hostName) is not reqistered with service (serviceId)"
     * - No host with the specified hostname is reqistered in component registered for the
     * requested service.
     *
     * @param request   The service request
     * @param hostName  The name of the host to use
     * @return A service response, null if no response is requred.
     */
    public ServiceResponse sendRequest(ServiceRequest request, String hostName) {
        IServiceInstance serviceInstance = null;
        String host = null;
        String portStr = null;
        try {
            serviceInstance = locateService.locateService(request.getServiceId(), hostName);
            host = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
            portStr = serviceInstance.getProperty(IServiceInstance.PORT);
        } catch (NoServiceFoundException e) {
            String message = "The host " + hostName + " is not registered with service "
                    + request.getServiceId();
            log.warn(message);
            e.printStackTrace(System.out);
            return createNotAvailableResponse();
        }
        
        if ((host == null) || (portStr == null)) {
            String message = "Could not find the required properties for host " + hostName;
            log.warn(message);
            return createNotAvailableResponse();
        }

        return doSendRequest(request, serviceInstance);
    }

    /**
     * Sends a service request to the specified host, and the specified port.
     * Use for test only
     *
     * @param request       The service request
     * @param hostName      The name of the host to use
     * @param portNumber    The port number
     * @return A service response, null if no response is required.
     */
    public ServiceResponse sendRequest(ServiceRequest request, String hostName, int portNumber) {
        IServiceInstance instance = new ServiceInstanceImpl(request.getServiceId());
        instance.setProperty(IServiceInstance.HOSTNAME, hostName);
        instance.setProperty(IServiceInstance.PORT, Integer.toString(portNumber));
        return doSendRequest(request, instance);
    }

    /**
     * Sends a service request to an available host that supports the requested service.
     * The request is sent asynchronously.
     *
     * @param request The service request
     * @return A transaction id that can be used to fetch the response later. If
     * no response is requred, -1 is returned.
     */
    public int sendRequestAsync(ServiceRequest request) {
        return doSendRequestAsync(request);
    }

    /**
     * Sends a service request to an available host that supports the requested service
     * and matches the given hostname.
     * The request is sent asynchronously.
     *
     * @logs.warning "The host (hostName) is not reqistered with service (serviceId)"
     * - No host with the specified hostname is reqistered in component registered for the
     * requested service.
     *
     * @param request  The service request
     * @param hostName The name of the host to use
     * @return A transaction id that can be used to fetch the response later. If
     * no response is requred, -1 is returned.
     */
    public int sendRequestAsync(ServiceRequest request, String hostName) {
        IServiceInstance serviceInstance = null;
        String host = null;
        String portStr = null;
        try {
            serviceInstance = locateService.locateService(request.getServiceId(), hostName);
            host = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
            portStr = serviceInstance.getProperty(IServiceInstance.PORT);
        } catch (NoServiceFoundException e) {
            String message = "The host " + hostName + " is not registered with service "
                    + request.getServiceId();
            log.warn(message);
            XMPResultHandler resultHandler = new XMPResultHandler();
            int transId = xmpClient.nextTransId();
            resultHandler.handleResult(createNotAvailableResult(transId));
            synchronized(resultHandlers) {
                resultHandlers.put(transId, resultHandler);
            }
            e.printStackTrace(System.out);
            return transId;
        }
        
        if ((host == null) || (portStr == null)) {
            String message = "Could not find the required properties for host " + hostName;
            log.warn(message);
            XMPResultHandler resultHandler = new XMPResultHandler();
            int transId = xmpClient.nextTransId();
            resultHandler.handleResult(createNotAvailableResult(transId));
            synchronized(resultHandlers) {
                resultHandlers.put(transId, resultHandler);
            }
            return transId;
        }

        return doSendRequestAsync(request, serviceInstance);
    }

    /**
     * Sends a service request to an available host that supports the requested service
     * and matches the given hostname and port number.
     * The request is sent asynchronously.
     *
     * @param request    The service request
     * @param hostName   The name of the host to use
     * @param portNumber The port that shall be used
     * @return A transaction id that can be used to fetch the response later. If
     * no response is requred, -1 is returned.
     */
    public int sendRequestAsync(ServiceRequest request, String hostName, int portNumber) {
        IServiceInstance instance = new ServiceInstanceImpl(request.getServiceId());
        instance.setProperty(IServiceInstance.HOSTNAME, hostName);
        instance.setProperty(IServiceInstance.PORT, Integer.toString(portNumber));
        return doSendRequestAsync(request, instance);
    }

    /**
     * Check if a result is ready for an asynchronous request.
     *
     * @logs.warning "No result handler found for transactionId: (transactionId)"
     * - The ServiceRequestManager does not have a result handler matching the
     * given transaction id. Make sure that the correct transaction id was used.
     *
     * @param transactionId The transaction id for the asynchronous request.
     * @return <code>true</code> if a result is ready, <code>false</code>
     *         otherwise.
     */
    public boolean isTransactionCompleted(int transactionId) {
        XMPResultHandler resultHandler = null;

        // transactionId -1 means that no response is required, just return true.
        if (transactionId < 0) {
            if (log.isDebugEnabled())
                log.debug("No response required, returning true.");
            return true;
        }

        synchronized(resultHandlers) {
            resultHandler = resultHandlers.get(transactionId);
        }
        if (resultHandler == null) {
            log.warn("No result handler found for transactionId: " + transactionId);
            return false;
        }

        XmpResult result = resultHandler.getXmpResult();
        if (result == null) {
            if (log.isDebugEnabled()) {
                log.debug("No result yet for transactionId: " + transactionId);
            }
            return false;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Transaction completed, transactionId: " + transactionId);
            }
            return true;
        }
    }

    /**
     * Receives a response to an asynchrounous service request.
     * <p/>
     * The call is blocking/synchrounous and will return a response
     * corresponding to the given transaction. In other words, this method
     * will return a response as soon as the transaction has completed.
     * To prevent blocking, call
     * {@link ServiceRequestManager#isTransactionCompleted(int)} before to be
     * sure that a response is ready for the transaction.
     * In the case where multiple XMP responses are received these responses
     * are appended into one {@link ServiceResponse}.
     *
     * @logs.warning "No result handler found for transactionId: (transactionId)"
     * - The ServiceRequestManager does not have a result handler matching the
     * given transaction id. Make sure that the correct transaction id was used.
     *
     * @logs.warning "Result not found for transactionId: (transactionId)"
     * - The result for this transaction may not be ready. Did
     * <code>isTransactionCompleted()</code> return true?
     *
     * @param transactionId a transaction from which a response is required.
     * @return the required response.
     */
    public ServiceResponse receiveResponse(int transactionId) {
        XMPResultHandler resultHandler = null;

        // transactionId -1 means that no response is required, just return null.
        if (transactionId < 0) {
            if (log.isDebugEnabled())
                log.debug("No response required, returning null.");
            return null;
        }

        synchronized(resultHandlers) {
            resultHandler = resultHandlers.remove(transactionId);
        }
        if (resultHandler == null) {
            log.warn("No result handler found for transactionId: " + transactionId);
            return null;
        }

        XmpResult result = resultHandler.getXmpResult();
        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("Result found for transactionId: " + transactionId);
            }
            return ServiceUtil.makeResponse(result, configurationHolder.getXmpClientId());
        } else {
            if (log.isDebugEnabled()) {
                log.warn("Result not found for transactionId: " + transactionId);
            }
            return null;
        }
    }

    /**
     * Send the response to the client that requested the service.
     * <p/>
     * The response is passed to the XMP service handler associated with the
     * sessionId.
     *
     * @logs.error "ServiceRequestManager is not initialized"
     * - Before the service request manager can handler requests it must be
     * initialized by execution engine.
     *
     * @logs.warning "Unable to find a service handler for this session: (sessionId)"
     * - No XMP service handler is associated with this session id. The
     * resonse cannot be sent back to the client.
     *
     * @throws ServiceRequestManagerException if the ServiceRequestManager is
     * not initialized or if no XMP service handler is found for the session id.
     *
     * @param sessionId The id of the session which the resonse is ready for.
     * @param serviceResponse The response.
     */
    public void sendResponse(String sessionId, ServiceResponse serviceResponse)
            throws ServiceRequestManagerException {
        if (!initialized.get()) {
            String errorMessage = "ServiceRequestManager is not initialized";
            log.error(errorMessage);
            throw new ServiceRequestManagerException(errorMessage);
        }
        if (log.isDebugEnabled()) {
            log.debug("Response received, sessionId: " + sessionId);
        }

        IXMPServiceHandler serviceHandler = null;
        synchronized(serviceHandlers) {
            serviceHandler = serviceHandlers.remove(sessionId);
        }
        if (serviceHandler == null) {
            String errorMessage = "Unable to find a service handler for this session: " + sessionId;
            log.warn(errorMessage);
            throw new ServiceRequestManagerException(errorMessage);
        } else {
            ServiceRequestManagerController.getInstance().removeSession();
            serviceHandler.sendResponse(serviceResponse);
        }
    }

    // ========== End of IServiceRequestManager methods =======================


    // ========== ServiceHandler methods ======================================

    /**
     * Handle a new XMP service request from a client.
     * <p>
     * The service request manager will look up the requested service in the
     * ApplicationManagement. The returned ApplicationExecution is then passed
     * together with the request to a XMP service handler. The handler is
     * associated with the sessionId of the current session so it can be found
     * when the response is ready to send back.
     * In case of errors, a response is sent back immediately.
     *
     * @logs.error "ServiceRequestManager is not initialized."
     * - Before the service request manager can receive requests it has to be
     * initialized by execution engine
     *
     * @logs.warning "Maximum number of concurrent XMP sessions reached."
     * - The service request manager cannot accept more XMP service request due to
     * load regulation. The response code 502 is returned to the client.
     *
     * @logs.info "Unable to load service from ApplicationManagement: (serviceId)"
     * - The requested service is not registered in the ApplicationManagement. Status
     * code 421 is returned to the client.
     *
     * @param responseQueue
     * @param serviceId
     * @param clientId
     * @param transactionId
     * @param validity
     * @param xmpDoc
     * @param attachments
     */
    public void handleRequest(XmpResponseQueue responseQueue,
                              String serviceId,
                              String clientId,
                              Integer transactionId,
                              int validity,
                              org.w3c.dom.Document xmpDoc,
                              ArrayList attachments) {

        if (!initialized.get()) {
            log.error("ServiceRequestManager is not initialized");
            responseQueue.addResponse(createNotAvailableAnswer(transactionId));
            return;
        }

        if (serviceId.equals(IServiceName.DIAGNOSE_SERVICE)) {
            handleDiagnoseServiceRequest(responseQueue, transactionId);
            return;
        }

        ISession session = sessionFactory.create();
        log.registerSessionInfo("session", session.getId());

        SessionMdcItems sessionMdcItems = new SessionMdcItems();
        sessionMdcItems.setLogData("clientid", clientId);
        sessionMdcItems.setLogData("transactionid", transactionId);
        sessionMdcItems.registerMdcItemsInLogger();

        if (!(ServiceRequestManagerController.getInstance().getCurrentState()
                instanceof OpenedState)) {
            if (log.isInfoEnabled())
                log.info("Service is not open.");
            responseQueue.addResponse(createNotAvailableAnswer(transactionId));
            ServiceRequestManagerController.getInstance().
                    updateStatistics(CallResult.FAILED, CallDirection.INBOUND);
            return;
        }

        if (!ServiceRequestManagerController.getInstance().addSession()) {
            log.warn("Maximum number of concurrent XMP sessions reached.");
            XmpAnswer answer = new XmpAnswer();
            answer.setStatusCode(ServiceResponse.STATUSCODE_RESOURCE_LIMIT_EXCEEDED);
            answer.setStatusText(ServiceResponse.STATUSTEXT_RESOURCE_LIMIT_EXCEEDED);
            answer.setTransactionId(transactionId);
            responseQueue.addResponse(answer);
            ServiceRequestManagerController.getInstance().
                    updateStatistics(CallResult.FAILED, CallDirection.INBOUND);
            return;
        }

        IApplicationExecution applicationExecution =
                applicationManagement.load(serviceId);
        if (applicationExecution == null) {
            if (log.isInfoEnabled())
                log.info("Unable to load service from ApplicationManagement: " + serviceId);
            responseQueue.addResponse(createNotAvailableAnswer(transactionId));
            ServiceRequestManagerController.getInstance().
                    updateStatistics(CallResult.FAILED, CallDirection.INBOUND);
            return;
        }

        ServiceRequest serviceRequest = createServiceRequest(xmpDoc, attachments);
        serviceRequest.setServiceId(serviceId);
        serviceRequest.setValidityTime(validity);

        IXMPServiceHandler serviceHandler = serviceHandlerFactory.create();
        IXMPResponseQueue xmpResponseQueue = new IXMPResponseQueueImpl(responseQueue);

        session.setData(ISession.SESSION_INITIATOR, clientId);
        session.setData(ISession.SERVICE_REQUEST, serviceRequest);
        session.registerSessionInLogger();
        session.setMdcItems(sessionMdcItems);

        synchronized(serviceHandlers) {
            serviceHandlers.put(session.getId(), serviceHandler);
        }

        serviceHandler.handleRequest(session,
                serviceRequest,
                clientId,
                transactionId,
                xmpResponseQueue,
                applicationExecution);
    }


    /**
     * Cancel a service request due to timeout.
     * <p/>
     * The XMP service handlers are iterated to find the matching client id
     * and transaction id. When a matching service handler is found, the
     * request is cancelled on that handler.
     *
     * @logs.error "ServiceRequestManager is not initialized"
     * - Before the service request manager can handler requests it must be
     * initialized by the execution engine.

     * @logs.warning "Unable to get the service handler for this session: (sessionId)"
     * - The XMP service handler for this client and transaction is not
     * available, it may have finished with the response at the same time as
     * the timeout occurred. The response is discarded since it's validity
     * time is expired, status code 408 is already sent to the client.
     *
     * @param clientId The id of the client that requested the service.
     * @param transactionId The id of the transaction that has timed out.
     */
    public void cancelRequest(String clientId, Integer transactionId) {
        if (!initialized.get()) {
            log.error("ServiceRequestManager is not initialized");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Cancelling request for clientId: " + clientId
                    + ", transactionId: " + transactionId);
        }
        Iterator it;
        synchronized(serviceHandlers) {
            Map<String, IXMPServiceHandler> copy =
                    new HashMap<String, IXMPServiceHandler>(serviceHandlers);
            it = copy.keySet().iterator();
        }
        String sessionId = null;
        while(it.hasNext()) {
            String key = (String) it.next();
            IXMPServiceHandler sh;
            synchronized(serviceHandlers) {
                sh = serviceHandlers.get(key);
            }
            if (sh != null && sh.equals(clientId, transactionId)) {
                sessionId = sh.getSessionId();
                if (log.isDebugEnabled()) {
                    log.debug("Found a matching service handler clientId: " + clientId
                            + ", transactionId: " + transactionId + " , sessionId: " + sessionId);
                }
                break;
            }
        }

        IXMPServiceHandler serviceHandler;
        synchronized(serviceHandlers) {
            serviceHandler = serviceHandlers.remove(sessionId);
        }
        if (serviceHandler == null) {
            log.warn("Unable to get the service handler for this session: " + sessionId);
        } else {
            ServiceRequestManagerController.getInstance().removeSession();
            try {
                serviceHandler.cancelRequest();
            } catch (ServiceRequestManagerException e) {
                if (log.isInfoEnabled())
                    log.info("Failed to cancel request, session: " + sessionId
                            + " transactionId: " + transactionId, e);
            }
            serviceHandler = null;
        }
    }

    //========== End of ServiceHandler methods ================================


    //========== ServiceEnabler methods =======================================

    public ServiceEnablerOperate initService(String service, String host, int port)
            throws ServiceEnablerException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing service " + service + " on " + host + ":" + port);
        }

        // Do this only for the first initialized service.
        if (initialized.compareAndSet(false, true)) {
            setupXMPServer(host, port);

            ServiceRequestManagerController.getInstance().setHost(host);
            ServiceRequestManagerController.getInstance().setPort(port);

            ServiceEnablerInfo statistics = null;
            try {
                statistics = supervision.getServiceEnablerStatistics(
                        ServiceRequestManagerController.getInstance());
                ServiceRequestManagerController.getInstance().setServiceEnablerInfo(statistics);
            } catch (Exception e) {
                throw new ServiceEnablerException("Failed to get ServiceEnablerStatistics.", e);
            }
        }

        XmpHandler.setServiceHandler(service, this);

        return ServiceRequestManagerController.getInstance();
    }

    //========== End of ServiceEnabler methods ================================


    //========== IEventReceiver methods =======================================

    public void doEvent(Event event) {
        if (event instanceof ApplicationEnded) {
            ExecutorServiceManager.getInstance().
                    getExecutorService(ServiceRequestManager.class).
                    execute(new EventHandler(event));
        } else if (event instanceof ServiceClosed) {
            ExecutorServiceManager.getInstance().
                    getExecutorService(ServiceRequestManager.class).
                    execute(new EventHandler(event));
        }
    }

    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            ExecutorServiceManager.getInstance().
                    getExecutorService(ServiceRequestManager.class).
                    execute(new EventHandler(event));
        }
    }

    //========== End of IEventReceiver methods ================================


    //========== Helpers for Initialization methods ===========================

    private void setupXMPServer(String host, int port) {
        xmpServer = IXMPServerImpl.getInstance();
        xmpServer.start(host, port);
        
        XmpHandler.setServiceHandler(IServiceName.DIAGNOSE_SERVICE, this);
    }

    private void setupXMPClient() {
        xmpClient.setClientId(configurationHolder.getXmpClientId());
        xmpClient.setLogger(new XMPLogger());
    }

    /**
     * Verify that all required resources are injected.
     *
     * @throws java.util.MissingResourceException
     */
    private void verifyResources() throws MissingResourceException {
        if (configuration == null) {
            throw new MissingResourceException("Configuration is null. Must be injected.",
                    "IConfiguration", "Configuration");
        }
        if (configurationHolder == null) {
            throw new MissingResourceException("ConfigurationHolder is null. Must be injected.",
                    "IServiceRequestManagerConfiguration", "ConfigurationHolder");
        }
        if (applicationManagement == null) {
            throw new MissingResourceException("ApplicationManagement is null. Must be injected.",
                    "IApplicationManagement", "ApplicationManagement");
        }
        if (serviceHandlerFactory == null) {
            throw new MissingResourceException("ServiceHandlerFactory is null. Must be injected.",
                    "IXMPServiceHandlerFactory", "ServiceHandlerFactory");
        }
        if (eventDispatcher == null) {
            throw new MissingResourceException("EventDispatcher is null. Must be injected.",
                    "IEventDispatcher", "EventDispatcher");
        }
        if (supervision == null) {
            throw new MissingResourceException("Supervision is null. Must be injected.",
                    "Supervision", "Supervision");
        }
        if (locateService == null) {
            throw new MissingResourceException("ServiceLocator is null. Must be injected.",
                    "ILocateService", "ServiceLocator");
        }
        if (sessionFactory == null) {
            throw new MissingResourceException("SessionFactory is null. Must be injected.",
                    "ISessionFactory", "SessionFactory");
        }
    }

    //========== End of helpers for Initialization methods ====================


    //========== Helpers for IServiceRequestManager methods ===================

    /**
     * Create a ServiceRequest from a XML document.
     *
     * @param xmpDoc The XMP document
     * @param attachments ?
     * @return A ServiceRequest
     */
    private ServiceRequest createServiceRequest(Document xmpDoc, ArrayList attachments) {
        ServiceRequest serviceRequest = new ServiceRequest();
        NodeList parameters = xmpDoc.getElementsByTagName("parameter");
        for (int i = 0; i < parameters.getLength(); ++i) {
            Element e = (Element) parameters.item(i);
            String parameterName = e.getAttribute("name");
            String parameterValue = "";
            if ( e.hasChildNodes() ) {
                parameterValue = e.getFirstChild().getNodeValue();
            }
            serviceRequest.setParameter(parameterName, parameterValue);
        }
        //todo attachments?

        return serviceRequest;
    }

    /**
     * Send a service request using the client.
     * Component register is used to get server.
     * Will retry on error and report failing components so Service Locator.
     *
     * @param request   The service request
     * @return A service response
     */
    private ServiceResponse doSendRequest(ServiceRequest request) {
        int transId = xmpClient.nextTransId();
        XmpResult xmpResult = null;
        boolean sendOk;
        IServiceInstance serviceInstance = null;

        // initial value true to get a host to start with.
        boolean hostError = true;
        String host;
        int port;

        for (int k=0; k < configurationHolder.getXmpServiceRequestRetries(); ++k) {
            // lookup host
            try {
                if (hostError) {
                    serviceInstance = locateService.locateService(request.getServiceId());
                } else {
                    serviceInstance = locateService.getAnotherService(serviceInstance);
                }
                host = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
                port = Integer.parseInt(serviceInstance.getProperty(IServiceInstance.PORT));
                
            } catch (NoServiceFoundException e) {
                ServiceRequestManagerController.getInstance().
                        updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
                if (k == 0) {
                    hostedServiceLog.warn("No hosts registrated for " + request.getServiceId() + ".");
                } else {
                    hostedServiceLog.warn("No more hosts to try");
                }
                e.printStackTrace(System.out);
                return createNotAvailableResponse();
            } catch (NumberFormatException e) {
                ServiceRequestManagerController.getInstance().
                        updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
                e.printStackTrace(System.out);
                return createNotAvailableResponse();
            }
            if (hostedServiceLog.isInfoEnabled()) {
                    hostedServiceLog.info("Sending " + request.getServiceId() + " request to " + host + ":" + port);
            }
            XMPResultHandler resultHandler = new XMPResultHandler();
            sendOk = xmpClient.sendRequest(transId,
                    ServiceUtil.makeRequest(request, transId, configurationHolder.getXmpClientId()),
                    request.getServiceId(), resultHandler, serviceInstance);

            if (!sendOk) {
                // Communication error, report failing service instance to service
                // locator and try again if there are more retries.
                if (hostedServiceLog.isDebugEnabled()) {
                    hostedServiceLog.debug("Communication error with " + host + ", trying next.");
                }
                locateService.reportServiceError(serviceInstance);
                hostError = true;

            } else if (request.getResponseRequired()) {
                //thread will block here
                resultHandler.waitForResult(configurationHolder.getXmpServiceRequestTimeout());
                xmpResult = resultHandler.getXmpResult();
                if (xmpResult != null) {
                    int statusCode = xmpResult.getStatusCode();
                    if (statusCode == ServiceResponse.STATUSCODE_SERVICE_NOT_AVAILABLE ||
                            statusCode == ServiceResponse.STATUSCODE_RESOURCE_LIMIT_EXCEEDED) {
                        // 421 or 502, report failing service instance to service
                        // locator and try again if there are more retries.
                        hostedServiceLog.warn("Status code " + statusCode + " received from "  + host + ", trying next.");
                        locateService.reportServiceError(serviceInstance);
                        hostError = true;
                    } else if (statusCode == ServiceResponse.STATUSCODE_REQUEST_FAILED) {
                        // 450, get another service instance and try again if
                        // there are more retries.
                        hostedServiceLog.warn("Status code " + statusCode + " received from "  + host + ", trying next.");
                        hostError = false;
                    } else {
                        if (hostedServiceLog.isInfoEnabled()) {
                            hostedServiceLog.info("Status code " + statusCode + " received from " + host + ".");
                        }
                        if (xmpResult.getStatusCode() == ServiceResponse.STATUSCODE_SUCCESS_COMPLETE) {
                            ServiceRequestManagerController.getInstance().
                                    updateStatistics(CallResult.CONNECTED, CallDirection.OUTBOUND);
                        } else {
                            ServiceRequestManagerController.getInstance().
                                    updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
                        }
                        return ServiceUtil.makeResponse(xmpResult, configurationHolder.getXmpClientId());
                    }
                } else {
                    hostedServiceLog.warn("No result received within specified time from " + host + ", trying next.");
                    hostError = false;
                }
            } else {
                if (hostedServiceLog.isDebugEnabled())
                    hostedServiceLog.debug("No response is required, returning null.");
                return null;
            }
        }

        if (xmpResult != null) {
            if (xmpResult.getStatusCode() == ServiceResponse.STATUSCODE_SUCCESS_COMPLETE) {
                ServiceRequestManagerController.getInstance().
                        updateStatistics(CallResult.CONNECTED, CallDirection.OUTBOUND);
            } else {
                ServiceRequestManagerController.getInstance().
                        updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
                hostedServiceLog.warn("No more hosts to try");
            }
            return ServiceUtil.makeResponse(xmpResult, configurationHolder.getXmpClientId());
        }

        hostedServiceLog.warn("No more hosts to try");

        ServiceRequestManagerController.getInstance().
                updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
        return createNotAvailableResponse();
    }

    /**
     * Send a service request to a specified component.
     * No retries.
     *
     * @param request   The service request
     * @param component The component
     * @return A service response
     */
    private ServiceResponse doSendRequest(ServiceRequest request, IServiceInstance instance) {
    	
    	String host = instance.getProperty(IServiceInstance.HOSTNAME);
    	String port = instance.getProperty(IServiceInstance.PORT);
    	
        if (hostedServiceLog.isInfoEnabled()) {
            hostedServiceLog.info("Sending " + request.getServiceId() + " request to "
                    + host + ":" + port);
        }
        int transId = xmpClient.nextTransId();
        XMPResultHandler resultHandler = new XMPResultHandler();
        boolean sendOk;
        sendOk = xmpClient.sendRequest(transId,
                ServiceUtil.makeRequest(request, transId, configurationHolder.getXmpClientId()),
                request.getServiceId(), resultHandler, instance);

        if (!sendOk) {
            hostedServiceLog.warn("Communication error with " + host + ".");

        } else if (request.getResponseRequired()) {
            //thread will block here
            resultHandler.waitForResult(configurationHolder.getXmpServiceRequestTimeout());
            XmpResult xmpResult = resultHandler.getXmpResult();
            if (xmpResult != null) {
                if (hostedServiceLog.isInfoEnabled()) {
                    hostedServiceLog.info("Status code " + xmpResult.getStatusCode() + " received from " + host + ".");
                }
                if (xmpResult.getStatusCode() == ServiceResponse.STATUSCODE_SUCCESS_COMPLETE) {
                    ServiceRequestManagerController.getInstance().
                            updateStatistics(CallResult.CONNECTED, CallDirection.OUTBOUND);
                } else {
                    ServiceRequestManagerController.getInstance().
                            updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
                }
                return ServiceUtil.makeResponse(xmpResult, configurationHolder.getXmpClientId());
            } else {
                hostedServiceLog.warn("No result received within specified time from " + host + ".");
            }
        } else {
            if (hostedServiceLog.isDebugEnabled())
                hostedServiceLog.debug("No response required, returning null.");
            return null;
        }

        ServiceRequestManagerController.getInstance().
                updateStatistics(CallResult.FAILED, CallDirection.OUTBOUND);
        return createNotAvailableResponse();
    }

    /**
     * Send a service request to the preferred host aynchronously
     *
     * @param request   The service request
     * @return A transaction id.
     */
    private int doSendRequestAsync(ServiceRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Sending asynchronous " + request.getServiceId()
                    + " request to preferred host.");
        }
        int transId = xmpClient.nextTransId();
        XMPResultHandler resultHandler = new XMPResultHandler();
        boolean sendOk;
        IServiceInstance serviceInstance = null;

        synchronized(resultHandlers) {
            resultHandlers.put(Integer.valueOf(transId), resultHandler);
        }

        for (int k=0; k < configurationHolder.getXmpServiceRequestRetries(); ++k) {
            // lookup host
            try {
                serviceInstance = locateService.locateService(request.getServiceId());
                Integer.parseInt(serviceInstance.getProperty(IServiceInstance.PORT));
            } catch (NoServiceFoundException e) {
                XmpResult result = createNotAvailableResult(transId);
                resultHandler.handleResult(result);
                e.printStackTrace(System.out);
                return transId;
            } catch (NumberFormatException e) {
                XmpResult result = createNotAvailableResult(transId);
                resultHandler.handleResult(result);
                e.printStackTrace(System.out);
                return transId;
            }

            sendOk = xmpClient.sendRequest(transId,
                    ServiceUtil.makeRequest(request, transId, configurationHolder.getXmpClientId()),
                    request.getServiceId(), resultHandler, serviceInstance);

            if (!sendOk) {
                log.error("Failed to send request, will try again. transactionId: " + transId);
                locateService.reportServiceError(serviceInstance);

            } else if (request.getResponseRequired()) {
                if (log.isDebugEnabled()) {
                    log.debug("SendRequest OK, transactionId: " + transId);
                }
                return transId;

            } else {
                if (log.isDebugEnabled())
                    log.debug("No response required, returning transactionId -1");
                return -1;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("No more retries.");
        }
        XmpResult result = createNotAvailableResult(transId);
        resultHandler.handleResult(result);
        return transId;
    }

    /**
     * Sends a service request asynchronously to a host.
     *
     * @param request   The service request
     * @param component The host to send the request to
     * @return A transaction id
     */
    private int doSendRequestAsync(ServiceRequest request, IServiceInstance instance) {
        if (log.isDebugEnabled()) {
            log.debug("Sending asynchronous " + request.getServiceId() + " request to "
                    + instance.getProperty(IServiceInstance.HOSTNAME) + ":" + instance.getProperty(IServiceInstance.PORT));
        }
        int transId = xmpClient.nextTransId();
        XMPResultHandler resultHandler = new XMPResultHandler();
        boolean sendOk;

        synchronized(resultHandlers) {
            resultHandlers.put(Integer.valueOf(transId), resultHandler);
        }

        sendOk = xmpClient.sendRequest(transId,
                ServiceUtil.makeRequest(request, transId, configurationHolder.getXmpClientId()),
                request.getServiceId(), resultHandler, instance);

        if (!sendOk) {
            log.error("Failed to send request, transactionId: " + transId);
            XmpResult result = createNotAvailableResult(transId);
            resultHandler.handleResult(result);
            return transId;

        } else if (request.getResponseRequired()) {
            if (log.isDebugEnabled()) {
                log.debug("SendRequest OK, transactionId: " + transId);
            }
            return transId;

        } else {
            if (log.isDebugEnabled())
                log.debug("No response required, returning transactionId -1");
            return -1;
        }
    }

    /**
     * Creates a 421 ServiceResponse. Service not available
     *
     * @return A ServiceResponse with status code 421
     */
    private ServiceResponse createNotAvailableResponse() {
        ServiceResponse notAvailableResponse = new ServiceResponse();
        notAvailableResponse.setStatusCode(ServiceResponse.STATUSCODE_SERVICE_NOT_AVAILABLE);
        notAvailableResponse.setStatusText(ServiceResponse.STATUSTEXT_SERVICE_NOT_AVAILABLE);
        return notAvailableResponse;
    }

    /**
     * Creates an XMP answer with status code 421, service not available
     * @param transactionId The transaction id
     * @return An XMP answer with status code 421
     */
    private XmpAnswer createNotAvailableAnswer(Integer transactionId) {
        XmpAnswer notAvailableAnswer = new XmpAnswer();
        notAvailableAnswer.setStatusCode(ServiceResponse.STATUSCODE_SERVICE_NOT_AVAILABLE);
        notAvailableAnswer.setStatusText(ServiceResponse.STATUSTEXT_SERVICE_NOT_AVAILABLE);
        notAvailableAnswer.setTransactionId(transactionId);
        return notAvailableAnswer;
    }

    /**
     * Creates an XMP result with status code 421.
     * @param transId The transaction id
     * @return An XMP result with status code 421
     */
    private XmpResult createNotAvailableResult(int transId) {
        XmpResult xmpResult = new XmpResult(transId,
                ServiceResponse.STATUSCODE_SERVICE_NOT_AVAILABLE,
                ServiceResponse.STATUSTEXT_SERVICE_NOT_AVAILABLE,
                null);
        return xmpResult;
    }

    /**
     * Handles a diagnose service request. If we have come this far, the service
     * can be considered UP.
     *
     * @param responseQueue
     * @param transactionId
     */
    private void handleDiagnoseServiceRequest(XmpResponseQueue responseQueue, int transactionId) {
        XmpAnswer diagnoseServiceAnswer;

        if(log.isDebugEnabled())
            log.debug("Handling DiagnoseService request. transactionId: " + transactionId);

        diagnoseServiceAnswer = new XmpAnswer();
        diagnoseServiceAnswer.setStatusCode(ServiceResponse.STATUSCODE_SUCCESS_COMPLETE);
        diagnoseServiceAnswer.setStatusText(ServiceResponse.STATUSTEXT_SUCCESS_COMPLETE);
        diagnoseServiceAnswer.setTransactionId(transactionId);

        responseQueue.addResponse(diagnoseServiceAnswer);
    }

    //========== End of helpers for IServiceRequestManager methods ============


    //========== Helpers for IEventReceiver methods ===========================

    /**
     * Handle an ApplicationEnded event.
     * If the service handler for the event's session id is still present,
     * send status code 421. Otherwise the event is just ingored. A response
     * has already been sent to the client.
     *
     * @param event The ApplicationEnded event.
     */
    private void handleApplicationEnded(Event event) {
        ApplicationEnded applicationEnded = (ApplicationEnded) event;
        if (log.isDebugEnabled()) {
            log.debug("Received applicationEnded event for session: "
                    + applicationEnded.getSessionId());
        }
        IXMPServiceHandler serviceHandler = null;
        log.debug("trying to find service handler for session: " + applicationEnded.getSessionId());
        synchronized(serviceHandlers) {
            serviceHandler = serviceHandlers.remove(applicationEnded.getSessionId());
        }
        if (serviceHandler != null) {
            ServiceResponse response = createNotAvailableResponse();
            try {
                log.debug("calling sendResponse.");
                serviceHandler.sendResponse(response);
            } catch (ServiceRequestManagerException e) {
                // The response could not be added to queue, nothing to do about...
                if (log.isDebugEnabled())
                    log.debug("Failed to att response to queue. session id: " +
                            applicationEnded.getSessionId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No service handler matches session id: "
                        + applicationEnded.getSessionId()
                        + ", session already finished.");
            }
        }
    }

    /**
     * Handle a ConfigurationChanged event.
     * The configuration is reloaded, however, clientId is not changed.
     * @param event
     */
    private void reloadConfiguration(Event event) {
        ConfigurationChanged configurationChanged = (ConfigurationChanged) event;
        if (log.isDebugEnabled()) {
            log.debug("Reloading configuration.");
        }
        configurationHolder.reload(configurationChanged.getConfiguration());
    }

    /**
     * Handle a ServiceClosed event.
     * This event is generated when the service is forced closed, all active
     * sessions should be terminated.
     */
    private void handleServiceClosedEvent() {
        if (log.isDebugEnabled())
            log.debug("ServiceClosed event is received. All active sessions will be terminated.");

        synchronized(serviceHandlers) {
            for(String sessionId : serviceHandlers.keySet()) {
                IXMPServiceHandler sh = serviceHandlers.remove(sessionId);
                if (sh != null) {
                    sh.terminate();
                }
                ServiceRequestManagerController.getInstance().removeSession();
            }
        }
    }

    /**
     * Used to execute a command from a thread pool.
     * This handles two types of events: ConfigurationChanged
     * and ApplicationEnded.
     *
     */
    private class EventHandler implements Runnable {
        private Event event;

        public EventHandler(Event event) {
            this.event = event;
        }

        public void run() {
            if (event instanceof ConfigurationChanged) {
                log.clearSessionInfo();
                reloadConfiguration(event);
            } else if (event instanceof ApplicationEnded) {
                handleApplicationEnded(event);
            } else if (event instanceof ServiceClosed) {
                handleServiceClosedEvent();
            }
        }
    }

    //========== End of helpers for IEventReceiver methods ====================
}