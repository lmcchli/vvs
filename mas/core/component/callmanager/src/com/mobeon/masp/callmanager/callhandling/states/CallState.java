/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states;

import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.events.SipEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.InboundCallState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.OutboundCallState;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask.Type;
import com.mobeon.masp.callmanager.callhandling.CallInternal;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.contact.Contact;
import com.mobeon.masp.callmanager.sip.message.SipResponseFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.TreeSet;


/**
 * Abstract base class that represents a state for an inbound or outbound
 * call.
 * <p>
 * See {@link InboundCallState} for a description on the inbound call states.
 * See {@link OutboundCallState} for a description on the outbound call states.
 *
 * @author Malin Flodin
 */
public abstract class CallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    /** Message sender used when sending SIP requests or responses in states.*/
    protected final SipMessageSender sipMessageSender =
            CMUtils.getInstance().getSipMessageSender();

    /** Response factory used when creating SIP responses in states.*/
    protected final SipResponseFactory sipResponseFactory =
            CMUtils.getInstance().getSipResponseFactory();

    /** Request factory used when creating SIP requests in states.*/
    protected final SipRequestFactory sipRequestFactory =
            CMUtils.getInstance().getSipRequestFactory();

    /** The Call that this state belongs to. */
    private CallInternal call;

    public CallState(CallInternal call) {
        this.call = call;
    }

    /**
     * Adminstrator has requested a lock and the call will disconnect as soon
     * as possible regardless of state.
     */
    public abstract void processLockRequest();

    /**
     * The system has requested to play a media object.
     * @param playEvent
     */
    public abstract void play(PlayEvent playEvent);

    /**
     * The system has requested to record a media object.
     * @param recordEvent
     */
    public abstract void record(RecordEvent recordEvent);

    /**
     * The system has requested to stop an ongoing play.
     * @param stopPlayEvent
     */
    public abstract void stopPlay(StopPlayEvent stopPlayEvent);

    /**
     * The system has requested to stop an ongoing record.
     * @param stopRecordEvent
     */
    public abstract void stopRecord(StopRecordEvent stopRecordEvent);

    /**
     * Either sends a Picture Fast Update request using the SIP INFO method
     * or ignores the request, which one depends on call state. A Picture Fast
     * Update request is only sent in Connected state and ignored otherwise.
     */
    public abstract void processVideoFastUpdateRequest();


    /**
     * Parses a SIP ACK request and performs certain actions depending on state.
     *
     * @param sipRequestEvent carries the SIP ACK request.
     */
    public abstract void processAck(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP BYE request and performs certain actions depending on state.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public abstract void processBye(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP CANCEL request and performs certain actions depending on
     * state.
     *
     * @param sipRequestEvent carries the SIP CANCEL request.
     */
    public abstract void processCancel(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP INVITE request and performs certain actions depending on
     * state. This INVITE message cannot be a re-INVITE.
     *
     * @param sipRequestEvent carries the SIP INVITE request.
     */
    public abstract void processInvite(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP INVITE request and performs certain actions depending on
     * state. This INVITE message can only be a re-INVITE, not a session
     * creating INVITE.
     *
     * @param sipRequestEvent carries the SIP RE-INVITE request.
     */
    public abstract void processReInvite(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP OPTIONS request and performs certain actions depending on
     * state.
     * @param sipRequestEvent
     */
    public abstract void processOptions(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP INFO request and performs certain actions depending on
     * state.
     * @param sipRequestEvent
     */
    public abstract void processInfo(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP PRACK request and performs certain actions depending on state.
     *
     * @param sipRequestEvent carries the SIP PRACK request.
     */
    public abstract void processPrack(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP UPDATE request and performs certain actions depending on state.
     *
     * @param sipRequestEvent carries the SIP UPDATE request.
     */
    public abstract void processUpdate(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP response and performs certain actions depending on state.
     *
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
     * @param sipTimeoutEvent carries information regarding the SIP timeout event.
     */
    public abstract void processSipTimeout(SipTimeoutEvent sipTimeoutEvent);

    /**
     * Handles a call timeout event.
     * <p>
     * The following call timeouts exist for inbound calls:
     * <ul>
     * <li>
     * {@link Type.CALL_NOT_CONNECTED} that expires if the call is not connected
     * in time.
     * </li>
     * <li>
     * {@link Type.MAX_CALL_DURATION} that expires when the call lasts longer
     * than the maximum allowed call duration.
     * </li>
     * </ul>
     * <p>
     * The following call timeouts exist for outbound calls:
     * <ul>
     * <li>
     * TODO: Drop 5! Document when EE timeout has been implemented.
     * </li>
     * </ul>
     * @param callTimeoutEvent carries information regarding the call timeout.
     */
    public abstract void handleCallTimeout(CallTimeoutEvent callTimeoutEvent);

    /**
     * Handles the detection of an abandoned inbound stream.
     * <p>
     * If the call is in such a state that the inbound stream is started and
     * should receive data, the call is disconnected, otherwise the event is
     * ignored.
     * The inbound stream is started and should receive data in the following
     * states:
     * {@link com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState},
     * {@link com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingEarlyMediaOutboundState},
     * and
     * {@link com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState}.
     */
    public abstract void handleAbandonedStream();

    /**
     * Adds the information in the <code>SipEvent</code>s <code>Contact</code> header to the <code>Set</code> of
     * far end <code>Connection</code>s.
     * @param sipEvent the event to retrieve the far end connection information from
     */
    protected void addFarEndConnection(SipEvent sipEvent) {
        // Store contact in far end connections
        TreeSet<Contact> contacts = sipEvent.getSipMessage().getContacts(null);
        if (!contacts.isEmpty()) {
            Contact contact = contacts.first();
            addFarEndConnection("SIP", contact.getSipUri().getHost(), contact.getSipUri().getPort());
        }
    }

    /**
     * Adds a far end connection to the outbound call
     * @param protocol the protocol of the far end connection
     * @param host the host of the far end connection
     * @param port the port of the far end connection
     */
    private void addFarEndConnection(String protocol, String host, int port) {
        call.addFarEndConnection(protocol, host, port);
    }

    // ==================== Common Helper methods ========================

    /**
     * Retrieves and stores the remote SDP (if any) of the given SIP request.
     * <p>
     * The following error scenarios are handled:
     * <ul>
     * <li>
     * If the SDP could not be parsed, a SIP "Not Acceptable Here" response
     * is sent.
     * </li>
     * <li>
     * If the new SDP is not equal to the original remote SDP, 
     * a SIP "Not Acceptable Here" response is sent.
     * </li>
     * <li>
     * If the SIP error response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to completed error state.
     * </li>
     * </ul>
     * @param sipRequestEvent carries the SIP request with the new SDP.
     * @throws IllegalStateException if an error occurs while retrieving the
     * SDP offer.
     */
    protected void retrieveAndStoreRemoteSdpAfterConnect(
            SipRequestEvent sipRequestEvent) throws IllegalStateException {
        try {
            call.parseRemoteSdp(sipRequestEvent.getSipMessage());
            call.checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp();
        } catch (SdpNotSupportedException e) {
            // SDP offer could not be parsed
            String message =
                    "Could not parse remote SDP: " + e.getMessage() +
                    ". A SIP 488 response will be sent.";
            if (log.isInfoEnabled()) log.info(message);

            // Send NotAcceptableHere response
            call.sendNotAcceptableHereResponse(sipRequestEvent, e.getSipWarning());

            throw(new IllegalStateException(message));
        }
    }

    protected void retrieveStoreAndOnlyVerifyMinimalRemoteSdpAfterConnect(
            SipRequestEvent sipRequestEvent) throws IllegalStateException {
        try {
            call.parseRemoteSdp(sipRequestEvent.getSipMessage());
            call.checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp();
        } catch (SdpNotSupportedException e) {
            // SDP offer could not be parsed
            String message =
                    "Could not successfully parse remote SDP: " + e.getMessage() +
                    ". A SIP 488 response will be sent.";
            if (log.isInfoEnabled()) log.info(message);

            // Send NotAcceptableHere response
            call.sendNotAcceptableHereResponse(sipRequestEvent, e.getSipWarning());

            throw(new IllegalStateException(message));
        }
    }

    /**
     * Sends a SIP "OK" response for the given <param>sipRequestEvent</param>.
     * <p>
     * If the SIP "OK" response could not be sent, an error is reported.
     *
     * @param sipRequestEvent   Contains the request to respond to.
     * @param sdp               The SDP sent in the response body.
     * @throws IllegalStateException if the SIP response could not be sent.
     */
    protected void sendOkResponse(SipRequestEvent sipRequestEvent, String sdp)
            throws IllegalStateException {

        try {
            SipResponse sipResponse =
                    CMUtils.getInstance().getSipResponseFactory().
                            createOkResponse(sipRequestEvent, sdp,
                                    call.getConfig().getRegisteredName());
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);

        } catch (Exception e) {
            call.errorOccurred(
                    "Could not send SIP \"Ok\" response: " + e.getMessage(),
                    false);
            throw(new IllegalStateException());
        }
    }
}
