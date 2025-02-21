/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.UnjoinedEvent;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.callmanager.sdp.SdpIntersection;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.releasecausemapping.Q850CauseLocationPair;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.address.SipURI;
import javax.sip.message.Response;
import javax.sip.message.Request;
import java.text.ParseException;

/**
 * Represents the outbound state Progressing.
 * <p>
 * The Progressing state is entered when the initial call setup INVITE is
 * sent. In Progressing state, the caller awaits a response from the callee.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public abstract class ProgressingOutboundState extends OutboundCallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public enum ProgressingSubState {
        CALLING, EARLY_MEDIA, PROCEEDING
    }

    public ProgressingOutboundState(OutboundCallInternal call) {
        super(call);
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * A SIP BYE MUST NOT be sent by a callee for early dialogs, see section 15
     * in RFC 3261. But, if a SIP BYE request is received, a SIP OK is sent
     * as response, a {@link FailedEvent} is generated and the state is
     * set to {@link FailedCompletedOutboundState}.
     * <p>
     * If the SIP response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorCompletedOutboundState}.
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP BYE request received in " + this +
                      " before the call is connected.");

        if (log.isInfoEnabled()) log.info("The far end has disconnected the call with a SIP BYE request.");

        // Set call failed
        callRejected(
                Response.TEMPORARILY_UNAVAILABLE,
                sipRequestEvent.getSipMessage().getQ850CauseLocation());

        // Send OK resposne
        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Handles a SIP re-INVITE request.
     * <p>
     * A SIP re-INVITE in this state means that an INVITE has
     * been received while the initial INVITE request is pending.
     * The re-INVITE is rejected with a SIP "Request Pending" response.
     * The state is left unchanged.
     * <p>
     * If the SIP response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorCompletedOutboundState}.
     *
     * @param sipRequestEvent carries the SIP re-INVITE request.
     */
    public synchronized void processReInvite(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP re-INVITE request received in " + this + ".");
        if (log.isInfoEnabled()) log.info("SIP re-INVITE request is rejected with a SIP 491 response");
        call.sendErrorResponse(
                Response.REQUEST_PENDING, sipRequestEvent,
                "A SIP INVITE is still pending.");
    }

    /**
     * Handles a SIP OPTIONS request.
     * <p>
     * A SIP OK response is sent.
     * If an error occurs when sending the OK response, an {@link ErrorEvent}
     * is generated and the state is set to {@link ErrorCompletedOutboundState}.
     * @param sipRequestEvent
     */
    public synchronized void processOptions(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP OPTIONS request received in " + this + ".");
        if (log.isInfoEnabled()) log.info("SIP OPTIONS request is answered.");
        call.sendOkResponse(sipRequestEvent, false);
    }

    /**
     * Handles a SIP response.
     * <p>
     * A response to a SIP INVITE request is handled as below:
     * <ul>
     * <li>
     * A provisional response is handled using
     * {@link #process1xxResponseToInvite(SipResponseEvent)}
     * </li>
     * <li>
     * A 2xx response is handled using
     * {@link #process2xxResponseToInvite(SipResponseEvent)}
     * </li>
     * <li>
     * A 3xx response is handled using
     * {@link #process3xxResponseToInvite(SipResponseEvent)}
     * </li>
     * <li>
     * A 5xx response is handled using
     * {@link #process5xxResponseToInvite(SipResponseEvent)}
     * </li>
     * <li>
     * All other responses are rejected using
     * {@link #callRejected(Integer, Q850CauseLocationPair)}
     * </li>
     * </ul>
     * <p>
     * A response to a SIP PRACK request is handled as below:
     * <ul>
     * <li>
     * A 481 or 408 response will result in the call being canceled.
     * A {@link FailedEvent} is generated, the state is set to
     * {@link FailedLingeringCancelOutboundState}, the streams are deleted and
     * a SIP CANCEL request is sent.
     * </li>
     * <li>
     * All other responses are ignored.
     * </li>
     * </ul>
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {

        if (log.isDebugEnabled())
            log.debug("A SIP (" + sipResponseEvent.getResponseCode() +
                    ") response was received for " + sipResponseEvent.getMethod() +
                    " request.");

        Integer responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INVITE);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INVITE response.");

            int responseType = responseCode / 100;
            if (responseType == 1) {
                if(responseCode != Response.TRYING) {
                    // Non-100 provisional response, reset timer for maximum duration until call is canceled if not accepted
                    call.cancelCallNotConnectedExtensionTimer();
                    call.startCallNotConnectedExtensionTimer();
                }
                process1xxResponseToInvite(sipResponseEvent);
            } else if (responseType == 2) {
                process2xxResponseToInvite(sipResponseEvent);
            } else if (responseType == 3) {
                process3xxResponseToInvite(sipResponseEvent);
            } else if (responseType == 5) {
                process5xxResponseToInvite(sipResponseEvent);
            } else {
                // All 4xx and 6xx responses
                
                // Check if response is from a forked dialog and update call with new dialog
                // Streams will be unjoined if needed in callRejected()
                handleForkedSipDialog(sipResponseEvent, false);
                
                callRejected(
                        responseCode,
                        sipResponseEvent.getSipMessage().getQ850CauseLocation());
            }
        }

        // Then check for PRACK responses
        responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.PRACK);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing PRACK response");

            if ((responseCode == Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST) ||
                    (responseCode == Response.REQUEST_TIMEOUT)) {
                if (log.isInfoEnabled())
                    log.info("SIP " + responseCode +
                            " response to a PRACK request. The call will " +
                            "be disconnected with a SIP CANCEL request.");

                // Set call failed
                call.setStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);

                call.fireEvent(new FailedEvent(
                        call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                        CallDirection.OUTBOUND,
                        "Call rejected due to " + responseCode + " response.",
                        call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        responseCode,
                        sipResponseEvent.getSipMessage().getQ850CauseLocation())));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send Cancel request
                sendCancelRequest();
            }
        }
    }

    /**
     * Handles a SIP timeout event.
     * <p>
     * If the INVITE request timed out a response was not received in
     * time. SIP timer B has expired (see RFC 3261). If there are more
     * contacts to try, a new INVITE is sent. Otherwise, the call is
     * considered rejected, i.e. a {@link FailedEvent}
     * is generated and the state is set to {@link FailedCompletedOutboundState}.
     * <p>
     * If the PRACK request timed out an {@link ErrorEvent} is generated,
     * the streams are deleted, the state is set to
     * {@link ErrorLingeringCancelOutboundState} and a CANCEL request is sent.
     * <p>
     * For other methods, an {@link ErrorEvent} is generated,
     * the streams are deleted, the state is set to
     * {@link ErrorCompletedOutboundState} and the call is considered disconnected.
     *
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {
        int responseCode = Response.REQUEST_TIMEOUT;
        if (log.isDebugEnabled())
            log.debug("A SIP timeout received in " + this + ".");

        if (sipTimeoutEvent.getMethod().equals(Request.INVITE)) {
            if (log.isInfoEnabled()) log.info(
                    "The SIP timeout is handled as if a SIP " + responseCode +
                            " response was received.");

            SipURI uri = call.getCurrentRemoteParty();
            CMUtils.getInstance().getRemotePartyController().blacklistRemoteParty(
                    uri.getHost() + ":" + uri.getPort());

            // Retrieve the new remote party address.
            SipURI remotePartyUri;
            try {
                remotePartyUri = call.getNewRemoteParty();
            } catch (ParseException e) {
                // No contact was found to redirect the call to, the call
                // is considered rejected.
                if (log.isInfoEnabled()) log.info(
                        "No new contact could be created to redirect the call to. " +
                        "The call is considered rejected.");
                callRejected(Response.REQUEST_TIMEOUT, null);
                return;
            }

            if (remotePartyUri == null) {
                // No contact was found to redirect the call to, the call
                // is considered rejected.
                if (log.isInfoEnabled()) log.info(
                        "No new contact was found to redirect the call to. " +
                        "The call is considered rejected.");
                callRejected(Response.REQUEST_TIMEOUT, null);
                return;
            }

            if (log.isDebugEnabled())
                log.debug("Remote party URI has been retrieved: " + remotePartyUri);


            try {
                if (log.isInfoEnabled()) log.info(
                        "Sending INVITE to remote party: " + remotePartyUri);

                // Send INVITE request
                sendInviteRequest(
                        remotePartyUri, call.getLocalSdpOffer(),
                        call.getNewCallId());

                if (log.isDebugEnabled())
                    log.debug("SIP INVITE request is sent.");

                call.setStateProgressing(ProgressingSubState.CALLING);

            } catch (IllegalStateException e) {
                if (log.isDebugEnabled())
                    log.debug("New Outbound call failed.");
            }

        } else if (sipTimeoutEvent.getMethod().equals(Request.PRACK)) {
            if (log.isInfoEnabled()) log.info(
                    "The SIP timeout received for PRACK request. The call will " +
                        "be disconnected with a SIP CANCEL request.");

            // Set call failed
            call.setStateError(ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
            call.fireEvent(new ErrorEvent(
                    call, CallDirection.OUTBOUND,
                    "SIP timeout occurred for PRACK. The call will " +
                            "be ended with a SIP CANCEL request.", false));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            // Send Cancel request
            sendCancelRequest();

        } else {
            call.errorOccurred(
                    "SIP timeout expired. The call is considered completed.",
                    false);
        }
    }

    //===================== Private Methods =======================

    /**
     * Processes a 1xx response received for an outbound INVITE.
     * How to treat the response depends upon which sub state where in.
     * @param sipResponseEvent carries the SIP response.
     */
    protected abstract void process1xxResponseToInvite(
            SipResponseEvent sipResponseEvent);

    /**
     * Processes a 2xx response received for an outbound INVITE.
     * How to treat the response depends upon which sub state where in.
     * @param sipResponseEvent carries the SIP response.
     */
    protected abstract void process2xxResponseToInvite(
            SipResponseEvent sipResponseEvent);

    /**
     * Processes a 3xx response received for an outbound INVITE.
     * How to treat the response depends upon which sub state where in.
     * @param sipResponseEvent carries the SIP response.
     */
    protected abstract void process3xxResponseToInvite(
            SipResponseEvent sipResponseEvent);

    /**
     * Processes a 5xx response received for an outbound INVITE.
     * How to treat the response depends upon which sub state where in.
     * @param sipResponseEvent carries the SIP response.
     */
    protected abstract void process5xxResponseToInvite(SipResponseEvent sipResponseEvent);

    /**
     * Makes sure that the call is allowed to handle redirections.
     * One-level of redirection is allowed if there are SSP instances
     * configured, otherwise no redirection is allowed.
     * <p>
     * If the call already is redirected or no redirections are allowed,
     * a {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedOutboundState}.
     *
     * @param sipResponseEvent
     * @throws IllegalStateException if the call already was redirected.
     */
    protected void assertCallRedirectionAllowed(
            SipResponseEvent sipResponseEvent) throws IllegalStateException {
        boolean rejectCall = false;

        if (call.isRedirectionAllowed()) {
            if (call.isRedirected()) {
                if (log.isInfoEnabled())
                    log.info("Multiple level of redirection was detected. It " +
                            "is not supported. The call is considered rejected.");
                rejectCall = true;
            }
        } else {
            if (log.isInfoEnabled())
                log.info("Redirections are not allowed. The call is " +
                        "considered rejected.");
            rejectCall = true;
        }

        if (rejectCall) {
            callRejected(
                    sipResponseEvent.getResponseCode(),
                    sipResponseEvent.getSipMessage().getQ850CauseLocation());
            throw(new IllegalStateException());
        }
    }

    /**
     * Verifies that the SDP answer stored in the call is not null.
     * <p>
     * If the SDP answer is null, a {@link FailedEvent} is generated, the
     * state is set to {@link FailedLingeringByeOutboundState} and a SIP
     * BYE request is sent.
     * <br>
     * If the SIP BYE request cannot be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     * @throws IllegalStateException if the SDP answer is null.
     */
    protected void assertSdpAnswerExists() throws IllegalStateException {
        if (call.getRemoteSdp() == null) {
            String message = "Response to INVITE contained no SDP answer.";
            if (log.isInfoEnabled()) log.info(message + " The call is disconnected with a SIP BYE request.");

            call.setStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);

            call.fireEvent(new FailedEvent(
                    call, FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                    CallDirection.OUTBOUND, message,
                    call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null, null)));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            // Send BYE request
            sendByeRequest();

            throw(new IllegalStateException());
        }
    }

    /**
     * Creates an outbound stream based on an SDP intersection.
     * <p>
     * If the stream could not be created, the call is disconnected and a
     * {@link ErrorEvent} is generated. If the response code indicates a
     * provisional response the state is set to
     * {@link ErrorLingeringCancelOutboundState} and a SIP CANCEL request is sent.
     * Otherwise the state is set to {@link ErrorLingeringByeOutboundState}
     * and a SIP BYE request is sent.
     * <br>
     * If the SIP BYE or CANCEL request cannot be sent, an error is reported.
     * @param responseCode
     * @param sdpIntersection
     * @throws IllegalStateException if the stream could not be created.
     */
    protected void createOutboundStream(
            int responseCode, SdpIntersection sdpIntersection)
            throws IllegalStateException {
        // Create the outbound stream. If error at creation,
        // disconnect the call
        try {
            call.createOutboundStream(sdpIntersection);
        } catch (Exception e) {
            String message =
                    "Could not create outbound stream: " + e.getMessage();

            if ((responseCode / 100) == 1) {
                call.setStateError(
                        ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
                if (log.isInfoEnabled()) log.info(message + ". Call is disconnected with a SIP CANCEL request.");
                call.fireEvent(new ErrorEvent(
                        call, CallDirection.OUTBOUND, message, false));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send CANCEL request
                sendCancelRequest();
            } else {
                call.setStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
                if (log.isInfoEnabled()) log.info(message + ". Call is disconnected with a SIP BYE request.");
                call.fireEvent(new ErrorEvent(
                        call, CallDirection.OUTBOUND, message, false));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send BYE request
                sendByeRequest();
            }
            throw(new IllegalStateException());
        }
    }

    /**
     * Returns the next contact URI to try for a redirected INVITE request.
     * <p>
     * If there is no next contact URI to try, a {@link FailedEvent} is
     * generated and the state is set to {@link FailedCompletedOutboundState}.
     * @return a contact URI to use in a redirected INVITE request.
     * @throws IllegalStateException if there is no next contact URI to try.
     */
    protected SipURI getNextContact(SipResponseEvent sipResponseEvent)
            throws IllegalStateException {

        SipURI contactUri;
        try {
            contactUri = call.getNewRemoteParty();
        } catch (ParseException e) {
            if (log.isInfoEnabled()) log.info("No contact could be created to redirect the call to. " +
                                              "The call is considered rejected.");
            callRejected(
                    sipResponseEvent.getResponseCode(),
                    sipResponseEvent.getSipMessage().getQ850CauseLocation());
            throw(new IllegalStateException());
        }

        if (contactUri == null) {
            // No contact was found to redirect the call to, the call
            // is considered rejected.
            if (log.isInfoEnabled()) log.info("No contact was found to redirect the call to! " +
                                              "The call is considered rejected.");
            callRejected(
                    sipResponseEvent.getResponseCode(),
                    sipResponseEvent.getSipMessage().getQ850CauseLocation());
            throw(new IllegalStateException());
        }
        return contactUri;
    }

    /**
     * Retrieves the SDP Answer from the SIP response and stores it in the call.
     * <p>
     * If an error occurred when retrieving the SDP answer, an {@link ErrorEvent}
     * is generated, the state is set to {@link ErrorLingeringByeOutboundState}
     * and a SIP BYE request is sent.
     * <br>
     * If the SIP BYE request could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorCompletedOutboundState}.
     * @param sipResponseEvent
     * @throws IllegalStateException if an error occurred while retrieving the
     * SDP answer.
     */
    protected void retrieveAndStoreSdpAnswer(SipResponseEvent sipResponseEvent)
            throws IllegalStateException {
        try {
            call.parseRemoteSdp(sipResponseEvent.getSipMessage());
        } catch (SdpNotSupportedException e) {
            String message =
                    "Could not parse remote SDP answer: " + e.getMessage();

            if (log.isDebugEnabled())
                log.debug(message);

            if ((sipResponseEvent.getResponseCode() / 100) == 1) {
                call.setStateError(
                        ErrorOutboundState.ErrorSubState.LINGERING_CANCEL);
                if (log.isInfoEnabled()) log.info(message + ". Call is disconnected with a SIP CANCEL request.");
                call.fireEvent(new ErrorEvent(
                        call, CallDirection.OUTBOUND, message, false));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send CANCEL request
                sendCancelRequest();
            } else {
                call.setStateError(ErrorOutboundState.ErrorSubState.LINGERING_BYE);
                if (log.isInfoEnabled()) log.info(message + ". Call is disconnected with a SIP BYE request.");
                call.fireEvent(new ErrorEvent(
                        call, CallDirection.OUTBOUND, message, false));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send BYE request
                sendByeRequest();
            }
            throw(new IllegalStateException());
        }
    }

    /**
     * Tries to find a match between the offered SDP (included in the
     * initial INVITE) and the SDP answer included in the SIP response.
     * <p>
     * If no SDP intersection was found, the call is disconnected and a
     * {@link FailedEvent} is generated. If the response code indicates a
     * provisional response the state is set to
     * {@link FailedLingeringCancelOutboundState} and a SIP CANCEL request is sent.
     * Otherwise the state is set to {@link FailedLingeringByeOutboundState}
     * and a SIP BYE request is sent.
     * <br>
     * If the SIP BYE or CANCEL request cannot be sent, an error is reported.
     * @param responseCode
     * @return the SDP intersection which is never null.
     * @throws IllegalStateException if an SDP intersection could not be found.
     */
    protected SdpIntersection retrieveSdpIntersection(int responseCode)
            throws IllegalStateException {


        SdpIntersection sdpIntersection =null;
        try
        {
            sdpIntersection=call.findSdpIntersection(
                    call.getConfiguredOutboundCallMediaTypes(), false);
        }
        catch (SdpInternalErrorException e)
        {
            log.error("retrieveSdpIntersection unable to find Intercsection",e);
        }
        // If no SDP intersection was found, the call is disconnected
        if ((sdpIntersection == null) ||
                (call.getCallProperties().getCallType() !=
                        sdpIntersection.getCallType())) {


            String message = "Media negotiation failed.";
            if ((sdpIntersection != null) &&
                    (call.getCallProperties().getCallType() != sdpIntersection.getCallType())) {
                message += " The call type (" + call.getCallProperties().getCallType() +
                        ") did not match the type of the SDP intersection (" +
                        sdpIntersection.getCallType() + ").";
            }

            int nsc = call.getConfig().getReleaseCauseMapping().
                    getNetworkStatusCode(null, null);

            if ((responseCode / 100) == 1) {
                call.setStateFailed(FailedOutboundState.FailedSubState.LINGERING_CANCEL);
                if (log.isInfoEnabled()) log.info(message +
                        ". Call is disconnected with a SIP CANCEL request.");
                call.fireEvent(new FailedEvent(
                        call, FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                        CallDirection.OUTBOUND, message, nsc));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send CANCEL request
                sendCancelRequest();
            } else {
                call.setStateFailed(FailedOutboundState.FailedSubState.LINGERING_BYE);
                if (log.isInfoEnabled()) log.info(message +
                        ". Call is disconnected with a SIP BYE request.");
                call.fireEvent(new FailedEvent(
                        call, FailedEvent.Reason.MEDIA_NEGOTIATION_FAILED,
                        CallDirection.OUTBOUND, message, nsc));

                // Make sure the streams are deleted after the event is sent.
                // This is done to make sure that the event is generated before any
                // event generated by stream.
                call.deleteStreams();

                // Send BYE request
                sendByeRequest();
            }

            throw(new IllegalStateException());
        }

        return sdpIntersection;
    }

    /**
     * Sends a redirected SIP INVITE request to the given contact URI.
     * <p>
     * If the SIP INVITE could not be created, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     * If the SIP INVITE could not be sent, an {@link FailedEvent} is generated
     * and the state is set to {@link FailedCompletedOutboundState}.
     * @param contactUri The contact to address the INVITE to.
     * @throws IllegalStateException if the SIP INVITE could not be sent.
     */
    protected void sendRedirectedInvite(SipURI contactUri)
            throws IllegalStateException {

        SipRequest sipRequest;
         try {

            sipRequest =
                    CMUtils.getInstance().getSipRequestFactory().
                            createNewInviteRequest(
                                    contactUri, call.getInitialSipRequest(),
                                    call.getConfig().getRegisteredName(),
                                    call.getCallingParty().getPresentationIndicator(),
                                    call.getCallProperties().getPreventLoopback(),
                                    call.getNewCallId(),
                                    call.getPChargingVector());
        } catch (Exception e) {
            call.errorOccurred(
                    "SIP INVITE request could not be created. " + e.getMessage(),
                    false);
            throw(new IllegalStateException());
        }

        call.dialogCreated(sipRequest);

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

    /**
     * This method handles a SIP Session Progress response and all 1xx responses
     * that should be treated as a SIP Session Progress response.
     * <p>
     * The SDP Answer (if any) is retrieved, parsed and stored in the call
     * using {@link #retrieveAndStoreSdpAnswer(SipResponseEvent)}.
     * If an SDP answer was included in the response it is treated as below.
     * <p>
     * <ul>
     * <li>
     * A check is made to verify that the SDP Answer was stored ok in the call
     * using {@link #assertSdpAnswerExists()}.
     * </li>
     * <li>
     * An SDP intersection between the SDP offer in the initial INVITE and the
     * SDP answer is located using {@link #retrieveSdpIntersection(int)}.
     * </li>
     * <li>
     * An outbound media stream is created using
     * {@link #createOutboundStream(int, SdpIntersection)}.
     * </li>
     * <li>
     * A {@link ProgressingEvent} is generated indicating early media and the
     * state is set to {@link ProgressingEarlyMediaOutboundState}.
     * </li>
     * </ul>
     * <p>
     * If an SDP answer is not found in the response, it is treated as no early
     * media: a {@link ProgressingEvent} is generated indicating NO early media
     * and the state is set to {@link ProgressingProceedingOutboundState}.
     * @param sipResponseEvent
     * @throws IllegalStateException if error occured while processing session
     * progress.
     */
    protected void processSessionProgressResponse(
            SipResponseEvent sipResponseEvent) throws IllegalStateException {
        // Retrieve and store the SDP answer
        retrieveAndStoreSdpAnswer(sipResponseEvent);

        if (log.isDebugEnabled())
            log.debug("SDP answer is parsed.");

        // Check if an SDP answer was included
        if (call.getRemoteSdp() == null) {
            if (log.isInfoEnabled()) log.info("SIP " + sipResponseEvent.getResponseCode() + " response " +
                                              "received with no SDP answer. It is handled as a call " +
                                              "progress only.");

            if (!(this instanceof ProgressingProceedingOutboundState)) {
                call.setStateProgressing(ProgressingSubState.PROCEEDING);
            }

            // SDP Answer NOT included => NO early media
            call.fireEvent(new ProgressingEvent(call, false));

        } else {
            // SDP Answer included => early media

            if (log.isInfoEnabled()) log.info("SIP " + sipResponseEvent.getResponseCode() + " response " +
                                              "received with an SDP answer. Early media negotiation is performed.");

            // Retrieve the SDP intersection
            SdpIntersection sdpIntersection =
                    retrieveSdpIntersection(sipResponseEvent.getResponseCode());

            if (log.isDebugEnabled())
                log.debug("SDP answer matches outbound media types.");

            addFarEndConnection(sipResponseEvent);

            // Create the outbound stream.
            createOutboundStream(
                    sipResponseEvent.getResponseCode(), sdpIntersection);

            if (log.isDebugEnabled())
                log.debug("Outbound stream created.");

            if (!(this instanceof ProgressingEarlyMediaOutboundState)) {
                call.setStateProgressing(ProgressingSubState.EARLY_MEDIA);
            }

            call.fireEvent(new ProgressingEvent(call, true));
        }
    }
    
    
    /**
     * A SIP Client transaction may belong simultaneously to multiple
     * dialogs in the early state. These dialogs all have
     * the same call ID and same From tag but different To tags.
     * <p>
     * If the sipResponseEvent is from a forked SIP Dialog, this method updates the call with the new dialog and terminate the old dialog.<br>
     * Then, if deleteOutboundStream is true, deletes the previously created outbound stream and fires an UnjoinedEvent.
     * 
     * @param sipResponseEvent The SipResponseEvent that could be from a forked dialog
     * @param deleteOutboundStream Whether the outbound stream is to be deleted if the dialog is forked
     * @return If the sipResponseEvent is from a forked SIP Dialog that belongs to the same call
     */
    protected boolean handleForkedSipDialog(SipResponseEvent sipResponseEvent, boolean deleteOutboundStream)
    {
        Dialog oldDialog = call.getDialog();
        Dialog newDialog = sipResponseEvent.getTransaction().getDialog();
        
        if(oldDialog == newDialog) {
            // dialog is not forked, nothing to do
            return false;
        }
        
        if(log.isDebugEnabled())
            log.debug("Forked dialog detected in " + this + ". Updating call with new dialog.");
        
        // Update call with new dialog
        call.setDialog(newDialog);
        
        // Terminate old dialog
        //SIP Client should be able to handle multiple active dialog in this state, but there is no current need for this. So the old dialog is deleted.
        oldDialog.delete();
        
        if(deleteOutboundStream) {
            // Delete outbound stream and send unjoined event
            
            if (log.isInfoEnabled()) 
                log.info("Forked dialog detected in " + this + ", delete outbound stream.");
            
            if(call.isCallJoined()) {
                UnjoinedEvent unjoinedEvent  = new UnjoinedEvent((Call)call.getJoinedToCall(), call);
                call.deleteOutboundStream();
                call.fireEvent(unjoinedEvent);
            }
            else {
                call.deleteOutboundStream();
            }
            
        }
        
        return true;
        
    }    


}

