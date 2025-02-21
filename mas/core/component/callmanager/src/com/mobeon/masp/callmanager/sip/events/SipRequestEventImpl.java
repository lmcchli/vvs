/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.Dialog;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Date;

/**
 * The SipRequestEvent is used to carry a received SIP request.
 * <p>
 * Normally SIP requests are carried in the SIP RequestEvent class which
 * contains the server transaction and the SIP request. But, for out-of-dialog
 * requests, the server transaction has not been created. It is created
 * when constructing the SipRequestEvent. Therefore, an event
 * is needed to carry both the original SIP Request Event and the newly
 * created server transaction.
 * <p>
 * NOTE: Creating the SipRequestEvent MUST be done before any further
 * processing of the request event due to the fact that a Server Transaction
 * MUST be created immediately if one does not exist. This is required by the
 * NIST SIP stack!
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipRequestEventImpl extends SipEventImpl implements SipRequestEvent {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final RequestEvent requestEvent;
    private final ServerTransaction serverTransaction;
    private SipMessage sipMessage;
    private final Date creationDate;

    private AtomicReference<String> toTag = new AtomicReference<String>();
    private boolean requestIsInitialInvite = false;

	private Object perf; //For performance stat purposes

    /**
     * @param   requestEvent    MUST NOT be null.
     * @throws  IllegalArgumentException
     *          if source, requestEvent or requestEvent.getRequest() is null.
     */
    public SipRequestEventImpl(RequestEvent requestEvent)
            throws IllegalArgumentException {
        if (requestEvent == null)
            throw new IllegalArgumentException("RequestEvent is null.");

        if (requestEvent.getRequest() == null)
            throw new IllegalArgumentException("Request is null.");
    

        this.requestEvent = requestEvent;
        this.sipMessage = new SipRequest(requestEvent.getRequest());
        this.serverTransaction = retrieveOrCreateTransaction();
        creationDate = new Date();
    }
    
    public void enterCheckPoint(String checkPointId){
        perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPointId);	
    }
    
    public void exitCheckPoint() {
    	 if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
             CommonOamManager.profilerAgent.exitCheckpoint(perf);
         }
    	
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public RequestEvent getRequestEvent() {
        return requestEvent;
    }

    public Request getRequest() {
        return requestEvent.getRequest();
    }

    public Transaction getTransaction() {
        return getServerTransaction();
    }

    public ServerTransaction getServerTransaction() {
        return serverTransaction;
    }

    public String getToTag() {
        String tag = toTag.get();

        if (tag == null) {
            Request request = getRequestEvent().getRequest();
            if (request != null) {
                ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
                tag = to.getTag();
            }
        }

        return tag;
    }

    public Dialog getDialog() {
        return serverTransaction.getDialog();
    }

    public String getMethod() {
        return getSipMessage().getMethod();
    }

    /**
     * Returns the SIP message contained in the request event.
     * Null is NEVER returned.
     * @return The SIP message that initiated the request event.
     */
    public SipMessage getSipMessage() {
        return sipMessage;
    }

    public SipProvider getSipProvider() {
        return (SipProvider)requestEvent.getSource();
    }

    public synchronized boolean isRequestInitialInvite() {
        return requestIsInitialInvite;
    }

    /**
     * @return true if the request was validated ok and false otherwise.
     */
    public boolean validateGeneralPartOfRequest() {
        boolean result = true;
        if (!getMethod().equals(Request.ACK)) {
            result = CMUtils.getInstance().getSipRequestValidator().validateGeneralRequest(this);
        }
        return result;
    }

    public void generateToTag() {
        this.toTag.set(CMUtils.getInstance().getSipStackWrapper().generateTag());
    }

    public synchronized void setInitialInvite() {
        requestIsInitialInvite = true;
    }

    public String toString() {
        return "SipRequestEvent: method=" + getMethod() +
                ", early id = " + getEarlyDialogId();
    }

    /**
     * Constructs an early dialog ID for the request event.
     * <p>
     * The dialog ID depends upon the events initial transaction.
     * The dialog ID is based on the CallID, To and From tags.
     * <p>
     * If the initial transaction is a server transaction
     * (i.e. the request event is received for an inbound call), the dialogID
     * will be <callid>:<from tag>
     * <br>
     * If the initial transaction is a client transaction
     * (i.e. the request event is received for an outbound call), the dialogID
     * will be <callid>:<to tag>
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
                if (getSipMessage().getFromHeaderTag() != null)
                    earlyDialogId += ":" + getSipMessage().getFromHeaderTag();

            } else {
                if (getSipMessage().getToHeaderTag() != null)
                    earlyDialogId += ":" + getSipMessage().getToHeaderTag();
            }

            earlyDialogId = earlyDialogId.toLowerCase();
        }

        return earlyDialogId;
    }

    /**
     * Constructs an established dialog ID for the request event.
     * <p>
     * The dialog ID is based on the CallID, To and From tags.
     * The dialogID will be:
     * <callid>:<to tag>:<from tag>
     * <p>
     * If the to tag does not exist in the request, the to tag is retrieved
     * from the sip event itself (if set).
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

            if (getSipMessage().getToHeaderTag() != null)
                dialogId += ":" + getSipMessage().getToHeaderTag();
            else {
                String toTag = getToTag();
                if (toTag != null)
                    dialogId += ":" + toTag;
            }

            if (getSipMessage().getFromHeaderTag() != null)
                dialogId += ":" + getSipMessage().getFromHeaderTag();

            dialogId = dialogId.toLowerCase();
        }
        return dialogId;
    }


    // ============================ Private methods ======================

    /**
     * Retrieves the server transaction from the request event.
     * If the server transaction is null, a new server transaction is created.
     *
     * @return  The server transaction retrieved from the request event or
     *          created. Null is returned if a new transaction could not be
     *          created.
     */
    private ServerTransaction retrieveOrCreateTransaction() {

        Request request = requestEvent.getRequest();
        ServerTransaction transaction = requestEvent.getServerTransaction();

        if (transaction == null) {
            // Create a new server transaction

            if (log.isDebugEnabled())
                log.debug("Creating a new server transaction. dialogId=" + sipMessage.getCallId().toLowerCase());

            SipProvider sipProvider = (SipProvider) requestEvent.getSource();

            try {
                transaction = sipProvider.getNewServerTransaction(request);

            } catch (TransactionAlreadyExistsException ex) {
                /**
                 * Failed to create a new server transaction for an incoming
                 * request due to the fact that a transaction already exists
                 * handling this Request.
                 * This may happen if a retransmit of the same request is
                 * received before the initial transaction is allocated, i.e.
                 * race condition that may be ignored.
                 */

                if (log.isDebugEnabled())
                    log.debug("TransactionAlreadyExistsException received when " +
                            "creating a new server request. " +
                            "Trying to retrieve the server transaction again... callId=" + sipMessage.getCallId().toLowerCase());

                // Try to retrieve server transaction again
                transaction = requestEvent.getServerTransaction();

            } catch (TransactionUnavailableException ex) {
                /**
                 * Failed to create a new server transaction for an incoming
                 * request. This occurs if a new transaction can not be created,
                 * for example the next hop of the request cannot be determined.
                 */
                if (log.isDebugEnabled())
                    log.debug("A new server transaction could not be created. callId=" + sipMessage.getCallId().toLowerCase(), ex);
            }

        }
        return transaction;
    }

    // TODO: Refactor and remove need for this. Then make sipMessage final!
    // Only used for basic tests
    public void setSipRequest(SipRequest sipRequest) {
        this.sipMessage = sipRequest;
    }

}
