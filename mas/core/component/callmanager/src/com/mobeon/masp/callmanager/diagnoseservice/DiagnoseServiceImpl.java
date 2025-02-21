/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.diagnoseservice;

import com.mobeon.masp.operateandmaintainmanager.DiagnoseService;
import com.mobeon.masp.operateandmaintainmanager.Status;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.callmanager.sip.SipStackWrapperImpl;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.configuration.SipTimers;

import javax.sip.SipListener;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.InvalidArgumentException;
import javax.sip.IOExceptionEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.DialogTerminatedEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.text.ParseException;

/**
 * TODO: Phase 2! Document
 * @author Malin Flodin
 */
public class DiagnoseServiceImpl implements SipListener, DiagnoseService {

    private static final HashMap<String, Status> STATUS_MAP =
            new HashMap<String, Status>();

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // A linked blocking queue that contains the result from the service request
    private final LinkedBlockingQueue<Status> statusQueue =
            new LinkedBlockingQueue<Status>();

    // Information about runtime environment
    private IConfiguration configuration;

    // Sip related fields
    private SipStackWrapperImpl sipStackWrapper;
    private SipMessageSender sipMessageSender;
    private SipHeaderFactory sipHeaderFactory;
    private SipRequestFactory sipRequestFactory;

    static {
        STATUS_MAP.put("up", Status.UP);
        STATUS_MAP.put("down", Status.DOWN);
        STATUS_MAP.put("impaired", Status.IMPAIRED);
    }

    public DiagnoseServiceImpl() {
    }

    public synchronized void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    public synchronized void init() throws ServiceEnablerException {

        // Check that all setters have been called prior to this method.
        checkSetters();

        // Read configuration
        configureDS();

        // Initialize SIP stack
        initializeSipRelated();
    }

    /**
     * Shall be used when the CallManager diagnose service no longer is used.
     * Deletes allocated resources such as the SIP stack.
     */
    public synchronized void delete() throws ServiceEnablerException {
        try {
            sipStackWrapper.delete();
        } catch (ObjectInUseException e) {
            String message = "ObjectInUseException was received when " +
                    "deleting SIP stack providers.";
            throw new ServiceEnablerException(message, e);
        }
    }

    //=================== SipListener methods =======================

    public void processRequest(RequestEvent requestEvent) {
        String method = "unknown";
        if ((requestEvent != null) && (requestEvent.getRequest() != null)) {
            method = requestEvent.getRequest().getMethod();
        }
        log.error("SIP " + method + " request was received and is ignored.");
    }

    public void processResponse(ResponseEvent responseEvent) {
        if (responseEvent == null) {
            log.error("Unknown SIP response received. It is ignored.");
        } else {
            Response response = responseEvent.getResponse();

            try {
                SipResponseEvent sipResponseEvent =
                        SipResponseEvent.createSipResponseEvent(responseEvent);
                Integer responseCode =
                        sipResponseEvent.retrieveResponseCodeForMethod(Request.OPTIONS);

                if (responseCode == null) {

                    log.error("SIP response " +
                            sipResponseEvent.getResponseCode() +
                            " received for method " +
                            sipResponseEvent.getMethod() +
                            ". It is ignored. Response: " +
                            sipResponseEvent.getResponse());

                    log.error("Unknown SIP response received. It is ignored.");
                } else {
                    if (log.isDebugEnabled())
                        log.debug("SIP " + responseCode +
                                " response received for OPTIONS request.");
                    try {
                        statusQueue.put(getStatus(sipResponseEvent.
                                getSipMessage().getOperationalStatus()));
                    } catch (InterruptedException e) {
                        log.error(
                                "Interrupted while putting experienced " +
                                "operational status in queue.");
                    }
                }


            } catch (IllegalArgumentException e) {
                log.error("SIP " + response.getStatusCode() +
                        " response is invalid. It is discarded.");
            }

        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        log.warn("SIP timeout event or transaction error occurred " +
                "for OPTIONS request.");
        try {
            statusQueue.put(Status.DOWN);
        } catch (InterruptedException e) {
            log.error(
                    "Interrupted while putting unknown " +
                    "operational status in queue.");
        }
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


    //=================== Diagnose Service methods =======================

    public Status serviceRequest(ServiceInstance si)
            throws IllegalArgumentException {

        Status status = Status.UNKNOWN;

        validateServiceInstance(si);

        try {
            sendOptionsRequest(si.getHostName(), si.getPort());
            status = waitForOptionsResponse();
        } catch (Exception e) {
            log.error("Could not send a SIP OPTIONS request used to " +
                    "diagnose SIP service on " + si.getHostName() +
                    ":" + si.getPort(), e);
        }

        return status;
    }

    //===================== Private methods =======================

    private void checkSetters() {
        if (configuration == null) {
            throw new IllegalStateException(
                    "Init was called prior to setting necessary fields. " +
                            "Configuration: " + configuration);
        }
    }

    private void configureDS() throws ServiceEnablerException {
        try {
            DiagnoseServiceConfiguration.getInstance().
                    setInitialConfiguration(configuration);
            DiagnoseServiceConfiguration.getInstance().update();
        } catch (Exception e) {
            throw new ServiceEnablerException(
                    "Could not configure Call Managers diagnose service.");
        }
    }

    private Status getStatus(String statusString) {
        Status status = STATUS_MAP.get(statusString);
        return status == null ? Status.UNKNOWN : status;
    }

    /**
     * Used for basic testing only
     */
    public SipHeaderFactory getSipHeaderFactory() {
        return sipHeaderFactory;
    }

    private void initializeSipRelated() throws ServiceEnablerException {

        try {
            sipStackWrapper = new SipStackWrapperImpl(
                    this,
                    DiagnoseServiceConfiguration.getInstance().getHostName(),
                    DiagnoseServiceConfiguration.getInstance().getPort(),
                    SipTimers.getDefaultSipTimers());
            sipStackWrapper.init();

            sipMessageSender = sipStackWrapper.getSipMessageSender();
            sipHeaderFactory = sipStackWrapper.getSipHeaderFactory();
            sipRequestFactory = sipStackWrapper.getSipRequestFactory();
        } catch (Exception e) {
            throw new ServiceEnablerException(
                    "Could not create SIP stack wrapper.", e);
        }
    }

    private void sendOptionsRequest(String host, int port)
            throws SipException, InvalidArgumentException, ParseException {

        SipRequest sipRequest = sipRequestFactory.createOptionsRequest(
                DiagnoseServiceConfiguration.getInstance().getHostName(),
                DiagnoseServiceConfiguration.getInstance().getPort(),
                host, port, 1);

        if (log.isDebugEnabled())
            log.debug("SIP OPTIONS request created.");

        sipMessageSender.sendRequest(sipRequest);

        if (log.isDebugEnabled())
            log.debug("SIP OPTIONS request sent.");
    }

    private void validateServiceInstance(ServiceInstance si)
            throws IllegalArgumentException {
        if (si == null) {
            throw new IllegalArgumentException("Service Instance is null.");
        }

        String host = si.getHostName();
        int port = si.getPort();

        if (host == null) {
            throw new IllegalArgumentException(
                    "Service Instance does not contain a host name");
        }

        if (port <= 0) {
            throw new IllegalArgumentException(
                    "Service Instance does not contain a valid port number.");
        }
    }

    private Status waitForOptionsResponse() {
        Status status = Status.UNKNOWN;
        try {
            status = statusQueue.take();
        } catch (InterruptedException e) {
            log.error(
                    "Interrupted while retrieving experienced " +
                    "operational status from queue.");
        }
        return status;
    }
}
