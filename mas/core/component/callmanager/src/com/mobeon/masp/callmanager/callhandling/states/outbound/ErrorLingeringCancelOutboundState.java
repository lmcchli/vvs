/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;

/**
 * Represents the sub state "LingeringCancel" of {@link ErrorOutboundState}.
 * This sub state is entered when the call has experienced an unrecoverable error,
 * a SIP CANCEL request has been sent and a SIP OK response is expected.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class ErrorLingeringCancelOutboundState extends ErrorOutboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public ErrorLingeringCancelOutboundState(OutboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Error state (sub state LingeringCancel)";
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * A SIP "OK" is sent as response to the BYE request.
     * The state is set to {@link ErrorCompletedOutboundState}.
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
        call.setStateError(ErrorSubState.COMPLETED);

        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Parses a SIP response.
     * <p>
     * A response to a sent CANCEL request is ignored. No change in state is
     * made until a final response has been received for the INVITE.
     * <br>
     * If the response is a final error response (i.e. 3xx-6xx) to the initial
     * INVITE, the state is set to {@link ErrorCompletedOutboundState}.
     * If the response is a final ok response (i.e. 2xx) to the initial INVITE,
     * a SIP ACK request is sent, the state is set to
     * {@link ErrorLingeringByeOutboundState} and a SIP BYE request is sent.
     * If the SIP ACK or SIP BYE request could not be sent, an {@link ErrorEvent}
     * is generated and the state is set to {@link ErrorCompletedOutboundState}.
     * <p>
     * A response to a INFO request is parsed and an error is logged if the
     * response indicated error with a Video Fast Update.
     *
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {

        if (log.isDebugEnabled())
            log.debug("SIP " + sipResponseEvent.getResponseCode() +
                    " response received for a " + sipResponseEvent.getMethod() +
                    " request in " + this + ".");


        // Check for response to INFO
        Integer responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INFO);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INFO response");
            processInfoResponse(sipResponseEvent);
        }

        // Check for response to INVITE
        responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INVITE);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INVITE response");
            int responseType = responseCode / 100;

            if (responseType == 1) {
                // A provisional response was received for INVITE
                // during pending cancellation. It is ignored.
                if (log.isInfoEnabled()) log.info("Provisional response for INVITE is ignored.");

            } else if (responseType == 2) {
                if (log.isInfoEnabled()) log.info("An OK response was received for INVITE during " +
                                                  "pending cancellation. The call is acknowledged and " +
                                                  "immediately disconnected with a SIP BYE request.");
                try {
                    // An OK response was received for INVITE during
                    // pending cancellation. The call was setup before
                    // the cancellation could take place. The call is
                    // acknowledged and immediately disconnected with a
                    // BYE request.

                    // Send ACK request
                    sendAckRequest();

                    if (log.isDebugEnabled())
                        log.debug("SIP ACK request is sent.");

                    call.setStateError(ErrorSubState.LINGERING_BYE);

                    // Send BYE request
                    sendByeRequest();
                } catch (IllegalStateException e) {
                    if (log.isDebugEnabled())
                        log.debug("Closing down outbound call failed.");
                }
            } else {
                // An error response was received for INVITE during
                // pending cancellation. The call is disconnected.
                if (log.isInfoEnabled()) log.info("Final error response received for INVITE. " +
                                                  "Call is disconnected.");
                call.setStateError(ErrorSubState.COMPLETED);
            }
        }
    }

}
