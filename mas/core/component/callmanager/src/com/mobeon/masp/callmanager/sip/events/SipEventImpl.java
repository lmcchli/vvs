/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.sip.message.SipMessage;

import javax.sip.Transaction;

/**
 * This class is an abstract class that carries a SIP event. This event could
 * either be a received SIP request, response or timeout.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public abstract class SipEventImpl implements SipEvent {

    public abstract String getMethod();

    public abstract SipMessage getSipMessage();

    public abstract Transaction getTransaction();

    public abstract String getEarlyDialogId();

    public abstract String getEstablishedDialogId();

    public String getTransactionId() {
        return getSipMessage().getTransactionId(getTransaction());
    }
}
