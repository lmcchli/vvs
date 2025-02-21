/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import java.util.TimerTask;

/**
 * This class is a timer task (and extends {@link TimerTask}) for call related
 * timers.
 * <p>
 * Currently the following call timers exists:
 * <ul>
 * <li>
 * {@link Type.MAX_CALL_DURATION}: <br>
 * A timer that expires when maximum duration time for an outbound call has been
 * reached.
 * </li>
 * <li>
 * {@link Type.CALL_NOT_CONNECTED} <br>
 * A timer that expires when an outbound call has not been connected in time.
 * </li>
 * <li>
 * {@link Type.CALL_NOT_ACCEPTED} <br>
 * A timer that expires when an inbound call has not been accepted by the
 * Call Manager client in time.
 * </li>
 * <li>
 * {@link Type.EXPIRES}: <br>
 * A timer that is scheduled if the Expires header is set for an inbound call
 * or if a Contact header has a certain expiration for a re-directed outbound
 * call. 
 * </li>
 * <li>
 * {@link Type.REDIRECTED_RTP}: <br>
 * A timer that is scheduled if the support for redirection of RTP is activated
 * for an inbound call when waiting for the re-INVITE messages. 
 * </li>
 *  <li>
 * {@link Type.SESSION_PROGRESS_RETRANSMISSION}: <br>
 * A timer that is scheduled for the support of regular SIP 183 Session Progress retransmission
 * for an inbound call while in early media 
 * </li>
 *  <li>
 * {@link Type.CALL_NOT_CONNECTED_EXTENSION}: <br>
 * A timer that expires when an outbound call not been connected in time.
 * This timer is reset every time a Non-100 SIP 1xx provisional response is received. 
 * </li>
 * </ul>
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class CallTimerTask extends TimerTask {
    private final CallImpl call;
    private final Type type;

    public enum Type {
        MAX_CALL_DURATION,
        CALL_NOT_CONNECTED,
        CALL_NOT_ACCEPTED,
        EXPIRES,
        NO_ACK,
        NO_RESPONSE,
        REDIRECTED_RTP,
        SESSION_PROGRESS_RETRANSMISSION,
        SESSION_ESTABLISHMENT,
        CALL_NOT_CONNECTED_EXTENSION
    }

    public CallTimerTask(CallImpl call, Type type) {
        this.call = call;
        this.type = type;
    }

    public void run() {
        call.timeoutCall(type);
    }
}

