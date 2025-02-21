/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState.ProgressingSubState;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.DialEvent;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CallManagerLicensingException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.ConnectionProperties;

import javax.sip.address.SipURI;

/**
 * Represents the outbound state Idle.
 * This is the first outbound state, i.e. the state entered
 * when an outbound call is initiated.
 * <p>
 * The purpose of this state is to handle a dial, i.e. a request
 * to initiate the call setup of a new outbound call. All other methods are
 * illegal in this state and throws IllegalStateException.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class IdleOutboundState extends OutboundCallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public IdleOutboundState(OutboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Idle state";
    }

    /**
     * Handles an administrators lock request.
     * <p>
     * Since an INVITE has not yet been sent, nothing is done but setting the
     * state to {@link FailedCompletedOutboundState} and generating a
     * {@link FailedEvent}.
     */
    public synchronized void processLockRequest() {
        if (log.isDebugEnabled())
            log.debug("Lock requested in " + this + ".");

        call.setStateFailed(FailedOutboundState.FailedSubState.COMPLETED);

        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call rejected due to lock request.",
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null, null)));
    }

    /**
     * Handles a call disconnect from the Call Manager client.
     * <p>
     * Since an INVITE has not yet been sent, nothing is done but setting the
     * state to {@link FailedCompletedOutboundState} and generating a
     * {@link FailedEvent} and {@link DisconnectedEvent}.
     *
     * @param disconnectEvent carries the disconnect request.
     */
    public synchronized void disconnect(DisconnectEvent disconnectEvent) {
        if (log.isDebugEnabled())
            log.debug("Disconnect requested in " + this + ".");

        call.setStateFailed(FailedOutboundState.FailedSubState.COMPLETED);

        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND, "Call disconnected by near end.",
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null, null)));

        // This is because EE expects one event per called method, but it does
        // not conform with the CCXML standard
        call.fireEvent(new DisconnectedEvent(
                call, DisconnectedEvent.Reason.NEAR_END, true));
    }

    /**
     * Initiates the call setup of a new outbound call.
     * <ul>
     * <li>
     * An inbound stream is created using {@link #createInboundStream()}.
     * </li>
     * <li>
     * An SDP offer is created using {@link #createSdpOffer(ConnectionProperties)}.
     * </li>
     * <li>
     * The remote party address is retrieved using
     * {@link #retrieveRemotePartyAddress()}.
     * </li>
     * <li>
     * A SIP INVITE request is sent using
     * {@link OutboundCallState#sendInviteRequest(javax.sip.address.SipURI,String,String)}.
     * </li>
     * <li>
     * The call is registered to receive events using
     * {@link OutboundCallInternal#registerToReceiveEvents()}
     * </li>
     * <li>
     * The
     * {@link com.mobeon.masp.callmanager.CallProperties#maxDurationBeforeConnected}
     * timer is started.
     * </li>
     * <li>
     * The state is set to {@link ProgressingCallingOutboundState}.
     * </li>
     * </ul>
     * @param dialEvent
     */
    public synchronized void dial(DialEvent dialEvent) {
        if (log.isDebugEnabled())
            log.debug("Dial initiated.");

        try {
            // Create the inbound stream.
            ConnectionProperties inboundConnectionProperties =
                    createInboundStream();

            if (log.isDebugEnabled())
                log.debug("Inbound stream is created.");

            // Create an SDP offer.
            String sdpOffer = createSdpOffer(inboundConnectionProperties);

            if (log.isDebugEnabled())
                log.debug("SDP offer is created.");

            // Retrieve the remote party address.
            SipURI remotePartyUri = retrieveRemotePartyAddress();
            if (log.isDebugEnabled())
                log.debug("Remote party uri has been retrieved: " +
                          remotePartyUri);

            if (log.isInfoEnabled()) log.info("Sending INVITE to remote party: " + remotePartyUri);

            ((com.mobeon.masp.callmanager.callhandling.CallImpl) call).enterSipInviteRingAckKpiCheckpoint(); // KPI
            // Send INVITE request
            sendInviteRequest(remotePartyUri, sdpOffer, call.getCallId());

            if (log.isDebugEnabled())
                log.debug("SIP INVITE request is sent.");

            // Register to receive events
            call.registerToReceiveEvents();

            if (log.isDebugEnabled())
                log.debug("Call is registered to receive events.");

            call.startNotConnectedTimer();

            call.setStateProgressing(ProgressingSubState.CALLING);

        } catch (IllegalStateException e) {
            if (log.isDebugEnabled())
                log.debug("Outbound call failed.");
        }
    }

    /**
     * Handles a SIP ACK request.
     * <p>
     * Since a SIP ACK is not allowed in this state (if it happens
     * it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP ACK request.
     *
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
     * Handles a SIP re-INVITE request.
     * <P>
     * Since a SIP re-INVITE is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipRequestEvent carries the SIP re-INVITE request.
     * @throws IllegalStateException always since a SIP re-INVITE is not
     * allowed in this state.
     */
    public synchronized void processReInvite(SipRequestEvent sipRequestEvent) {
        String errorMsg = "SIP re-INVITE request is not allowed in " +
                this + ".";
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
        String errorMsg = "SIP OPTIONS request is not allowed in " +
                this + ".";
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
        String errorMsg = "SIP INFO request is not allowed in " +
                this + ".";
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
        String errorMsg = "SIP PRACK request is not allowed in " +
                this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP response.
     * <p>
     * Since a SIP response is not allowed in this state (if it
     * happens it suggests internal implementational error),
     * IllegalStateException is thrown.
     *
     * @param sipResponseEvent carries the SIP response.
     * @throws IllegalStateException always since a SIP response is not
     * allowed in this state.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {
        String errorMsg = "SIP response is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a timeout event.
     * <p>
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

    //========================= Private methods ===========================

    /**
     * Creates an inbound media stream and returns the connection properties for
     * the new stream.
     * <p>
     * If the stream could not be created an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     * @throws IllegalStateException if the stream could not be created.
     */
    private ConnectionProperties createInboundStream()
            throws IllegalStateException {

        ConnectionProperties inboundConnectionProperties;
        try {
            // In this case (offer created locally) we do not have an SDP
            // intersection so we pass null instead.
            inboundConnectionProperties = call.createInboundStream(null);
        } catch(CallManagerLicensingException e) {

            call.setStateFailed(FailedOutboundState.FailedSubState.COMPLETED);

            call.fireEvent(new FailedEvent(
                    call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                    CallDirection.OUTBOUND, "No license available",
                    call.getConfig().getReleaseCauseMapping().getNoLicenseNetworkStatusCode()));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();


            throw(new IllegalStateException());

        } catch (Exception e) {
            call.errorOccurred(
                    "Could not create inbound stream: " + e.getMessage(), false);
            throw(new IllegalStateException());
        }
        return inboundConnectionProperties;
    }

    /**
     * Creates an SDP offer based on the connection properties for the inbound
     * media stream.
     * <p>
     * If the SDP offer could not be created an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     * @param inboundConnectionProperties Properties for the inbound media stream.
     * @return the created SDP offer.
     * @throws IllegalStateException if the SDP offer could not be created.
     */
    private String createSdpOffer(
            ConnectionProperties inboundConnectionProperties)
            throws IllegalStateException {

        String sdpOffer;
        try {
            sdpOffer = call.createSdpOffer(inboundConnectionProperties);
        } catch (SdpInternalErrorException e) {
            call.errorOccurred("Could not create SDP offer: " + e.getMessage(), false);
            throw(new IllegalStateException());
        }
        return sdpOffer;
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

}


