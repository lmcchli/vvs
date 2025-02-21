/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.sip.header.SipWarning;

/**
 * Signals that a received remote SDP is not supported. This is detected while
 * parsing the remote SDP.
 * <p>
 * Contains a {@link SipWarning} further describing the reason why the SDP is
 * not supported.
 *
 * @author Malin Flodin
 */
public class SdpNotSupportedException extends Exception {
    private final SipWarning sipWarning;

    public SdpNotSupportedException(SipWarning sipWarning, String message) {
        super(message);
        this.sipWarning = sipWarning;
    }

    public SipWarning getSipWarning() {
        return sipWarning;
    }

}
