/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallCommandEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.header.PEarlyMediaHeader;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia.PEarlyMediaTypes;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.ProxyEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.RedirectEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.NegotiateEarlyMediaTypesEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingInboundState.AlertingSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedInboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorInboundState.ErrorSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedInboundState.FailedSubState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.*;
import com.mobeon.masp.callmanager.sdp.SdpSessionDescriptionFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.SipUtils;
import com.mobeon.masp.callmanager.configuration.ConfigConstants;
import com.mobeon.masp.callmanager.configuration.ReliableResponseUsage;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;

import gov.nist.javax.sip.header.ExtensionHeaderImpl;
import gov.nist.javax.sip.header.SIPHeader;

import javax.sip.Dialog;
import javax.sip.message.Response;

import java.util.Date;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an inbound call.
 * It basically consists of a state machine to which it relays all incoming SIP
 * requests/responses and call commands from the system.
 *
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class InboundCallImpl extends CallImpl implements InboundCallInternal {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Inbound states
    private final IdleInboundState idleState = new IdleInboundState(this);
    private final AlertingNewCallInboundState alertingNewCallState = new AlertingNewCallInboundState(this); 
    private final AlertingWaitForPrackInboundState alertingWaitForPrackState = new AlertingWaitForPrackInboundState(this);

    private final AlertingWaitForCallHoldInboundState alertingWaitForCallHoldState = new AlertingWaitForCallHoldInboundState(this);
    private final AlertingWaitForNewMediaInboundState alertingWaitForNewMediaState = new AlertingWaitForNewMediaInboundState(this);
    private final AlertingWaitForNewMediaAckInboundState alertingWaitForNewMediaAckState = new AlertingWaitForNewMediaAckInboundState(this);

    private final AlertingEarlyMediaInboundState alertingEarlyMediaState = new AlertingEarlyMediaInboundState(this);
    private final AlertingEarlyMediaInboundWaitForPrackState alertingEarlyMediaInboundWaitForPrackState = new AlertingEarlyMediaInboundWaitForPrackState(this);
    private final AlertingEarlyMediaWaitForPrackInboundState alertingEarlyMediaWaitForPrackState = new AlertingEarlyMediaWaitForPrackInboundState(this);

    private final AlertingSessionEstablishmentWaitForPrackInboundState alertingSessionEstablishmentWaitForPrackState = new AlertingSessionEstablishmentWaitForPrackInboundState(this);
    private final AlertingSessionEstablishmentInboundState alertingSessionEstablishmentState = new AlertingSessionEstablishmentInboundState(this);

    private final AlertingProxyingInboundState alertingProxyingState = new AlertingProxyingInboundState(this);

    private final AlertingAcceptingInboundState alertingAcceptingState = new AlertingAcceptingInboundState(this);
    private final ConnectedInboundState connectedState = new ConnectedInboundState(this);

    private final DisconnectedCompletedInboundState disconnectedCompletedState = new DisconnectedCompletedInboundState(this);
    private final DisconnectedLingeringByeInboundState disconnectedLingeringByeState = new DisconnectedLingeringByeInboundState(this);
    private final FailedWaitingForAckInboundState failedWaitingForAckState = new FailedWaitingForAckInboundState(this);
    private final FailedLingeringByeInboundState failedLingeringByeState = new FailedLingeringByeInboundState(this);
    private final FailedCompletedInboundState failedCompletedState = new FailedCompletedInboundState(this);
    private final ErrorCompletedInboundState errorCompletedState = new ErrorCompletedInboundState(this);
    private final ErrorLingeringByeInboundState errorLingeringByeState = new ErrorLingeringByeInboundState(this);

    // Call Information.

    /** The redirecting party of the inbound call. */
    private final AtomicReference<RedirectingParty> redirectingParty = new AtomicReference<RedirectingParty>();

    // Execution environment related
    private final AtomicReference<IApplicationManagment> applicationManagement = new AtomicReference<IApplicationManagment>();
    private final AtomicReference<String> defaultServiceName = new AtomicReference<String>();
    private final AtomicReference<String> sessionProtocol = new AtomicReference<String>();
    private final SessionMdcItems sessionMdcItems = new SessionMdcItems();

    // SIP/SDP related
    private final SipRequestEvent initialSipRequestEvent;

    // Call Timers
    private final CallTimerTask callNotAcceptedTimerTask = new CallTimerTask(this, CallTimerTask.Type.CALL_NOT_ACCEPTED);
    private final CallTimerTask expiresTimerTask = new CallTimerTask(this, CallTimerTask.Type.EXPIRES);
    private CallTimerTask redirectedRtpTimerTask;
    private CallTimerTask sessionProgressRetransmissionTimerTask;
    private CallTimerTask sessionEstablishmentTimerTask;

    // Misc
    private final AtomicBoolean playHasBeenLogged = new AtomicBoolean(false);
    private final AtomicBoolean acceptReceivedInWaitForPrack = new AtomicBoolean(false);
    private RemotePartyAddress uas = new RemotePartyAddress(null, 0);
    private final AtomicBoolean earlyMediaRequested = new AtomicBoolean(false);

    public InboundCallImpl(SdpSessionDescriptionFactory sdpFactory,
                           CallProperties.CallType callType,
                           Dialog dialog,
                           CallParameters callParameters,
                           String defaultServiceName,
                           IApplicationManagment applicationManagement,
                           ISession session,
                           String sessionProtocol,
                           SipRequestEvent sipRequestEvent,
                           CallManagerConfiguration config)
            throws IllegalArgumentException {
        super(config);
        
        // Initiate the call in IDLE state
        setCurrentState(idleState);

        // Set SIP/SDP stack related
        setSessionDescriptionFactory(sdpFactory);
        setDialog(dialog);
        initialSipRequestEvent = sipRequestEvent;

        // Set call parameters
        setCallType(callType);
        setCalledParty(callParameters.getCalledParty());
        setCallingParty(callParameters.getCallingParty());
        setRedirectingParty(callParameters.getRedirectingParty());

        // Set execution environment related
        setApplicationManagement(applicationManagement);
        setDefaultServiceName(defaultServiceName);
        setSessionProtocol(sessionProtocol);

        // Set session and session logging data
        // NOTE that call parameters must have been set first
        setSession(session);
        setSessionLoggingData();
    }



    public IApplicationManagment getApplicationManagement() {
        return applicationManagement.get();
    }

    public void setApplicationManagement(IApplicationManagment applicationManagement) {
        this.applicationManagement.set(applicationManagement);
    }

    public String getDefaultServiceName() {
        return defaultServiceName.get();
    }

    public void setDefaultServiceName(String defaultServiceName) {
        this.defaultServiceName.set(defaultServiceName);
    }

    public String getSessionProtocol() {
        return sessionProtocol.get();
    }

    public void setSessionProtocol(String sessionProtocol) {
        this.sessionProtocol.set(sessionProtocol);
    }

    public String toString() {
        return "Inbound call: <CallType = " + getCallType() +
                ">, <Calling Party = " + getCallingParty() +
                ">, <Called Party = " + getCalledParty() +
                ">, <Redirecting Party = " + getRedirectingParty() + ">";
    }

//===================== InboundCall methods start =======================

    public void accept() {
        AcceptEvent acceptEvent = new AcceptEvent();
        queueEvent(acceptEvent);
    }

    public void proxy(RemotePartyAddress uas) {
        ProxyEvent proxyEvent = new ProxyEvent(uas);
        queueEvent(proxyEvent);
    }

    public void reject(String rejectEventTypeName, String reason) {
        RejectEvent rejectEvent = new RejectEvent(RejectEvent.mapToRejectEventType(rejectEventTypeName), reason);
        queueEvent(rejectEvent);
    }

    public void negotiateEarlyMediaTypes() {
        NegotiateEarlyMediaTypesEvent event =
                new NegotiateEarlyMediaTypesEvent();
        queueEvent(event);
    }

    public void disconnect() {
        DisconnectEvent disconnectEvent = new DisconnectEvent();
        queueEvent(disconnectEvent);
    }
    
    
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode) {
        RedirectEvent redirectEvent = new RedirectEvent(destination,redirectCode);
        queueEvent(redirectEvent);        
    }

//===================== InboundCall methods end =======================


//================== InboundCallInternal methods start ====================

    public void setAcceptReceivedInWaitForPrack(boolean flag){
        acceptReceivedInWaitForPrack.set(flag);
    }

    public boolean isAcceptReceivedInWaitForPrack(){
        return acceptReceivedInWaitForPrack.get();
    }

    public void setEarlyMediaRequested(boolean earlyMediaRequested) {
        this.earlyMediaRequested.set(earlyMediaRequested);
    }

    public boolean isEarlyMediaRequested(){
        return earlyMediaRequested.get();
    }

    public void setUas(RemotePartyAddress uas){
        this.uas = uas;
    }

    public RemotePartyAddress getUas(){
        return uas;
    }

    public RedirectingParty getRedirectingParty() {
        return redirectingParty.get();
    }

    protected void setRedirectingParty(RedirectingParty redirectingParty) {
        this.redirectingParty.set(redirectingParty);
    }

    // Inbound call timers
    public void startNotAcceptedTimer() {
        getCallTimer().schedule(
                callNotAcceptedTimerTask,
                getConfig().getCallNotAcceptedTimer());

        if (log.isDebugEnabled())
            log.debug("Max Duration Before Accepted Timer has been scheduled to: " +
                    getConfig().getCallNotAcceptedTimer());
    }

    public void startExpiresTimer(SipRequestEvent sipRequestEvent) {
        long expiresTime =
                sipRequestEvent.getSipMessage().getExpireTimeFromExpiresHeader();
        if (expiresTime > 0) {
            expiresTime = expiresTime * 1000;
            getCallTimer().schedule(expiresTimerTask, expiresTime);

            if (log.isDebugEnabled())
                log.debug("Expires Timer has been scheduled for " +
                        expiresTime + " milli seconds.");
        }
    }

    public void startRedirectedRtpTimer() {
        redirectedRtpTimerTask =
            new CallTimerTask(this, CallTimerTask.Type.REDIRECTED_RTP);

        getCallTimer().schedule(
                redirectedRtpTimerTask,
                getConfig().getSupportForRedirectingRtpTimeout());

        if (log.isDebugEnabled())
            log.debug("Redirected Rtp Timer has been scheduled to: " +
                    getConfig().getSupportForRedirectingRtpTimeout());
    }

    public void cancelRedirectedRtpTimer() {
        if (redirectedRtpTimerTask != null)
            redirectedRtpTimerTask.cancel();
    }

    /**
     * This timer is used to send regular SIP 183 Session Progress while in early media
     * as described in RFC 3261 (section 13.3.1.1 Progress), in RFC 3262 (section 3), or
     * as configured.
     */
    public void startSessionProgressRetransmissionTimer() { 
        if(sessionProgressRetransmissionTimerTask == null) {
            
            int timer = getConfig().getSessionProgressRetransmissionTimer();
            
            // if timer == 0 means use default RFC values, else use configured value
            if (timer == 0) {
                // Regular Session Progress Retransmission do not include sdp, so they are reliable only when Required
                if(useReliableProvisionalResponses() == ReliableResponseUsage.YES) {
                    //Reliable Regular Session Progress Retransmission default to two and a half minute as per RFC3262 section 3
                    timer = 150000; 
                }
                else {
                    //Regular Session Progress Retransmission default to 60 seconds as per RFC3261 section 13.3.1.1
                    timer = 60000; 
                }
            } else {
                timer = timer * 1000;
                
                // Make sure value is at least 60 seconds since this timer is not handled in session establishment state (which lasts 30 seconds max)
                if (timer < 60000) {
                    timer = 60000; 
                }
            }
                
            sessionProgressRetransmissionTimerTask = 
                    new CallTimerTask(this, CallTimerTask.Type.SESSION_PROGRESS_RETRANSMISSION);
            
            getCallTimer().scheduleAtFixedRate(
                    sessionProgressRetransmissionTimerTask,
                    timer, timer);
            
            if (log.isDebugEnabled())
                log.debug("SIP 183 Session Progress Retransmission Timer has been scheduled to " + timer);
        }
    }
    
    public void cancelSessionProgressRetransmissionTimer() {
        if(sessionProgressRetransmissionTimerTask != null)
            sessionProgressRetransmissionTimerTask.cancel();

    }

    /**
     * This timer is used to limit the time Call Manager will wait on UAC to satisfy
     * session establishment conditions.  Namely preconditions and unicast.
     */
    public void startSessionEstablishmentTimer() { 
        if (sessionEstablishmentTimerTask == null) {
            int timer = getConfig().getSessionEstablishmentTimer();
            timer = timer * 1000;
            sessionEstablishmentTimerTask = new CallTimerTask(this, CallTimerTask.Type.SESSION_ESTABLISHMENT);
            getCallTimer().schedule(sessionEstablishmentTimerTask, timer);

            if (log.isDebugEnabled())
                log.debug("Session Establishment Timer has been scheduled in " + timer + " seconds");
        }
    }

    public void cancelSessionEstablishmentTimer() {
        if (sessionEstablishmentTimerTask != null)
            sessionEstablishmentTimerTask.cancel();
    }

    public boolean isPendingSdpACallHold() {
        return getPendingRemoteSdp().isSdpMediaDescriptionOnHold();
    }

    /**
     * Loads the application.
     * Retrieves the call dispatcher and sets call specific parameters in
     * the session data.
     * @throws NullPointerException
     *      A NullPointerException is thrown if the service could not be
     *      loaded or if the event dispatcher or session is null.
     */
    public synchronized void loadService() {

        IApplicationExecution applicationInstance = null;

        // First try to load service based on calling party user name or
        // telephone number

        // If a telephone number is available it is used, otherwise the user
        // name is used.
        String phoneNumber = getCalledParty().getTelephoneNumber();
        String user = getCalledParty().getSipUser();
        String serviceName;

        if (phoneNumber != null)
            serviceName = phoneNumber;
        else
            serviceName = user;

        if (serviceName != null) {
            if (log.isDebugEnabled())
                log.debug("Trying to load service based on called party: " +
                        serviceName);

            applicationInstance = getApplicationManagement().load(serviceName);
        }

        // If a service has not yet been loaded, use the default service name
        // if not null
        String defaultServiceName = getDefaultServiceName();
        if (applicationInstance == null) {
            if (defaultServiceName != null) {
                if (log.isDebugEnabled())
                    log.debug("Trying to load service based on default service name: " +
                            defaultServiceName);

                applicationInstance =
                        getApplicationManagement().load(defaultServiceName);
            } else {
                throw new NullPointerException("Default service name is null.");
            }
        }

        if (applicationInstance == null) {
            throw new NullPointerException(
                    "Application Execution instance is null when loading service.");
        }

        applicationInstance.setSession(session);
        // Retrieve the event dispatcher.
        IEventDispatcher eventDispatcher =
                applicationInstance.getEventDispatcher();
        if (eventDispatcher == null) {
            throw new NullPointerException(
                    "Event dispatcher is null in application execution.");
        }
        setEventDispatcher(eventDispatcher);

        if (session == null) {
            throw new NullPointerException("Session is null.");
        }
        session.setMdcItems(sessionMdcItems);
        session.setData(ISession.SESSION_INITIATOR, getSessionProtocol());

        // Add all the headers in the session
        ListIterator headersIterator = initialSipRequestEvent.getRequest().getHeaderNames();
        String headerName;
        while (headersIterator.hasNext()) {
            headerName = (String)headersIterator.next();
            ListIterator headerIterator = initialSipRequestEvent.getRequest().getHeaders(headerName);
                      
            // If different headers have similar name but conflict with the mapping (eg: History-Info and History_Info in the same INVITE), 
            // the second one to be added to the session object will overwrite the first.
            // Currently, there are no registered SIP headers using '_' (2012-05-16 http://www.iana.org/assignments/sip-parameters) 
            headerName = SipUtils.mapInternalSipHeaderName(headerName);
            
            int i = 1;
            while (headerIterator.hasNext()) {
                SIPHeader header = (SIPHeader)headerIterator.next();
                session.setData(headerName.toLowerCase() + ((i == 1) ? "" : i), header.getHeaderValue());
                i++;
            }
        }

        applicationInstance.start();
    }

    public void setStateAlerting(AlertingSubState substate) {

        switch (substate) {
            case NEW_CALL:
                setCurrentState(alertingNewCallState);
                break;
            case WAIT_FOR_PRACK:
                callNotAcceptedTimerTask.cancel();
                setCurrentState(alertingWaitForPrackState);
                startNoAckTimer();
                break;
            case EARLY_MEDIA:
                cancelNoAckTimer();
                callNotAcceptedTimerTask.cancel();
                cancelSessionEstablishmentTimer();
                setCurrentState(alertingEarlyMediaState);
                startSessionProgressRetransmissionTimer();
                break;
            case EARLY_MEDIA_INBOUND_WAIT_FOR_PRACK:
                setCurrentState(alertingEarlyMediaInboundWaitForPrackState);
                startNoAckTimer();
                break;
            case EARLY_MEDIA_WAIT_FOR_PRACK:
                callNotAcceptedTimerTask.cancel();
                setCurrentState(alertingEarlyMediaWaitForPrackState);
                startNoAckTimer();
                startSessionProgressRetransmissionTimer();
                break;
            case SESSION_ESTABLISHMENT_WAIT_FOR_PRACK:
                callNotAcceptedTimerTask.cancel();
                setCurrentState(alertingSessionEstablishmentWaitForPrackState);
                startNoAckTimer();
                startSessionEstablishmentTimer();
                startSessionProgressRetransmissionTimer();
                break;
            case SESSION_ESTABLISHMENT:
                cancelNoAckTimer();
                setCurrentState(alertingSessionEstablishmentState);
                break;
            case ACCEPTING:
                cancelSessionProgressRetransmissionTimer();
                cancelNoAckTimer();
                callNotAcceptedTimerTask.cancel();
                expiresTimerTask.cancel();
                setCurrentState(alertingAcceptingState);
                startNoAckTimer();
                break;
            case WAIT_FOR_CALL_HOLD:
                cancelNoAckTimer();
                startRedirectedRtpTimer();
                setCurrentState(alertingWaitForCallHoldState);
                break;
            case WAIT_FOR_NEW_MEDIA:
                cancelRedirectedRtpTimer();
                startRedirectedRtpTimer();
                setCurrentState(alertingWaitForNewMediaState);
                break;
            case WAIT_FOR_NEW_MEDIA_ACK:
                cancelRedirectedRtpTimer();
                setCurrentState(alertingWaitForNewMediaAckState);
                break;
            case PROXYING:
                callNotAcceptedTimerTask.cancel();
                expiresTimerTask.cancel();
                setCurrentState(alertingProxyingState);
                setConnected(true);  // Statistic purposes
                startNoAckTimer();
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    /**
     * The UAC has acknowledged the dialog setup. All streams have been
     * created. The call state is set to Connected.
     */
    public void setStateConnected() {
        cancelRedirectedRtpTimer();
        cancelNoAckTimer();
        setConnected(true);
        setCurrentState(connectedState);

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");

        if (log.isInfoEnabled()) log.info("Call Type is " + getCallType());
    }

    public void setStateDisconnected(DisconnectedSubState substate) {
        if (substate == DisconnectedSubState.COMPLETED)
            removeCall();

        cancelCallTimers();

        switch (substate) {
            case COMPLETED:
                setCurrentState(disconnectedCompletedState);
                break;
            case LINGERING_BYE:
                setCurrentState(disconnectedLingeringByeState);
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    public void setStateError(ErrorSubState substate) {
        if (substate == ErrorSubState.COMPLETED)
            removeCall();

        cancelCallTimers();

        switch (substate) {
            case COMPLETED:
                setCurrentState(errorCompletedState);
                break;
            case LINGERING_BYE:
                setCurrentState(errorLingeringByeState);
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    public void setStateFailed(FailedSubState substate) {
        if (substate == FailedSubState.COMPLETED)
            removeCall();

        cancelCallTimers();

        switch (substate) {
            case COMPLETED:
                setCurrentState(failedCompletedState);
                break;
            case LINGERING_BYE:
                setCurrentState(failedLingeringByeState);
                break;
            case WAITING_FOR_ACK:
                setCurrentState(failedWaitingForAckState);
                startNoAckTimer();
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }


    /**
     * @return the initial SIP INVITE request event that created the call.
     */
    public SipRequestEvent getInitialSipRequestEvent() {
        return initialSipRequestEvent;
    }

    public ReliableResponseUsage useReliableProvisionalResponses() {
        ReliableResponseUsage useReliablyResponses = ReliableResponseUsage.NO;
        SipMessage message = initialSipRequestEvent.getSipMessage();

        if (message.isReliableProvisionalResponsesRequired()) {
            useReliablyResponses = ReliableResponseUsage.YES;
        } else if (message.isReliableProvisionalResponsesSupported()) {
            useReliablyResponses = getConfig().getReliableResponseUsage();
        }

        return useReliablyResponses;
    }

    public void logPlayTime() {
        if (!playHasBeenLogged.get()) {
            long timeDifference = new Date().getTime() -
                                  initialSipRequestEvent.getCreationDate().getTime();

            if (log.isInfoEnabled()) log.info("The time between the INVITE and the first play is: " +
                                              timeDifference + " milli seconds");
            playHasBeenLogged.set(true);
        }
    }

//================== InboundCallInternal methods end ====================

    //================== CallImpl methods start ====================

    public String getCallId() {
        return initialSipRequestEvent.getSipMessage().getCallId();
    }

    /**
     * Processes a call command from the system.
     * <p>
     * Accept commands are processed using {@link #processAccept(AcceptEvent)}.
     * NegotiateEarlyMediaTypesEvent commands are processed using
     * {@link #processNegotiateEarlyMediaTypesEvent(NegotiateEarlyMediaTypesEvent)}.
     * Reject commands are processed using {@link #processReject(RejectEvent)}.
     * Disconnect commands are processed using
     * {@link #processDisconnect(DisconnectEvent)}.
     * <p>
     * If an unknown callCommand was issued, a FailedEvent indicating
     * INTERNAL_ERROR is fired and the call is disconnected.
     * <p>
     * If there is an internal implementational error, processing a call command
     * can cause IllegalStateException. If this occurs, it is logged as an
     * error but otherwise ignored.
     * @param callCommandEvent
     */
    void processCallCommand(CallCommandEvent callCommandEvent) {
        try {
            if (callCommandEvent instanceof AcceptEvent) {
                processAccept((AcceptEvent) callCommandEvent);
            } else if (callCommandEvent instanceof ProxyEvent) {
                processProxy((ProxyEvent) callCommandEvent);
            } else if (callCommandEvent instanceof NegotiateEarlyMediaTypesEvent) {
                processNegotiateEarlyMediaTypesEvent((NegotiateEarlyMediaTypesEvent) callCommandEvent);
            } else if (callCommandEvent instanceof RejectEvent) {
                processReject((RejectEvent) callCommandEvent);
            }  else if (callCommandEvent instanceof RedirectEvent) {
                processRedirect((RedirectEvent) callCommandEvent);
            }else if (callCommandEvent instanceof DisconnectEvent) {
                processDisconnect((DisconnectEvent) callCommandEvent);
            } else if (callCommandEvent instanceof PlayEvent) {
                processPlay((PlayEvent) callCommandEvent);
            } else if (callCommandEvent instanceof RecordEvent) {
                processRecord((RecordEvent) callCommandEvent);
            } else if (callCommandEvent instanceof StopPlayEvent) {
                processStopPlay((StopPlayEvent) callCommandEvent);
            } else if (callCommandEvent instanceof StopRecordEvent) {
                processStopRecord((StopRecordEvent) callCommandEvent);
            }
        } catch (IllegalStateException e) {
            log.error("Exception received that suggests internal " +
                    "implementation error in Call Manager.", e);
        }
    }

    //================== CallImpl methods start ====================




    /**
     * When an error has occured, an {@link ErrorEvent} is generated and
     * the state is set to {@link ErrorCompletedInboundState}
     * @param message The error message.
     * @param alreadyDisconnected Indicates if the call already was disconnected
     * when the error occured.
     */
    public void errorOccurred(String message, boolean alreadyDisconnected) {
        if (log.isInfoEnabled()) log.info(message);

        setStateError(ErrorSubState.COMPLETED);

        fireEvent(new ErrorEvent(
                this, CallDirection.INBOUND, message, alreadyDisconnected));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        deleteStreams();
    }

    /**
     * An inbound call is joinable if both streams are created, the call is
     * in {@link ConnectedInboundState} and the call is not already joined.
     * @return True if the inbound call is possible to join.
     */
    public boolean isJoinable() {
        boolean isStateJoinable =
                (getCurrentState() instanceof ConnectedInboundState);
        boolean areStreamsCreated =
                (getInboundStream() != null) && (getOutboundStream() != null);
        return isStateJoinable && areStreamsCreated && !isCallJoined();
    }

    public void cancelCallTimers() {
        callNotAcceptedTimerTask.cancel();
        expiresTimerTask.cancel();
        cancelSessionProgressRetransmissionTimer();
        cancelSessionEstablishmentTimer();
        cancelRedirectedRtpTimer();
    }

    /**
     * Registers the session in the logger.
     * Ensures that the session exists and registers the session ID in the logger and
     * also sets calling, called and redirecting party in the session lof data before
     * the session is registered.
     */
    public void setSessionLoggingData() {
        ISession session = getSession();
        // Ensuring that the session is available
        if (session != null) {
            // Adding calling telephone number to session log data
            if (getCallingParty() != null) {
                Object calling = getCallingParty().getTelephoneNumber();
                if (calling != null) {
                    session.setSessionLogData("calling", calling);
                }
            }

            // Adding called telephone number to session log data
            if (getCalledParty() != null) {
                Object called = getCalledParty().getTelephoneNumber();
                if (called != null) {
                    session.setSessionLogData("called", called);
                }
            }

            // Adding redirecting telephone number to session log data
            if (getRedirectingParty() != null) {
                Object redirecting = getRedirectingParty().getTelephoneNumber();
                if (redirecting != null) {
                    session.setSessionLogData("redirecting", redirecting);
                }
            }

            // Register session and session log data in logger
            session.registerSessionInLogger();
        }
    }

    public boolean isPEarlyMediaPresentAndInactive() {
        boolean presentAndInactive = false;
        ExtensionHeaderImpl extensionHeaderEarlyMedia = null;

        try {
            if (initialSipRequestEvent != null && initialSipRequestEvent.getRequest() != null) {
                extensionHeaderEarlyMedia = (ExtensionHeaderImpl)initialSipRequestEvent.getRequest().getHeader(PEarlyMediaHeader.NAME);
                if (extensionHeaderEarlyMedia != null) {
                    presentAndInactive = extensionHeaderEarlyMedia.getValue().equalsIgnoreCase(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE.getValue());
                }
            }
        } catch (Exception e) { ; }

        return presentAndInactive;
    }

    //================== PRIVATE methods  ====================

    /**
     * Processes a call accept from the system by injecting it into the call's
     * state machine.
     * @param acceptEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processAccept(AcceptEvent acceptEvent) {
        ((InboundCallState)getCurrentState()).accept(acceptEvent);
    }

    /**
     * Processes a proxy call from the system by injecting it into the call's state machine.
     * @param proxyEvent
     * @throws IllegalStateException is thrown if the event is received in an illegal state.
     */
    private void processProxy(ProxyEvent proxyEvent) {
        ((InboundCallState)getCurrentState()).proxy(proxyEvent);
    }

    /**
     * Processes a request to negotiate early media types from the system by
     * injecting it into the call's state machine.
     * @param event
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processNegotiateEarlyMediaTypesEvent(
            NegotiateEarlyMediaTypesEvent event) {
        ((InboundCallState)getCurrentState()).negotiateEarlyMediaTypes(event);
    }

    /**
     * Processes a reject from the system by injecting it into the call's
     * state machine.
     * @param rejectEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processReject(RejectEvent rejectEvent) {
        ((InboundCallState)getCurrentState()).reject(rejectEvent);
    }
    
    
    /**
     * Processes a redirect from the system by injecting it into the call's
     * state machine.
     * @param redirectEvent
     */
    private void processRedirect(RedirectEvent redirectEvent) {
        ((InboundCallState)getCurrentState()).redirect(redirectEvent);
    }

    /**
     * Processes a disconnect from the system by injecting it into the call's
     * state machine.
     * @param disconnectEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processDisconnect(DisconnectEvent disconnectEvent) {
        ((InboundCallState)getCurrentState()).disconnect(disconnectEvent);
    }

    public Boolean isSupportForRedirectedRtpActivated() {
        boolean activatedRedirectedRtpSupport = false;

        // The redirected RTP can only be supported if activated in the configuration
        // and for redirected calls.

        if ((getRedirectingParty() != null) && getConfig().isSupportForRedirectingRTPConfigured()) {

            // Also in order to support redirected RTP, the User-Agent header
            // received in the initial INVITE must match one of the configured
            // values for user agents.

            HashSet<String> configuredUAs =
                getConfig().getSupportForRedirectingRtpUserAgents();

            if (configuredUAs.contains(ConfigConstants.USER_AGENT_ALL)) {
                // All user agents are configured for redirecting RTP
                activatedRedirectedRtpSupport = true;
            } else {
                // Retrieve the User-Agent header from the initial INVITE and
                // compare it with configuration
                String remoteUserAgent = initialSipRequestEvent.getSipMessage().getUserAgent();
                for (String ua : configuredUAs) {
                    if (remoteUserAgent.toLowerCase().contains(ua)) {
                        activatedRedirectedRtpSupport = true;
                        break;
                    }
                }
            }
        }
        return activatedRedirectedRtpSupport;
    }


    @Override
    public void sendRedirectResponse(RedirectDestination destination, RedirectStatusCode redirectCode, SipRequestEvent sipRequestEvent) {
        try {
            int statusCode = redirectCode.getCode();
            SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().createRedirectResponse(statusCode , sipRequestEvent, destination);
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            errorOccurred(
                    "Could not send SIP Redirect response for " +
                            sipRequestEvent,
                    true);
            sendErrorResponse(
                    Response.SERVER_INTERNAL_ERROR,
                    sipRequestEvent,
                    e.getMessage());
        }
    }






}
