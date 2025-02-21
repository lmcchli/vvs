/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.header.PEarlyMediaHeader;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia.PEarlyMediaTypes;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.events.NotAllowedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.ProxyEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.RedirectEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.NegotiateEarlyMediaTypesEvent;
import com.mobeon.masp.callmanager.configuration.EarlyMediaHeaderUsage;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.PlayFailedEvent;
import com.mobeon.masp.stream.RecordFailedEvent;

import gov.nist.javax.sip.header.ExtensionHeaderImpl;

import javax.sip.header.ExtensionHeader;
import javax.sip.message.Response;

import java.text.ParseException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Base class that represents a state for an inbound call. Contains the
 * default behavior for the actions that can occur for an inbound call.
 * <p>
 * All inbound states implements the toString method containing the state name.
 * For example, {@link AlertingAcceptingInboundState#toString()} gives the
 * following state name: <code>"Alerting state (sub state Accepting)"</code>
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public abstract class InboundCallState extends CallState {

    /** Strings used by the Call Manager client for passing proprietary headers in SIP responses */ 
    protected static final String PROPRIETARY_HEADER_FOR_SESSION_PROGRESS_RESPONSE = "addProprietaryHeaderForSessionProgressResponse";  
    protected static final String PROPRIETARY_HEADER_FOR_INITIAL_INVITE_200OK_RESPONSE = "addProprietaryHeadersForInitialInvite200OkResponse";
    
    /** The Call that this state belongs to. */
    protected final InboundCallInternal call;

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    InboundCallState(InboundCallInternal call) {
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
     * Play media can only be done in {@link ConnectedInboundState}
     * and {@link AlertingNewCallInboundState} (for early media).
     * If requested in another state, a {@link PlayFailedEvent} is generated.
     * @param playEvent carries information regarding the play request.
     */
    public synchronized void play(PlayEvent playEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to play media was received in " + this +
                    ". It is not allowed in this state.");

        call.fireEvent( new PlayFailedEvent(
                playEvent.getId(), "Could not play media in " + this + "."));
    }

    /**
     * Handles a request to record media.
     * <p>
     * Recording media can only be done in {@link ConnectedInboundState}.
     * If requested in another state, a {@link RecordFailedEvent} is generated.
     * @param recordEvent carries information regarding the play request.
     */
    public synchronized void record(RecordEvent recordEvent) {
        if (log.isDebugEnabled())
            log.debug("Request to record media was received in " + this +
                    ". It is not allowed in this state.");

        call.fireEvent( new RecordFailedEvent(
                recordEvent.getId(), RecordFailedEvent.CAUSE.EXCEPTION,
                "Could not play media in " + this + "."));
    }

    /**
     * Handles a request to stop play media.
     * <p>
     * Play media can only be done in {@link ConnectedInboundState}.
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
     * Recoding media can only be done in {@link ConnectedInboundState}.
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
     * {@link ConnectedInboundState}. If received in another state, it is simply
     * ignored.
     */
    public synchronized void processVideoFastUpdateRequest() {
        if (log.isDebugEnabled())
            log.debug("Request to send a Video Fast Update request was received in " +
                      this + ".");
        if (log.isInfoEnabled())
            log.info("Request to send a Video Fast Update request is received " +
                    "and ignored since media is not available in the current call state.");
    }

    /**
     * This method is used when the Call Manager client accepts the inbound
     * call.
     * <p>
     * Only allowed in {@link AlertingNewCallInboundState}.
     * A {@link NotAllowedEvent} is generated when used in another inbound state
     * than those allowed.
     *
     * @param acceptEvent carries the information regarding the accept event.
     */
    public synchronized void accept(AcceptEvent acceptEvent) {
        String errorMsg = "Accept is not allowed in " + this + ".";

        if (log.isDebugEnabled())
            log.debug(errorMsg);

        if (log.isInfoEnabled())
            log.info("The service is accepting the call. It is not allowed in " +
                    "current state.");

        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * This method is used when the Call Manager client proxies the inbound call.
     * <p>
     * Only allowed in {@link AlertingNewCallInboundState}.
     * A {@link NotAllowedEvent} is generated when used in another inbound state than those allowed.
     *
     * @param proxyEvent carries the information regarding the proxy event.
     */
    public synchronized void proxy(ProxyEvent proxyEvent) {
        String errorMsg = "Proxy is not allowed in " + this + ".";

        if (log.isDebugEnabled())
            log.debug(errorMsg);

        if (log.isInfoEnabled())
            log.info("The service is proxying the call. It is not allowed in " +
                    "current state.");

        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * This method is used when the Call Manager client wants initiate a media
     * type negotiation prior to accepting the call in order to be able to play
     * early media on the call.
     * <p>
     * Only effective in {@link AlertingNewCallInboundState} in which an
     * {@link EarlyMediaAvailableEvent} is generated when this method has
     * completed.
     * If received in another state a {@link NotAllowedEvent} is generated.
     * @param event carries the information regarding the negotiation of early
     * media types.
     */
    public synchronized void negotiateEarlyMediaTypes(
            NegotiateEarlyMediaTypesEvent event) {
        String errorMsg = "Negotiate early media types is not allowed in " +
                          this + ".";
        if (log.isDebugEnabled())
            log.debug(errorMsg);

        if (log.isInfoEnabled())
            log.info("The service is negotiating early media. It is not allowed " +
                    "in current state.");
        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * This method is used when the Call Manager client rejects the inbound
     * call.
     * <p>
     * Only allowed in {@link AlertingNewCallInboundState}.
     * A {@link NotAllowedEvent} is generated when used in another inbound state
     * than those allowed.
     *
     * @param rejectEvent carries the information regarding the reject event.
     */
    public synchronized void reject(RejectEvent rejectEvent) {
        notAllowed("Reject");
    }

    private void notAllowed(String actionName) {
        String errorMsg = actionName + " is not allowed in " + this + ".";

        if (log.isDebugEnabled())
            log.debug(errorMsg);

        if (log.isInfoEnabled())
            log.info("The service is rejecting the call. It is not allowed in " +
                    "current state.");
        call.fireEvent(new NotAllowedEvent(call, errorMsg));
    }

    /**
     * Disconnects the active call.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     */
    public abstract void disconnect(DisconnectEvent disconnectEvent);

    /**
     * Handles a SIP ACK request.
     * <p>
     * A SIP ACK request should only be received as a confirmation on
     * an INVITE or re-INVITE that was accepted.
     * Currently re-INVITEs are handled in some scenarios, but the 
     * details around the SIP ACK request is not of interest. 
     * The details are only of interest for the initial INVITE, 
     * i.e. in the following states:
     * {@link AlertingAcceptingInboundState},
     * For other states, the SIP ACK request is logged.
     * @param sipRequestEvent carries the SIP ACK request.
     */
    public synchronized void processAck(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP ACK request was received in " + this + ".");

        if (log.isInfoEnabled())
            log.info("SIP ACK request is received and ignored.");
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public abstract void processBye(SipRequestEvent sipRequestEvent);

    /**
     * Handles a SIP CANCEL request.
     * <p>
     * There is no common default behavior. This method is therefore abstract.
     * @param sipRequestEvent carries the SIP CANCEL request.
     */
    public abstract void processCancel(SipRequestEvent sipRequestEvent);

    /**
     * Parses a SIP INVITE request and performs certain actions depending on
     * state. This INVITE message cannot be a re-INVITE.
     * Only allowed in the following states: {@link IdleInboundState}.
     *
     * @param sipRequestEvent carries the SIP INVITE request.
     *
     * @throws IllegalStateException when used in another inbound state than
     * those allowed.
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
     * SIP INFO requests are only supported when the call is joined. 
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
     * {@link DisconnectedLingeringByeInboundState} and
     * {@link ConnectedInboundState}, the SIP response is NOT ignored.
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {
        if (log.isDebugEnabled())
            log.debug("A SIP response " + sipResponseEvent.getResponseCode() +
                    " for a " + sipResponseEvent.getMethod() +
                    " request was received in " + this +
                    ". It is ignored since it makes no sense in this state!");
    }

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
            log.debug("A stream was detected abandoned in " + this +
                    ". It is ignored.");
    }

    // ==================== Common Helper methods ========================

    /**
     * Handles a SIP INFO response received from peer.
     * @param sipResponseEvent
     */
    protected void processInfoResponse(SipResponseEvent sipResponseEvent) {
        if (log.isInfoEnabled()) log.info("Response to INFO request is parsed.");

        call.parseMediaControlResponse(sipResponseEvent.getSipMessage());
    }

    /**
     * Retrieves and stores the remote SDP (if any) of the given SIP request.
     * <p>
     * The following error scenarios are handled:
     * <ul>
     * <li>
     * If the SDP could not be parsed, a SIP "Not Acceptable Here" response
     * is sent, a {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedInboundState}.
     * </li>
     * <li>
     * If the SIP error response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     * </li>
     * </ul>
     * @param sipRequestEvent   carries the SIP INVITE request.
     * @param equalPreviousSdp  indicates if there is a requirement that this Sdp 
     *                          must equal a previously received one
     * @throws IllegalStateException if an error occurs while retrieving the
     * SDP offer or if the sdp does not equal a previously received one although it 
     * is required.
     */
    protected void retrieveAndStoreRemoteSdpPriorToAccept(
            SipRequestEvent sipRequestEvent, boolean equalPreviousSdp) 
    throws IllegalStateException {
        try {
            call.parseRemoteSdp(sipRequestEvent.getSipMessage());
            if (equalPreviousSdp)
                call.checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp();
        } catch (SdpNotSupportedException e) {
            // SDP offer could not be parsed
            String message =
                    "Could not parse remote SDP: " + e.getMessage() +
                    ". A SIP 488 response will be sent.";
            if (log.isInfoEnabled()) log.info(message);

            // Set the call to failed
            call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
            call.fireEvent(new FailedEvent(
                    call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                    CallDirection.INBOUND, message,
                    call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null, null)));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            // Send NotAcceptableHere response
            call.sendNotAcceptableHereResponse(
                    call.getInitialSipRequestEvent(), e.getSipWarning());

            throw(new IllegalStateException(message));
        }
    }

    /**
     * Sends a SIP BYE request.
     * <p>
     * If the BYE request could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorInboundState}.
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
     * Sends a SIP "OK" response for the initial INVITE request.
     * <p>
     * If the SIP "OK" response could not be sent, an error is reported.
     * @param sdpAnswer The SDP answer sent in the response body.
     * @throws IllegalStateException if the SIP response could not be sent.
     */
    protected void sendOkResponse(String sdpAnswer) throws IllegalStateException {
        sendOkResponse(sdpAnswer, null);
    }

    /**
     * Sends a SIP "OK" response for the initial INVITE request.
     * <p>
     * If the SIP "OK" response could not be sent, an error is reported.
     * @param sdpAnswer The SDP answer sent in the response body.
     * @param extensionHeaderList The Proprietary Header(s) sent in the response body
     * @throws IllegalStateException if the SIP response could not be sent.
     */
    protected void sendOkResponse(String sdpAnswer, List<String> extensionHeaderList) throws IllegalStateException {
        try {
            SipResponse sipResponse =
                    CMUtils.getInstance().getSipResponseFactory().
                            createOkResponse(
                                    call.getInitialSipRequestEvent(),
                                    sdpAnswer,
                                    call.getConfig().getRegisteredName());

            if (extensionHeaderList != null) {
                // Add any extension headers that have been passed by calling method
                Iterator<String> extensionHeaderIterator = extensionHeaderList.iterator();
                while(extensionHeaderIterator.hasNext()) {
                     String proprietaryHeaderForResponseKey = extensionHeaderIterator.next();

                     Object tmp = call.getSession().getData(proprietaryHeaderForResponseKey);
                     if(tmp != null) {
                         if (tmp instanceof ExtensionHeader[]) {
                             ExtensionHeader[] headers = (ExtensionHeader[]) tmp;

                             if (log.isDebugEnabled()) {
                                 log.debug("Retrieved from session: " + proprietaryHeaderForResponseKey +"=" +  Arrays.toString(headers));
                             }

                             for (ExtensionHeader header : headers) {
                                 //add header to response
                                 sipResponse.addExtensionHeader(header);
                             }
                         }
                     }
                }
            }

            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            call.errorOccurred(
                    "Could not send SIP \"Ok\" response: " + e.getMessage(),
                    false);
            throw(new IllegalStateException());
        }
    }

    /**
     * This method is used when the Call Manager client redirects the inbound
     * call.
     * <p>
     * Only allowed in {@link AlertingNewCallInboundState}.
     * A {@link NotAllowedEvent} is generated when used in another inbound state
     * than those allowed.
     * @param redirectEvent
     */
    public void redirect(RedirectEvent redirectEvent) {
        notAllowed("Redirect");
                
    }

    /**
     * This method sets the <b>P-Early-Media header</b> in the sipResponse based on following cases:
     * 
     * <p>
     * <table border=1>
     * <tr><td><b>pEarlyMediaHeaderInSipResponse config value</b></td><td><b>SIP Incoming P-Early-Media header</b></td><td><b>Early media scenario</b></td><td><b>P-Early-Media header</b></td></tr>
     * <tr><td>{@link EarlyMediaHeaderUsage.OFF}</td><td>[anything]</td><td>yes/no</td><td>Nothing. Call flow COULD add a header</td></tr>
     * <tr><td rowspan=3>{@link EarlyMediaHeaderUsage.SUPPORTED}</td><td rowspan=2>'supported'</td><td>yes</td><td>'sendrecv'</td></tr>
     * <tr><td>no</td><td>'inactive'</td></tr>
     * <tr><td>[anything else]</td><td>yes/no</td><td>Nothing. Call flow COULD add a header</td></tr>
     * <tr><td rowspan=2>{@link EarlyMediaHeaderUsage.FORCED}</td><td rowspan=2>[anything]</td><td>yes</td><td>'sendrecv'</td></tr>
     * <tr><td>no</td><td>'inactive'</td></tr>
     * </table>
     * 
     * @param sipResponse The SIP Response that will include the P-Early-Media header
     * @param earlyMedia If this is an Early Media scenario
     * @throws java.text.ParseException if the header field could not be created.
     */
    protected void processPEarlyMediaInInitialRequest(SipResponse sipResponse, boolean earlyMedia) throws ParseException {

        EarlyMediaHeaderUsage earlyMediaHeaderUsage = call.getConfig().getPEarlyMediaHeaderInSipResponse();
        if (EarlyMediaHeaderUsage.OFF == earlyMediaHeaderUsage) {
            return;
        } else if (EarlyMediaHeaderUsage.SUPPORTED == earlyMediaHeaderUsage) {
            ExtensionHeaderImpl extensionHeaderEarlyMedia = (ExtensionHeaderImpl)call.getInitialSipRequestEvent().getRequest().getHeader(PEarlyMediaHeader.NAME);
            if (extensionHeaderEarlyMedia != null) {
                String value = extensionHeaderEarlyMedia.getValue();
                if (PEarlyMediaTypes.PEARLY_MEDIA_SUPPORTED.getValue().equalsIgnoreCase(value)) {
                    addPEarlyMediaHeader(sipResponse, earlyMedia);
                }
            }
        } else {
            // {@link EarlyMediaHeaderUsage.FORCED} case
            addPEarlyMediaHeader(sipResponse, earlyMedia);
        }
    }

    protected void addPEarlyMediaHeader(SipResponse sipResponse, boolean earlyMedia) throws ParseException {
        if (earlyMedia)
            sipResponse.setPEearlyMediaHeader(PEarlyMediaTypes.PEARLY_MEDIA_SENDRECV.getValue());
        else 
            sipResponse.setPEearlyMediaHeader(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE.getValue());
    }

    /**
     * Add proprietary headers to a SIP Response. The headers are given by the Call Manager client 
     * by setting the "addProprietaryHeaderForSessionProgressResponse" variable in the session
     * object related to this call.
     * 
     * @param sipResponse The SIP Response that will included the extra headers
     */
    protected void addProprietaryHeadersFromApplication(SipResponse sipResponse, String proprietaryHeaderString) {
        
        Object tmp = call.getSession().getData(proprietaryHeaderString); 
        if(tmp != null) { 
            
            if (tmp instanceof ExtensionHeader[]) {
                ExtensionHeader[] headers = (ExtensionHeader[]) tmp;
                
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved from session: " + proprietaryHeaderString +"=" +  Arrays.toString(headers));
                }
                    
                for (ExtensionHeader header : headers) {                     
                    //add header to response
                    sipResponse.addExtensionHeader(header);
                }
            }    
        }
    }

}
