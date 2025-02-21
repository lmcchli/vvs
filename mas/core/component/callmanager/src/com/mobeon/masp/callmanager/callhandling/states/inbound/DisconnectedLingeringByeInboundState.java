/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;

/**
 * Represents the sub state "LingeringBye" of {@link DisconnectedInboundState}.
 * This sub state is entered when the Call Manager client has disconnected a
 * call in {@link ConnectedInboundState}, a SIP BYE request has been sent and
 * a SIP OK response is expected.
 * <p>
 * In this state accept and reject is not allowed. A disconnect
 * request generates a {@link DisconnectedEvent}.
 * <p>
 * All methods are synchronized to handle each event atomically.
 *
 * @author Malin Flodin
 */
public class DisconnectedLingeringByeInboundState extends DisconnectedInboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public DisconnectedLingeringByeInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Disconnected state (sub state LingeringBye)";
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * A SIP "OK" is sent as response to the BYE request.
     * The state is set to {@link DisconnectedCompletedInboundState}.
     * <p>
     * If the SIP OK response could not be sent, an {@link ErrorEvent} is
     * generated and the state is set to {@link ErrorInboundState}.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP BYE request received in " + this + ".");

        if (log.isInfoEnabled()) log.info("The far end has disconnected the call with a SIP BYE request.");

        // The disconnect is now completed!
        call.setStateDisconnected(DisconnectedSubState.COMPLETED);

        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Parses a SIP response.
     * <p>
     * In this state, a SIP "OK" response is expected to a previously sent
     * SIP BYE request.
     * <p>
     * All responses received not addressing a BYE or INFO request are ignored.
     * Any response to the BYE request results in
     * the state being set to {@link DisconnectedCompletedInboundState}.
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

        // Check for response to BYE
        responseCode =
                sipResponseEvent.retrieveResponseCodeForMethod(Request.BYE);

        if (responseCode != null) {
            if (log.isDebugEnabled())
                log.debug("Processing BYE response");
            if (log.isInfoEnabled()) log.info("Response has been received for the SIP BYE request. " +
                                              "The call is now disconnected.");
            call.setStateDisconnected(DisconnectedSubState.COMPLETED);
        }
    }

    /**
     * Handles a SIP timeout event.
     * <p/>
     * The state is set to {@link ErrorInboundState}.
     *
     * @param sipTimeoutEvent carries information regarding the timeout event.
     */
    public synchronized void processSipTimeout(SipTimeoutEvent sipTimeoutEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP timeout expired in " + this + ".");

        if (log.isInfoEnabled()) {
            log.info("SIP timeout expired. The call is considered completed.");
        }

        call.setStateError(ErrorInboundState.ErrorSubState.COMPLETED);
    }
}
