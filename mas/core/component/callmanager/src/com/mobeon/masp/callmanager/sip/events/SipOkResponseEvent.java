/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import javax.sip.ResponseEvent;

/**
 * This class carries a received SIP OK response.
 *
 * @author Malin Flodin
 */
public class SipOkResponseEvent extends SipResponseEvent {

    public SipOkResponseEvent(ResponseEvent responseEvent) {
        super(responseEvent);
    }
}
