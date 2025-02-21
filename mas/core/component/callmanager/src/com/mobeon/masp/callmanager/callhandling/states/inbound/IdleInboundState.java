/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.NegotiateEarlyMediaTypesEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingInboundState.AlertingSubState;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Response;

/**
 * Represents the state Idle for inbound calls.
 * The Idle State is the first inbound state, i.e. the state entered
 * when the initial INVITE for an inbound call is received.
 * <p>
 * The purpose of the Idle state is to handle the initial INVITE used to
 * establish an inbound call. All other methods are illegal in this state and
 * throws IllegalStateException.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class IdleInboundState extends InboundCallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public IdleInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Idle state";
    }

    /**
     * Handles an administrators lock request.
     * <p>
     * Since a lock request is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @throws IllegalStateException always since a lock request is not allowed
     * in this state.
     */
    public synchronized void processLockRequest() {
        String errorMsg = "Lock request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a request to send a Video Fast Update request.
     * <p>
     * Since a video fast update request is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @throws IllegalStateException always since a video fast update request
     * is not allowed in this state.
     */
    public synchronized void processVideoFastUpdateRequest() {
        String errorMsg = "Sending a Video Fast Update request is not " +
                "allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a call accept from the Call Manager client.
     * <p>
     * Since a call cannot be accepted in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param acceptEvent carries the accept request.
     * @throws IllegalStateException always since a call accept
     * is not allowed in this state.
     */
    public synchronized void accept(AcceptEvent acceptEvent) {
        String errorMsg = "Accept is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a request to negotiate early media types from the Call Manager
     * client.
     * <p>
     * Since a early media types cannot be accepted in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param event carries the negotiateEarlyMediaTypes request.
     * @throws IllegalStateException always since a early media types negotiation
     * is not allowed in this state.
     */
    public synchronized void negotiateEarlyMediaTypes(
            NegotiateEarlyMediaTypesEvent event) {
        String errorMsg =
                "Negotiate early media types is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a call reject from the Call Manager client.
     * <p>
     * Since a call cannot be rejected in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param rejectEvent carries the reject request.
     * @throws IllegalStateException always since a call reject
     * is not allowed in this state.
     */
    public synchronized void reject(RejectEvent rejectEvent) {
        String errorMsg = "Reject is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a call disconnect from the Call Manager client.
     * <p>
     * Since a call cannot be disconnected in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param disconnectEvent carries the disconnect request.
     * @throws IllegalStateException always since a call disconnect
     * is not allowed in this state.
     */
    public synchronized void disconnect(DisconnectEvent disconnectEvent) {
        String errorMsg = "Disconnect is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP ACK request.
     * <p>
     * Since a SIP ACK is not allowed in this state (if it happens
     * it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP ACK request.
     * @throws IllegalStateException always since a SIP ACK is not allowed in
     * this state.
     */
    public synchronized void processAck(SipRequestEvent sipRequestEvent) {
        String errorMsg = "SIP ACK request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * Since a SIP BYE is not allowed in this state (if it happens
     * it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     * @throws IllegalStateException always since a SIP BYE is not allowed in
     * this state.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        String errorMsg = "SIP BYE request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP CANCEL request.
     * <p>
     * Since a SIP CANCEL is not allowed in this state (if it happens
     * it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP CANCEL request.
     * @throws IllegalStateException always since a SIP CANCEL is not allowed in
     * this state.
     */
    public synchronized void processCancel(SipRequestEvent sipRequestEvent) {
        String errorMsg = "SIP CANCEL request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Parses a SIP INVITE request.
     * <p>
     * <ul>
     * <li>
     * A SIP "Trying" response is sent using
     * {@link #sendTryingResponse(SipRequestEvent)}.
     * </li>
     * <li>
     * The SDP offer (if any) is retrieved and stored using
     * {@link #retrieveAndStoreRemoteSdpPriorToAccept(SipRequestEvent, boolean)}.
     * </li>
     * <li>
     * A service instance is loaded using {@link #loadService(SipRequestEvent)}.
     * </li>
     * <li>
     * The call is registered to receive events using
     * {@link InboundCallInternal#registerToReceiveEvents()}
     * </li>
     * <li>
     * The Call Not Accepted timer is started using
     * {@link InboundCallInternal#startNotAcceptedTimer()}.
     * </li>
     * <li>
     * The Expires timer is started using
     * {@link InboundCallInternal#startExpiresTimer(SipRequestEvent)}.
     * </li>
     * <li>
     * An {@link AlertingEvent} is generated and the state is set to
     * {@link AlertingNewCallInboundState}.
     * </li>
     * </ul>
     * @param sipRequestEvent carries the SIP INVITE request.
     */
    public synchronized void processInvite(SipRequestEvent sipRequestEvent)
    {
        if (log.isDebugEnabled())
            log.debug("SIP INVITE received in " + this + ".");

        try {
            // Send a SIP "Trying" response
            sendTryingResponse(sipRequestEvent);

            if (log.isDebugEnabled())
                log.debug("Trying response is sent.");

            // Retrieve and store the remote SDP offer
            retrieveAndStoreRemoteSdpPriorToAccept(sipRequestEvent, false);
            if (log.isDebugEnabled())
                log.debug("Remote SDP offer is parsed.");

            if (call.getRemoteSdp() == null) {
                if (log.isDebugEnabled())
                    log.debug("No SDP offer in INVITE.");
            } else {
                if (log.isDebugEnabled())
                    log.debug("SDP offer was included in the INVITE.");
            }

            addFarEndConnection(sipRequestEvent);

            // Load service
            loadService(sipRequestEvent);

            if (log.isDebugEnabled())
                log.debug("Service instance is loaded.");

            // Register to receive events
            call.registerToReceiveEvents();

            if (log.isDebugEnabled())
                log.debug("Call is registered to receive events.");

            call.startNotAcceptedTimer();

            call.startExpiresTimer(sipRequestEvent);

            // Go to Alerting state
            call.setStateAlerting(AlertingSubState.NEW_CALL);

            // Inbound call Ok, send Alerting event.
            call.fireEvent(new AlertingEvent(call));

        } catch (IllegalStateException e) {
            if (log.isDebugEnabled())
                log.debug("Call failed when handling initial INVITE.");
        }
    }

    /**
     * Handles a SIP re-INVITE request.
     * <p>
     * Since a SIP re-INVITE is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP re-INVITE request.
     * @throws IllegalStateException always since a SIP re-INVITE is not
     * allowed in this state.
     */
    public synchronized void processReInvite(SipRequestEvent sipRequestEvent) {
        String errorMsg =
                "SIP re-INVITE request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP INFO request.
     * <p>
     * Since a SIP INFO is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP INFO request.
     * @throws IllegalStateException always since a SIP INFO is not
     * allowed in this state.
     */
    public synchronized void processInfo(SipRequestEvent sipRequestEvent) {
        String errorMsg =
                "SIP INFO request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP PRACK request.
     * <p>
     * Since a SIP PRACK is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP PRACK request.
     * @throws IllegalStateException always since a SIP PRACK is not
     * allowed in this state.
     */
    public synchronized void processPrack(SipRequestEvent sipRequestEvent) {
        String errorMsg =
                "SIP PRACK request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP OPTIONS request.
     * <p>
     * Since a SIP OPTIONS is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP OPTIONS request.
     * @throws IllegalStateException always since a SIP OPTIONS is not
     * allowed in this state.
     */
    public synchronized void processOptions(SipRequestEvent sipRequestEvent) {
        String errorMsg =
                "SIP OPTIONS request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP response.
     * Since a SIP response is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipResponseEvent carries the SIP response.
     * @throws IllegalStateException always since a SIP response is not
     * allowed in this state.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {
        String errorMsg =
                "SIP response is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a timeout event.
     * Since a timeout event is not allowed in this state
     * (if it happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipTimeoutEvent carries the information regarding the SIP timeout
     * event.
     * @throws IllegalStateException always since a timeout event is not
     * allowed in this state.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {
        String errorMsg = "SIP request/response timeout is not allowed in " +
                this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a Call timeout.
     * <p>
     * Since a Call timeout event is not allowed in this state
     * (if it happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param callTimeoutEvent carries the information regarding the Call timeout.
     * @throws IllegalStateException always since a Call timeout is not
     * allowed in this state.
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {
        String errorMsg = "A call timeout <" + callTimeoutEvent.getType() +
                "> was received but is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles the detection of an abandoned stream.
     * <p>
     * Since it is not allowed in this state (if it happens it suggests internal
     * implementational erro), an IllegalStateException is thrown.
     * @throws IllegalStateException always since detection of an abandoned
     * stream is not allowed in this state.
     */
    public synchronized void handleAbandonedStream() {
        String errorMsg =
                "A stream was detected abandoned. It is not allowed in " +
                this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }



    //========================= Private methods ===========================

    /**
     * Loads and starts a service instance.
     * <p>
     * The following error scenarios are handled:
     * <ul>
     * <li>
     * If the service could not be loaded, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     * A SIP "Server Internal Error" response is sent for the INVITE.
     * </li>
     * <li>
     * If the SIP error response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     * </li>
     * </ul>
     * @param sipRequestEvent carries the SIP INVITE request.
     * @throws IllegalStateException if the service could not be loaded.
     */
    private void loadService(SipRequestEvent sipRequestEvent)
            throws IllegalStateException {

        // TODO: Phase 2! Handle EE-indicated congestion!
        try {
            call.loadService();
            if (log.isInfoEnabled()) log.info("Service is loaded.");
        } catch (Exception e) {
            log.error("Could not load service " +
                    CMUtils.getInstance().getServiceName() +
                    ". A SIP 500 response will be sent.", e);

            call.errorOccurred(
                    "Service could not be loaded: " + e.getMessage(), false);

            // Send Server Internal Error response
            call.sendErrorResponse(
                    Response.SERVER_INTERNAL_ERROR, sipRequestEvent,
                    "Service could not be loaded.");

            throw(new IllegalStateException());
        }
    }

    /**
     * Sends a SIP "Trying" response for the initial INVITE request.
     * <p>
     * The following error scenarios are handled:
     * <ul>
     * <li>
     * If the SIP "Trying" response could not be sent, the error is logged,
     * an {@link ErrorEvent} is generated and the state is set to
     * {@link ErrorInboundState}.
     * </li>
     * </ul>
     * @param sipRequestEvent carries the SIP INVITE request.
     * @throws IllegalStateException if the SIP response could not be sent.
     */
    private void sendTryingResponse(SipRequestEvent sipRequestEvent)
            throws IllegalStateException {

        try {
            SipResponse sipResponse =
                    sipResponseFactory.createTryingResponse(
                            sipRequestEvent,
                            call.getConfig().getRegisteredName());
            sipMessageSender.sendResponse(sipResponse);
        } catch (Exception e) {
            call.errorOccurred(
                    "Could not send SIP \"Trying\" response: " + e.getMessage(),
                    false);
            throw(new IllegalStateException());
        }
    }

}
