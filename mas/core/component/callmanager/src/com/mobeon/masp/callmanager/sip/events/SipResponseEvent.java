/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.ResponseEvent;
import javax.sip.Transaction;
import javax.sip.SipProvider;
import javax.sip.message.Response;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.Header;


/**
 * The SipResponseEvent is used to carry a received SIP response.
 * <p>
 * It wraps the {@link ResponseEvent}.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SipResponseEvent extends SipEventImpl {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final ResponseEvent responseEvent;
    private final SipMessage sipMessage;

    /**
     * @param   responseEvent   MUST NOT be null.
     * @throws  IllegalArgumentException
     *          if source, responseEvent or responseEvent.getResponse() is null.
     */
    public SipResponseEvent(ResponseEvent responseEvent)
        throws IllegalArgumentException {

        if (responseEvent == null)
            throw new IllegalArgumentException("ResponseEvent is null.");

        if (responseEvent.getResponse() == null)
            throw new IllegalArgumentException("Response is null.");

        this.responseEvent = responseEvent;
        this.sipMessage = new SipResponse(
                responseEvent.getResponse(),
                responseEvent.getClientTransaction(),
                (SipProvider)responseEvent.getSource());
    }

    public ResponseEvent getResponseEvent() {
        return responseEvent;
    }

    public Transaction getTransaction() {
        return responseEvent.getClientTransaction();
    }

    public SipMessage getSipMessage() {
        return sipMessage;
    }

    public String getMethod() {
        return getSipMessage().getMethod();
    }

    /**
     * @return Returns the response contained in this event. Null is never returned.
     */
    public Response getResponse() {
        return responseEvent.getResponse();
    }

    public int getResponseCode() {
        return responseEvent.getResponse().getStatusCode();
    }

    public String getReasonPhrase() {
        return responseEvent.getResponse().getReasonPhrase();
    }

    /**
     * Get the value of the Retry-After header. Will return null if no Retry-After header exist.
     * @return a positive integer indicating the retry after time in seconds.
     */
    public Integer getRetryAfter() {
        Header header = responseEvent.getResponse().getHeader(RetryAfterHeader.NAME);
        if (header instanceof RetryAfterHeader) {
            return ((RetryAfterHeader)header).getRetryAfter();
        } else {
            return null;
        }
    }

    /**
     * TODO: Drop 6! Document
     * @param method
     * @return null if the response did not match the given method or was not
     * retrievable.
     */
    public Integer retrieveResponseCodeForMethod(String method) {
        Integer responseCode = null;

        String responseMethod = getMethod();
        if ((responseMethod != null) && (responseMethod.equals(method)))
            responseCode = getResponseCode();

        return responseCode;
    }

    public String toString() {
        return "Sip Response Event: code=" + getResponseCode() +
                ", early id = " + getEarlyDialogId();
    }

    public static SipResponseEvent createSipResponseEvent(
            ResponseEvent responseEvent) {

        int responseType = responseEvent.getResponse().getStatusCode() / 100;

        SipResponseEvent sipResponseEvent;
        if (responseType == 1) {
            sipResponseEvent =
                    new SipProvisionalResponseEvent(responseEvent);
        } else if (responseType == 2) {
            sipResponseEvent = new SipOkResponseEvent(responseEvent);
        } else {
            sipResponseEvent = new SipErrorResponseEvent(responseEvent);
        }

        return sipResponseEvent;
    }

    /**
     * Constructs an early dialog ID for the response event.
     * <p>
     * The dialog ID depends upon the events initial transaction.
     * The dialog ID is based on the CallID, To and From tags.
     * <p>
     * If the initial transaction is a server transaction
     * (i.e. the response event is received for a request sent for an inbound
     * call), the dialogID will be <callid>:<to tag>
     * <br>
     * If the initial transaction is a client transaction
     * (i.e. the response event is received for a request sent for an outbound
     * call), the dialogID will be <callid>:<from tag>
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
                if (getSipMessage().getToHeaderTag() != null)
                    earlyDialogId += ":" + getSipMessage().getToHeaderTag();

            } else {
                if (getSipMessage().getFromHeaderTag() != null)
                    earlyDialogId += ":" + getSipMessage().getFromHeaderTag();
            }

            earlyDialogId = earlyDialogId.toLowerCase();
        }

        return earlyDialogId;
    }

    /**
     * Constructs an established dialog ID for the response event.
     * <p>
     * The dialog ID is based on the CallID, To and From tags.
     * The dialogID will be:
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

            if (getSipMessage().getFromHeaderTag() != null)
                dialogId += ":" + getSipMessage().getFromHeaderTag();

            if (getSipMessage().getToHeaderTag() != null)
                dialogId += ":" + getSipMessage().getToHeaderTag();

            dialogId = dialogId.toLowerCase();
        }
        return dialogId;
    }

}
