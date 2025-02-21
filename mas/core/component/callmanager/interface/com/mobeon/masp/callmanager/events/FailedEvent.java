/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallDirection;

/**
 * TODO: Drop 6! Document
 * TODO: Drop 6! Document that the call might be null in a FailedEvent or
 * ErrorEvent, but not in any other event.
 *
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class FailedEvent implements Event {

    public enum Reason {
        /** The call has been rejected by near end. This means that the call
         * has been rejected either by the service or by the Call Manager.
         * Call Manager can reject a call due to for example the current load
         * situation, the current administrative state, or no licenses are available or if the SIP INVITE
         * is not supported for some reason. */
        REJECTED_BY_NEAR_END,

        /** The call has been rejected by far end. This means that the call has
         * been rejected by the SIP phone, the SIP gateway or the network. */
        REJECTED_BY_FAR_END,

        /** The call has been disconnected by near end due to the detection of
         * an abandoned stream, i.e. the call has been abandoned by far end. */
        FAR_END_ABANDONED,

        /** The call has been disconnected since the Call Manager client has
         * not accepted the call in time, i.e. the call has been abandoned by
         * near end. */
        NEAR_END_ABANDONED,

        /** The call has been rejected due to "media negotiation failure".
         * For an inbound call, this occurs if the media suggested in the SDP
         * offer (in the INVITE) was not sufficient for the Call Manager.
         * For an outbound call, this occurs if the media suggested in the SDP
         * answer (in the SessionProgress or OK response) was not sufficient
         * for the Call Manager. */
        MEDIA_NEGOTIATION_FAILED
    }

    private final Call call;
    private final Reason reason;
    private final String message;
    private final CallDirection direction;
    private final int networkStatusCode;

    public FailedEvent(Call call, Reason reason,
                       CallDirection direction,
                       String message,
                       int networkStatusCode) {
        this.call = call;
        this.reason = reason;
        this.direction = direction;
        this.message = message;
        this.networkStatusCode = networkStatusCode;
    }

    public Call getCall() {
        return call;
    }

    public CallDirection getDirection() {
        return direction;
    }

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Only defined if the event occurs due to
     * {@link Reason.REJECTED_BY_FAR_END} for an outbound call.
     * Default network status code is returned for the other situations.
     * @return the Network Status Code that corresponds to the SIP response.
     */
    public int getNetworkStatusCode() {
        return networkStatusCode;
    }

    public String toString() {
        return "FailedEvent: <Call=" + call + ">, <Reason = " + reason +
                ">, <Direction = " + direction + ">, <Message = " + message +
                ">, <Network Status Code = " + networkStatusCode + ">";
    }
}
