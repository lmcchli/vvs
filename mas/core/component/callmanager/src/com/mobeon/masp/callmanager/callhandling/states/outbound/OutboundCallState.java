/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia.PEarlyMediaTypes;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.NotAllowedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.SendTokenEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.DialEvent;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.releasecausemapping.Q850CauseLocationPair;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.PlayFailedEvent;
import com.mobeon.masp.stream.RecordFailedEvent;

import javax.sip.address.SipURI;
import javax.sip.ClientTransaction;
import javax.sip.header.Header;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * Base class that represents a state for an outbound call. Contains the
 * default behavior for the actions that can occur for an outbound call.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public abstract class OutboundCallState extends CallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    /** The Call that this state belongs to. */
    protected final OutboundCallInternal call;

    public OutboundCallState(OutboundCallInternal call) {
        super(call);
        this.call = call;
    }

    /**
     * Handles an administrators lock request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     */
    public abstract void processLockRequest();

    /**
     * Handles a request to play media.
     * <p>
     * Play media can only be done in {@link ConnectedOutboundState}.
     * If requested in another state, a {@link PlayFailedEvent} is generated.
     * @param playEvent carries information regarding the play request.
     */
    public synchronized void play(PlayEvent playEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to play media was received in " + this +
                    ". It is not allowed in this state!");

        call.fireEvent( new PlayFailedEvent(
                playEvent.getId(), "Could not play media in " + this + "."));
    }

    /**
     * Handles a request to record media.
     * <p>
     * Recording media can only be done in {@link ConnectedOutboundState}.
     * If requested in another state, a {@link RecordFailedEvent} is generated.
     * @param recordEvent carries information regarding the play request.
     */
    public synchronized void record(RecordEvent recordEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to record media was received in " + this +
                    ". It is not allowed in this state!");

        call.fireEvent( new RecordFailedEvent(
                recordEvent.getId(), RecordFailedEvent.CAUSE.EXCEPTION,
                "Could not play media in " + this + "."));
    }

    /**
     * Handles a request to stop play media.
     * <p>
     * Play media can only be done in {@link ConnectedOutboundState}.
     * If stop playing media is requested in another state, it is simply ignored.
     * @param stopPlayEvent carries information regarding the stop play request.
     */
    public synchronized void stopPlay(StopPlayEvent stopPlayEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to stop an ongoing play was received in " +
                    this + ". It is ignored since it makes no sense in this state!");
    }

    /**
     * Handles a request to stop recording media.
     * <p>
     * Recoding media can only be done in {@link ConnectedOutboundState}.
     * If stop recording media is requested in another state, it is simply ignored.
     * @param stopRecordEvent carries information regarding the stop record
     * request.
     */
    public synchronized void stopRecord(StopRecordEvent stopRecordEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to stop an ongoing record was received in " +
                    this + ". It is ignored since it makes no sense in this state!");
    }

    /**
     * Handles a request to send a Video Fast Update request.
     * A Video Fast Update request can only be sent in
     * {@link ConnectedOutboundState}. If received in another state, it is
     * simply ignored.
     */
    public synchronized void processVideoFastUpdateRequest() {
        if (log.isDebugEnabled())
            log.debug("Request to send a Video Fast Update request was received in " +
                      this + ".");
        if (log.isInfoEnabled()) log.info("Request to send a Video Fast Update request is received " +
                                          "and ignored since media is not available in the current call state.");
    }

    /**
     * Disconnects the call.
     * Only makes sense in the following states:
     * {@link ConnectedOutboundState} and {@link ProgressingOutboundState}.
     * <p>
     * In all states extending the {@link CallCompletedOutboundState} the call
     * is already disconnected, a {@link DisconnectedEvent} is generated
     * and the request is otherwise ignored.
     * <p>
     * This is considered the state default behavior and is implemented in this
     * base class.
     * <p>
     * If received in state {@link IdleOutboundState} it suggests an internal
     * error and an IllegalStateException is thrown.
     *
     * @param disconnectEvent carries the information regarding the disconnect.
     * @throws IllegalStateException when used in {@link IdleOutboundState}.
     */
    public synchronized void disconnect(DisconnectEvent disconnectEvent) {
        if (log.isDebugEnabled())
            log.debug("Disconnect received in " + this + ".");
        if (log.isInfoEnabled()) log.info("The service is disconnecting the call. It is ignored since " +
                                          "the call already is disconnected or is about to disconnect.");
        call.fireEvent(new DisconnectedEvent(
                call, DisconnectedEvent.Reason.NEAR_END, true));
    }

    /**
     * Initiates the setup of a new outbound call.
     * <p>
     * Only allowed in the following states: {@link IdleOutboundState}
     * <p>
     * Generates a {@link NotAllowedEvent} when used in another state
     * than those allowed.
     *
     * @param dialEvent
     */
    public synchronized void dial(DialEvent dialEvent) {
        String errorMsg = "Dial is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg);
        if (log.isInfoEnabled()) log.info("The service is dialing. It is not allowed " +
                                          "in current state.");
        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * Sends a token to the peer call party.
     * <p>
     * Only allowed in the following states: {@link ConnectedOutboundState}
     * <p>
     * Generates a {@link NotAllowedEvent} when used in another state
     * than those allowed.
     *
     * @param sendTokenEvent carries the information regarding the token to send.
     */
    public synchronized void sendToken(SendTokenEvent sendTokenEvent) {
        String errorMsg = "SendToken is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg);
        if (log.isInfoEnabled()) log.info("The service is sending tokens. It is not allowed " +
                                          "in current state.");
        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * Handles a SIP ACK request.
     * <p>
     * A SIP ACK request should only be received as a confirmation on
     * an accepted inbound INVITE or a re-INVITE.
     * Currently re-INVITEs are not supported, therefore a SIP ACK request
     * does not make sense in any outbound state. The SIP ACK request is
     * therefore ignored.
     * @param sipRequestEvent carries the SIP ACK request.
     */
    public synchronized void processAck(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP ACK request was received in " + this + ".");
        if (log.isInfoEnabled()) log.info(
                "SIP ACK request is received and ignored.");
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public abstract void processBye(SipRequestEvent sipRequestEvent);


    /**
     * A SIP CANCEL can only be sent by a caller (i.e. only relevant for
     * inbound calls), see section 15 in RFC 3261.
     * Therefore the SIP CANCEL request is ignored for all outbound states.
     * @param sipRequestEvent carries the SIP CANCEL request.
     */
    public synchronized void processCancel(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP CANCEL request was received in " + this + ". " +
                      "It is ignored since it makes no sense in this state!");
        if (log.isInfoEnabled()) log.info("SIP CANCEL request is ignored since it makes no sense that " +
                                          "a UAS sent it.");
    }

    /**
     * Parses a SIP INVITE request. This INVITE message cannot be a re-INVITE.
     * Not allowed in any outbound state.
     *
     * @param sipRequestEvent carries the SIP INVITE request.
     *
     * @throws IllegalStateException when used in any outbound state.
     */
    public synchronized void processInvite(SipRequestEvent sipRequestEvent) {
        String errorMsg = "SIP INVITE Request is not allowed in " + this + ".";
        if (log.isDebugEnabled())
            log.debug( errorMsg + " IllegalStateException is thrown.");
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Handles a SIP re-INVITE request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipRequestEvent carries the SIP re-INVITE request.
     */
    public abstract void processReInvite(SipRequestEvent sipRequestEvent);

    /**
     * Handles a SIP OPTIONS request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipRequestEvent
     */
    public abstract void processOptions(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP INFO request and performs certain actions depending on
     * state.
     * <p>
     * SIP INFO requests are only supported in when calls are joined.
     * @param sipRequestEvent
     */
    public synchronized void processInfo(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled()) {
            log.debug("SIP INFO request was received in " + this + ". " +
                    "It is rejected with a SIP 405 response.");
        }

        if (log.isInfoEnabled()) {
            log.info("SIP INFO request is rejected since it is not supported in current state.");
        }

        call.sendMethodNotAllowedResponse(sipRequestEvent);
    }

    /**
     * Parses a SIP PRACK request and performs certain actions depending on
     * state.
     * <p>
     * SIP PRACK requests are only supported when provisional responses has been
     * sent, otherwise they are rejected with a SIP 403 "Forbidden" response.
     * @param sipRequestEvent
     */
    public synchronized void processPrack(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled()) {
            log.debug("SIP PRACK request was received in " + this + ". " +
                    "It is rejected with a SIP 403 response.");
        }

        if (log.isInfoEnabled()) {
            log.info("SIP PRACK request is rejected since it is not " +
                    "supported in current state.");
        }

        call.sendErrorResponse(Response.FORBIDDEN, sipRequestEvent,
                "PRACK request received in a state where it cannot be handled.");
    }

    /**
     * Parses a SIP UPDATE request and performs certain actions depending on state.
     * <p>
     * SIP UPDATE requests are only supported when precondition and/or unicast is enabled,
     * otherwise they are rejected with a SIP 403 "Forbidden" response.
     * @param sipRequestEvent sipRequestEvent
     */
    public synchronized void processUpdate(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP UPDATE request was received in " + this + ". " + "It is rejected with a SIP 403 response.");

        if (log.isInfoEnabled())
            log.info("SIP UPDATE request is rejected since it is not supported in current state.");

        call.sendErrorResponse(Response.FORBIDDEN, sipRequestEvent, "UPDATE request received in a state where it cannot be handled.");
    }

    /**
     * Handles a SIP response.
     * <p>
     * The default behavior is that the SIP response is ignored.
     * A SIP response should only be received as a response to a sent
     * request and the default behavior represents no request sent.
     * <p>
     * For states where a request has been sent (e.g.
     * {@link DisconnectedLingeringByeOutboundState},
     * {@link ProgressingOutboundState} and
     * {@link ConnectedOutboundState}, the SIP response is NOT ignored.
     * @param sipResponseEvent carries the SIP response.
     */
    public abstract void processSipResponse(SipResponseEvent sipResponseEvent);

    /**
     * Handles a SIP timeout event (that occured when sending/receiving a SIP
     * request/response).
     * <p/>
     * <b>The timeout event can have one of two causes:</b>
     * <ul>
     * <li>TRANSACTION:<br>
     * This can occur for two possible reasons; a previously sent request has
     * timed out without a response or a transaction error has occured (i.e.
     * a request/response could not be sent/received over the socket).
     * </li>
     * <li>RETRANSMIT:<br>
     * The stack asks the transaction unit (TU), i.e. the Call Manager, to
     * retransmit a previously sent request/response. This should never occur
     * since the Call Manager implementation configures the SIP stack to handle
     * all retransmissions itself using the RETRANSMISSION_FILTER configuration
     * property.
     * </li>
     * </ul>
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public abstract void processSipTimeout(SipTimeoutEvent sipTimeoutEvent);

    /**
     * Handles a Call timeout.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param callTimeoutEvent carries information regarding the call timeout.
     */
    public abstract void handleCallTimeout(CallTimeoutEvent callTimeoutEvent);

    /**
     * Handles the detection of an abandoned stream.
     * <p>
     * The default behavior is that this event is ignored.
     */
    public synchronized void handleAbandonedStream() {
        if (log.isDebugEnabled())
            log.debug("A stream was detected abandoned in " + this + ".");
        if (log.isInfoEnabled()) log.info("A stream was detected abandoned. It is ignored.");
    }


    // ==================== Common Helper methods ========================

    /**
     * Handles a call rejected from peer.
     * <p>
     * The network status code is lookup based on the SIP response code or a
     * Q.850 cause/location pair if such exists.
     * A {@link FailedEvent} is generated (containing the network status code)
     * and the state is set to {@link FailedCompletedOutboundState}.
     * @param responseCode
     */
    protected void callRejected(Integer responseCode,
                                Q850CauseLocationPair q850Pair) {
        int networkStatusCode =
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        responseCode, q850Pair);

        if (log.isInfoEnabled()) log.info("Tried to setup a " + call.getCallProperties().getCallType() +
                                          " call but it was rejected by peer with a SIP " +
                                          responseCode + " response. The response is mapped to " +
                                          "network status code " + networkStatusCode + ".");

        call.setStateFailed(FailedOutboundState.FailedSubState.COMPLETED);

        FailedEvent failedEvent = new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.OUTBOUND,
                "Call rejected by far end with SIP response code " + responseCode,
                networkStatusCode);
        call.fireEvent(failedEvent);

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        call.deleteStreams();
    }

    /**
     * Handles a SIP INFO response received from peer.
     * @param sipResponseEvent
     */
    protected void processInfoResponse(SipResponseEvent sipResponseEvent) {
        if (log.isInfoEnabled()) log.info("Response to INFO request is parsed.");

        call.parseMediaControlResponse(sipResponseEvent.getSipMessage());
    }

    /**
     * Retrieves the remote party address.
     * <p>
     * If no remote party address could be found. an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorCompletedOutboundState}.
     * @return the retrieved remote party address.
     * @throws IllegalStateException if no remote party address could be found.
     */
    protected SipURI retrieveRemotePartyAddress() throws IllegalStateException {
        // Retrieve the remote party address. If null, Call Manager is not
        // registered towards any SSP yet.

        SipURI remotePartyUri;
        try {
            remotePartyUri = call.getNewRemoteParty();
        } catch (ParseException e) {
            call.errorOccurred(
                    "Could not call out since no remote party could be created.",
                    false);
            throw(new IllegalStateException());
        }

        if (remotePartyUri == null) {
            call.errorOccurred(
                    "Could not call out since no remote party was found. " +
                            "This only occurs if Call Manager is not " +
                            "registered with any SSP.",
                    false);
            throw(new IllegalStateException());
        }

        return remotePartyUri;
    }

    /**
     * Sends an SIP ACK request.
     * <p>
     * If the ACK could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     * @throws IllegalStateException if the ACK could not be sent.
     */
    protected void sendAckRequest() throws IllegalStateException {
        try {
            SipRequest sipRequest =
                    CMUtils.getInstance().getSipRequestFactory().
                            createAckRequest(call.getDialog());
            CMUtils.getInstance().getSipMessageSender().
                    sendRequestWithinDialog(call.getDialog(), sipRequest);
            if (log.isDebugEnabled())
                log.debug("SIP ACK request is sent.");
        } catch (Exception e) {
            boolean alreadyDisconnected =
                    this instanceof CallCompletedOutboundState;
            call.errorOccurred(
                    "SIP ACK request could not be sent. The call " +
                            "is considered completed. " + e.getMessage(),
                    alreadyDisconnected);
            throw(new IllegalStateException());
        }
    }

    /**
     * Sends a SIP BYE request.
     * <p>
     * If the BYE request could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     */
    protected void sendByeRequest() {
        try {
            SipRequest sipRequest = CMUtils.getInstance().
                    getSipRequestFactory().createByeRequest(
                    call.getDialog(), call.getPChargingVector());
            CMUtils.getInstance().getSipMessageSender().
                    sendRequestWithinDialog(call.getDialog(), sipRequest);
            if (log.isDebugEnabled())
                log.debug("SIP BYE request is sent.");

        } catch (Exception e) {
            call.errorOccurred("SIP BYE request could not be sent. " +
                    "The call is considered completed. " + e.getMessage(),
                    true);
        }
    }

    /**
     * Sends a CANCEL request.
     * <p>
     * If the SIP CANCEL could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     */
    protected void sendCancelRequest() {
        // Send CANCEL request
        try {
            SipRequest sipRequest =
                    CMUtils.getInstance().getSipRequestFactory().
                            createCancelRequest(call.getCurrentInviteTransaction());
            CMUtils.getInstance().getSipMessageSender().sendRequest(sipRequest);
            if (log.isDebugEnabled())
                log.debug("SIP CANCEL request is sent.");
        } catch (Exception e) {
            call.errorOccurred(
                    "SIP CANCEL request could not be sent. The call is " +
                            "considered completed. " + e.getMessage(),
                    true);
        }
    }

    /**
     * Sends a PRACK request.
     * <p>
     * If the SIP PRACK could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     */
    protected void sendPrackRequest(SipResponseEvent sipResponseEvent) {
        // Send PRACK request
        try {
            SipRequest sipRequest =
                    CMUtils.getInstance().getSipRequestFactory().
                            createPrackRequest(
                                    call.getDialog(),
                                    sipResponseEvent.getResponse(),
                                    call.getPChargingVector());

            if (log.isDebugEnabled())
                log.debug("SIP PRACK request created; now fixing the Allow Header");
            
            sipRequest.removeAllowHeader();
            if (log.isDebugEnabled())
                log.debug("Existing Allow Header removed");

            sipRequest.addAllowHeader();
            if (log.isDebugEnabled())
                log.debug("New (more restrictive) Allow Header added");
            
            CMUtils.getInstance().getSipMessageSender().
                    sendRequestWithinDialog(call.getDialog(), sipRequest);

            if (log.isDebugEnabled())
                log.debug("SIP PRACK request is sent.");

        } catch (Exception e) {
            call.errorOccurred(
                    "SIP PRACK request could not be sent. The call is " +
                            "considered completed. " + e.getMessage(),
                    false);
        }
    }

    /**
     * Creates and sends an initial INVITE request.
     * <p>
     * If the INVITE request could not be created, an {@link ErrorEvent}
     * is generated and the state is set to {@link ErrorCompletedOutboundState}.
     * If the INVITE request could not be sent, a {@link FailedEvent}
     * is generated and the state is set to {@link FailedCompletedOutboundState}.
     * @param remotePartyUri The URI to which the INVITE is addressed.
     * @param sdpOffer The SDP offer included in the request body.
     * @param callId
     * @throws IllegalStateException if the INVITE could not be created or sent.
     */
    protected void sendInviteRequest(
            SipURI remotePartyUri, String sdpOffer, String callId)
            throws IllegalStateException {

        SipRequest sipRequest;
        try {
            sipRequest = CMUtils.getInstance().getSipRequestFactory().createInviteRequest(
                    remotePartyUri,
                    call.getCalledParty(),
                    call.getCallingParty(),
                    call.getCallProperties().getDiversionParty(),
                    call.getConfig().getRegisteredName(),
                    call.getCallProperties().getMaxDurationBeforeConnected() / 1000 + 1,
                    call.getCallProperties().getPreventLoopback(),
                    sdpOffer, callId, call.getPChargingVector(),
                    call.getConfig().getRestrictedOutboundHeaders());

        } catch (Exception e) {
            call.errorOccurred(
                    "SIP INVITE request could not be created: " + e.getMessage(),
                    false);
            throw(new IllegalStateException());
        }

        call.dialogCreated(sipRequest);

        /**
         * For outbound calls (B2BUA), specifically in PROXY mode, the proxy must add a restricted header
         * (P-Early-Media) stating that no early media SHOULD be played on the UAS side.
         */
        if (ConfigurationReader.getInstance().getConfig().getApplicationProxyMode()) {
            Header pearlymedia = new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE);
            sipRequest.getRequest().addHeader(pearlymedia);
        }
        
        ClientTransaction transaction;
        try {
            transaction = CMUtils.getInstance().getSipMessageSender().sendRequest(
                    sipRequest);
        } catch (Exception e) {
            int responseCode = Response.SERVICE_UNAVAILABLE;
            if (log.isInfoEnabled()) log.info("Tried to setup a " + call.getCallProperties().getCallType() +
                                              " call but transaction error occurred. " +
                                              "It is handled as if it was rejected by peer with a SIP " +
                                              responseCode + " response.", e);

            callRejected(responseCode, null);

            throw(new IllegalStateException());
        }

        call.setCurrentInviteTransaction(transaction);
        call.setDialog(transaction.getDialog());
    }
}

