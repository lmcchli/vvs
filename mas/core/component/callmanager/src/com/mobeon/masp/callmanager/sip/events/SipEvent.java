/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.events.EventObject;

import javax.sip.Transaction;

/**
 * This is an interface that represents a SIP event.
 * The event could either be a received SIP request, response or timeout.
 *
 * @author Malin Flodin
 */

public interface SipEvent extends EventObject {

    public String getMethod();

    public SipMessage getSipMessage();

    public Transaction getTransaction();

    public String getEarlyDialogId();

    public String getEstablishedDialogId();

}
