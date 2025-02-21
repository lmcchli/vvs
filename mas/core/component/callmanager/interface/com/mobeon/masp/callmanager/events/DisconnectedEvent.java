/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;


/**
 * An event generated when a call has been disconnected by either call party.
 * This event is also generated when the call manager client disconnects a call
 * that is already disconnected.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Malin Flodin
 */
public class DisconnectedEvent implements Event {

    public enum Reason {
        /** The call has been disconnected by near end. This means that the call
         * has been disconnected either by the service or by the Call Manager.
         * Call Manager disconnects a call if an administrator issues a lock
         * request. */
        NEAR_END,

        /** The call has been disconnected by far end. This means that the call
         * has been disconnected by the SIP phone, the SIP gateway or
         * the network. */
        FAR_END,

        /** The call has been disconnected by near end due to the detection of
         * an abandoned stream, i.e. the call has been abandoned by far end. */
        FAR_END_ABANDONED
    }

    private final Call call;
    private final Reason reason;
    private final boolean alreadyDisconnected;

    public DisconnectedEvent(Call call, Reason reason,
                             boolean alreadyDisconnected) {
        this.call = call;
        this.reason = reason;
        this.alreadyDisconnected = alreadyDisconnected;
    }

    public Call getCall() {
        return call;
    }

    public Reason getReason() {
        return reason;
    }

    public boolean isAlreadyDisconnected() {
        return alreadyDisconnected;
    }

    public String toString() {
        return "DisconnectedEvent <Call=" + call + ">, <Reason=" + reason +
                ">, <AlreadyDisconnected=" + alreadyDisconnected + ">";
    }
}
