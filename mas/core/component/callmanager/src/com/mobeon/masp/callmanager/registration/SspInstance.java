/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.registration.RegistrationTimerTask.Type;
import com.mobeon.masp.callmanager.registration.events.RegistrationTimeoutEvent;
import com.mobeon.masp.callmanager.registration.events.RegisterEvent;
import com.mobeon.masp.callmanager.registration.events.UnregisterEvent;
import com.mobeon.masp.callmanager.sip.events.SipOkResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipErrorResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.events.SipProvisionalResponseEvent;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.registration.states.RegistrationState;
import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.registration.states.UnregisteringState;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.queuehandling.CommandExecutor;
import com.mobeon.masp.callmanager.CMUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import java.text.ParseException;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is a client to one SSP instance. It is responsible for registration
 * and unregistration towards that SSP.
 *
 * TODO: Drop 6! It is not easy code that some events come through method and others through queueEvent. Redesign!
 * @author Malin Flodin
 */
public class SspInstance implements CommandExecutor {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Thread-safe due to immutable, i.e set at construction time
    private static final Timer registerTimer = new Timer();

    // Thread-safe due to immutable, i.e set at construction time and never
    // changed
    private SequenceGuaranteedEventQueue eventQueue =
            new SequenceGuaranteedEventQueue(this, SspInstance.class);

    // Load states
    // Thread-safe due to immutable, i.e set at construction and never changed
    private final UnregisteredState unregisteredState;
    private final UnregisteringState unregisteringState;
    private final RegisteredState registeredState;
    private final RegisteringState registeringState;

    // Thread-safe since only set or get in synchronized methods
    private AtomicReference<RegistrationState> currentState =
            new AtomicReference<RegistrationState>();
    private int cSeq = 1;
    private SipRequest ongoingSipRegisterRequest = null;

    // Thread-safe due to immutable, i.e set at construction and never changed
    private final RemotePartyAddress address;
    private CallIdHeader callIdHeader = null;

    private RegistrationTimerTask retryTimerTask = null;
    private RegistrationTimerTask backoffTimerTask = null;

    // This parameter is used to indicate that CallManager wishes to unregister
    // from the SSP, but it has to wait until the current REGISTER is completed.
    // Thread-safe since only set or get in synchronized methods.
    private Boolean pendingUnregister = false;

    // This parameter is used to indicate that CallManager wishes to register
    // to the SSP, but it has to wait until the current (UN)REGISTER is completed.
    // Thread-safe since only set or get in synchronized methods.
    private Boolean pendingRegister = false;


    // Contains the registered name sent in the previous register request.
    private String registeredName = null;

    /**
     * @param address MUST NOT be null.
     * @throws IllegalArgumentException if address is null.
     */
    public SspInstance(RemotePartyAddress address) {

        if ((address == null)) {
                throw new IllegalArgumentException(
                        "Could not create the SSP instance. Argument was null. " +
                                "<Address=" + address + ">.");
        }

        this.address = address;

        // Create states
        unregisteredState = new UnregisteredState(this);
        unregisteringState = new UnregisteringState(this);
        registeredState = new RegisteredState(this);
        registeringState = new RegisteringState(this);

        // Initialize
        currentState.set(unregisteredState);
    }

    // TODO: Drop 6! Document that it requires to be used from synchronized methods!
    private CallIdHeader getCallIdHeader() {
        if (callIdHeader == null) {
            callIdHeader =
                    CMUtils.getInstance().getSipStackWrapper().getNewCallId();
        }
        return callIdHeader;
    }
    //=========================== Public Getters =====================

    public RemotePartyAddress getAddress() {
        return address;
    }

    //=========================== Other Public methods =====================
    // All these public methods should result in an event queued in the event queue.

    public void handleTimeout(RegistrationTimerTask.Type type) {
        RegistrationTimeoutEvent registerTimeoutEvent =
                new RegistrationTimeoutEvent(type);
        queueEvent(registerTimeoutEvent);
    }

    public void doRegister() {
        RegisterEvent registerEvent = new RegisterEvent();
        queueEvent(registerEvent);
    }

    public void doUnregister() {
        UnregisterEvent unregisterEvent = new UnregisterEvent();
        queueEvent(unregisterEvent);
    }

    /**
     * Queues an event in the registration event queue. Each event is handled
     * one at a time in the order they arrived.
     * @param event
     */
    public void queueEvent(EventObject event) {
        eventQueue.queue(event);
    }

    public String toString() {
        return "SSP = " + address;
    }

    /**
     * Used for basic testing only
     */
    public synchronized void cancelTimers() {
        cancelBackoffTimer();
        cancelRegisterTimer();
    }

    //=========================== CommandExecutor methods =====================

    /**
     * This method is called when an event in the Registration event queue
     * shall be processed.
     * <p>
     * All events are injected into the registration state machine.
     * <ul>
     * <li>{@link RegisterEvent} is injected using
     * {@link RegistrationState#doRegister()}
     * </li>
     * <li>{@link UnregisterEvent} is injected using
     * {@link RegistrationState#doUnregister()}
     * </li>
     * <li>{@link RegistrationTimeoutEvent} is injected using
     * {@link RegistrationState#handleBackoffTimeout()} or
     * {@link RegistrationState#handleReRegisterTimeout()}
     * depending on the type of the timer that expired.
     * </li>
     * <li>{@link SipErrorResponseEvent} is injected using
     * {@link RegistrationState#processSipErrorResponse(int)}
     * </li>
     * <li>{@link SipOkResponseEvent} is injected using
     * {@link RegistrationState#processSipOkResponse(int)}
     * </li>
     * <li>{@link SipTimeoutEvent} is injected using
     * {@link RegistrationState#processSipTimeout()}
     * </li>
     * </ul>
     * <p>
     * This method must never throw an exception. Therefore, this method
     * catches all exceptions. If an exception is thrown, it is logged as an
     * error.
     *
     * @param eventObject
     */
    public synchronized void doCommand(EventObject eventObject) {

        // Clearing session info from logger this is run in a thread picked
        // from a pool and has no session relation.
        log.clearSessionInfo();

        if (log.isDebugEnabled())
            log.debug("DoCommand: " + eventObject);

        try {
            if (eventObject instanceof RegisterEvent) {
                currentState.get().doRegister();

            } else if (eventObject instanceof UnregisterEvent) {
                currentState.get().doUnregister();

            } else if (eventObject instanceof RegistrationTimeoutEvent) {
                RegistrationTimeoutEvent timeoutEvent =
                        (RegistrationTimeoutEvent)eventObject;
                if (timeoutEvent.getType() == Type.REGISTER_RETRY) {
                   currentState.get().handleReRegisterTimeout();
                } else if (timeoutEvent.getType() == Type.REGISTER_BACKOFF) {
                    currentState.get().handleBackoffTimeout();
                }

            } else if (eventObject instanceof SipErrorResponseEvent) {
                SipErrorResponseEvent sipErrorResponseEvent = (SipErrorResponseEvent)eventObject;
                processSipErrorResponse(sipErrorResponseEvent.getResponseCode());

            } else if (eventObject instanceof SipOkResponseEvent) {
                SipOkResponseEvent sipOkResponseEvent = (SipOkResponseEvent)eventObject;
                processSipOkResponse(
                        sipOkResponseEvent.getSipMessage().getContactExpireTime());

            } else if (eventObject instanceof SipProvisionalResponseEvent) {
                if (log.isDebugEnabled())
                    log.debug("Provisional response is received for SIP " +
                            "REGISTER request. It is ignored.");

            } else if (eventObject instanceof SipTimeoutEvent) {
                processSipTimeout();
            }

        } catch (Throwable e) {
            String errorMsg = "Exception occurred in doCommand. This must " +
                    "never happen! Error in implementation of CallManager.";
            log.error(errorMsg, e);
        }
    }

    //====== Package private methods introduced to simplify basic test  ========

    void processSipErrorResponse(int errorCode) {
        CMUtils.getInstance().getRegistrationDispatcher().removeOngoingRegistration(
                this, ongoingSipRegisterRequest);
        currentState.get().processSipErrorResponse(errorCode);
    }

    void processSipOkResponse(int expireTime) {
        CMUtils.getInstance().getRegistrationDispatcher().removeOngoingRegistration(
                this, ongoingSipRegisterRequest);
        currentState.get().processSipOkResponse(expireTime);
    }

    void processSipTimeout() {
        CMUtils.getInstance().getRegistrationDispatcher().removeOngoingRegistration(
                this, ongoingSipRegisterRequest);

        currentState.get().processSipTimeout();
    }

    //======= Public methods intended to be used within subpackages  ========

    public boolean isRegisterPending() {
        return pendingRegister;
    }

    public void setPendingRegister(boolean pendingRegister) {
        this.pendingRegister = pendingRegister;
    }

    public boolean isUnregisterPending() {
        return pendingUnregister;
    }

    public void setPendingUnregister(boolean pendingUnregister) {
        this.pendingUnregister = pendingUnregister;
    }

    public void markAsRegistered() {
        SspStatus.getInstance().addSpp(this);
    }

    public void markAsUnregistered() {
        SspStatus.getInstance().removeSpp(this);
    }

    public synchronized RegistrationState getCurrentState() {
        return currentState.get();
    }

    public void setRegisteredState() {
        markAsRegistered();
        currentState.set(registeredState);

        if (log.isDebugEnabled())
            log.debug("Registration state set to: " + currentState.get());

        if (log.isInfoEnabled()) log.info("Registered to " + this);
    }

    public void setRegisteringState() {
        currentState.set(registeringState);
        if (log.isDebugEnabled())
            log.debug("Registration state set to: " + currentState.get());
    }

    public void setUnregisteredState() {
        markAsUnregistered();
        currentState.set(unregisteredState);

        if (log.isDebugEnabled())
            log.debug("Registration state set to: " + currentState.get());

        if (log.isInfoEnabled()) log.info("No longer registered to " + this);
    }

    public void setUnregisteringState() {
        markAsUnregistered();
        currentState.set(unregisteringState);
        if (log.isDebugEnabled())
            log.debug("Registration state set to: " + currentState.get());
    }

    public void sendRegister()
            throws InvalidArgumentException, ParseException, SipException {
        registeredName = ConfigurationReader.getInstance().getConfig().getRegisteredName();

        ongoingSipRegisterRequest =
                CMUtils.getInstance().getSipRequestFactory().createRegisterRequest(
                        registeredName,
                        address.getHost(), address.getPort(),
                        cSeq++, getCallIdHeader(), null);

        CMUtils.getInstance().getRegistrationDispatcher().addOngoingRegistration(
                this, ongoingSipRegisterRequest);

        try {
            CMUtils.getInstance().getSipMessageSender().
                    sendRequest(ongoingSipRegisterRequest);

            if (log.isDebugEnabled())
                log.debug("SIP REGISTER sent to SSP " + address);

        } catch (SipException e) {
            CMUtils.getInstance().getRegistrationDispatcher().removeOngoingRegistration(
                    this, ongoingSipRegisterRequest);
            throw(e);
        }

    }

    public void sendUnregister()
            throws InvalidArgumentException, ParseException, SipException {
        if (registeredName == null) {
            registeredName =  ConfigurationReader.getInstance().
                    getConfig().getRegisteredName();
        }

        ongoingSipRegisterRequest =
                CMUtils.getInstance().getSipRequestFactory().createRegisterRequest(
                        registeredName,
                        address.getHost(), address.getPort(),
                        cSeq++, getCallIdHeader(), 0);

        CMUtils.getInstance().getRegistrationDispatcher().addOngoingRegistration(
                this, ongoingSipRegisterRequest);

        try {
            CMUtils.getInstance().getSipMessageSender().
                    sendRequest(ongoingSipRegisterRequest);

            if (log.isDebugEnabled())
                log.debug("SIP (UN)REGISTER sent to SSP " + address);

        } catch (SipException e) {
            CMUtils.getInstance().getRegistrationDispatcher().removeOngoingRegistration(
                    this, ongoingSipRegisterRequest);
            throw(e);
        }

    }

    public void cancelBackoffTimer() {
        if (backoffTimerTask != null) {
            backoffTimerTask.cancel();
        }
    }

    public void cancelRegisterTimer() {
        if (retryTimerTask != null) {
            retryTimerTask.cancel();
        }
    }

    /**
     * @param expireTime in seconds.
     */
    public void startRetryTimer(int expireTime) {
        int scaledExpireTime = expireTime * 1000;

        int retryTime = scaledExpireTime -
                ConfigurationReader.getInstance().getConfig().
                        getRegisterBeforeExpirationTime();
        if (retryTime <= 0) {
            retryTime = scaledExpireTime;
        }

        if (log.isDebugEnabled())
            log.debug("The registration expires in " + scaledExpireTime + " milli seconds. " +
                    "Scheduling registration retry in " + retryTime + " milli seconds.");

        retryTimerTask = new RegistrationTimerTask(
                this, RegistrationTimerTask.Type.REGISTER_RETRY);
        registerTimer.schedule(retryTimerTask, retryTime);
    }

    public void startBackoffTimer() {
        int backoff = ConfigurationReader.getInstance().getConfig().getRegisterBackoffTimer();

        if (log.isDebugEnabled())
            log.debug("Scheduling backoff after " + backoff + " milli seconds.");

        backoffTimerTask = new RegistrationTimerTask(
                this, RegistrationTimerTask.Type.REGISTER_BACKOFF);
        registerTimer.schedule(backoffTimerTask, backoff);
    }

    public String getDebugInfo() {
        return "(State=" + currentState.get() + ", SSP=" + address + ")";
    }
}
