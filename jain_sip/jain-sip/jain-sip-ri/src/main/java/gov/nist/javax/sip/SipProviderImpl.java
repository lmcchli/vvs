/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip;

import java.util.*;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import javax.sip.*;

import gov.nist.core.*;
import java.io.*;
import java.text.ParseException;

import org.apache.log4j.Logger;

/*
 * Contributions (bug fixes) made by:
 * Daniel J. Martinez Manzano,
 * Hagai Sela. Bug reports by Shanti Kadiyala,
 * Rhys Ulerich Bug,Victor Hugo 
 */
/**
 * Implementation of the JAIN-SIP provider interface.
 * 
 * @version 1.2 $Revision: 1.39 $ $Date: 2006/11/12 21:52:50 $
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 */


public final class SipProviderImpl implements javax.sip.SipProvider,
        SIPTransactionEventListener {

    private static final Logger log = Logger.getLogger(SipProviderImpl.class);

    protected SipListener sipListener;

    protected SipStackImpl sipStack;

    /*
      * A set of listening points associated with the provider At most one LP per
      * transport
      */
    private HashMap<String, ListeningPoint> listeningPoints;

    private EventScanner eventScanner;

    private String address;

    private int port;

    private boolean automaticDialogSupportEnabled;

    /**
     * Stop processing messages for this provider. Post an empty message to our
     * message processing queue that signals us to quit.
     */
    protected void stop() {
        // Put an empty event in the queue and post ourselves a message.
        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug("Exiting provider");
        for (Object o : listeningPoints.values()) {
            ListeningPointImpl listeningPoint = (ListeningPointImpl) o;
            listeningPoint.removeSipProvider();
        }
        this.eventScanner.stop();

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getListeningPoint(java.lang.String)
      */
    public ListeningPoint getListeningPoint(String transport) {
        if (transport == null)
            throw new NullPointerException("Null transport param");
        return this.listeningPoints.get(transport.toUpperCase());
    }

    /**
     * Handle the SIP event - because we have only one listener and we are
     * already in the context of a separate thread, we dont need to enque the
     * event and signal another thread.
     *
     * @param sipEvent
     *            is the event to process.
     *
     */

    public void handleEvent(EventObject sipEvent, SIPTransaction transaction) {
        if (log.isDebugEnabled()) {
            log.debug("handleEvent " + sipEvent + "currentTransaction = "
                    + transaction + "this.sipListener = "
                    + this.sipListener + "sipEvent.source = "
                    + sipEvent.getSource());
            if (sipEvent instanceof RequestEvent) {
                Dialog dialog = ((RequestEvent) sipEvent).getDialog();
                if (log.isDebugEnabled()) log.debug("Dialog = " + dialog);
            } else if (sipEvent instanceof ResponseEvent) {
                Dialog dialog = ((ResponseEvent) sipEvent).getDialog();
                if (log.isDebugEnabled()) log.debug("Dialog = " + dialog);
            }
            sipStack.getLogWriter().logStackTrace();
        }

        EventWrapper eventWrapper = new EventWrapper(sipEvent, transaction);

        if (!sipStack.reEntrantListener) {
            // Run the event in the context of a single thread.
            this.eventScanner.addEvent(eventWrapper);
        } else {
            // just call the delivery method
            this.eventScanner.deliverEvent(eventWrapper);
        }
    }

    /** Creates a new instance of SipProviderImpl */
    protected SipProviderImpl(SipStackImpl sipStack) {
        this.eventScanner = sipStack.eventScanner; // for quick access.
        this.sipStack = sipStack;
        this.eventScanner.incrementRefcount();
        this.listeningPoints = new HashMap<String, ListeningPoint>();
        this.automaticDialogSupportEnabled = this.sipStack
                .isAutomaticDialogSupportEnabled();
    }

    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#clone()
      */
    protected Object clone() throws java.lang.CloneNotSupportedException {
        throw new java.lang.CloneNotSupportedException();
    }

    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#addSipListener(javax.sip.SipListener)
      */
    public void addSipListener(SipListener sipListener)
            throws TooManyListenersException {

        // Check if a SIP listener already is registered for the provider
        if ((this.sipListener != null) && (this.sipListener != sipListener))
            throw new TooManyListenersException(
                    "Provider already has a listener. " +
                            "Only one listener per provider allowed.");

        // If there is no SIP listener for the stack, add this one as default
        if (sipStack.sipListener == null) {
            sipStack.sipListener = sipListener;
            if (log.isDebugEnabled())
                log.debug("Set SIP stack listener to: " + sipListener);
        }

        if (log.isDebugEnabled())
            log.debug("Adding SIP listener to provider. <SipListener = " +
                    sipListener + ">, <SipProvider = " + this + ">");

        this.sipListener = sipListener;
    }

    /*
      * This method is deprecated (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getListeningPoint()
      */

    public ListeningPoint getListeningPoint() {
        if (this.listeningPoints.size() > 0)
            return this.listeningPoints.values().iterator().next();
        else
            return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getNewCallId()
      */
    public CallIdHeader getNewCallId() {
        String callId = Utils.generateCallIdentifier(this.getListeningPoint()
                .getIPAddress());
        CallID callid = new CallID();
        try {
            callid.setCallId(callId);
        } catch (java.text.ParseException ex) {
            if (log.isDebugEnabled()) log.debug(ex.getMessage(), ex);
        }
        return callid;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getNewClientTransaction(javax.sip.message.Request)
      */
    public ClientTransaction getNewClientTransaction(Request request)
            throws TransactionUnavailableException {
        if (request == null)
            throw new NullPointerException("null request");
        if (!sipStack.isAlive())
            throw new TransactionUnavailableException("Stack is stopped");

        SIPRequest sipRequest = (SIPRequest) request;
        if (sipRequest.getTransaction() != null)
            throw new TransactionUnavailableException(
                    "Transaction already assigned to request");
        // Be kind and assign a via header for this provider if the user is sloppy
        if  (sipRequest.getTopmostVia() == null) {
            ListeningPointImpl lp = (ListeningPointImpl) this.getListeningPoint("udp");
            Via via = lp.getViaHeader();
            request.setHeader(via);
        }
        // Give the request a quick check to see if all headers are assigned.
        try {
            sipRequest.checkHeaders();
        } catch (ParseException ex) {
            throw new TransactionUnavailableException(ex.getMessage());
        }

        /*
           * User decided to give us his own via header branch. Lets see if it
           * results in a clash. If so reject the request.
           */
        if (sipRequest.getTopmostVia().getBranch() != null &&
                sipRequest.getTopmostVia().getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) &&
                sipStack.findTransaction((SIPRequest)request, false) != null) {
            throw new TransactionUnavailableException("Transaction already exists!");
        }


        String transport = sipRequest.getTopmostVia().getTransport();
        ListeningPointImpl listeningPoint = (ListeningPointImpl) this
                .getListeningPoint(transport);
        // Check to see if the sentby  of the topmost via header matches the sentby of our listening point.
        if (listeningPoint == null ||
                !listeningPoint.getSentBy().equalsIgnoreCase(sipRequest.getTopmostVia().getSentBy().toString())) {

            // TODO: Clean up logging
            if (log.isDebugEnabled()) {
                log.debug("listeningPoint=" + listeningPoint +
                        " transport=" + transport +
                        " listeningPoints=" + listeningPoints);
                if (listeningPoint != null)
                    log.debug("listeningPoint=" + listeningPoint.getIPAddress() + ":" +
                            listeningPoint.getPort());
            }

//            if (sipStack.isLoggingEnabled()) {
//                sipStack.getLogWriter().logError(
//                        "listeningPoint " + listeningPoint);
//                if (listeningPoint != null)
//                    sipStack.getLogWriter().logError(
//                            "port = " + listeningPoint.getPort());
//
//            }
            String sentBy = null;
            if (listeningPoint != null)
                sentBy = listeningPoint.getSentBy();

            throw new TransactionUnavailableException(
                    "sentBy does not match the sentby setting of the ListeningPoint "
                            + " listeningPoint sentBy = " + sentBy +
                            " topmost VIA sentBy = "
                            + sipRequest.getTopmostVia().getSentBy().toString());
        }

        if (request.getMethod().equalsIgnoreCase(Request.CANCEL)) {
            SIPClientTransaction ct = (SIPClientTransaction) sipStack
                    .findCancelTransaction((SIPRequest) request, false);
            if (ct != null) {
                SIPClientTransaction retval = sipStack.createClientTransaction(
                        (SIPRequest) request, ct.getNextHop(), ct.getListeningPoint());
                retval.addEventListener(this);
                if (ct.getDialog() != null) {
                    retval.setDialog((SIPDialog) ct.getDialog(),
                            sipRequest.getDialogId(false));
                }
                return retval;
            }

        }
        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug(
                    "could not find existing transaction for "
                            + ((SIPRequest) request).getFirstLine()
                            + " creating a new one ");

        // Could not find a dialog or the route is not set in dialog.

        Hop hop;
        try {
            hop = sipStack.getNextHop((SIPRequest) request);
            if (hop == null)
                throw new TransactionUnavailableException(
                        "Cannot resolve next hop -- transaction unavailable");
        } catch (SipException ex) {
            throw new TransactionUnavailableException(
                    "Cannot resolve next hop -- transaction unavailable", ex);
        }
        String dialogId = sipRequest.getDialogId(false);
        SIPDialog dialog = sipStack.getDialog(dialogId);
        if (dialog != null && dialog.getState() == DialogState.TERMINATED) {

            // throw new TransactionUnavailableException
            // ("Found a terminated dialog -- possible re-use of old tag
            // parameters");
            sipStack.removeDialog(dialog);

        }

        // An out of dialog route was found. Assign this to the
        // client transaction.

        try {
            // Set the brannch id before you ask for a tx.
            // If the user has set his own branch Id and the
            // branch id starts with a valid prefix, then take it.
            // otherwise, generate one.
            if (sipRequest.getTopmostVia().getBranch() == null
                    || !sipRequest.getTopmostVia().getBranch().startsWith(
                    SIPConstants.BRANCH_MAGIC_COOKIE)) {
                sipRequest.getTopmostVia().setBranch(Utils.generateBranchId());
            }

            SIPClientTransaction ct = sipStack.createClientTransaction(
                    sipRequest, hop, listeningPoint);
            if (ct == null)
                throw new TransactionUnavailableException("Cound not create tx");

            // if the stack supports dialogs then
            if (sipStack.isDialogCreated(request.getMethod())) {
                // create a new dialog to contain this transaction
                // provided this is necessary.
                // This could be a re-invite
                // in which case the dialog is re-used.
                // (but noticed by Brad Templeton)
                if (dialog != null)
                    ct.setDialog(dialog, sipRequest.getDialogId(false));
                else if (this.isAutomaticDialogSupportEnabled()) {
                    SIPDialog sipDialog = sipStack.createDialog(ct);
                    ct.setDialog(sipDialog, sipRequest.getDialogId(false));
                }
            } else {
                if (dialog != null) {
                    ct.setDialog(dialog, sipRequest.getDialogId(false));
                }

            }

            // The provider is the event listener for all transactions.
            ct.addEventListener(this);
            return ct;

        } catch (java.text.ParseException ex) {
            InternalErrorHandler.handleException(ex);
            throw new TransactionUnavailableException(
                    "Unexpected Exception FIXME! ", ex);
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getNewServerTransaction(javax.sip.message.Request)
      */
    public ServerTransaction getNewServerTransaction(Request request)
            throws TransactionAlreadyExistsException,
            TransactionUnavailableException {

        if (!sipStack.isAlive())
            throw new TransactionUnavailableException("Stack is stopped");
        SIPServerTransaction transaction;
        SIPRequest sipRequest = (SIPRequest) request;
        try {
            sipRequest.checkHeaders();
        } catch (ParseException ex) {
            sipStack.getLogWriter().logError("Missing a required Header", ex);
            throw new TransactionUnavailableException(ex.getMessage());
        }

        /*
           * Got a notify.
           */
        if (sipRequest.getMethod().equals(Request.NOTIFY)
                && sipRequest.getFromTag() != null
                && sipRequest.getToTag() == null) {

            SIPClientTransaction ct = sipStack.findSubscribeTransaction(
                    sipRequest, (ListeningPointImpl) this.getListeningPoint());
            if (ct == null) {
                throw new TransactionUnavailableException(
                        "Cannot find matching Subscription ");
            }
        }
        if (sipStack.isDialogCreated(sipRequest.getMethod())) {
            if (sipStack.findTransaction((SIPRequest) request, true) != null)
                throw new TransactionAlreadyExistsException(
                        "server transaction already exists!");

            transaction = (SIPServerTransaction) ((SIPRequest) request)
                    .getTransaction();
            if (transaction == null)
                throw new TransactionUnavailableException(
                        "Transaction not available");

            sipStack.addTransaction(transaction);

            // So I can handle timeouts.
            transaction.addEventListener(this);
            if (log.isDebugEnabled())
                log.debug("Adding event listener to transaction. " +
                        "Transaction=" + transaction + ", listener=" + this);

            if (isAutomaticDialogSupportEnabled()) {
                // If automatic dialog support is enabled then
                // this tx gets his own dialog.
                String dialogId = sipRequest.getDialogId(true);
                SIPDialog dialog = sipStack.getDialog(dialogId);
                if (dialog == null) {
                    dialog = sipStack.createDialog(transaction);

                }
                transaction.setDialog(dialog, sipRequest.getDialogId(true));
                dialog.addRoute(sipRequest);
                if (dialog.getRemoteTag() != null
                        && dialog.getLocalTag() != null) {
                    this.sipStack.putDialog(dialog);
                }
            }

        } else {
            if (isAutomaticDialogSupportEnabled()) {
                // Under autmatic dialog support, dialog is tied into a
                // transaction.
                // You cannot create a server tx except for dialog creating
                // transactions.
                // after that, all subsequent transactions are created for you
                // by the stack.
                transaction = (SIPServerTransaction) sipStack.findTransaction(
                        (SIPRequest) request, true);
                if (transaction != null)
                    throw new TransactionAlreadyExistsException(
                            "Transaction exists! ");
                transaction = (SIPServerTransaction) ((SIPRequest) request)
                        .getTransaction();
                if (transaction == null)
                    throw new TransactionUnavailableException(
                            "Transaction not available!");

                // Map the transaction.
                sipStack.addTransaction(transaction);

                // If there is a dialog already assigned then just update the
                // dialog state.
                String dialogId = sipRequest.getDialogId(true);
                SIPDialog dialog = sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                }

            } else {
                transaction = (SIPServerTransaction) sipStack.findTransaction(
                        (SIPRequest) request, true);
                if (transaction != null)
                    throw new TransactionAlreadyExistsException(
                            "Transaction exists! ");
                transaction = (SIPServerTransaction) ((SIPRequest) request)
                        .getTransaction();
                if (transaction != null) {

                    // Map the transaction.
                    sipStack.mapTransaction(transaction);

                    // If there is a dialog already assigned then just
                    // assign the dialog to the transaction.
                    String dialogId = sipRequest.getDialogId(true);
                    SIPDialog dialog = sipStack.getDialog(dialogId);
                    if (dialog != null) {
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, sipRequest
                                .getDialogId(true));
                    }

                    return transaction;
                } else {
                    // tx does not exist so create the tx.

                    transaction = sipStack.createServerTransaction(sipRequest);

                    if ( transaction == null)
                        throw new TransactionUnavailableException(
                                "Transaction unavailable -- too many server transactions");

                    sipStack.mapTransaction(transaction);

                    // If there is a dialog already assigned then just
                    // assign the dialog to the transaction.
                    String dialogId = sipRequest.getDialogId(true);
                    SIPDialog dialog = sipStack.getDialog(dialogId);
                    if (dialog != null) {
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, sipRequest
                                .getDialogId(true));
                    }

                    return transaction;
                }
            }

        }
        return transaction;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getSipStack()
      */
    public SipStack getSipStack() {
        return this.sipStack;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#removeSipListener(javax.sip.SipListener)
      */
    public void removeSipListener(SipListener sipListener) {

        // Only remove if it is the listener registered for this provider.
        if (sipListener == this.sipListener) {
            this.sipListener = null;

            // Check if this listener is the SIP stack listener as well
            if (sipStack.sipListener == sipListener) {

                // If so, try to make another listener the SIP stack listener
                SipListener foundListener = null;

                for (Iterator it = sipStack.getSipProviders(); it.hasNext();) {
                    SipProviderImpl nextProvider = (SipProviderImpl) it.next();
                    if (nextProvider.sipListener != null) {
                        foundListener = nextProvider.sipListener;
                        break;
                    }
                }
                sipStack.sipListener = foundListener;
            }
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#sendRequest(javax.sip.message.Request)
      */
    public void sendRequest(Request request) throws SipException {
        if (!sipStack.isAlive())
            throw new SipException("Stack is stopped.");
        if (request.getMethod().equals(Request.ACK)){
            Dialog dialog = sipStack.getDialog(((SIPRequest)request).getDialogId(false));
            if ( dialog != null && dialog.getState() != null) {
                sipStack.getLogWriter().logWarning
                        ("Dialog exists -- you may want to use Dialog.sendAck() "+ dialog.getState());
            }
        }
        Hop hop = sipStack.getRouter((SIPRequest)request).getNextHop(request);
        if (hop == null)
            throw new SipException("could not determine next hop!");
        SIPRequest sipRequest = (SIPRequest) request;
        if (sipRequest.getTopmostVia() == null)
            throw new SipException("Invalid SipRequest -- no via header!");



        try {
            /*
                * JvB: Via branch should already be OK, dont touch it here? Some
                * apps forward statelessly, and then it's not set. So set only when
                * not set already, dont overwrite CANCEL branch here..
                */
            Via via = sipRequest.getTopmostVia();
            String branch = via.getBranch();
            if (branch == null || branch.length() == 0) {
                via.setBranch(sipRequest.getTransactionId());
            }

            String transport = hop.getTransport();
            ListeningPointImpl listeningPoint = (ListeningPointImpl) this
                    .getListeningPoint(transport);
            if (listeningPoint == null)
                throw new SipException(
                        "whoopsa daisy! no listening point found for transport "
                                + transport);

            int sourcePort = listeningPoint.getPort();
            MessageProcessor messageProcessor = sipStack.getMessageProcessor(
                    sourcePort, hop.getTransport());

            if (messageProcessor != null) {
                messageProcessor.sendMessage(hop, null, sipRequest);
            } else {
                throw new SipException(
                        "Could not find a message processor for port="
                                + sourcePort + ", transport=" + transport);
            }

        } catch (IOException ex) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logException(ex);
            }

            throw new SipException(
                    "IO Exception occured while Sending Request", ex);

        } catch (ParseException ex1) {
            InternalErrorHandler.handleException(ex1);
        } finally {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug(
                        "done sending " + request.getMethod() + " to hop "
                                + hop);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#sendResponse(javax.sip.message.Response)
      */
    public void sendResponse(Response response) throws SipException {
        if (!sipStack.isAlive())
            throw new SipException("Stack is stopped");
        SIPResponse sipResponse = (SIPResponse) response;
        Via via = sipResponse.getTopmostVia();
        if (via == null)
            throw new SipException("No via header in response!");
        String transport = via.getTransport();

        // check to see if Via has "received paramaeter". If so
        // set the host to the via parameter. Else set it to the
        // Via host.
        String host = via.getReceived();

        if (host == null)
            host = via.getHost();

        // Symmetric nat support
        int port = via.getRPort();
        if (port == -1) {
            port = via.getPort();
            if (port == -1) {
                if (transport.equalsIgnoreCase("TLS"))
                    port = 5061;
                else
                    port = 5060;
            }
        }

        // for correct management of IPv6 addresses.
        if (host.indexOf(":") > 0)
            if (host.indexOf("[") < 0)
                host = "[" + host + "]";

        Hop hop = sipStack.getAddressResolver().resolveAddress(new HopImpl(host, port, transport));


        ListeningPointImpl listeningPoint = (ListeningPointImpl) this
                .getListeningPoint(transport);
        if (listeningPoint == null)
            throw new SipException(
                    "whoopsa daisy! no listening point found for transport "
                            + transport);

        int sourcePort = listeningPoint.getPort();
        MessageProcessor messageProcessor = sipStack.getMessageProcessor(
                sourcePort, hop.getTransport());

        if (messageProcessor != null) {
            try {
                messageProcessor.sendMessage(hop, null, sipResponse);
            } catch (IOException e) {
                throw new SipException(e.getMessage());
            }
        } else {
            throw new SipException(
                    "Could not find a message processor for port="
                            + sourcePort + ", transport=" + transport);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#setListeningPoint(javax.sip.ListeningPoint)
      */
    public void setListeningPoint(ListeningPoint listeningPoint) {
        if (listeningPoint == null)
            throw new NullPointerException("Null listening point");
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        lp.setSipProvider(this);
        String transport = lp.getTransport().toUpperCase();
        this.address = listeningPoint.getIPAddress();
        this.port = listeningPoint.getPort();
        // This is the first listening point.
        this.listeningPoints.clear();
        this.listeningPoints.put(transport, listeningPoint);

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getNewDialog(javax.sip.Transaction)
      */

    public Dialog getNewDialog(Transaction transaction) throws SipException {
        if (transaction == null)
            throw new NullPointerException("Null transaction!");

        if (!sipStack.isAlive())
            throw new SipException("Stack is stopped.");

        if (isAutomaticDialogSupportEnabled())
            throw new SipException(" Error - AUTOMATIC_DIALOG_SUPPORT is on");

        if (!sipStack.isDialogCreated(transaction.getRequest().getMethod()))
            throw new SipException("Dialog cannot be created for this method "
                    + transaction.getRequest().getMethod());

        SIPDialog dialog;
        SIPTransaction sipTransaction = (SIPTransaction) transaction;

        if (transaction instanceof ServerTransaction) {
            SIPServerTransaction st = (SIPServerTransaction) transaction;
            Response response = st.getLastResponse();
            if ( response != null ) {
                if (response.getStatusCode() != 100)
                    throw new SipException("Cannot set dialog after response has been sent");
            }
            SIPRequest sipRequest = (SIPRequest) transaction.getRequest();
            String dialogId = sipRequest.getDialogId(true);
            dialog = sipStack.getDialog(dialogId);
            if (dialog == null) {
                dialog = sipStack.createDialog((SIPTransaction) transaction);
                // create and register the dialog and add the inital route set.
                dialog.addTransaction(sipTransaction);
                dialog.addRoute(sipRequest);
                sipTransaction.setDialog(dialog, null);
            } else {
                sipTransaction.setDialog(dialog, sipRequest.getDialogId(true));
            }
        } else {

            SIPClientTransaction sipClientTx = (SIPClientTransaction) transaction;

            SIPResponse response = sipClientTx.getLastResponse();

            if (response == null) {
                // A response has not yet been received, then set this up as the
                // default dialog.
                SIPRequest request = (SIPRequest) sipClientTx.getRequest();

                String dialogId = request.getDialogId(false);
                dialog = sipStack.getDialog(dialogId);
                if (dialog != null) {
                    throw new SipException("Dialog already exists!");
                } else {
                    dialog = sipStack.createDialog(sipTransaction);
                }
                sipClientTx.setDialog(dialog, null);

            } else {
                throw new SipException(
                        "Cannot call this method after response is received!");
            }
        }
        return dialog;

    }

    /**
     * Invoked when an error has ocurred with a transaction. Propagate up to the
     * listeners.
     *
     * @param transactionErrorEvent
     *            Error event.
     */
    public void transactionErrorEvent(
            SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent
                .getSource();

        if (transactionErrorEvent.getErrorID() == SIPTransactionErrorEvent.TRANSPORT_ERROR) {
            // There must be a way to inform the TU here!!
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "TransportError occured on " + transaction);
            }
            // Treat this like a timeout event. (Suggestion from Christophe).
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            TimeoutEvent ev;

            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent(this, (ServerTransaction) errorObject,
                        timeout);
            } else {
                ev = new TimeoutEvent(this, (ClientTransaction) errorObject,
                        timeout);
            }
            // Handling transport error like timeout
            this.handleEvent(ev, (SIPTransaction) errorObject);
        } else if (transactionErrorEvent.getErrorID() == SIPTransactionErrorEvent.TIMEOUT_ERROR) {
            // This is a timeout event.
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            TimeoutEvent ev;

            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent(this, (ServerTransaction) errorObject,
                        timeout);
            } else {
                ev = new TimeoutEvent(this, (ClientTransaction) errorObject,
                        timeout);
            }
            this.handleEvent(ev, (SIPTransaction) errorObject);

        } else if (transactionErrorEvent.getErrorID() == SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT) {
            // This is a timeout retransmit event.
            // We should never get this if retransmit filter is
            // enabled (ie. in that case the stack should handle.
            // all retransmits.
            Object errorObject = transactionErrorEvent.getSource();
            Transaction tx = (Transaction) errorObject;

            if (tx.getDialog() != null)
                InternalErrorHandler.handleException("Unexpected event !",
                        this.sipStack.getLogWriter());

            Timeout timeout = Timeout.RETRANSMIT;
            TimeoutEvent ev;

            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent(this, (ServerTransaction) errorObject,
                        timeout);
            } else {
                ev = new TimeoutEvent(this, (ClientTransaction) errorObject,
                        timeout);
            }
            this.handleEvent(ev, (SIPTransaction) errorObject);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#getListeningPoints()
      */
    public ListeningPoint[] getListeningPoints() {

        ListeningPoint[] retval = new ListeningPointImpl[this.listeningPoints
                .size()];
        this.listeningPoints.values().toArray(retval);
        return retval;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#addListeningPoint(javax.sip.ListeningPoint)
      */
    public void addListeningPoint(ListeningPoint listeningPoint)
            throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (lp.getSipProvider() != null && lp.getSipProvider() != this)
            throw new ObjectInUseException(
                    "Listening point assigned to another provider");
        String transport = lp.getTransport().toUpperCase();
        if (this.listeningPoints.isEmpty()) {
            // first one -- record the IP address/port of the LP

            this.address = listeningPoint.getIPAddress();
            this.port = listeningPoint.getPort();
        } else {
            if ((!this.address.equals(listeningPoint.getIPAddress()))
                    || this.port != listeningPoint.getPort())
                throw new ObjectInUseException(
                        "Provider already has different IP Address associated");

        }
        if (this.listeningPoints.containsKey(transport)
                && this.listeningPoints.get(transport) != listeningPoint)
            throw new ObjectInUseException(
                    "Listening point already assigned for transport!");

        // This is for backwards compatibility.
        lp.setSipProvider(this);

        this.listeningPoints.put(transport, lp);

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#removeListeningPoint(javax.sip.ListeningPoint)
      */
    public void removeListeningPoint(ListeningPoint listeningPoint)
            throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (lp.getMessageProcessor().inUse())
            throw new ObjectInUseException("Object is in use");
        this.listeningPoints.remove(lp.getTransport().toUpperCase());

    }

    /**
     * Remove all the listening points for this sip provider. This is called
     * when the stack removes the Provider
     */
    public void removeListeningPoints() {
        for (Iterator it = this.listeningPoints.values().iterator(); it
                .hasNext();) {
            ListeningPointImpl lp = (ListeningPointImpl) it.next();
            lp.getMessageProcessor().stop();
            it.remove();
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipProvider#setAutomaticDialogSupportEnabled(boolean)
      */
    public void setAutomaticDialogSupportEnabled(
            boolean automaticDialogSupportEnabled) {
        this.automaticDialogSupportEnabled = automaticDialogSupportEnabled;
    }

    /**
     * @return Returns the automaticDialogSupportEnabled.
     */
    public boolean isAutomaticDialogSupportEnabled() {
        return automaticDialogSupportEnabled;
    }

    public ContactHeader createContactForProvider(String transport) {
        try {
            String ipAddress = this.getListeningPoint(transport).getIPAddress();
            int port = this.getListeningPoint(transport).getPort();
            SipURI sipURI = new SipUri();
            sipURI.setHost(ipAddress);
            sipURI.setPort(port);
            sipURI.setTransportParam(transport);
            Contact contact = new Contact();
            AddressImpl address = new AddressImpl();
            address.setURI(sipURI);
            contact.setAddress(address);
            return contact;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

}
