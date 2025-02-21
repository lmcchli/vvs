/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;

/**
 * The SipTimeoutEvent is used to carry a received SIP timeout or transaction
 * error.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipTimeoutEvent extends SipEventImpl {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final TimeoutEvent timeoutEvent;
    private final SipMessage sipMessage;

    /**
     * @param timeoutEvent MUST NOT be null.
     * @throws IllegalArgumentException if source or timeoutEvent is null.
     * Also if the transaction or request in the timeoutEvent are null.
     */
    public SipTimeoutEvent(TimeoutEvent timeoutEvent)
            throws IllegalArgumentException {
        if (timeoutEvent == null)
            throw new IllegalArgumentException(
                    "Creating SipTimeoutEvent but timeoutEvent is null.");

        this.timeoutEvent = timeoutEvent;

        if ((isServerTimeout() && timeoutEvent.getServerTransaction() == null) ||
                (!isServerTimeout() && timeoutEvent.getClientTransaction() == null)) {
            throw new IllegalArgumentException(
                    "Creating SipTimeoutEvent but the SIP transaction is null.");
        }

        Request request = getRequest();
        if (request == null)
            throw new IllegalArgumentException(
                    "Creating SipTimeoutEvent but the SIP request is null.");

        this.sipMessage = new SipRequest(getRequest());

    }

    public SipMessage getSipMessage() {
        return sipMessage;
    }

    public Transaction getTransaction() {
        if (timeoutEvent.isServerTransaction()) {
            return timeoutEvent.getServerTransaction();
        } else {
            return timeoutEvent.getClientTransaction();
        }
    }

    public String getMethod() {
        return getRequest().getMethod();
    }

    public int getEventReason() {
        return timeoutEvent.getTimeout().getValue();
    }


    public String toString() {
        return "SipTimeoutEvent: " + ", early id = " + getEarlyDialogId();
    }


    /**
     * Constructs an early dialog ID for the timeout event.
     * <p>
     * The dialog ID depends upon the events initial transaction and on the
     * transaction for the timeout event.
     * The dialog ID is based on the CallID, To and From tags.
     * <p>
     * If the initial transaction is a server transaction
     * (i.e. the request event is received for an inbound call), the dialogID
     * will be as follows:<br>
     * If a server transaction timed out: <callid>:<from tag>
     * If a client transaction timed out: <callid>:<to tag>
     * <p>
     * If the initial transaction is a client transaction
     * (i.e. the request event is received for an outbound call), the dialogID
     * will be as follows:<br>
     * If a server transaction timed out: <callid>:<to tag>
     * If a client transaction timed out: <callid>:<from tag>
     *
     * @return A dialog id
     */
    public String getEarlyDialogId() {
        Transaction transaction = getTransaction();

        String earlyDialogId = null;

        if (transaction == null) {
            if (log.isDebugEnabled())
                log.debug("Could not retrieve early dialogid. " +
                        "Transaction is null.");
        }  else if (transaction.getDialog() == null)  {
            if (log.isDebugEnabled())
                log.debug("Could not retrieve early dialogid. " +
                        "Transaction dialog is null.");
        } else {
            earlyDialogId = getSipMessage().getCallId();

            if (transaction.getDialog().isServer()) {
                if (transaction instanceof ServerTransaction) {
                    if (getSipMessage().getFromHeaderTag() != null)
                        earlyDialogId += ":" + getSipMessage().getFromHeaderTag();
                } else {
                    if (getSipMessage().getToHeaderTag() != null)
                        earlyDialogId += ":" + getSipMessage().getToHeaderTag();
                }
            } else {
                if (transaction instanceof ServerTransaction) {
                    if (getSipMessage().getToHeaderTag() != null)
                        earlyDialogId += ":" + getSipMessage().getToHeaderTag();
                } else {
                    if (getSipMessage().getFromHeaderTag() != null)
                        earlyDialogId += ":" + getSipMessage().getFromHeaderTag();
                }
            }

            earlyDialogId = earlyDialogId.toLowerCase();
        }

        return earlyDialogId;
    }

    /**
     * Constructs an established dialog ID for the timeout event.
     * <p>
     * The dialog ID depends upon the transaction for the timeout event.
     * The dialog ID is based on the CallID, To and From tags.
     * <p>
     * If the transaction is a server transaction, the dialogID
     * will be as follows:<br>
     * <callid>:<to tag>:<from tag>
     * <p>
     * If the transaction is a client transaction, the dialogID
     * will be as follows:<br>
     * <callid>:<from tag>:<to tag>
     *
     * @return A dialog id
     */
    public String getEstablishedDialogId() {
        String dialogId = null;
        Transaction transaction = getTransaction();

        if (transaction == null) {
            if (log.isDebugEnabled())
                log.debug("Could not retrieve established dialogid. " +
                        "Transaction is null.");
        } else {
            dialogId = getSipMessage().getCallId();

            if (transaction instanceof ServerTransaction) {
                if (getSipMessage().getToHeaderTag() != null)
                    dialogId += ":" + getSipMessage().getToHeaderTag();
                if (getSipMessage().getFromHeaderTag() != null)
                    dialogId += ":" + getSipMessage().getFromHeaderTag();
            } else {
                if (getSipMessage().getFromHeaderTag() != null)
                    dialogId += ":" + getSipMessage().getFromHeaderTag();
                if (getSipMessage().getToHeaderTag() != null)
                    dialogId += ":" + getSipMessage().getToHeaderTag();
            }

            dialogId = dialogId.toLowerCase();
        }
        return dialogId;
    }

    // ====================== Private methods ======================

    private Request getRequest() {
        Request request;
        if (isServerTimeout()) {
            request = timeoutEvent.getServerTransaction().getRequest();
        } else {
            request = timeoutEvent.getClientTransaction().getRequest();
        }
        return request;
    }

    private boolean isServerTimeout() {
        return timeoutEvent.isServerTransaction();
    }

}
