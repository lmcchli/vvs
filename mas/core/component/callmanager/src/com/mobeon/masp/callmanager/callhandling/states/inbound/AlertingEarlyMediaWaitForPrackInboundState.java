/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingInboundState.AlertingSubState;
import com.mobeon.masp.callmanager.configuration.ReliableResponseUsage;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;

import javax.sip.message.Response;

/**
 * Represents the sub state "Early media, wait for PRACK" of
 * {@link AlertingInboundState}.
 * <p>
 * This sub state is entered when the the Call Manager client has chosen to
 * negotiate early media for the call and for some reason (requested by
 * terminal or given in configuration) the Session Progress response has been
 * sent reliably.
 * In this state Call Manager is awaiting a SIP PRACK request before being
 * able to continue with the call setup.
 * <p>
 * In this state, the Call Manager client may accept, disconnect or reject the
 * call.
 * <p>
 * This class is thread-safe.
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Nyfeldt
 */
public class AlertingEarlyMediaWaitForPrackInboundState
        extends AlertingInboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public AlertingEarlyMediaWaitForPrackInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Alerting state (sub state EarlyMediaWaitForPrack)";
    }

    /**
     * Handles an administrators lock request.
     * <p>
     * A {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedInboundState}.
     * The initial inbound INVITE is rejected with a SIP
     * "Temporarily Unavailable" response.
     * <p>
     * If the SIP error response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     */
    public synchronized void processLockRequest() {
        String message = "The Service is temporarily unavailable due to " +
                         "the current administrative state: Locked.";

        if (log.isDebugEnabled())
            log.debug("Lock requested in " + this + ". " + message);

        if (log.isInfoEnabled()) log.info(
                "Due to a lock request, the INVITE is rejected with SIP " +
                        "503 response.");

        // Set call failed
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

        call.sendErrorResponse(
                Response.SERVICE_UNAVAILABLE,
                call.getInitialSipRequestEvent(), message);
    }

    /**
     * This method is used when the Call Manager client rejects the inbound
     * call.
     * <p>
     * A {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedInboundState}.
     * A SIP "Forbidden" response is sent for the initial INVITE.
     * <p>
     * If the SIP response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorInboundState}.
     *
     * @param rejectEvent carries the information regarding the reject event.
     */
    public synchronized void reject(RejectEvent rejectEvent) {
        if (log.isDebugEnabled())
            log.debug("Reject received in " + this + ". ");

        if (log.isInfoEnabled()) log.info(
                "The service is rejecting the call. A SIP " + rejectEvent.getCode() + " response will be sent.");

        // Set call to failed
        call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.INBOUND, rejectEvent.getMessage(),
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(null, null)));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        call.deleteStreams();

        call.sendErrorResponse(rejectEvent.getCode(), call.getInitialSipRequestEvent(), rejectEvent.getMessage());
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * A {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedInboundState}.
     * A SIP "Request Terminated" response is sent for the pending INVITE request.
     * A SIP "OK" is sent as response to the BYE request.
     * <p>
     * If a SIP response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorInboundState}.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP BYE request received in " + this + ".");

        if (log.isInfoEnabled()) log.info(
                "The far end has disconnected the call with a SIP BYE request.");

        // Set call to failed
        call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.INBOUND, "Call disconnected early by far end.",
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null,
                        sipRequestEvent.getSipMessage().getQ850CauseLocation())));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        call.deleteStreams();

        // Send Request Terminated response to initial INVITE
        call.sendErrorResponse(
                Response.REQUEST_TERMINATED, call.getInitialSipRequestEvent(),
                "Call terminated due to a BYE request.");

        // Send OK response to BYE request
        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Handles a SIP CANCEL request.
     * <p>
     * A {@link FailedEvent} is generated and the state is set to
     * {@link FailedCompletedInboundState}.
     * A SIP "Request Terminated" response is sent for the pending INVITE request.
     * A SIP "OK" is sent as response to the CANCEL request.
     * <p>
     * If a SIP response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorInboundState}.
     *
     * @param sipRequestEvent carries the SIP CANCEL request.
     */
    public synchronized void processCancel(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP CANCEL received in " + this + ".");

        if (log.isInfoEnabled()) log.info(
                "The far end has canceled the call with a SIP CANCEL request.");

        // Set call to failed
        call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
        call.fireEvent(new FailedEvent(
                call, FailedEvent.Reason.REJECTED_BY_FAR_END,
                CallDirection.INBOUND, "Call disconnected early by far end.",
                call.getConfig().getReleaseCauseMapping().getNetworkStatusCode(
                        null,
                        sipRequestEvent.getSipMessage().getQ850CauseLocation())));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        call.deleteStreams();

        // Send Request Terminated response to initial INVITE
        call.sendErrorResponse(
                Response.REQUEST_TERMINATED, call.getInitialSipRequestEvent(),
                "Call terminated due to a CANCEL request.");

        // Send OK response to CANCEL request
        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * This method is used when the Call Manager client accepts the inbound
     * call.
     * <ul>
     * <li>
     * Buffer the accpet until the prack is received {@link #sendOkResponse(String)}.
     * </li>
     * <li>
     * The state is set to {@link AlertingAcceptingInboundState}.
     * </li>
     * </ul>
     *
     * @param acceptEvent carries the information regarding the accept event.
     */
    public synchronized void accept(AcceptEvent acceptEvent) {
        if (log.isDebugEnabled())
            log.debug("Accept received in " + this + ".");

        if (log.isInfoEnabled()) log.info("The service is accepting the call in AlertingEarlyMediaWaitForPrackInboundState.");

        call.setAcceptReceivedInWaitForPrack(true);

    }



    /**
     * This method is used when a SIP 183 "Session Progress" response has been
     * sent reliably and Call Manager receives a PRACK request for that response.
     * <ul>
     * <li>
     * If the PRACK request contains a new SDP offer, the offer is parsed and
     * examined using
     * {@link #retrieveAndStoreRemoteSdpPriorToAccept(SipRequestEvent, boolean)}.
     * </li>
     * <li>
     * A SIP "OK" response is sent for the PRACK using
     * {@link #sendOkResponse(SipRequestEvent, String)}. If the PRACK contained
     * a new acceptable SDP offer, the SIP "OK" response will contain an
     * SDP answer identical to the one sent previously in the SIP 183
     * "Session Progress" response.
     * </li>
     * <li>
     * The state is set to {@link AlertingEarlyMediaInboundState}.
     * </li>
     * <li>
     * An {@link EarlyMediaAvailableEvent} is generated.
     * </li>
     * </ul>
     *
     * @param sipRequestEvent carries the SIP PRACK request.
     */
    public synchronized void processPrack(SipRequestEvent sipRequestEvent) {

        String sdpAnswer = null;

        if (log.isDebugEnabled())
            log.debug("SIP PRACK received in " + this + ".");

        try {
            if (call.containsSdp(sipRequestEvent)) {
                if (log.isDebugEnabled())
                    log.debug("A new remote SDP offer is received.");

                // Retrieve and store the remote SDP offer
                retrieveAndStoreRemoteSdpPriorToAccept(sipRequestEvent, true);
                if (log.isDebugEnabled())
                    log.debug("Remote SDP offer is parsed and accepted.");

                sdpAnswer = call.getLocalSdpAnswer();

                if (log.isDebugEnabled()) log.debug(
                        "SDP answer is included in Ok response for PRACK: " +
                                sdpAnswer);
            }

            // Send a SIP "Ok" response to PRACK
            sendOkResponse(sipRequestEvent, sdpAnswer);

            if (log.isDebugEnabled())
                log.debug("OK response is sent for PRACK.");

            // Go to Alerting Early Media Negotiated state
            call.setStateAlerting(AlertingSubState.EARLY_MEDIA);
            if(call.isAcceptReceivedInWaitForPrack())
            {
                call.accept();
                call.setAcceptReceivedInWaitForPrack(false);

            }
            call.fireEvent(new EarlyMediaAvailableEvent(call));




        } catch (IllegalStateException e) {
            if (log.isDebugEnabled()) log.debug("Call setup failed.");
        }

    }

    /**
     * Handles a SIP timeout event.
     * <p>
     * An {@link ErrorEvent} is generated, the state is set to
     * {@link ErrorCompletedInboundState} and a SIP 504 "Server Timeout"
     * response is sent for the INVITE.
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP timeout expired in " + this + ".");

        String message = "The inbound call has timed out while waiting " +
                "for a PRACK. The timer that expired was a SIP timer. " +
                "The call is rejected with a 504 response.";

        if (log.isInfoEnabled()) log.info(message + " CalledParty: " +
                call.getCalledParty().toString()
                + " dialogId=" + call.getInitialDialogId());

        // Set call to error state
        call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
        call.fireEvent(new ErrorEvent(
                call, CallDirection.INBOUND, message, false));

        // Make sure the streams are deleted after the event is sent.
        // This is done to make sure that the event is generated before any
        // event generated by stream.
        call.deleteStreams();

        call.sendErrorResponse(
                Response.SERVER_TIMEOUT, call.getInitialSipRequestEvent(),
                "Reliable provisional response was not acknowledged in time.");
    }

    /**
     * Handles a Call timeout.
     * <p/>
     * If the call timeout is of type {@link CallTimerTask.Type.NO_ACK}, an
     * {@link ErrorEvent} is generated, the state is set to
     * {@link ErrorCompletedInboundState} and the call is rejected with a
     * SIP 504 "Server Timeout" response.
     *
     * @param callTimeoutEvent Carries the timeout event.
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("A call timeout <" + callTimeoutEvent.getType() +
                    "> was received in " + this + ".");

        if (callTimeoutEvent.getType() == CallTimerTask.Type.NO_ACK) {
            String message = "The inbound call has timed out while waiting " +
                    "for a PRACK. The timer that expired was an internal " +
                    "safety timer. The call is rejected with a 504 response.";

            if (log.isInfoEnabled()) log.info(message + " CalledParty: " +
                    call.getCalledParty().toString()
                    + " dialogId=" + call.getInitialDialogId());

            // Set call to error state
            call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
            call.fireEvent(new ErrorEvent(
                    call, CallDirection.INBOUND, message, false));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            call.sendErrorResponse(
                    Response.SERVER_TIMEOUT, call.getInitialSipRequestEvent(),
                    "Reliable provisional response was not acknowledged in time.");

        } else if (callTimeoutEvent.getType() == CallTimerTask.Type.CALL_NOT_ACCEPTED) {
            int nsc = call.getConfig().getReleaseCauseMapping().
                    getNetworkStatusCode(null, null);

            String message = "The inbound call was not accepted by the service " +
                    "in time. It is considered abandoned and a SIP 408 " +
                    "response will be sent.";
            if (log.isInfoEnabled()) log.info(message);

            // Set call to failed
            call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
            call.fireEvent(new FailedEvent(
                    call, FailedEvent.Reason.NEAR_END_ABANDONED,
                    CallDirection.INBOUND, message, nsc));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            call.sendErrorResponse(
                    Response.REQUEST_TIMEOUT, call.getInitialSipRequestEvent(),
                    "Call was not accepted in time.");

        } else if (callTimeoutEvent.getType() == CallTimerTask.Type.EXPIRES) {
            int nsc = call.getConfig().getReleaseCauseMapping().
                    getNetworkStatusCode(null, null);

            String message = "The expires timer expired for the INVITE. " +
                    "A SIP 487 response will be sent.";
            if (log.isInfoEnabled()) log.info(message);

            // Set call to failed
            call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
            call.fireEvent(new FailedEvent(
                    call, FailedEvent.Reason.REJECTED_BY_NEAR_END,
                    CallDirection.INBOUND, message, nsc));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            call.sendErrorResponse(
                    Response.REQUEST_TERMINATED, call.getInitialSipRequestEvent(),
                    "Expires timer expired.");

        } else {
            if (log.isDebugEnabled())
                log.debug("The call timeout <" + callTimeoutEvent.getType() +
                        "> is ignored.");
        }
    }
}
