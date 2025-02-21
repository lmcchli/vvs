/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.outbound;

import com.mobeon.masp.callmanager.callhandling.OutboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;

/**
 * A base class for all outbound states where the call is completed for any
 * reason e.g. {@link DisconnectedCompletedOutboundState} or
 * {@link FailedOutboundState}.
 * <p>
 * All states where the call has been completed have similar event handling.
 * The purpose of this base class is to gather this similar event handling.
 * All handling that is specific to each state is handled in that state.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public abstract class CallCompletedOutboundState extends OutboundCallState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public CallCompletedOutboundState(OutboundCallInternal call) {
        super(call);
    }

    /**
     * Handles an administrators lock request.
     * <p>
     * In this state, the call has already been disconnected or is about to
     * disconnect.
     * The lock request is therefore ignored.
     */
    public synchronized void processLockRequest() {
        if (log.isDebugEnabled())
            log.debug("Lock requested in " + this + ".");

        if (log.isInfoEnabled()) log.info("Lock request is ignored by this call, since the call is" +
                                          "already disconnected or about to disconnect.");
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * The call is already disconnected, but a SIP "OK" is sent as response
     * to the BYE request.
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
        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Handles a SIP re-INVITE request.
     * <p>
     * Since re-negotiation is not supported, the re-INVITE is rejected with a
     * SIP "Not Acceptable Here" response.
     * The state is left unchanged.
     * <p>
     * If the response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorCompletedOutboundState}.
     *
     * @param sipRequestEvent carries the SIP re-INVITE request.
     */
    public synchronized void processReInvite(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP re-INVITE request received in " + this + ".");

        if (log.isInfoEnabled()) log.info("SIP re-INVITE request is rejected with a SIP 488 response");

        // Send "Not Acceptable Here" response
        call.sendNotAcceptableHereResponse(
                sipRequestEvent,
                SipWarning.RENEGOTIATION_NOT_SUPPORTED);
    }

    /**
     * Handles a SIP OPTIONS request.
     * <p>
     * A SIP "OK" response is sent. The state is left unchanged.
     * <p>
     * If the response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorOutboundState}.
     *
     * @param sipRequestEvent carries the SIP re-INVITE request.
     */
    public synchronized void processOptions(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP OPTIONS request received in " + this + ".");
        if (log.isInfoEnabled()) log.info("SIP OPTIONS request is answered.");
        call.sendOkResponse(sipRequestEvent, false);
    }

    /**
     * Parses a SIP response. In this state, only responses to INFO requests are
     * expected. This method checks and logs the result of the INFO request
     * using {@link #processInfoResponse(SipResponseEvent)}.
     * @param sipResponseEvent carries the SIP response.
     */
    public synchronized void processSipResponse(SipResponseEvent sipResponseEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP " + sipResponseEvent.getResponseCode() +
                    " response received for a " + sipResponseEvent.getMethod() +
                    " request in " + this + ".");

        Integer responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.INFO);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing INFO response");
            processInfoResponse(sipResponseEvent);
        }
    }

    /**
     * Handles a SIP timeout event.
     * <p>
     * An {@link ErrorEvent} is generated and the state is set to
     * {@link ErrorCompletedOutboundState}.
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP timeout expired in " + this + ".");
        call.errorOccurred(
                "SIP timeout expired. The call is considered completed.",
                true);
    }
    /**
     * Handles a Call timeout.
     * <p>
     * A Call timeout should not be handled if the call is already completed.
     */
    public synchronized void handleCallTimeout(CallTimeoutEvent callTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("A call timeout <" + callTimeoutEvent.getType() +
                    "> was received in " + this + ". It is ignored.");
    }

}
