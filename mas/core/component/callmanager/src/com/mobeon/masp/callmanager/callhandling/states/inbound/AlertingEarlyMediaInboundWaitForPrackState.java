package com.mobeon.masp.callmanager.callhandling.states.inbound;

import javax.sip.message.Response;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;

/**
 * Represents the sub state "Early media Inbound, wait for PRACK" of
 * {@link AlertingInboundState}.
 * <p>
 * This sub state is entered when the the Call Manager client has already
 * negotiated early media for the call and for some reason (requested by
 * terminal or given in configuration) the regular Session Progress response
 * retransmission has been sent reliably.
 * A reliable Session Progress response is sent every 2 and a half minutes
 * in order to prevent proxies from canceling the call.
 * 
 * In this state Call Manager is awaiting a SIP PRACK request.
 * 
 * <p>
 * In this state, the Call Manager client may play early media, record early media,
 * accept or reject the call.
 * <p>
 * This class is thread-safe.
 * All methods are synchronized to handle each event atomically.
 *
 * @author Julien Grillon-Labelle
 */
public class AlertingEarlyMediaInboundWaitForPrackState extends AlertingEarlyMediaInboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public AlertingEarlyMediaInboundWaitForPrackState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Alerting state (sub state EarlyMediaInboundWaitForPrack)";
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

            // Go back to Alerting Early Media Negotiated state
            call.setStateAlerting(AlertingSubState.EARLY_MEDIA);
            
        } catch (IllegalStateException e) {
            if (log.isDebugEnabled()) log.debug("Call setup failed.");
        }
  

    }

    /**
     * Handles a Call timeout.
     * <p/>
     * If the call timeout is of type {@link CallTimerTask.Type.NO_ACK}, an
     * {@link ErrorEvent} is generated, the state is set to
     * {@link ErrorCompletedInboundState} and the call is rejected with a
     * SIP 504 "Server Timeout" response.
     * <p>
     * If it was the {@link CallTimerTask.Type.EXPIRES} timer that
     * expired, a {@link FailedEvent} is generated, the state is set to
     * {@link FailedCompletedInboundState} and the call is rejected with a
     * SIP 487 "Request Terminated" response.
     * <p>
     * Other timers should no expire in this state, and are ignored.
     * @param callTimeoutEvent carries information regarding the call timeout.
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("A call timeout <" + callTimeoutEvent.getType() +
                    "> was received in " + this + ".");
        
        if (callTimeoutEvent.getType() == CallTimerTask.Type.NO_ACK) {
            String message = "The inbound call has timed out while waiting "
                    + "for a PRACK. The timer that expired was an internal "
                    + "safety timer. The call is rejected with a 504 response.";

            if (log.isInfoEnabled())
                log.info(message + " CalledParty: " + call.getCalledParty().toString() + " dialogId=" + call.getInitialDialogId());

            // Set call to error state
            call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
            call.fireEvent(new ErrorEvent(call, CallDirection.INBOUND, message, false));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            call.sendErrorResponse(Response.SERVER_TIMEOUT, call.getInitialSipRequestEvent(),
                    "Reliable provisional response was not acknowledged in time.");

        } else if (callTimeoutEvent.getType() == CallTimerTask.Type.EXPIRES) {
            String message = "The expires timer expired for the INVITE. " + "A SIP 487 response will be sent.";
            if (log.isInfoEnabled())
                log.info(message);

            int nsc = call.getConfig().getReleaseCauseMapping().
                    getNetworkStatusCode(null, null);
            
            // Set call to failed
            call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);
            call.fireEvent(new FailedEvent(call, FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND, message, nsc));

            // Make sure the streams are deleted after the event is sent.
            // This is done to make sure that the event is generated before any
            // event generated by stream.
            call.deleteStreams();

            call.sendErrorResponse(Response.REQUEST_TERMINATED, call.getInitialSipRequestEvent(), "Expires timer expired.");

        } else {
            if (log.isDebugEnabled())
                log.debug("The call timeout <" + callTimeoutEvent.getType() + "> is ignored.");
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


}
