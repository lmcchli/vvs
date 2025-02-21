/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;

/**
 * Represents the sub state "WaitingForResponse" of
 * {@link FailedOutboundState}.
 * This sub state is entered when the outbound call has failed before any
 * response has been received for the initial INVITE.
 * <p>
 * In this state, we are waiting for any response to the INVITE to be able to
 * send either a SIP CANCEL request (if a provisional response is received) or
 * a SIP BYE request (if a final response is received). If a final ok response
 * is received (i.e. a 2xx response), a SIP ACK is sent before the SIP BYE
 * request.
 * <p>
 * All methods are synchronized to handle each event atomically.
 * @author Malin Flodin
 */
public class FailedWaitingForResponseOutboundState extends FailedOutboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public FailedWaitingForResponseOutboundState(OutboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Failed state (sub state WaitingForResponse)";
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * A SIP "OK" is sent as response to the BYE request.
     * The state is set to {@link FailedCompletedOutboundState}.
     * <p>
     * If the SIP OK response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorCompletedOutboundState}.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP BYE request received in " + this + ".");

        if (log.isInfoEnabled()) log.info("The far end has disconnected the call with a SIP BYE request.");

        // The disconnect is now completed!
        call.setStateFailed(FailedSubState.COMPLETED);

        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Handles a SIP response.
     * <p>
     * Only responses to the initial INVITE is considered, all other responses
     * are ignored.
     * <p>
     * If a provisional response is received for the INVITE, the state is set
     * to {@link FailedLingeringCancelOutboundState} and a SIP CANCEL
     * request is sent.
     * <p>
     * If a final ok response is received for the INVITE (i.e. a 2xx response),
     * a SIP ACK request is sent, the state is set to
     * {@link FailedLingeringByeOutboundState} and a SIP BYE request is
     * sent.
     * <p>
     * If a final error response is received for the INVITE (i.e. a 3xx-6xx
     * response) the state is set to {@link FailedCompletedOutboundState}.
     * <p>
     * If a request could not be sent (ACK, BYE or CANCEL), an {@link ErrorEvent}
     * is generated and the state is set to {@link ErrorCompletedOutboundState}.
     *
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {

        if (log.isDebugEnabled())
            log.debug("A SIP (" + sipResponseEvent.getResponseCode() +
                    ") response was received for " + sipResponseEvent.getMethod() +
                    " request.");

        // Check for response to INFO
        Integer responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INFO);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INFO response");
            processInfoResponse(sipResponseEvent);
        }

        responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INVITE);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INVITE response.");

            call.cancelNoResponseTimer();

            int responseType = responseCode / 100;

            if (responseType == 1) {
                if (log.isInfoEnabled()) log.info("Provisional response for INVITE received while " +
                                                  "waiting to disconnect call. A SIP CANCEL request " +
                                                  "can now be sent.");
                call.setStateFailed(FailedSubState.LINGERING_CANCEL);

                // Send CANCEL request
                sendCancelRequest();

            } else if (responseType == 2) {
                if (log.isInfoEnabled()) log.info("Final ok response for INVITE received while " +
                                                  "waiting to disconnect call. A SIP BYE request " +
                                                  "can now be sent.");
                try {
                    // Send ACK request
                    sendAckRequest();

                    call.setStateFailed(FailedSubState.LINGERING_BYE);

                    // Send BYE request
                    sendByeRequest();

                } catch (IllegalStateException e) {
                    if (log.isDebugEnabled())
                        log.debug("Closing down outbound call failed.");
                }

            } else {
                if (log.isInfoEnabled()) log.info("Final error response for INVITE received while " +
                                                  "waiting to disconnect call. The call is now disconnected.");
                call.setStateFailed(FailedSubState.COMPLETED);
            }
        }
    }

    /**
     * Handles a Call timeout.
     * <p/>
     *
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("A call timeout <" + callTimeoutEvent.getType() +
                    "> was received in " + this + ".");

        if (callTimeoutEvent.getType() == CallTimerTask.Type.NO_RESPONSE) {
            String message = "The outbound call has timed out while waiting " +
                    "for a response. The call is now considered completed.";

            log.error(message + " dialogId=" + call.getInitialDialogId());

            // Set call to failed
            call.setStateFailed(FailedSubState.COMPLETED);

        } else {
            if (log.isDebugEnabled())
                log.debug("The call timeout <" + callTimeoutEvent.getType() +
                        "> is ignored.");
        }
    }
}
