/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

/**
 * An event generated when the peer UA in an outbound call indicates progress
 * using provisional responses such as "Ringing" or "SignalProgressing".
 * <p>
 * Indicates if early media is available on the inbound stream of the outbound
 * call.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class ProgressingEvent implements Event {
    private final Call call;
    private final boolean earlyMedia;
    private final int responseCode;

    public ProgressingEvent(Call call, boolean earlyMedia) {
        this.call = call;
        this.earlyMedia = earlyMedia;
        this.responseCode = 0;
    }

    public ProgressingEvent(Call call, boolean earlyMedia, int responseCode) {
        this.call = call;
        this.earlyMedia = earlyMedia;
        this.responseCode = responseCode;
    }

    public Call getCall() {
        return call;
    }

    public boolean isEarlyMedia() {
        return earlyMedia;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String toString() {
        return "ProgressingEvent <Call=" + call + ">, <IsEarlyMedia=" + earlyMedia + ">" +
            (responseCode == 0 ? "" : "<responseCode=" + responseCode + ">");
    }
}
