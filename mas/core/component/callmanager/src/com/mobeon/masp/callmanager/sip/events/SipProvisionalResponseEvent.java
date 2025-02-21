/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import javax.sip.ResponseEvent;

/**
 * This class carries a received SIP provisional response.
 *
 * @author Malin Flodin
 */
public class SipProvisionalResponseEvent  extends SipResponseEvent {

    public SipProvisionalResponseEvent(ResponseEvent responseEvent) {
        super(responseEvent);
    }
}
