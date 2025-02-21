/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import javax.sip.ResponseEvent;

/**
 * This class carries a received SIP error response.
 *
 * @author Malin Flodin
 */
public class SipErrorResponseEvent extends SipResponseEvent {

    public SipErrorResponseEvent(ResponseEvent responseEvent) {
        super(responseEvent);
    }
}
