/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.SendTokenEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallCommandEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.DialEvent;
import com.mobeon.masp.callmanager.sdp.SdpSessionDescriptionFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.contact.Contact;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedOutboundState.DisconnectedSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState.ProgressingSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.*;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorOutboundState.ErrorSubState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedOutboundState.FailedSubState;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;

import javax.sip.ClientTransaction;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.text.ParseException;

/**
 * Represents an outbound call.
 * It basically consists of a state machine to which it relays all incoming SIP
 * requests/responses and call commands from the system.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class OutboundCallImpl extends CallImpl implements OutboundCallInternal {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Outbound states
    private final IdleOutboundState idleState = new IdleOutboundState(this);
    private final ConnectedOutboundState connectedState =
            new ConnectedOutboundState(this);
    private final DisconnectedCompletedOutboundState disconnectedCompletedState =
            new DisconnectedCompletedOutboundState(this);
    private final DisconnectedLingeringByeOutboundState disconnectedLingeringByeState =
            new DisconnectedLingeringByeOutboundState(this);
    private final ErrorCompletedOutboundState errorCompletedState =
            new ErrorCompletedOutboundState(this);
    private final ErrorLingeringByeOutboundState errorLingeringByeState =
            new ErrorLingeringByeOutboundState(this);
    private final ErrorLingeringCancelOutboundState errorLingeringCancelState =
            new ErrorLingeringCancelOutboundState(this);
    private final FailedCompletedOutboundState failedCompletedState =
            new FailedCompletedOutboundState(this);
    private final FailedLingeringByeOutboundState failedLingeringByeState =
            new FailedLingeringByeOutboundState(this);
    private final FailedLingeringCancelOutboundState failedLingeringCancelState =
            new FailedLingeringCancelOutboundState(this);
    private final FailedWaitingForResponseOutboundState failedWaitingForResponseState =
            new FailedWaitingForResponseOutboundState(this);
    private final ProgressingCallingOutboundState progressingCallingState =
            new ProgressingCallingOutboundState(this);
    private final ProgressingEarlyMediaOutboundState progressingEarlyMediaState =
            new ProgressingEarlyMediaOutboundState(this);
    private final ProgressingProceedingOutboundState progressingProceedingState =
            new ProgressingProceedingOutboundState(this);


    // Call Information.
    private final CallProperties callProperties;
    private AtomicReference<String> callId = new AtomicReference<String>();


    // Call timers
    private final CallTimerTask callNotConnectedTimerTask =
            new CallTimerTask(this, CallTimerTask.Type.CALL_NOT_CONNECTED);
    private final CallTimerTask callTooLongTimerTask =
            new CallTimerTask(this, CallTimerTask.Type.MAX_CALL_DURATION);
    private final CallTimerTask noResponseTimerTask =
            new CallTimerTask(this, CallTimerTask.Type.NO_RESPONSE);
    private CallTimerTask callNotConnectedExtensionTimerTask;


    // SIP/SDP related
    private final AtomicReference<SipRequest> initialSipRequest =
            new AtomicReference<SipRequest>();
    private final AtomicReference<ClientTransaction> currentInviteTransaction =
            new AtomicReference<ClientTransaction>();
    private final AtomicReference<SipURI> currentRemoteParty =
            new AtomicReference<SipURI>();


    // Misc
    // Thread-safe due to use of contactsLock and synchronized sections.
    private TreeSet<Contact> redirectContacts;
    private final Object contactsLock = new Object();



    public OutboundCallImpl(CallProperties callProperties,
                            IEventDispatcher eventDispatcher,
                            ISession session,
                            String callId,
                            SdpSessionDescriptionFactory sdpFactory,
                            CallManagerConfiguration config) {
        super(config);

        // Initiate the call in IDLE state
        setCurrentState(idleState);

        // Set SIP/SDP stack related
        setSessionDescriptionFactory(sdpFactory);

        // Set execution environment related
        setEventDispatcher(eventDispatcher);
        setSession(session);

        // Set call information
        this.callId.set(callId);

        // Store the call properties
        this.callProperties = callProperties;
        setCalledParty(callProperties.getCalledParty());
        setCallingParty(callProperties.getCallingParty());
        if (callProperties.getCallType() == CallProperties.CallType.UNKNOWN)
            callProperties.setCallType(getConfig().getCallType());
        setCallType(callProperties.getCallType());
    }

    public SipURI getCurrentRemoteParty() {
        return currentRemoteParty.get();
    }

    public void setCurrentRemoteParty(SipURI currentRemoteParty) {
        this.currentRemoteParty.set(currentRemoteParty);
    }

    /**
     * This method rejects an outbound call.
     * The event receiver is removed, the call is set to state
     * {@link FailedCompletedOutboundState} and
     * a {@link FailedEvent} is generated. The call is also marked removed so
     * that any attempt to remove it later will result in no action.
     * <p>
     * This method shall only be used if a newly created call shall be rejected
     * due to for example the load situation and must be called before the new
     * outbound call is returned to the call client.
     *
     * @param reason Indicates the reason for the call reject and is included in
     * the {@link FailedEvent}.
     */
    public void reject(String reason) {
        if (log.isInfoEnabled()) log.info(reason);

        IEventDispatcher eventDispatcher = getEventDispatcher();
        if (eventDispatcher != null) {
            eventDispatcher.removeEventReceiver(this);
        }

        // Note that it is important to indicate that the call already is removed.
        // This is done so that for example a disconnect() in Idle state cannot
        // result in the call being removed twice.
        callIsRemoved.set(true);
        setCurrentState(failedCompletedState);

        // Report that the outbound call is being rejected
        fireEvent(new FailedEvent(
                this, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, reason,
                ConfigurationReader.getInstance().getConfig().
                        getReleaseCauseMapping().getNetworkStatusCode(null, null)));
    }

    public String toString() {
        return "Outbound call: <Call Properties = " + getCallProperties() + ">";
    }
//===================== OutboundCall methods start =======================

    public void disconnect() {
        DisconnectEvent disconnectEvent = new DisconnectEvent();
        queueEvent(disconnectEvent);
    }

    public void sendToken(ControlToken[] tokens) {
        SendTokenEvent sendTokenEvent = new SendTokenEvent(tokens);
        queueEvent(sendTokenEvent);
    }

//===================== OutboundCall methods end =======================



//================== OutboundCallInternal methods start ====================

    public String getCallId() {
        return callId.get();
    }

    public void setStateConnected() {
        setConnected(true);

        callNotConnectedTimerTask.cancel();
        cancelCallNotConnectedExtensionTimer();
        startCallDurationTimer();

        CMUtils.getInstance().getCallDispatcher().updateOutboundCallDialogId(
                this, getDialog().getDialogId(), true);

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
            case LINGERING_CANCEL:
                setCurrentState(errorLingeringCancelState);
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    public void setStateFailed(FailedSubState substate) {

        if (substate == FailedSubState.COMPLETED )
            removeCall();


        cancelCallTimers();

        switch (substate) {
            case COMPLETED:
                setCurrentState(failedCompletedState);
                break;
            case LINGERING_BYE:
                setCurrentState(failedLingeringByeState);
                break;
            case LINGERING_CANCEL:
                setCurrentState(failedLingeringCancelState);
                break;
            case WAITING_FOR_RESPONSE:
                startNoResponseTimer();
                setCurrentState(failedWaitingForResponseState);
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    public void setStateProgressing(ProgressingSubState substate) {
        switch (substate) {
            case CALLING:
                cancelCallNotConnectedExtensionTimer();
                setCurrentState(progressingCallingState);
                startCallNotConnectedExtensionTimer();
                break;
            case EARLY_MEDIA:
                setCurrentState(progressingEarlyMediaState);
                break;
            case PROCEEDING:
                setCurrentState(progressingProceedingState);
                break;
        }

        if (log.isDebugEnabled())
            log.debug("State is set to " + getCurrentState() + ".");
    }

    public CallProperties getCallProperties() {
        return callProperties;
    }

    public void startNotConnectedTimer() {
        getCallTimer().schedule(
                callNotConnectedTimerTask,
                getCallProperties().getMaxDurationBeforeConnected());
        if (log.isDebugEnabled())
            log.debug("Max Duration Before Connected Timer has been scheduled to: " +
                    getCallProperties().getMaxDurationBeforeConnected());
    }

    public void startCallDurationTimer() {
        int maxCallDuration = getCallProperties().getMaxCallDuration();
        if (maxCallDuration > 0) {
            getCallTimer().schedule(callTooLongTimerTask, maxCallDuration);
        }
    }

    public void startNoResponseTimer() {
        getCallTimer().schedule(
                noResponseTimerTask,
                getCallProperties().getMaxDurationBeforeConnected());
        if (log.isDebugEnabled())
            log.debug("No Response Timer has been scheduled to: " +
                    getCallProperties().getMaxDurationBeforeConnected());
    }

    public void startNoConnectedTimer() {
        getCallTimer().schedule(
                noResponseTimerTask,
                getCallProperties().getMaxDurationBeforeConnected());
        if (log.isDebugEnabled())
            log.debug("No Response Timer has been scheduled to: " +
                    getCallProperties().getMaxDurationBeforeConnected());
    }
    
    public void startCallNotConnectedExtensionTimer() {
       
        callNotConnectedExtensionTimerTask =
                new CallTimerTask(this, CallTimerTask.Type.CALL_NOT_CONNECTED_EXTENSION);
        
        // Hard-coded to 3 minutes as per RFC3261 section 13.3.1.1 Progress
        getCallTimer().schedule(
                callNotConnectedExtensionTimerTask, 180000);
        
        if (log.isDebugEnabled())
            log.debug("Call Not Connected Extension Timer has been scheduled to: 180000");
    }

    public void cancelNoResponseTimer() {
        noResponseTimerTask.cancel();
    }
    
    public void cancelCallNotConnectedExtensionTimer() {
        if(callNotConnectedExtensionTimerTask != null)
            callNotConnectedExtensionTimerTask.cancel();
    }

    public void dialogCreated(SipRequest sipRequest) {
        // The first time a dialog is created (i.e. not a redirection),
        // insert the call in the call dispatcher
        if (getDialog() != null)
            CMUtils.getInstance().getCallDispatcher().
                    removeCall(getInitialDialogId(), getEstablishedDialogId());

        CMUtils.getInstance().getCallDispatcher().insertOutboundCall(this, sipRequest);
        setInitialSipRequest(sipRequest);
    }

    /**
     * @return A new unique call id.
     */
    public String getNewCallId() {
        callId.set(CMUtils.getInstance().getSipStackWrapper().
                getNewCallId().getCallId());
        return callId.get();
    }

    public synchronized ClientTransaction getCurrentInviteTransaction() {
        return currentInviteTransaction.get();
    }

    public synchronized void setCurrentInviteTransaction(
            ClientTransaction transaction) {
        this.currentInviteTransaction.set(transaction);
    }

    public SipURI getNewRemoteParty() throws ParseException {
        SipURI remoteParty = null;
        if (isRedirected())
            remoteParty = getNextContact();
        else if(callProperties.getOutboundCallServerHost() != null) {
            URI uri = CMUtils.getInstance().getSipHeaderFactory().
                    createUriFromCallParty(
                            callProperties.getCalledParty(),
                            callProperties.getOutboundCallServerHost(),
                            getOutboundCallServerPort());
            if (uri.isSipURI()) remoteParty = (SipURI)uri;
        }
        else {
            RemotePartyAddress remotePartyAddress =
                    CMUtils.getInstance().getRemotePartyController().
                            getRandomRemotePartyAddress();
            if (remotePartyAddress != null) {
                URI uri = CMUtils.getInstance().getSipHeaderFactory().
                        createUriFromCallParty(
                                callProperties.getCalledParty(),
                                remotePartyAddress.getHost(),
                                remotePartyAddress.getPort());
                if (uri.isSipURI()) remoteParty = (SipURI)uri;
            }
        }

        setCurrentRemoteParty(remoteParty);
        return remoteParty;
    }

    private int getOutboundCallServerPort() {
        CallManagerConfiguration config =
                ConfigurationReader.getInstance().getConfig();
        if(callProperties.isOutboundCallServerPortSet())
            return callProperties.getOutboundCallServerPort();
        else
            return config.getOutboundCallServerPort();
    }

    private SipURI getNextContact() {

        SipURI nextContact = null;

        synchronized(contactsLock) {
            while (!redirectContacts.isEmpty()) {
                Contact contact = redirectContacts.first();
                redirectContacts.remove(contact);
                SipURI uri = contact.getSipUri();
                String remotePartyId = uri.getHost() + ":" + uri.getPort();

                if (!CMUtils.getInstance().getRemotePartyController().
                        isRemotePartyBlackListed(remotePartyId)) {
                    nextContact = uri;
                    break;
                } else {
                    log.debug("Contact <" + uri + "> found in contact list " +
                            "but it is not used since it is black listed.");
                }
            }
        }

        return nextContact;
    }

    public boolean isRedirected() {
        synchronized(contactsLock) {
            return (redirectContacts != null);
        }
    }

    public boolean isRedirectionAllowed() {
        return !(getConfig().getRemoteParty().isSipProxy());
    }

    public void retrieveContacts(SipResponseEvent sipResponseEvent) {
        synchronized(contactsLock) {
            redirectContacts =
                    sipResponseEvent.getSipMessage().getContacts(
                            getInitialSipRequest().getRequest().getRequestURI());
        }
    }

    public synchronized SipRequest getInitialSipRequest() {
        return initialSipRequest.get();
    }

    public synchronized void setInitialSipRequest(SipRequest sipRequest) {
        initialSipRequest.set(sipRequest);
    }

    public boolean isProvisionalResponseReliable(SipResponseEvent sipResponseEvent) {
        return sipResponseEvent.getSipMessage().
                isReliableProvisionalResponsesRequired();
    }

//================== OutboundCallInternal methods end ====================


//================== CallImpl methods start ====================
    /**
     * Processes a call command from the system or an internal dial command.
     * <p>
     * Disconnect commands are processed using
     * {@link #processDisconnect(DisconnectEvent)}.
     * SendToken commands are processed using
     * {@link #processSendToken(SendTokenEvent)}.
     * Dial commands are processed using {@link #processDial(DialEvent)}.
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
            if (log.isDebugEnabled())
                log.debug("Processing call command: " + callCommandEvent +
                        " currentState:" + getCurrentState());
            if (callCommandEvent instanceof DisconnectEvent) {
                processDisconnect((DisconnectEvent) callCommandEvent);
            } else if (callCommandEvent instanceof SendTokenEvent) {
                processSendToken((SendTokenEvent) callCommandEvent);
            } else if (callCommandEvent instanceof DialEvent) {
                processDial((DialEvent) callCommandEvent);
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

//================== CallImpl methods end ====================


//================== PRIVATE methods start ====================

    /**
     * Processes a dial (generated internally) by injecting it into the call's
     * state machine.
     * @param dialEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processDial(DialEvent dialEvent) {
        ((OutboundCallState)getCurrentState()).dial(dialEvent);
    }

    /**
     * Processes a send token from the system by injecting it into the call's
     * state machine.
     * @param sendTokenEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processSendToken(SendTokenEvent sendTokenEvent) {
        ((OutboundCallState)getCurrentState()).sendToken(sendTokenEvent);
    }

    /**
     * Processes a disconnect from the system by injecting it into the call's
     * state machine.
     * @param disconnectEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    private void processDisconnect(DisconnectEvent disconnectEvent) {
        ((OutboundCallState)getCurrentState()).disconnect(disconnectEvent);
    }

//================== PRIVATE methods end ====================

    public void cancelCallTimers() {
        callNotConnectedTimerTask.cancel();
        callTooLongTimerTask.cancel();
        cancelCallNotConnectedExtensionTimer();
    }

    /**
     * When an error has occured, an {@link ErrorEvent} is generated and
     * the state is set to {@link ErrorCompletedOutboundState}
     * @param message The error message.
     * @param alreadyDisconnected Indicates if the call already was disconnected
     * when the error occured.
     */
    public void errorOccurred(String message, boolean alreadyDisconnected) {
        if (log.isInfoEnabled()) log.info(message);
        setStateError(ErrorSubState.COMPLETED);
        fireEvent(new ErrorEvent(
                this, CallDirection.OUTBOUND, message, alreadyDisconnected));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        deleteStreams();
    }

    /**
     * An outbound call is joinable if both streams are created, the call is
     * in state {@link ProgressingEarlyMediaOutboundState} or
     * {@link ConnectedOutboundState} and the call is not already joined.
     * @return True if the inbound call is possible to join.
     */
    public boolean isJoinable() {
        CallState state = getCurrentState();
        boolean isStateJoinable =
                (state instanceof ProgressingEarlyMediaOutboundState) ||
                        (state instanceof ConnectedOutboundState);
        boolean areStreamsCreated =
                (getInboundStream() != null) && (getOutboundStream() != null);
        return isStateJoinable && areStreamsCreated && !isCallJoined();
    }


    /**
     * Registers the session in the logger.
     * If there is no session it egisters the session ID in the logger and
     * also sets calling, called and redirecting party in the
     * <param>mdcItems</param> log data and registers the
     * <param>mdcItems</param> in logger.
     */
    public void setSessionLoggingData() {
        if (getSession() != null) {
            getSession().registerSessionInLogger();
        }
    }
}
