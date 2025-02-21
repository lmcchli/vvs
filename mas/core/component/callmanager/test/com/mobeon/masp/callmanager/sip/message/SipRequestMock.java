/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import org.jmock.Mock;

import javax.sip.message.Request;

/**
 * A mocked SIPRequest used in basic tests.
 * @author Malin Flodin
 */
public class SipRequestMock extends SipMessageMock {
    private final SipRequest sipRequest;

    public SipRequestMock() {
        Mock mockRequest = new Mock(Request.class);
        sipRequest = new SipRequest((Request)(mockRequest.proxy()));
        super.init(mockRequest, sipRequest);
    }

    public SipRequest getSipRequest() {
        return sipRequest;
    }
}
