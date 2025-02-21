/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states.inbound;

import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Response;

/**
 * Represents the sub state "WaitingForAck" of {@link FailedInboundState}.
 * This sub state is entered when the inbound call for some reason is
 * disconnected after the Call Manager client has accepted the call but
 * before the SIP ACK has been received. This can for example occur due to an
 * administrator lock request or a disconnect requested by the Call Manager
 * client.
 * <p>
 * In this state, we are waiting for the
 * ACK to be able to send a SIP BYE request to disconnect the call properly.
 * <p>
 * All methods are synchronized to handle each event atomically.
 * @author Malin Flodin
 */
public class FailedWaitingForAckInboundState extends FailedInboundState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public FailedWaitingForAckInboundState(InboundCallInternal call) {
        super(call);
    }

    public String toString() {
        return "Failed state (sub state WaitingForAck)";
    }

    /**
     * Handles a SIP ACK request.
     * <p>
     * Since a pending disconnect exists (due to for example a previously
     * requested disconnect or media negotiation failure), a SIP BYE request
     * is sent.
     * The state is set to {@link FailedLingeringByeInboundState}.
     * <p>
     * If an error occured while sending the SIP BYE request, an
     * {@link ErrorEvent} is generated and the state is set to
     * {@link ErrorInboundState}.
     * @param sipRequestEvent carries the SIP ACK request.
     */
    public synchronized void processAck(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP ACK request received in " + this +
                      ". A SIP BYE request shall be sent.");

        if (log.isInfoEnabled()) log.info("A SIP ACK request received while waiting for disconnecting " +
                                          "the call. A SIP BYE request can now be sent.");
        try {
            SipRequest sipRequest = CMUtils.getInstance().
                    getSipRequestFactory().createByeRequest(
                    call.getDialog(), call.getPChargingVector());
            CMUtils.getInstance().getSipMessageSender().sendRequestWithinDialog(
                    call.getDialog(), sipRequest);

            call.cancelNoAckTimer();
            call.setStateFailed(FailedSubState.LINGERING_BYE);
        } catch (Exception e) {
            call.errorOccurred(
                    "SIP BYE request could not be sent. The call is " +
                    "considered completed. " + e.getMessage(),
                    true);
        }
    }

    /**
     * Handles a SIP BYE request.
     * <p>
     * The state is set to {@link FailedCompletedInboundState}.
     * A SIP "OK" is sent as response to the BYE request.
     * If the response could not be sent, an {@link ErrorEvent} is generated
     * and the state is set to {@link ErrorInboundState}.
     *
     * @param sipRequestEvent carries the SIP BYE request.
     */
    public synchronized void processBye(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP BYE request received in " + this + ".");

        if (log.isInfoEnabled()) log.info("The far end has disconnected the call with a SIP BYE request.");

        call.setStateFailed(FailedSubState.COMPLETED);

        // Send OK response for BYE request
        call.sendOkResponse(sipRequestEvent, true);
    }

    /**
     * Handles a SIP re-INVITE request.
     * <p>
     * A SIP re-INVITE in this state means that an INVITE has been
     * received while the initial INVITE request is pending.
     * The re-INVITE is rejected with a SIP "Request Pending" response.
     * The state is left unchanged.
     * <p>
     * If the response could not be sent, the state is set to
     * {@link ErrorInboundState} and an {@link ErrorEvent} is generated.
     * @param sipRequestEvent carries the SIP re-INVITE request.
     */
    public synchronized void processReInvite(SipRequestEvent sipRequestEvent) {
        if (log.isDebugEnabled())
            log.debug("SIP Re-INVITE received in " + this + ".");

        if (log.isInfoEnabled()) log.info("SIP re-INVITE request is rejected with a SIP 491 response");

        call.sendErrorResponse(Response.REQUEST_PENDING, sipRequestEvent,
                "A SIP INVITE is still pending.");
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

        if (callTimeoutEvent.getType() == CallTimerTask.Type.NO_ACK) {
            String message = "The inbound call has timed out while waiting " +
                    "for an ACK. The call is now considered completed.";
            //if (log.isInfoEnabled()) log.info(message);
            log.error(message + " CalledParty: " + call.getCalledParty().toString()
            + " dialogId=" + call.getInitialDialogId());

            // Set call to failed
            call.setStateFailed(FailedInboundState.FailedSubState.COMPLETED);

        } else {
            if (log.isDebugEnabled())
                log.debug("The call timeout <" + callTimeoutEvent.getType() +
                        "> is ignored.");
        }
    }
}
