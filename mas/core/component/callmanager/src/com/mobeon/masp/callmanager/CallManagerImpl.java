/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2007.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.callhandling.*;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.JoinRequestEvent;
import com.mobeon.masp.callmanager.events.UnjoinRequestEvent;
import com.mobeon.masp.callmanager.events.JoinErrorEvent;
import com.mobeon.masp.callmanager.events.UnjoinErrorEvent;
import com.mobeon.masp.callmanager.sip.SipStackWrapperImpl;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.masp.callmanager.statistics.StatisticsCollector;
import com.mobeon.masp.callmanager.registration.RegistrationDispatcher;
import com.mobeon.masp.callmanager.notification.OutboundNotification;
import com.mobeon.masp.callmanager.notification.NotificationDispatcher;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ServiceEnabler;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.common.eventnotifier.*;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.callmanager.sip.SipStackAuditor;
import javax.sip.*;
import javax.sip.message.Request;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides call management functionality not related to an active call.
 * Implements {@link CallManager} for this purpose.
 * <p>
 * Receives incoming SIP requests/responses.
 * Implements {@link javax.sip.SipListener} for this purpose.
 * Relays incoming SIP requests/responses to a {@link CallManagerController}
 * where they are further processed and sent to a corresponding active call
 * (if one exists) or handled out-of-dialog.
 * <p>
 * Provides the {@link ServiceEnabler} interface, i.e. the Call Manager is not
 * initiated until the method initService has been called. At that point
 * initialization is performed, e.g. the SIP stack is started.
 * During initialization, configuration is read by CM.
 * <p>
 * In order to be able to detect when configuration should be re-read, the
 * {@link IEventReceiver} interface is implemented.
 * <P>
 * After creating a CallManagerImpl instance, necessary data must be set using
 * the following methods:
 * {@link #setApplicationManagment}, {@link #setConfiguration},
 * {@link #setStreamFactory}, {@link #setSupervision},
 * {@link #setEventDispatcher} and {@link #setSessionFactory}.
 * <br>
 * Then initialization should be done using {@link #initService}.
 * After necessary data has been set and initialization has been performed,
 * CallManagerImpl can be used.
 * <p>
 * CallManagerImpl is thread-safe.
 *
 * @author Malin Flodin
 */

public class CallManagerImpl
        implements CallManager, SipListener, ServiceEnabler, IEventReceiver {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Information about runtime environment
    private IApplicationManagment applicationManagement;
    private IStreamFactory streamFactory;
    private IConfiguration configuration;
    private Supervision supervision;
    private CallManagerLicensing callManagerLicensing;
    private IEventDispatcher eventDispatcher;

    private SipStackWrapperImpl sipStackWrapper;
    private SipStackAuditor sipStackAuditor;

    // Used to verify that initService is only done once.
    private AtomicBoolean initiated = new AtomicBoolean(false);
    private ISessionFactory sessionFactory;

    /*
     * Max number of open server transactions in the transaction table.
     * Incoming requests that have the capability to create ServerTransactions
     * will not be processed if server transaction table exceeds this size.
     */
    private int maxServerTransactions = 5000;

    public CallManagerImpl() {
    }

    /**
     * Shall be used when the CallManager no longer is used.
     * Deletes allocated resources such as the SIP stack.
     */
    public synchronized void delete() throws ServiceEnablerException {

        Collection<CallInternal> calls =
                CMUtils.getInstance().getCallDispatcher().getAllCalls();
        for (CallInternal call : calls) {
            ((CallImpl)call).cancelCallTimers();
            ((CallImpl)call).removeCall();
        }

        CMUtils.getInstance().getRemotePartyController().delete();

        CMUtils.getInstance().delete();
        OutboundHostPortUsage.getInstance().clear();

        try {
            sipStackWrapper.delete();
        } catch (ObjectInUseException e) {
            String message = "ObjectInUseException was received when " +
                    "deleting SIP stack providers.";
            throw new ServiceEnablerException(message, e);
        }

        eventDispatcher.removeEventReceiver(this);
    }

    //========================= ServiceEnabler interface =======================

    public synchronized ServiceEnablerOperate initService(
            String service, String host, int port) throws ServiceEnablerException {

        if (initiated.compareAndSet(false, true)) {
            // Check that all setters have been called prior to this method.
            checkSetters();

            CMUtils cmUtils = CMUtils.getInstance();
            cmUtils.setCallManagerLicensing(this.callManagerLicensing);
            
            // Read configuration
            configureCM();
            
            initializeCallManagerLicensing();

            // Create utilities singleton that will carry globally available utilities
            cmUtils.setServiceName(service);
            cmUtils.setLocalHost(host);
            cmUtils.setLocalPort(port);
            cmUtils.setProtocol("sip");
            cmUtils.setVersion("SIP/2.0");
            
            // Initialize the rest of Call Manager
            initializeCallManager();

            // Initialize environment
            initializeEnvironment();

            // Initialize SIP stack as late as possible to minimize the risk of
            // receiving calls before the CM is initialized.
            initializeSipRelated();

            // Finally, when all setters should have been called on CMUtils, call init
            cmUtils.init();

            if (log.isInfoEnabled())
                log.info("Call Manager is initiated");

            return cmUtils.getCmController();

        } else {
            throw new ServiceEnablerException(
                    "Call Manager has already been initialized.");
        }
    }

    private void initializeCallManagerLicensing() throws ServiceEnablerException {
        try {
            this.callManagerLicensing.init();
        } catch (Exception e) {
            throw new ServiceEnablerException(
                    "Could not initialize Call Manager Licensing.", e);
        }
    }

    //=========================== Public Setters =========================

    public synchronized void setApplicationManagment(
            IApplicationManagment applicationManagement) {
        this.applicationManagement = applicationManagement;
    }

    public synchronized void setStreamFactory(IStreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    public void setSessionFactory(ISessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public synchronized void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    public synchronized void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }
    
    public synchronized void setCallManagerLicensing(CallManagerLicensing callManagerLicensing) {
        this.callManagerLicensing = callManagerLicensing;
    }

    /**
     * Sets the event dispatcher that should be used to receive global events
     * such as the "Configuration Changed" event.
     */
     public synchronized void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

     
    /**
     * Sets the maximum number of open server transactions that the transaction table can have.
     * 
     * @param maxServerTransactions The maximum number of server transactions.
     */
    public synchronized void setMaxServerTransactions(int maxServerTransactions) {
        this.maxServerTransactions = maxServerTransactions;
    }
 

    //========================= CallManager interface =========================

    public OutboundCall createCall(CallProperties callProperties,
                                   IEventDispatcher eventDispatcher,
                                   ISession session)
            throws IllegalArgumentException, RuntimeException {
        OutboundCallImpl call;
        String errorMessage;

        if (log.isInfoEnabled()) log.info("Request to create outbound call received. CallProperties=<" +
                                          callProperties + ">, EventDispatcher=<" + eventDispatcher +
                                          ">, Session=<" + session + ">");

        if (!initiated.get()) {
            // Calls cannot be created during intialization. Ignore!
            errorMessage = "Request to create outbound call received during " +
                    "initialization. No new call is created.";
            log.warn(errorMessage);

            // Report that a new outbound call is being rejected
            ErrorEvent errorEvent = new ErrorEvent(
                    null, CallDirection.OUTBOUND, errorMessage, false);
            fireEvent(eventDispatcher, errorEvent);

            throw new RuntimeException(errorMessage);
        }

        if ((eventDispatcher == null) || (session == null)) {

            // Necessary parameter not set for call
            errorMessage =
                    "Required parameter when creating outbound call is null. " +
                    "No new call is created. " +
                    "<Eventdispatcher=" + eventDispatcher +
                    ">, <Session=" + session + ">";
            if (log.isInfoEnabled()) log.info(errorMessage);

            // Report that a new outbound call is being rejected
            ErrorEvent errorEvent = new ErrorEvent(
                    null, CallDirection.OUTBOUND, errorMessage, false);
            fireEvent(eventDispatcher, errorEvent);

            throw new IllegalArgumentException(errorMessage);

        } else {

            try {
                // Verify that the call properties has a called party set
                CallPropertiesUtility.assertCalledPartySetCorrectly(callProperties);
            } catch (IllegalArgumentException e) {
                errorMessage = "Call properties when creating outbound call " +
                               "is not set correctly: " + e.getMessage() +
                               " No new call is created.";
                if (log.isInfoEnabled()) log.info(errorMessage, e);

                // Report that a new outbound call is being rejected
                ErrorEvent errorEvent = new ErrorEvent(
                        null, CallDirection.OUTBOUND, errorMessage, false);
                fireEvent(eventDispatcher, errorEvent);

                throw e;
            }

            try {
                // Verify that the call properties has a calling party set
                CallPropertiesUtility.assertCallingPartySetCorrectly(callProperties);
            } catch (IllegalArgumentException e) {
                CallingParty callingParty = new CallingParty();
                String number = ConfigurationReader.getInstance().getConfig().getOutboundCallCallingParty();

                if (number == null || number.equals("")) {
                    errorMessage = "Call properties when creating outbound call " +
                            "is not set correctly: " + e.getMessage() +
                            " No default Calling party found in configuration. No new call is created.";
                    if (log.isInfoEnabled())
                        log.info(errorMessage, e);

                    // Report that a new outbound call is being rejected
                    ErrorEvent errorEvent = new ErrorEvent(
                            null, CallDirection.OUTBOUND, errorMessage, false);
                    fireEvent(eventDispatcher, errorEvent);

                    throw e;

                } else {
                    callingParty.setTelephoneNumber(number);
                    if (log.isInfoEnabled())
                        log.info("No calling party was set in call properties when creating " +
                                "outbound call, using configured value: " + callingParty);
                    callProperties.setCallingParty(callingParty);
                }
            }

            try {
                // Verify that the call properties has a valid port
                CallPropertiesUtility.assertPortSetCorrectly(callProperties);
            } catch (IllegalArgumentException e) {
                errorMessage = "Call properties when creating outbound call " +
                        "is not set correctly: " + e.getMessage() +
                        ". No new call is created.";
                if (log.isInfoEnabled())
                    log.info(errorMessage, e);

                // Report that a new outbound call is being rejected
                ErrorEvent errorEvent = new ErrorEvent(
                        null, CallDirection.OUTBOUND, errorMessage, false);
                fireEvent(eventDispatcher, errorEvent);
                throw e;
            }

            try {
                // Verify that the call properties has a maxCallDurationBeforeConnected value set
                CallPropertiesUtility.assertMaxCallDurationBeforeConnected(callProperties);
            } catch (IllegalArgumentException e) {
                int connectTimer =
                        ConfigurationReader.getInstance().getConfig().getOutboundCallConnectTimer();
                if (log.isInfoEnabled())
                    log.info("No max duration before connected value was set in call " +
                            "properties when creating outbound call, " +
                            "using configured value: " + connectTimer);
                callProperties.setMaxDurationBeforeConnected(connectTimer);
            }

            try {

                call = CallFactory.createOutboundCall(
                        callProperties, eventDispatcher, session,
                        CMUtils.getInstance().getSipStackWrapper().
                        getNewCallId().getCallId());

                // NOTE: this call creation is not queued as an event in
                // CallManagerController but handled in a separate method
                // (handleOutboundCall).
                // It is done this way in order to make sure that the decision
                // whether to accept or reject the call has been made before
                // createCall returns.
                CMUtils.getInstance().getCmController().handleOutboundCall(call);

            } catch (Exception e) {
                errorMessage = "Internal error occurred when creating new " +
                               "outbound call. No new call is created.";
                if (log.isInfoEnabled()) log.info(errorMessage, e);
                throw new RuntimeException(errorMessage);
            }
            return call;
        }
    }


    public void join(Call firstCall, Call secondCall,
                     IEventDispatcher eventDispatcher) {

        if (log.isInfoEnabled()) log.info("Request to join calls received. FirstCall=<" +
                                          firstCall + ">, SecondCall=<" + secondCall + ">");

        if ((firstCall == null) || (secondCall == null)) {
            if (log.isInfoEnabled()) log.info("Could not join calls since at least one call is null. " +
                                              "FirstCall=<" + firstCall + ">, SecondCall=<" + secondCall + ">");
            fireEvent(eventDispatcher,
                    new JoinErrorEvent(firstCall, secondCall, "Call is null."));
        } else {
            CMUtils.getInstance().getCmController().queueEvent(
                    new JoinRequestEvent(firstCall, secondCall, eventDispatcher));
        }
    }

    public void unjoin(Call firstCall, Call secondCall,
                       IEventDispatcher eventDispatcher) {

        if (log.isInfoEnabled()) log.info("Request to unjoin calls received. FirstCall=<" +
                                          firstCall + ">, SecondCall=<" + secondCall + ">");

        if ((firstCall == null) || (secondCall == null)) {
            if (log.isInfoEnabled()) log.info("Could not unjoin calls since at least one call is null. " +
                                              "FirstCall=<" + firstCall + ">, SecondCall=<" + secondCall + ">");
            fireEvent(eventDispatcher,
                    new UnjoinErrorEvent(firstCall, secondCall, "Call is null."));
        } else {
            CMUtils.getInstance().getCmController().queueEvent(
                    new UnjoinRequestEvent(firstCall, secondCall, eventDispatcher));
        }
    }




    public void sendSipMessage(String method,  IEventDispatcher eventDispatcher,
                               ISession session,
                               Collection<NamedValue<String,String>> parameters)
            throws IllegalArgumentException {

        // Assert that eventDispatcher is not null
        if (eventDispatcher == null) {
             String msg = "Error in sendSipMessage. Event dispatcher is null";
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        }

        // Put all parameters in a map. Parameter names are converted to lowercase
        // Assert that no parameter names or values are null.
        Map<String,String> paramMap = new HashMap<String,String>();
        for (NamedValue<String,String> nv : parameters) {

            if (nv.getName() == null || nv.getValue() == null) {
                String msg = "Error in sendSipMessage. Parameter names/values may not be null";
                log.warn(msg);
                throw new IllegalArgumentException(msg);
            }
            paramMap.put(nv.getName().toLowerCase(), nv.getValue());

        }

        // Handle method
        if (CallManager.METHOD_MWI.equals(method)) {
            OutboundNotification notification =
                    new OutboundNotification(method, paramMap, eventDispatcher,
                            session);

            notification.doNotify();

        } else {
            String msg = "Error in sendSipMessage. Unknown method: " + method;
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        }

    }

    //===================== SipListener methods =======================

    /**
     * This method is called when a SIP request is received by the SIP stack.
     * Since request events can take time to process, a
     * {@link com.mobeon.masp.callmanager.sip.events.SipRequestEvent} is
     * created and queued in the event queue.
     * @param requestEvent The received SIP request event.
     */
    public void processRequest(RequestEvent requestEvent) {
        
        Object perf = null;
        
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Sip.Req");
                
            }

            if (!initiated.get()) {
                log.warn("SIP request received during initialization. " +
                "It is discarded.");
                return;
            }

            SipRequestEvent sipRequestEvent;

            try {
                sipRequestEvent = new SipRequestEventImpl(requestEvent);
            } catch (IllegalArgumentException e) {
                log.error("Received invalid SIP request. It is discarded.");
                return;
            }

            if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getMethod() + " request is received.");

            
            if(log.isDebugEnabled() && !sipRequestEvent.getMethod().equals(Request.OPTIONS))
            {
                try{
                    log.debug("SIP Call id: "+sipRequestEvent.getSipMessage().getCallId()+" SIP:  "+sipRequestEvent.getMethod()+ " request is received.");
                }
                catch(Exception e)  {}
            }
            if (log.isDebugEnabled()) {
                log.debug("Request received: \n" + requestEvent.getRequest());
            }

            sipRequestEvent.enterCheckPoint("MAS.CM.In.SIPReqEvent.QControl");

            CMUtils.getInstance().getCmController().queueEvent(sipRequestEvent);
            
        } finally {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }

    /**
     * This method is called when a SIP response is received by the SIP stack.
     * Since response events can take time to process, a
     * {@link com.mobeon.masp.callmanager.sip.events.SipResponseEvent}
     * is created and queued in the event queue.
     * @param responseEvent The received SIP response event.
     */
    public void processResponse(ResponseEvent responseEvent) {

        if (!initiated.get()) {
            log.warn("SIP response received during initialization. " +
                    "It is discarded.");
            return;
        }

        SipResponseEvent sipResponseEvent;

        try {
            sipResponseEvent =
                    SipResponseEvent.createSipResponseEvent(responseEvent);

        } catch (IllegalArgumentException e) {
            log.error("Received invalid SIP response. It is discarded.");
            return;
        }

        if (log.isInfoEnabled()) log.info("SIP " + sipResponseEvent.getResponseCode() +
                                          " response is received for " + sipResponseEvent.getMethod() +
                                          " request.");

        
        if(log.isDebugEnabled())
        {
            try{
            log.debug("SIP Call id: "+sipResponseEvent.getSipMessage().getCallId()+" SIP:  "+sipResponseEvent.getResponseCode()+" response is received for SIP:" + sipResponseEvent.getMethod() +
                    " request.");
            }
            catch(Exception e)  {}
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Response received: \n" + responseEvent.getResponse());
        }

        CMUtils.getInstance().getCmController().queueEvent(sipResponseEvent);
    }

    /**
     * This method is called when a SIP timeout event is generated by the SIP
     * stack. Since timeout events can take time to process, a
     * {@link com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent}
     * is created and queued in the event queue.
     * @param timeoutEvent The generated SIP timeout event.
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {

        if (!initiated.get()) {
            log.warn("Timeout for SIP request received during initialization. " +
                    "It is ignored.");
            return;
        }

        SipTimeoutEvent sipTimeoutEvent;

        try {
            sipTimeoutEvent = new SipTimeoutEvent(timeoutEvent);
        } catch (IllegalArgumentException e) {
            log.error("Timeout of SIP request is invalid. It is discarded.");
            return;
        }

        if (log.isInfoEnabled()) log.info("Timeout occurred for SIP " +
                                          sipTimeoutEvent.getMethod() + " request.");

        if(log.isDebugEnabled())
        {
            try{
                log.debug("SIP Call id: "+sipTimeoutEvent.getSipMessage().getCallId()+" SIP timeout for:  "+sipTimeoutEvent.getMethod() + " request.");
            }
            catch(Exception e)  {}

        }

        
        CMUtils.getInstance().getCmController().queueEvent(sipTimeoutEvent);
    }

    public void processIOException(IOExceptionEvent ioExceptionEvent) {
        String warning = "IOException occurred when sending SIP message. ";
        if (ioExceptionEvent != null)
            warning += "<Remote host = " + ioExceptionEvent.getHost() +
                    ">, <Remote port = " + ioExceptionEvent.getPort() +
                    ">, <Transport protocol = " +
                    ioExceptionEvent.getTransport() + ">";
        log.warn(warning);
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        if (log.isDebugEnabled())
            log.debug("A TransactionTerminatedEvent is received and ignored.");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        if (log.isDebugEnabled())
            log.debug("A DialogTerminatedEvent is received and ignored.");
    }

    //===================== EventReceiver methods =======================


    /**
     * This method is used to receive session-related events fired within the
     * event dispatcher stored in the Call Manager.
     * Call Manager ignores all events of this type.
     * @param event The session related event.
     */
    public void doEvent(Event event) {
        // Do nothing
    }

    /**
     * This method is used to receive global events fired by any event
     * dispatcher in the system.
     * <p>
     * Call Manager is only interested in the {@link ConfigurationChanged}
     * event which causes Call Manager to reload the configuration.
     * If the event is of type {@link ConfigurationChanged}, a thread is
     * allocated from a thread-pool to handle the configuration reload and this
     * method returns immediately after that.
     * @param event The global event.
     */
    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            ExecutorServiceManager.getInstance().
                    getExecutorService(CallManagerImpl.class).
                    execute(new ConfigurationReloader());
        }
    }



    //===================== Private methods =======================

    /**
     * Fires an event to the event dispatcher.
     * 
     * @param eventDispatcher The event dispatcher
     * @param event The fired event
     */
    private void fireEvent(IEventDispatcher eventDispatcher, Event event) {
        if (log.isDebugEnabled())
            log.debug("Firing event: <EventDispatcher = " +
                    eventDispatcher + ">, <Event = " + event + ">");

        if (eventDispatcher != null) {
            try {
                if (log.isInfoEnabled()) log.info("Fired event: " + event);
                eventDispatcher.fireEvent(event);
            } catch (Exception e) {
                log.error(
                        "Exception occurred when firing event: " + e.getMessage(),
                        e);
            }
        }
        updateStatistics(event);
    }

    private static void updateStatistics(Event event) {
        List<CallEventListener> callEventListeners = CMUtils.getInstance().getCallEventListeners();
        for (CallEventListener callEventListener : callEventListeners) {
            callEventListener.processCallEvent(event);
        }
    }

    private void checkSetters() {
        if ((applicationManagement == null) ||
                (configuration == null) ||
                (supervision == null) ||
                (eventDispatcher == null) ||
                (streamFactory == null) ||
                (sessionFactory == null) ||
                (callManagerLicensing == null)) {
            throw new IllegalStateException("Init was called prior to " +
                    "setting necessary fields. ApplicationManagement: " +
                    applicationManagement +
                    ", Configuration: " + configuration +
                    ", Supervision: " + supervision +
                    ", EventDispatcher: " + eventDispatcher +
                    ", StreamFactory: " + streamFactory +
                    ", SessionFactory: " + sessionFactory +
                    ", CallManagerLicensing: " + callManagerLicensing);
        }
    }

    private void configureCM() throws ServiceEnablerException {
        try {
            ConfigurationReader.getInstance().setInitialConfiguration(
                    configuration);
            ConfigurationReader.getInstance().update();
        } catch (Exception e) {
            throw new ServiceEnablerException("Could not configure Call Manager.");
        }
    }

    private void initializeCallManager() throws ServiceEnablerException {

        CallManagerControllerImpl cmController = new CallManagerControllerImpl();

        try {
            CMUtils.getInstance().setServiceEnablerInfo(
                    supervision.getServiceEnablerStatistics(cmController));
        } catch (Exception e) {
            throw new ServiceEnablerException(
                    "Could not retrieve Service Enabler Info.", e);
        }

        CMUtils.getInstance().setCallManager(this);
        CMUtils.getInstance().setSessionFactory(sessionFactory);
        CMUtils.getInstance().setCallDispatcher(new CallDispatcher());
        CMUtils.getInstance().setRegistrationDispatcher(new RegistrationDispatcher());
        CMUtils.getInstance().setNotificationDispatcher(new NotificationDispatcher());
        CMUtils.getInstance().setSipRequestDispatcher(new SipRequestDispatcher());
        CMUtils.getInstance().setSipResponseDispatcher(new SipResponseDispatcher());
        CMUtils.getInstance().setSipTimeoutDispatcher(new SipTimeoutDispatcher());
        CMUtils.getInstance().setRemotePartyController(new RemotePartyControllerImpl());
        CMUtils.getInstance().setStatisticsCollector(new StatisticsCollector());
        CMUtils.getInstance().setCallManagerController(cmController);
        
        eventDispatcher.addEventReceiver(this);
    }

    private void initializeEnvironment() {
        CMUtils.getInstance().setApplicationManagement(applicationManagement);
        CMUtils.getInstance().setStreamFactory(streamFactory);
        CMUtils.getInstance().setSupervision(supervision);
    }

    private void initializeSipRelated() throws ServiceEnablerException {

        try {
            sipStackWrapper = new SipStackWrapperImpl(
                    this,
                    CMUtils.getInstance().getLocalHost(),
                    CMUtils.getInstance().getLocalPort(),
                    ConfigurationReader.getInstance().getConfig().getSipTimers(),
                    this.maxServerTransactions);
            
            sipStackWrapper.init();

            CMUtils.getInstance().setSipStackWrapper(sipStackWrapper);
            CMUtils.getInstance().setSipMessageSender(
                    sipStackWrapper.getSipMessageSender());
            CMUtils.getInstance().setSipHeaderFactory(
                    sipStackWrapper.getSipHeaderFactory());
            CMUtils.getInstance().setSipRequestFactory(
                    sipStackWrapper.getSipRequestFactory());
            CMUtils.getInstance().setSipRequestValidator(
                    sipStackWrapper.getSipRequestValidator());
            CMUtils.getInstance().setSipResponseFactory(
                    sipStackWrapper.getSipResponseFactory());
            
            
            // Start a thread for auditing dialogs and transactions in the SIP stack
            sipStackAuditor = new SipStackAuditor(sipStackWrapper);
            sipStackAuditor.start();
            
        } catch (Exception e) {
            throw new ServiceEnablerException(
                    "Could not create SIP stack wrapper.", e);
        }
    }

    /**
     * This method is used to reload the Call Manager configuration.
     * It re-reads the configuration using
     * {@link ConfigurationReader#update()} and signals to the remote party
     * controller to re-initialize the current set of SSP clients using
     * {@link RemotePartyController#reInitialize()}.
     * <p>
     * If an error occured while re-reading the configuration, an error log is
     * generated.
     */
    private void reloadConfiguration() {
        try {
            ConfigurationReader.getInstance().update();
            CMUtils.getInstance().getRemotePartyController().reInitialize();
            CMUtils.getInstance().getCallManagerLicensing().refresh();
        } catch (Exception e) {
            log.error("Could not reload configuration for Call Manager due " +
                    "to the following error: " + e.getMessage(), e);
        }
    }

    /**
     * This class is used only to simplify the code. It is used to execute a
     * command from a thread pool. Implements Runnable.
     *
     * @author Malin Flodin
     */
    private final class ConfigurationReloader implements Runnable {
        public void run() {
            // Clearing session info from logger this is run in a thread picked
            // from a pool and has no session relation.
            log.clearSessionInfo();
            reloadConfiguration();
        }
    }
}

