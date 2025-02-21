/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.SipStackWrapper;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponseFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequestValidator;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.callhandling.CallDispatcher;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.registration.RegistrationDispatcher;
import com.mobeon.masp.callmanager.statistics.StatisticsCollector;
import com.mobeon.masp.callmanager.notification.NotificationDispatcher;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe after initialization has been done.
 *
 * @author Malin Flodin
 */
public class CMUtils {

    private static final CMUtils INSTANCE = new CMUtils();

    // Inidicates if CMUtils has been initialized or not. Setters may not be
    // called after initialization, and getters may not be called before.
    private AtomicBoolean initiated = new AtomicBoolean(false);

    // Information about runtime environment
    private IApplicationManagment applicationManagement;
    private IStreamFactory streamFactory;
    private Supervision supervision;

    // Call Manager related utilities
    private CallManagerImpl callManager;
    private CallDispatcher callDispatcher;
    private RegistrationDispatcher registrationDispatcher;
    private NotificationDispatcher notificationDispatcher;
    private SipRequestDispatcher sipRequestDispatcher;
    private SipResponseDispatcher sipResponseDispatcher;
    private SipTimeoutDispatcher sipTimeoutDispatcher;
    private CallManagerControllerImpl cmController;
    private RemotePartyController remotePartyController;
    private ServiceEnablerInfo serviceEnablerInfo;
    private List<CallEventListener> callEventListeners =
            new ArrayList<CallEventListener>();
    private StatisticsCollector statisticsCollector;

    // SIP related utilities
    private SipStackWrapper sipStackWrapper;
    private SipMessageSender sipMessageSender;
    private SipHeaderFactory sipHeaderFactory;
    private SipResponseFactory sipResponseFactory;
    private SipRequestFactory sipRequestFactory;
    private SipRequestValidator sipRequestValidator;

    // Service parameters
    private String serviceName;
    private String localHost;
    private int localPort;
    private String protocol;
    private String version;
    private CallManagerLicensing callManagerLicensing;
    private ISessionFactory sessionFactory;


    /**
     * @return The single CMUtils instance.
     */
    public static CMUtils getInstance() {
        return INSTANCE;
    }


    /**
     * Creates the single CMUtils instance.
     */
    private CMUtils() {
    }

    /**
     * Initializes CMUtils. Requires that all setters have been called to set
     * required data.
     * <p>
     * This method should only be called once when the Call Manager component
     * is initiated.
     */
    public synchronized void init() throws ServiceEnablerException {
        // TODO: Phase 2! Add check if all setters has been called before this.
        initiated.set(true);
    }

    public synchronized void delete() {
        initiated.set(false);
        callEventListeners.clear();
    }

    //========== Public Getters ============

    public IApplicationManagment getApplicationManagement() {
        return applicationManagement;
    }

    public List<CallEventListener> getCallEventListeners() {
        return callEventListeners;
    }

    public CallManagerImpl getCallManager() {
        return callManager;
    }

    public CallManagerControllerImpl getCmController() {
        return cmController;
    }

    public SipHeaderFactory getSipHeaderFactory() {
        return sipHeaderFactory;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public ServiceEnablerInfo getServiceEnablerInfo() {
        return serviceEnablerInfo;
    }

    public String getServiceName() {
        return serviceName;
    }

    public SipResponseFactory getSipResponseFactory() {
        return sipResponseFactory;
    }

    public SipRequestFactory getSipRequestFactory() {
        return sipRequestFactory;
    }

    public SipRequestValidator getSipRequestValidator() {
        return sipRequestValidator;
    }

    public SipMessageSender getSipMessageSender() {
        return sipMessageSender;
    }

    public SipStackWrapper getSipStackWrapper() {
        return sipStackWrapper;
    }

    public StatisticsCollector getStatisticsCollector() {
        return statisticsCollector;
    }

    public IStreamFactory getStreamFactory() {
        return streamFactory;
    }

    public Supervision getSupervision() {
        return supervision;
    }

    public String getVersion() {
        return version;
    }
    
    public CallManagerLicensing getCallManagerLicensing() {
        return callManagerLicensing;
    }

    public CallDispatcher getCallDispatcher() {
        return callDispatcher;
    }

    public RegistrationDispatcher getRegistrationDispatcher() {
        return registrationDispatcher;
    }

    public NotificationDispatcher getNotificationDispatcher() {
        return notificationDispatcher;
    }

    public RemotePartyController getRemotePartyController() {
        return remotePartyController;
    }

    public SipRequestDispatcher getSipRequestDispatcher() {
        return sipRequestDispatcher;
    }

    public SipResponseDispatcher getSipResponseDispatcher() {
        return sipResponseDispatcher;
    }

    public SipTimeoutDispatcher getSipTimeoutDispatcher() {
        return sipTimeoutDispatcher;
    }

    //========== Public Setters ============

    public void addCallEventListener(CallEventListener callEventListener) {
        callEventListeners.add(callEventListener);
    }

    public synchronized void setApplicationManagement(
            IApplicationManagment applicationManagement) {
        this.applicationManagement = applicationManagement;
    }

    public synchronized void setCallDispatcher(CallDispatcher callDispatcher) {
        this.callDispatcher = callDispatcher;
    }

    public synchronized void setCallManager(CallManagerImpl callManager) {
        this.callManager = callManager;
    }

    public synchronized ISessionFactory getSessionFactory(){
        return sessionFactory;
    }

    public void setSessionFactory(ISessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public synchronized void setCallManagerController(
            CallManagerControllerImpl cmController) {
        this.cmController = cmController;
    }

    public synchronized void setLocalHost(String localHost) {
        this.localHost = localHost;
    }
    
    public synchronized void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public synchronized void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public synchronized void setRegistrationDispatcher(
            RegistrationDispatcher registrationDispatcher) {
        this.registrationDispatcher = registrationDispatcher;
    }

    public synchronized void setNotificationDispatcher(
            NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    public synchronized void setRemotePartyController(
            RemotePartyController remotePartyController) {
        this.remotePartyController = remotePartyController;
    }

    public synchronized void setServiceEnablerInfo(
            ServiceEnablerInfo serviceEnablerInfo) {
        this.serviceEnablerInfo = serviceEnablerInfo;
    }

    public synchronized void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public synchronized void setSipHeaderFactory(SipHeaderFactory sipHeaderFactory) {
        this.sipHeaderFactory = sipHeaderFactory;
    }

    public synchronized void setSipRequestDispatcher(
            SipRequestDispatcher sipRequestDispatcher) {
        this.sipRequestDispatcher = sipRequestDispatcher;
    }

    public synchronized void setSipRequestFactory(
            SipRequestFactory sipRequestFactory) {
        this.sipRequestFactory = sipRequestFactory;
    }

    public synchronized void setSipRequestValidator(
            SipRequestValidator sipRequestValidator) {
        this.sipRequestValidator = sipRequestValidator;
    }

    public synchronized void setSipResponseDispatcher(
            SipResponseDispatcher sipResponseDispatcher) {
        this.sipResponseDispatcher = sipResponseDispatcher;
    }

    public synchronized void setSipResponseFactory(
            SipResponseFactory sipResponseFactory) {
        this.sipResponseFactory = sipResponseFactory;
    }

    public synchronized void setSipMessageSender(
            SipMessageSender sipMessageSender) {
        this.sipMessageSender = sipMessageSender;
    }

    public synchronized void setSipStackWrapper(
            SipStackWrapper sipStackWrapper) {
        this.sipStackWrapper = sipStackWrapper;
    }

    public synchronized void setSipTimeoutDispatcher(
            SipTimeoutDispatcher sipTimeoutDispatcher) {
        this.sipTimeoutDispatcher = sipTimeoutDispatcher;
    }

    public synchronized void setStatisticsCollector(
            StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    public synchronized void setStreamFactory(IStreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    public synchronized void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    public synchronized void setVersion(String version) {
        this.version = version;
    }
    
    public synchronized void setCallManagerLicensing(CallManagerLicensing callManagerLicensing) {
        this.callManagerLicensing = callManagerLicensing;
    }
    

}
