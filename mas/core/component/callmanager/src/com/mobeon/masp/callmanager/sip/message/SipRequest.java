/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import javax.sip.message.Request;

/**
 * A container for a SIP request that is created by the Call Manager and
 * shall be sent to peer. This SIP request is created by the '
 * SipRequestFactoryImpl.
 *
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SipRequest extends SipMessageImpl {
    private final Request request;

    public SipRequest(Request request) {
        super(request);
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public String getMethod() {
        return request.getMethod();
    }
}
