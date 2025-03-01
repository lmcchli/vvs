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
/**************************************************************************/
/* Product of NIST Advanced Networking Technologies Division		      */
/**************************************************************************/
package gov.nist.javax.sip.stack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.message.*;
import gov.nist.core.*;

import javax.sip.header.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;

/*
 * Bugs in this class were reported by Antonis Karydas, Brad Templeton, Jeff
 * Adams, Alex Rootham , Martin Le Clerk, Christophe Anzille, Andreas Bystrom,
 * Lebing Xie, Jeroen van Bemmel. Hagai Sela reported a bug in updating the
 * route set (on RE-INVITE).
 */

/**
 * Tracks dialogs. A dialog is a peer to peer association of communicating SIP
 * entities. For INVITE transactions, a Dialog is created when a success message
 * is received (i.e. a response that has a To tag). The SIP Protocol stores
 * enough state in the message structure to extract a dialog identifier that can
 * be used to retrieve this structure from the SipStack.
 *
 * @version 1.2 $Revision: 1.42 $ $Date: 2006/11/23 17:26:57 $
 *
 * @author M. Ranganathan
 *
 *
 */

public class SIPDialog implements javax.sip.Dialog {

    private static final Logger log = Logger.getLogger(SIPDialog.class);

    private static final long serialVersionUID = -1429794423085204069L;

    private boolean dialogTerminatedEventDelivered; // prevent duplicate

    // private DefaultRouter defaultRouter;

    private String method;

    // delivery of the event

    private boolean isAssigned;

    private boolean reInviteFlag;

    private Object applicationData; // Opaque pointer to application data.

    private SIPRequest originalRequest;

    // Last response.
    private AtomicReference<SIPResponse> lastResponse = new AtomicReference<SIPResponse>();

    private SIPTransaction firstTransaction;

    private SIPTransaction lastTransaction;

    private String dialogId;

    private AtomicLong localSequenceNumber = new AtomicLong();

    private AtomicLong remoteSequenceNumber = new AtomicLong();

    private String myTag;

    private String hisTag;

    private RouteList routeList;

    private SIPTransactionStack sipStack;

    private int dialogState;

    private AtomicBoolean ackSeen = new AtomicBoolean(false);

    protected AtomicReference<SIPRequest> lastSentAck = new AtomicReference<SIPRequest>();

    protected AtomicReference<SIPRequest> lastReceivedAck = new AtomicReference<SIPRequest>();

    protected boolean ackProcessed;

    protected volatile DialogTimerTask timerTask;
    protected final Object timerTaskLock = new Object();

    protected volatile DialogDeleteTask deletetimerTask;
    protected final Object deletetimerTaskLock = new Object();

    protected Long nextSeqno;

    private AtomicInteger retransmissionTicksLeft = new AtomicInteger(0);

    private AtomicInteger prevRetransmissionTicks = new AtomicInteger(0);

    private AtomicLong originalLocalSequenceNumber = new AtomicLong();

    // Audit tag used by the SIP Stack audit
    public long auditTag = 0;

    // The following fields are extracted from the request that created the
    // Dialog.

    private javax.sip.address.Address localParty;

    private javax.sip.address.Address remoteParty;

    protected CallIdHeader callIdHeader;

    public final static int EARLY_STATE = DialogState._EARLY;

    public final static int CONFIRMED_STATE = DialogState._CONFIRMED;

    public final static int TERMINATED_STATE = DialogState._TERMINATED;

    // the amount of time to keep this dialog around before the stack GC's it

    private static final int DIALOG_LINGER_TIME = 8;

    private boolean serverTransactionFlag;

    private SipProviderImpl sipProvider;

    private boolean terminateOnBye;

    private boolean byeSent; // Flag set when BYE is sent, to disallow new
    // requests

    private Address remoteTarget;

    private EventHeader eventHeader; // for Subscribe notify

    // //////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////
    class LingerTimer extends SIPStackTimerTask {

        public LingerTimer() {

        }

        protected void runTask() {
            SIPDialog dialog = SIPDialog.this;
            sipStack.removeDialog(dialog);
        }

    }

    class DialogTimerTask extends SIPStackTimerTask {
        int nRetransmissions;

        SIPServerTransaction transaction;

        public DialogTimerTask(SIPServerTransaction transaction) {
            this.transaction = transaction;
            this.nRetransmissions = 0;
        }

        protected void runTask() {
            // If I ACK has not been seen on Dialog,
            // resend last response.
            SIPDialog dialog = SIPDialog.this;
            if (log.isDebugEnabled())
                log.debug("Running dialog timer");

            nRetransmissions++;
            SIPServerTransaction transaction = this.transaction;
            /*
                * Issue 106. Section 13.3.1.4 RFC 3261 The 2xx response is passed
                * to the transport with an interval that starts at T1 seconds and
                * doubles for each retransmission until it reaches T2 seconds If
                * the server retransmits the 2xx response for 64*T1 seconds without
                * receiving an ACK, the dialog is confirmed, but the session SHOULD
                * be terminated.
                */
            if (nRetransmissions > sipStack.getTimerH()) {
                // MMANY: Only if the timeout occurs for an initial INVITE
                // shall the dialog be terminated.
                if (!isReInvite())
                    dialog.setState(SIPDialog.TERMINATED_STATE);

                if (transaction != null)
                    transaction
                            .raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            } else if (!dialog.isAckSeen() && transaction != null) {
                // Retransmit to 200 until ack receivedialog.
                SIPResponse response = transaction.getLastResponse();
                if (response.getStatusCode() == 200) {
                    try {

                        // resend the last response.
                        if (dialog.toRetransmitFinalResponse(sipStack.getT2()))
                            transaction.sendResponseToProcessor(response,
                                    transaction.originalRequest.getMessageChannel());

                    } catch (IOException ex) {
                        raiseIOException(transaction.getPeerAddress(),
                                transaction.getPeerPort(),
                                transaction.getListeningPoint().getTransport());

                    } finally {
                        // Need to fire the timer so
                        // transaction will eventually
                        // time out whether or not
                        // the IOException occurs
                        // Note that this firing also
                        // drives Listener timeout.
                        if (log.isDebugEnabled()) {
                            log.debug("resend 200 response from "
                                    + dialog);
                        }
                        transaction.fireTimer();
                    }
                }
            }

            // Stop running this timer if the dialog is in the
            // confirmed state or ack seen if retransmit filter on.
            // MMANY: Or if it is a re-INVITE that has timed out
            // (in this case the state will not be set to terminated).
            if (dialog.isAckSeen() || dialog.dialogState == TERMINATED_STATE ||
                    ((isReInvite()) && (nRetransmissions > sipStack.getTimerH()))) {
                this.transaction = null;
                this.cancel();

            }

        }

    }

    /**
     * This timer task is used to garbage collect the dialog after some time.
     *
     */

    class DialogDeleteTask extends SIPStackTimerTask {

        protected void runTask() {
            // MMANY: Stop the client-not-sending-any-ACK guard timer if
            // ack is seen or if we are in the middle of a re-INVITE and
            // the dialog state is CONFIRMED (thus an ACK already has
            // been received for the initial INVITE)
            // TODO: Maybe this is not sufficient for when we will be
            // sending re-INVITEs.
            if (isAckSeen() ||
                    (isReInvite() && getState() == DialogState.CONFIRMED))
                this.cancel();
            else
                delete();
        }

    }

    // ///////////////////////////////////////////////////////////
    // Constructors.
    // ///////////////////////////////////////////////////////////
    /**
     * Protected Dialog constructor.
     */
    private SIPDialog() {
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = -1; // not yet initialized.
        localSequenceNumber.set(0);
        remoteSequenceNumber.set(-1);

    }

    /**
     * Constructor given the first transaction.
     *
     * @param transaction
     *            is the first transaction.
     */
    public SIPDialog(SIPTransaction transaction) {
        this();
        if (transaction == null)
            throw new NullPointerException("Null tx");
        this.sipStack = transaction.sipStack;

        // this.defaultRouter = new DefaultRouter((SipStack) sipStack,
        // sipStack.outboundProxy);

        this.sipProvider = transaction.getSipProvider();
        if (sipProvider == null)
            throw new NullPointerException("Null Provider!");
        this.addTransaction(transaction);
        if (log.isDebugEnabled()) {
            log.debug("Creating a dialog : " + this +
                    "  provider port = "
                    + this.sipProvider.getListeningPoint().getPort());
        }
    }

    /**
     * Constructor given a transaction and a response.
     *
     * @param transaction --
     *            the transaction ( client/server)
     * @param sipResponse --
     *            response with the appropriate tags.
     */
    public SIPDialog(SIPTransaction transaction, SIPResponse sipResponse) {

        this(transaction);
        if (sipResponse == null)
            throw new NullPointerException("Null SipResponse");
        this.setLastResponse(transaction, sipResponse);

    }

    /**
     * create a sip dialog with a response ( no tx)
     */
    public SIPDialog(SipProviderImpl sipProvider, SIPResponse sipResponse) {

        this.sipProvider = sipProvider;
        this.sipStack = (SIPTransactionStack) sipProvider.getSipStack();
        this.setLastResponse(null, sipResponse);
        this.localSequenceNumber.set(sipResponse.getCSeq().getSeqNumber());
        this.originalLocalSequenceNumber.set(localSequenceNumber.get());
        this.myTag = sipResponse.getFrom().getTag();
        this.hisTag = sipResponse.getTo().getTag();
        this.localParty = sipResponse.getFrom().getAddress();
        this.remoteParty = sipResponse.getTo().getAddress();
        // this.defaultRouter = new DefaultRouter((SipStack) sipStack,
        // sipStack.outboundProxy);

        this.method = sipResponse.getCSeq().getMethod();
        this.callIdHeader = sipResponse.getCallId();
        this.serverTransactionFlag = false;
        if (log.isDebugEnabled()) {
            log.debug("Creating a dialog : " + this);
        }
    }

    // ///////////////////////////////////////////////////////////
    // Private methods
    // ///////////////////////////////////////////////////////////
    /**
     * Return true if this is a client dialog.
     *
     * @return true if the transaction that created this dialog is a client
     *         transaction and false otherwise.
     */
    private boolean isClientDialog() {
        SIPTransaction transaction = (SIPTransaction) this
                .getFirstTransaction();
        return transaction instanceof SIPClientTransaction;
    }

    /**
     * Raise an io exception for asyncrhonous retransmission of responses
     *
     * @param host --
     *            host to where the io was headed
     * @param port --
     *            remote port
     * @param protocol --
     *            protocol (udp/tcp/tls)
     */
    private void raiseIOException(String host, int port, String protocol) {
        // Error occured in retransmitting response.
        // Deliver the error event to the listener
        // Kill the dialog.

        IOExceptionEvent ioError = new IOExceptionEvent(this, host, port,
                protocol);
        sipProvider.handleEvent(ioError, null);

        setState(SIPDialog.TERMINATED_STATE);
    }

    /**
     * Set the remote party for this Dialog.
     *
     * @param sipMessage --
     *            SIP Message to extract the relevant information from.
     */
    private void setRemoteParty(SIPMessage sipMessage) {

        if (!isServer()) {

            this.remoteParty = sipMessage.getTo().getAddress();
        } else {
            this.remoteParty = sipMessage.getFrom().getAddress();

        }
        if (log.isDebugEnabled()) {
            log.debug("settingRemoteParty " + this.remoteParty);
        }
    }

    /**
     * Add a route list extracted from a record route list. If this is a server
     * dialog then we assume that the record are added to the route list IN
     * order. If this is a client dialog then we assume that the record route
     * headers give us the route list to add in reverse order.
     *
     * @param recordRouteList --
     *            the record route list from the incoming message.
     */

    private void addRoute(RecordRouteList recordRouteList) {
        try {
            if (this.isClientDialog()) {
                // This is a client dialog so we extract the record
                // route from the response and reverse its order to
                // careate a route list.
                this.routeList = new RouteList();
                // start at the end of the list and walk backwards

                ListIterator li = recordRouteList.listIterator(recordRouteList
                        .size());
                while (li.hasPrevious()) {
                    RecordRoute rr = (RecordRoute) li.previous();
                    Route route = new Route();
                    AddressImpl address =
                            (AddressImpl)rr.getAddress().clone();

                    route.setAddress(address);
                    route.setParameters((NameValueList) rr.getParameters()
                            .clone());

                    this.routeList.add(route);
                }
            } else {
                // This is a server dialog. The top most record route
                // header is the one that is closest to us. We extract the
                // route list in the same order as the addresses in the
                // incoming request.
                this.routeList = new RouteList();
                ListIterator li = recordRouteList.listIterator();
                while (li.hasNext()) {
                    RecordRoute rr = (RecordRoute) li.next();
                    Route route = new Route();
                    AddressImpl address =
                            (AddressImpl)rr.getAddress().clone();
                    route.setAddress(address);
                    route.setParameters((NameValueList) rr.getParameters()
                            .clone());
                    routeList.add(route);
                }
            }
        } finally {
            for (Object aRouteList : routeList) {
                SipURI sipUri = (SipURI) (((Route) aRouteList).getAddress()
                        .getURI());
                if (!sipUri.hasLrParam()) {
                    log.warn("NON LR route in Route set detected for dialog : "
                            + this);
                }
            }
        }
    }

    /**
     * Add a route list extacted from the contact list of the incoming message.
     *
     * @param contact --
     *            contact extracted from the incoming message.
     *
     */

    private void setRemoteTarget(ContactHeader contact) {
        this.remoteTarget = contact.getAddress();
    }

    /**
     * Extract the route information from this SIP response and add the relevant
     * information to the route set.
     *
     * @param sipResponse
     *            is the SIP response for which we want to add the route.
     */
    private synchronized void addRoute(SIPResponse sipResponse) {
        ContactList contactList = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("addRoute: dialogState: " + this
                        + "state = " + this.getState());
            }
            if (sipResponse.getStatusCode() == 100) {
                // Do nothing for trying messages.
                return;
            } else if (this.dialogState == TERMINATED_STATE) {
                // Do nothing if the dialog state is terminated.
                return;
            } else if (this.dialogState == CONFIRMED_STATE) {
                // cannot add route list after the dialog is initialized.
                // Remote target is updated on RE-INVITE but not
                // the route list.
                if (sipResponse.getStatusCode() / 100 == 2 && !this.isServer()) {
                    contactList = sipResponse.getContactHeaders();
                    if (contactList != null
                            && SIPRequest.isTargetRefresh(sipResponse.getCSeq()
                            .getMethod())) {
                        this.setRemoteTarget((ContactHeader) contactList
                                .getFirst());
                    }
                }
                return;
            }

            // Update route list on response if I am a client dialog.
            if (!isServer()) {

                RecordRouteList rrlist = sipResponse.getRecordRouteHeaders();
                // Add the route set from the incoming response in reverse
                // order for record route headers.
                if (rrlist != null) {
                    this.addRoute(rrlist);
                } else {
                    // Set the rotue list to the last seen route list.
                    this.routeList = new RouteList();
                }

                contactList = sipResponse.getContactHeaders();
                if (contactList != null) {
                    this
                            .setRemoteTarget((ContactHeader) contactList
                                    .getFirst());
                }
            }

        } finally {
            if (log.isDebugEnabled()) {
                log.debug("added a route = " + routeList.encode()
                        + "contactRoute = " + contactList);
            }
        }
    }

    /**
     * Get a cloned copy of route list for the Dialog.
     *
     * @return -- a cloned copy of the dialog route list.
     */
    private synchronized RouteList getRouteList() {
        if (log.isDebugEnabled())
            log.debug("getRouteList " + this);
        // Find the top via in the route list.
        ListIterator li;
        RouteList retval = new RouteList();

        if (this.routeList != null) {
            li = routeList.listIterator();
            while (li.hasNext()) {
                Route route = (Route) li.next();
                retval.add(route.clone());
            }
        }

        return retval;
    }

    /**
     * Sends ACK Request to the remote party of this Dialogue.
     *
     * @param request
     *            the new ACK Request message to send.
     * @throws SipException
     *             if implementation cannot send the ACK Request for any other
     *             reason
     */
    private void sendAck(Request request, boolean throwIOExceptionAsSipException)
            throws SipException {
        SIPRequest ackRequest = (SIPRequest) request;
        if (log.isDebugEnabled())
            log.debug("sendAck" + this);

        if (!ackRequest.getMethod().equals(Request.ACK))
            throw new SipException("Bad request method -- should be ACK");
        if (this.getState() == null
                || this.getState().getValue() == EARLY_STATE) {
            log.warn("Bad Dialog State for " + this
                    + " dialogID = " + this.getDialogId());
            throw new SipException("Bad dialog state " + this.getState());
        }

        if (!this.getCallId().getCallId().equals(
                ((SIPRequest) request).getCallId().getCallId())) {

            log.warn("Bad CallId. CallID " + this.getCallId() +
                    "RequestCallID = " +
                    ackRequest.getCallId().getCallId() +
                    "dialog =  " + this);

            throw new SipException("Bad call ID in request");
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("setting from tag For outgoing ACK= "
                        + this.getLocalTag());
                log.debug("setting To tag for outgoing ACK = "
                        + this.getRemoteTag());
                log.debug("ack = " + ackRequest);
            }
            if (this.getLocalTag() != null)
                ackRequest.getFrom().setTag(this.getLocalTag());
            if (this.getRemoteTag() != null)
                ackRequest.getTo().setTag(this.getRemoteTag());
        } catch (ParseException ex) {
            throw new SipException(ex.getMessage());
        }

        Hop hop = sipStack.getNextHop(ackRequest);
        // Hop hop = defaultRouter.getNextHop(ackRequest);
        if (hop == null)
            throw new SipException("No route!");
        try {
            if (log.isDebugEnabled())
                log.debug("hop = " + hop);
            ListeningPointImpl lp = (ListeningPointImpl) this.sipProvider
                    .getListeningPoint(hop.getTransport());
            if (lp == null)
                throw new SipException(
                        "No listening point for this provider registered at "
                                + hop);

            lp.getMessageProcessor().sendMessage(hop, null, ackRequest);
            this.lastSentAck.set(ackRequest);

        } catch (IOException ex) {
            if (throwIOExceptionAsSipException)
                throw new SipException("Could not send ack", ex);
            this.raiseIOException(hop.getHost(), hop.getPort(), hop
                    .getTransport());
        } catch (SipException ex) {
            log.warn(ex);
            throw ex;
        } catch (Exception ex) {
            log.warn(ex);
            throw new SipException("Could not create message channel", ex);
        }
        this.ackSeen.set(true);

    }

    // /////////////////////////////////////////////////////////////
    // Package local methods
    // /////////////////////////////////////////////////////////////

    /**
     * Set the stack address. Prevent us from routing messages to ourselves.
     *
     * @param sipStack
     *            the address of the SIP stack.
     *
     */
    void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;

    }

    /**
     * Return True if this dialog is terminated on BYE.
     *
     */
    boolean isTerminatedOnBye() {

        return this.terminateOnBye;
    }

    /**
     * Mark that the dialog has seen an ACK.
     */
    void ackReceived(SIPRequest sipRequest) {

        // Suppress retransmission of the final response
        if (isAckSeen())
            return;

        SIPServerTransaction tr = this.getInviteTransaction();
        if (tr != null) {
            if (tr.getCSeq() == sipRequest.getCSeq().getSeqNumber()) {
                this.ackSeen.set(true);
                this.lastReceivedAck.set(sipRequest);
                if (log.isDebugEnabled()) {
                    log.debug("ackReceived for "
                            + tr.getMethod());
                }
                this.setState(CONFIRMED_STATE);
            }
        }
    }

    /**
     * Return true if a terminated event was delivered to the application as a
     * result of the dialog termination.
     *
     */
    synchronized boolean testAndSetIsDialogTerminatedEventDelivered() {
        boolean retval = this.dialogTerminatedEventDelivered;
        this.dialogTerminatedEventDelivered = true;
        return retval;
    }

    // /////////////////////////////////////////////////////////
    // Public methods
    // /////////////////////////////////////////////////////////

    /*
      * @see javax.sip.Dialog#setApplicationData()
      */
    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getApplicationData()
      */
    public Object getApplicationData() {
        return this.applicationData;
    }

    /**
     * Updates the next consumable seqno.
     *
     */
    public synchronized void requestConsumed() {
        this.nextSeqno = this.getRemoteSeqNumber() + 1;

        if (log.isDebugEnabled()) {
            log.debug("Request Consumed -- next consumable Request Seqno = "
                    + this.nextSeqno);
        }

    }

    /**
     * Return true if this request can be consumed by the dialog.
     *
     * @param dialogRequest
     *            is the request to check with the dialog.
     * @return true if the dialogRequest sequence number matches the next
     *         consumable seqno.
     */
    public synchronized boolean isRequestConsumable(SIPRequest dialogRequest) {
        // have not yet set remote seqno - this is a fresh
        if (dialogRequest.getMethod().equals(Request.ACK))
            throw new RuntimeException("Illegal method");

        // JvB: Acceptable iff remoteCSeq < cseq. remoteCSeq==-1
        // when not defined yet, so that works too
        return remoteSequenceNumber.get() < dialogRequest.getCSeq().getSeqNumber();
    }

    /**
     * This method is called when a forked dialog is created from the client
     * side. It starts a timer task. If the timer task expires before an ACK is
     * sent then the dialog is cancelled (i.e. garbage collected ).
     *
     */
    public void doDeferredDelete() {
        if (sipStack.timer == null)
            this.setState(TERMINATED_STATE);
        else {
            startDeleteTimer();
        }
    }

    /**
     * Set the state for this dialog.
     *
     * @param state
     *            is the state to set for the dialog.
     */

    public void setState(int state) {
        if (log.isDebugEnabled()) {
            log.debug("Setting dialog state for " + this + "newState = " + state +
                    ", firsttransaction=" + firstTransaction);
            if (state != -1 && state != this.dialogState) {
                log.debug(this + "  old dialog state is "
                        + this.getState() + "  New dialog state is "
                        + DialogState.getObject(state));
            }

        }
        this.dialogState = state;
        // Dialog is in terminated state set it up for GC.
        if (state == TERMINATED_STATE) {
            if (sipStack.timer != null) { // may be null after shutdown
                sipStack.timer.schedule(new LingerTimer(),
                        DIALOG_LINGER_TIME * 1000);
            }
            this.stopTimer();

        }
    }

    /**
     * Return true if the dialog has already seen the ack.
     *
     * @return flag that records if the ack has been seen.
     */
    public boolean isAckSeen() {
        return this.ackSeen.get();
    }

    /**
     * Get the last sent ACK for this dialog.
     */
    public SIPRequest getLastSentAck() {
        return this.lastSentAck.get();
    }

    /**
     * Get the transaction that created this dialog.
     */
    public Transaction getFirstTransaction() {
        return this.firstTransaction;
    }

    /**
     * Gets the route set for the dialog. When acting as an User Agent Server
     * the route set MUST be set to the list of URIs in the Record-Route header
     * field from the request, taken in order and preserving all URI parameters.
     * When acting as an User Agent Client the route set MUST be set to the list
     * of URIs in the Record-Route header field from the response, taken in
     * reverse order and preserving all URI parameters. If no Record-Route
     * header field is present in the request or response, the route set MUST be
     * set to the empty set. This route set, even if empty, overrides any
     * pre-existing route set for future requests in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route and Contact header
     * fields. However, these requests do not cause the dialog's route set to be
     * modified.
     * <p>
     * The User Agent Client uses the remote target and route set to build the
     * Request-URI and Route header field of the request.
     *
     * @return an Iterator containing a list of route headers to be used for
     *         forwarding. Empty iterator is returned if route has not been
     *         established.
     */
    public Iterator getRouteSet() {
        if (this.routeList == null) {
            return new LinkedList().listIterator();
        } else {
            return this.getRouteList().listIterator();
        }
    }

    /**
     * Add a Route list extracted from a SIPRequest to this Dialog.
     *
     * @param sipRequest
     */
    public synchronized void addRoute(SIPRequest sipRequest) {
        if (log.isDebugEnabled()) {
            log.debug("addRoute: dialogState: " + this
                    + "state = " + this.getState());
        }
        if (this.dialogState == CONFIRMED_STATE
                || this.dialogState == TERMINATED_STATE) {
            return;
        }
        // Incoming Request has the route list
        RecordRouteList rrlist = sipRequest.getRecordRouteHeaders();
        // Add the route set from the incoming response in reverse
        // order
        if (rrlist != null) {

            this.addRoute(rrlist);
        } else {
            // Set the rotue list to the last seen route list.
            this.routeList = new RouteList();
        }
        // put the contact header from the incoming request into
        // the route set.
        ContactList contactList = sipRequest.getContactHeaders();
        if (contactList != null) {
            this.setRemoteTarget((ContactHeader) contactList.getFirst());
        }
    }

    /**
     * Set the dialog identifier.
     */
    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    /**
     * Creates a new dialog based on a received NOTIFY. The dialog state is
     * initialized appropriately. The NOTIFY differs in the From tag
     *
     * Made this a separate method to clearly distinguish what's happening here -
     * this is a non-trivial case
     *
     * @param subscribeTx -
     *            the transaction started with the SUBSCRIBE that we sent
     * @param notifyST -
     *            the ServerTransaction created for an incoming NOTIFY
     * @return -- a new dialog created from the subscribe original SUBSCRIBE
     *         transaction.
     *
     *
     */
    public static SIPDialog createFromNOTIFY(SIPClientTransaction subscribeTx,
                                             SIPTransaction notifyST) {
        SIPDialog d = new SIPDialog(notifyST);
        //
        // The above sets d.firstTransaction to NOTIFY (ST), correct that
        //
        d.serverTransactionFlag = false;
        // they share this one
        d.firstTransaction = d.lastTransaction = subscribeTx;
        d.terminateOnBye = false;
        d.localSequenceNumber.set(d.firstTransaction.getCSeq());
        SIPRequest not = (SIPRequest) notifyST.getRequest();
        d.remoteSequenceNumber.set(not.getCSeq().getSeqNumber());
        d.setDialogId(not.getDialogId(true));
        d.setLocalTag(not.getToTag());
        d.setRemoteTag(not.getFromTag());

        // Dont use setLocal / setRemote here, they make other assumptions
        d.localParty = not.getTo().getAddress();
        d.remoteParty = not.getFrom().getAddress();

        // initialize d's route set based on the NOTIFY. Any proxies must have
        // Record-Routed
        d.addRoute(not);
        d.setState(CONFIRMED_STATE); // set state, *after* setting route set!
        return d;
    }

    /**
     * Return true if is server.
     *
     * @return true if is server transaction created this dialog.
     */
    public boolean isServer() {
        if (log.isDebugEnabled())
            log.debug("isServer: firstTransaction = " + firstTransaction +
                    ", serverTransactionFlag = " + serverTransactionFlag +
                    ", localParty = " + localParty + ", Dialog = " + this);

        if (this.firstTransaction == null)
            return this.serverTransactionFlag;
        else
            return this.firstTransaction instanceof SIPServerTransaction;

    }

    /**
     * Return true if this is a re-establishment of the dialog.
     *
     * @return true if the reInvite flag is set.
     */
    protected boolean isReInvite() {
        return this.reInviteFlag;
    }

    /**
     * Get the id for this dialog.
     *
     * @return the string identifier for this dialog.
     *
     */
    public String getDialogId() {

        if (this.dialogId == null && this.lastResponse.get() != null)
            this.dialogId = this.lastResponse.get().getDialogId(isServer());

        return this.dialogId;
    }

    /**
     * Add a transaction record to the dialog.
     *
     * @param transaction
     *            is the transaction to add to the dialog.
     */
    public void addTransaction(SIPTransaction transaction) {

        SIPRequest sipRequest = transaction.getOriginalRequest();

        // Processing a re-invite.
        if (firstTransaction != null && firstTransaction != transaction
                && transaction.getMethod().equals(firstTransaction.getMethod())) {
            this.reInviteFlag = true;

            // MMANY: Added this reset of ackSeen so that 200 OK
            // will be resent for re-INVITEs as well if the ACK is delayed.
            if (transaction instanceof SIPServerTransaction)
                this.ackSeen.set(false);
        }

        /*
           * JvB: removed this
           *
           * Set state to Completed if we are processing a // BYE transaction for
           * the dialog. // Server transaction terminates after the response to
           * the bye. if (transaction instanceof SIPClientTransaction &&
           * sipRequest.getMethod().equals(Request.BYE) && this.terminateOnBye) {
           * this.setState(TERMINATED_STATE); }
           */

        if (firstTransaction == null) {
            // Record the local and remote sequenc
            // numbers and the from and to tags for future
            // use on this dialog.
            firstTransaction = transaction;
            if (sipRequest.getMethod().equals(Request.SUBSCRIBE))
                this.eventHeader = (EventHeader) sipRequest
                        .getHeader(EventHeader.NAME);

            this.setLocalParty(sipRequest);
            this.setRemoteParty(sipRequest);
            this.setCallId(sipRequest);
            this.originalRequest = sipRequest;
            this.method = sipRequest.getMethod();

            if (transaction instanceof SIPServerTransaction) {
                this.hisTag = sipRequest.getFrom().getTag();
                // My tag is assigned when sending response
            } else {
                setLocalSequenceNumber(sipRequest.getCSeq().getSeqNumber());
                this.originalLocalSequenceNumber.set(localSequenceNumber.get());
                this.myTag = sipRequest.getFrom().getTag();
                if (myTag == null)
                    if (log.isInfoEnabled())
                        log.info("The request's From header is missing the required Tag parameter.");
            }
        } else if (transaction.getMethod().equals(firstTransaction.getMethod())
                && !(firstTransaction.getClass().equals(transaction.getClass()))) {
            // This case occurs when you are processing a re-invite.
            // Switch from client side to server side for re-invite
            // (put the other side on hold).
            firstTransaction = transaction;
            this.setLocalParty(sipRequest);

            // MMANY: Resetting the original local sequence number to the
            // one of the re-INVITE
            this.originalLocalSequenceNumber.set(sipRequest.getCSeq().getSeqNumber());

            this.setRemoteParty(sipRequest);
            this.setCallId(sipRequest);
            this.originalRequest = sipRequest;
            this.method = sipRequest.getMethod();

        }
        if (transaction instanceof SIPServerTransaction)
            setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());

        // If this is a server transaction record the remote
        // sequence number to avoid re-processing of requests
        // with the same sequence number directed towards this
        // dialog.

        this.lastTransaction = transaction;
        // set a back ptr in the incoming dialog.
        // CHECKME -- why is this here?
        // transaction.setDialog(this,sipRequest);
        if (log.isDebugEnabled()) {
            log.debug("Transaction Added " + this + myTag
                    + "/" + hisTag +
                    " TID = "
                    + transaction.getTransactionId() + "/"
                    + transaction.IsServerTransaction());
        }
    }

    /**
     * Set the remote tag.
     *
     * @param hisTag
     *            is the remote tag to set.
     */
    private void setRemoteTag(String hisTag) {
        if (log.isDebugEnabled()) {
            log.debug("setRemoteTag(): " + this + " remoteTag = " + this.hisTag
                    + " new tag = " + hisTag);
        }
        if (this.hisTag != null && !hisTag.equals(this.hisTag)) {
            InternalErrorHandler.
                    handleException("Unexpected tag "
                            + this.hisTag + " trying to set to " + hisTag);
        }
        this.hisTag = hisTag;
    }

    /**
     * Get the last transaction from the dialog.
     */
    public SIPTransaction getLastTransaction() {
        return this.lastTransaction;
    }

    /**
     * Get the INVITE transaction (null if no invite transaction).
     */
    public SIPServerTransaction getInviteTransaction() {
        synchronized(timerTaskLock) {
            DialogTimerTask t = this.timerTask;
            if (t != null)
                return t.transaction;
            else
                return null;
        }

    }

    /**
     * Set the local sequece number for the dialog (defaults to 1 when the
     * dialog is created).
     *
     * @param lCseq
     *            is the local cseq number.
     *
     */
    private void setLocalSequenceNumber(long lCseq) {
        if (log.isDebugEnabled())
            log.debug("setLocalSequenceNumber: original 	"
                    + this.localSequenceNumber.get() + " new  = " + lCseq);
        if (lCseq <= this.localSequenceNumber.get())
            throw new RuntimeException("Sequence number should not decrease !");
        this.localSequenceNumber.set(lCseq);
    }

    /**
     * Set the remote sequence number for the dialog.
     *
     * @param rCseq
     *            is the remote cseq number.
     *
     */
    public void setRemoteSequenceNumber(long rCseq) {
        if (log.isDebugEnabled())
            log.debug("setRemoteSeqno " + this + "/" + rCseq);
        this.remoteSequenceNumber.set(rCseq);
    }

    /**
     * Increment the local CSeq # for the dialog. This is useful for if you want
     * to create a hole in the sequence number i.e. route a request outside the
     * dialog and then resume within the dialog.
     */
    public void incrementLocalSequenceNumber() {
        this.localSequenceNumber.incrementAndGet();
    }

    /**
     * Get the remote sequence number (for cseq assignment of outgoing requests
     * within this dialog).
     *
     * @deprecated
     * @return local sequence number.
     */

    public int getRemoteSequenceNumber() {
        return (int) this.remoteSequenceNumber.get();
    }

    /**
     * Get the local sequence number (for cseq assignment of outgoing requests
     * within this dialog).
     *
     * @deprecated
     * @return local sequence number.
     */

    public int getLocalSequenceNumber() {
        return (int) this.localSequenceNumber.get();
    }

    /**
     * Get the sequence number for the request that origianlly created the
     * Dialog.
     *
     * @return -- the original starting sequence number for this dialog.
     */
    public long getOriginalLocalSequenceNumber() {
        return this.originalLocalSequenceNumber.get();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getLocalSequenceNumberLong()
      */
    public long getLocalSeqNumber() {
        return this.localSequenceNumber.get();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getRemoteSequenceNumberLong()
      */
    public long getRemoteSeqNumber() {
        return this.remoteSequenceNumber.get();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getLocalTag()
      */
    public String getLocalTag() {
        return this.myTag;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getRemoteTag()
      */
    public String getRemoteTag() {

        return hisTag;
    }

    /**
     * Set local tag for the transaction.
     *
     * @param mytag
     *            is the tag to use in From headers client transactions that
     *            belong to this dialog and for generating To tags for Server
     *            transaction requests that belong to this dialog.
     */
    private void setLocalTag(String mytag) {
        if (log.isDebugEnabled()) {
            log.debug("set Local tag " + mytag + " "
                    + this.dialogId);
        }

        this.myTag = mytag;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#delete()
      */

    public void delete() {
        // the reaper will get him later.
        this.setState(TERMINATED_STATE);
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getCallId()
      */
    public CallIdHeader getCallId() {
        return this.callIdHeader;
    }

    /**
     * set the call id header for this dialog.
     */
    private void setCallId(SIPRequest sipRequest) {
        this.callIdHeader = sipRequest.getCallId();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getLocalParty()
      */

    public javax.sip.address.Address getLocalParty() {
        return this.localParty;
    }

    private void setLocalParty(SIPMessage sipMessage) {
        if (!isServer()) {
            this.localParty = sipMessage.getFrom().getAddress();
        } else {
            this.localParty = sipMessage.getTo().getAddress();
        }
    }

    /**
     * Returns the Address identifying the remote party. This is the value of
     * the To header of locally initiated requests in this dialogue when acting
     * as an User Agent Client.
     * <p>
     * This is the value of the From header of recieved responses in this
     * dialogue when acting as an User Agent Server.
     *
     * @return the address object of the remote party.
     */
    public javax.sip.address.Address getRemoteParty() {

        if (log.isDebugEnabled()) {
            log.debug("gettingRemoteParty " + this.remoteParty);
        }
        return this.remoteParty;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getRemoteTarget()
      */
    public javax.sip.address.Address getRemoteTarget() {

        return this.remoteTarget;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#getState()
      */
    public DialogState getState() {
        if (this.dialogState == -1)
            return null; // not yet initialized
        return DialogState.getObject(this.dialogState);
    }

    /**
     * Returns true if this Dialog is secure i.e. if the request arrived over
     * TLS, and the Request-URI contained a SIPS URI, the "secure" flag is set
     * to TRUE.
     *
     * @return <code>true</code> if this dialogue was established using a sips
     *         URI over TLS, and <code>false</code> otherwise.
     */
    public boolean isSecure() {
        return this.getFirstTransaction().getRequest().getRequestURI()
                .getScheme().equalsIgnoreCase("sips");
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#sendAck(javax.sip.message.Request)
      */
    public void sendAck(Request request) throws SipException {
        this.sendAck(request, true);
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#createRequest(java.lang.String)
      */
    public Request createRequest(String method) throws SipException {

        if (this.lastResponse.get() != null)
            return this.createRequest(method, this.lastResponse.get());
        else
            throw new SipException("Dialog not yet established -- no response!");
    }

    /**
     * The method that actually does the work of creating a request.
     *
     * @param method
     * @param sipResponse
     * @return a request
     * @throws SipException
     */
    private Request createRequest(String method, SIPResponse sipResponse)
            throws SipException {
        // Check if the dialog is in the right state (RFC 3261 section 15).
        // The caller's UA MAY send a BYE for either
        // CONFIRMED or EARLY dialogs, and the callee's UA MAY send a BYE on
        // CONFIRMED dialogs, but MUST NOT send a BYE on EARLY dialogs.

        // Throw out cancel request.

        if (method == null || sipResponse == null)
            throw new NullPointerException("null argument");

        if (method.equals(Request.CANCEL))
            throw new SipException("Dialog.createRequest(): Invalid request");

        if (this.getState() == null
                || (this.getState().getValue() == TERMINATED_STATE && !method
                .equalsIgnoreCase(Request.BYE))
                || (this.isServer()
                && this.getState().getValue() == EARLY_STATE && method
                .equalsIgnoreCase(Request.BYE)))
            throw new SipException("Dialog  " + getDialogId()
                    + " not yet established or terminated " + this.getState());

        SipUri sipUri;
        if (this.getRemoteTarget() != null)
            sipUri = (SipUri) this.getRemoteTarget().getURI().clone();
        else {
            sipUri = (SipUri) this.getRemoteParty().getURI().clone();
            sipUri.clearUriParms();
        }

        CSeq cseq = new CSeq();
        try {
            cseq.setMethod(method);
            cseq.setSeqNumber(this.getLocalSeqNumber());
        } catch (Exception ex) {
            log.error("Unexpected error",ex);
            InternalErrorHandler.handleException(ex);
        }
        // Add a via header for the outbound request based on the
        // transport of the message processor.

        ListeningPointImpl lp = (ListeningPointImpl) this.sipProvider
                .getListeningPoint(sipResponse.getTopmostVia().getTransport());
        if (lp == null) {
            log.warn("Cannot find listening point for transport "
                    + sipResponse.getTopmostVia().getTransport());
            throw new SipException("Cannot find listening point for transport "
                    + sipResponse.getTopmostVia().getTransport());
        }
        Via via = lp.getViaHeader();

        From from = (From) this.lastResponse.get().getFrom().clone();
        from.setAddress(this.localParty);
        To to = (To) this.lastResponse.get().getTo().clone();
        to.setAddress(this.remoteParty);
        SIPRequest sipRequest = sipResponse.createRequest(sipUri, via, cseq,
                from, to);

        // The default contact header is obtained from the provider. The
        // application
        // can override this.

        // JvB: Should only do this for target refresh requests,
        // ie not for BYE, PRACK, etc
        if (SIPRequest.isTargetRefresh(method)) {
            ContactHeader contactHeader = this.sipProvider
                    .createContactForProvider(lp.getTransport());
            sipRequest.setHeader(contactHeader);
        }

        try {
            // Guess of local sequence number - this is being re-set when
            // the request is actually dispatched
            if (!method.equals(Request.ACK)) {
                cseq.setSeqNumber(this.localSequenceNumber.get() + 1);
            } else {
                // This is an ACK request. Get the cseq number
                // from the last original local sequence number.

                long seqno = this.originalLocalSequenceNumber.get();
                cseq.setSeqNumber(seqno);
            }

        } catch (InvalidArgumentException ex) {
            InternalErrorHandler.handleException(ex);
        }

        if (method.equals(Request.SUBSCRIBE)) {

            if (eventHeader != null)
                sipRequest.addHeader(eventHeader);

        }

        /*
           * RFC3261, section 12.2.1.1:
           *
           * The URI in the To field of the request MUST be set to the remote URI
           * from the dialog state. The tag in the To header field of the request
           * MUST be set to the remote tag of the dialog ID. The From URI of the
           * request MUST be set to the local URI from the dialog state. The tag
           * in the From header field of the request MUST be set to the local tag
           * of the dialog ID. If the value of the remote or local tags is null,
           * the tag parameter MUST be omitted from the To or From header fields,
           * respectively.
           */

        try {
            if (this.getLocalTag() != null) {
                from.setTag(this.getLocalTag());
            } else {
                from.removeTag();
            }
            if (this.getRemoteTag() != null) {
                to.setTag(this.getRemoteTag());
            } else {
                to.removeTag();
            }
        } catch (ParseException ex) {
            InternalErrorHandler.handleException(ex);
        }

        // get the route list from the dialog.
        this.updateRequest(sipRequest);

        return sipRequest;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#sendRequest(javax.sip.ClientTransaction)
      */
    public void sendRequest(ClientTransaction clientTransactionId)
            throws TransactionDoesNotExistException, SipException {

        if (clientTransactionId == null)
            throw new NullPointerException("null parameter");

        SIPRequest dialogRequest = ((SIPClientTransaction) clientTransactionId)
                .getOriginalRequest();

        if (log.isDebugEnabled())
            log.debug("dialog.sendRequest " + " dialog = "
                    + this + "\ndialogRequest = \n" + dialogRequest);

        if (dialogRequest.getMethod().equals(Request.ACK)
                || dialogRequest.getMethod().equals(Request.CANCEL))
            throw new SipException("Bad Request Method. "
                    + dialogRequest.getMethod());
        // Cannot send bye until the dialog has been established.
        if (this.getState() == null
                && !(this.getMethod().equals(Request.SUBSCRIBE) && dialogRequest
                .getMethod().equals(Request.NOTIFY))) {
            log.warn("null dialog state for " + this);
            throw new SipException("Bad dialog state " + this.getState());
        }

        // JvB: added, allow re-sending of BYE after challenge
        if (byeSent && isTerminatedOnBye()
                && !dialogRequest.getMethod().equals(Request.BYE)) {
            log.warn("BYE already sent for " + this);
            throw new SipException("Cannot send request; BYE already sent");
        }

        if (dialogRequest.getTopmostVia() == null) {
            Via via = ((SIPClientTransaction) clientTransactionId)
                    .getOutgoingViaHeader();
            dialogRequest.addHeader(via);
        }
        if (!this.getCallId().getCallId().equalsIgnoreCase(
                dialogRequest.getCallId().getCallId())) {

            if (log.isInfoEnabled())
                log.info("Bad CallId. CallID " + this.getCallId() +
                        " RequestCallID = " +
                        dialogRequest.getCallId().getCallId() +
                        " Dialog =  " + this);

            throw new SipException("Bad call ID in request");
        }

        // Set the dialog back pointer.
        ((SIPClientTransaction) clientTransactionId).setDialog(this,
                this.dialogId);

        this.addTransaction((SIPTransaction) clientTransactionId);
        // Enable the retransmission filter for the transaction

        ((SIPClientTransaction) clientTransactionId).isMapped = true;

        From from = (From) dialogRequest.getFrom();
        To to = (To) dialogRequest.getTo();

        // Caller already did the tag assignment -- check to see if the
        // tag assignment is OK.
        if (this.getLocalTag() != null && from.getTag() != null
                && !from.getTag().equals(this.getLocalTag()))
            throw new SipException("From tag mismatch expecting	 "
                    + this.getLocalTag());

        if (this.getRemoteTag() != null && to.getTag() != null
                && !to.getTag().equals(this.getRemoteTag()))
            throw new SipException("To header tag mismatch expecting "
                    + this.getRemoteTag());

        /*
           * The application is sending a NOTIFY before sending the response of
           * the dialog.
           */
        if (this.getLocalTag() == null
                && dialogRequest.getMethod().equals(Request.NOTIFY)) {
            if (!this.getMethod().equals(Request.SUBSCRIBE))
                throw new SipException(
                        "Trying to send NOTIFY without SUBSCRIBE Dialog!");
            this.setLocalTag(from.getTag());

        }

        try {
            if (this.getLocalTag() != null)
                from.setTag(this.getLocalTag());
            if (this.getRemoteTag() != null)
                to.setTag(this.getRemoteTag());

        } catch (ParseException ex) {

            InternalErrorHandler.handleException(ex);

        }

        try {
            // Increment before setting!!
            localSequenceNumber.incrementAndGet();
            dialogRequest.getCSeq().setSeqNumber(getLocalSeqNumber());
        } catch (InvalidArgumentException ex) {
            log.warn(ex);
        }

        try {
            ((SIPClientTransaction) clientTransactionId)
                    .sendMessage(dialogRequest);
            // If the method is BYE then mark the dialog completed.
            // The completed state is just a conveniance. It is not
            // part of the RFC.
            // Note that if the BYE is rejected then the
            // Dialog should bo back to the ESTABLISHED state.
            if (dialogRequest.getMethod().equals(Request.BYE)) {
                // this.setState(TERMINATED_STATE);
                this.byeSent = true;
            }
        } catch (IOException ex) {
            throw new SipException("error sending message");
        }

    }

    /**
     * Return yes if the last response is to be retransmitted.
     */
    private boolean toRetransmitFinalResponse(int T2) {
        if (retransmissionTicksLeft.decrementAndGet() == 0) {
            if ( 2* prevRetransmissionTicks.get() <= T2 )
                retransmissionTicksLeft.set(2 * prevRetransmissionTicks.get());
            else
                retransmissionTicksLeft.set(prevRetransmissionTicks.get());
            prevRetransmissionTicks.set(retransmissionTicksLeft.get());
            return true;
        } else
            return false;

    }

    protected void setRetransmissionTicks() {
        retransmissionTicksLeft.set(1);
        prevRetransmissionTicks.set(1);
    }

    /**
     * Resend the last ack.
     */
    public void resendAck(long cseqNumber) throws SipException {
        // Check for null.
        // Bug report and fix by Antonis Karydas.
        SIPRequest ack = this.lastSentAck.get();

        if (ack != null) {
            if (ack.getHeader(TimeStampHeader.NAME) != null
                    && sipStack.generateTimeStampHeader) {
                TimeStamp ts = new TimeStamp();
                try {
                    ts.setTimeStamp(System.currentTimeMillis());
                    ack.setHeader(ts);
                } catch (InvalidArgumentException e) {
                    if (log.isDebugEnabled()) log.debug(e.getMessage(), e);
                }
            }

            // Send the ACK and use the CSeq value from the received
            // 2xx response.
            try {
                CSeq cseq = (CSeq) ack.getCSeq();
                cseq.setSeqNumber(cseqNumber);
            } catch (Exception ex) {
                log.error("Unexpected error",ex);
                InternalErrorHandler.handleException(ex);
            }

            this.sendAck(ack, false);
        }

    }

    /**
     * Get the method of the request/response that resulted in the creation of
     * the Dialog.
     *
     * @return -- the method of the dialog.
     */
    public String getMethod() {
        // Method of the request or response used to create this dialog
        return this.method;
    }

    /**
     * Start the dialog timer.
     *
     * @param transaction
     */

    protected void startTimer(SIPServerTransaction transaction) {
        synchronized(timerTaskLock) {
            if (this.timerTask != null && timerTask.transaction == transaction) {
                if (log.isDebugEnabled())
                    log.debug(this + ": Timer already running for " + getDialogId());
                return;
            }

            if (log.isDebugEnabled())
                log.debug("Starting dialog timer for " + getDialogId());

            this.ackSeen.set(false);

            if (this.timerTask != null) {
                this.timerTask.transaction = transaction;
            } else {
                this.timerTask = new DialogTimerTask(transaction);
                sipStack.timer.schedule(timerTask,
                        sipStack.getBaseTimerInterval(),
                        sipStack.getBaseTimerInterval());

            }
            this.setRetransmissionTicks();
        }
    }

    /**
     * Start the dialog delete timer.
     * First cancels a previously started delete timer.
     */

    protected void startDeleteTimer() {
        synchronized(deletetimerTaskLock) {
            if (this.deletetimerTask != null) {
                this.deletetimerTask.cancel();
            }

            if (log.isDebugEnabled())
                log.debug("Starting delete dialog timer for " + getDialogId());

            // Delete the transaction after the max ack timeout.
            this.deletetimerTask = new DialogDeleteTask();

            sipStack.timer.schedule(deletetimerTask,
                    sipStack.getTimerH() * SIPTransactionStack.BASE_TIMER_INTERVAL);
        }

    }

    /**
     * Stop the dialog timer. This is called when the dialog is terminated.
     *
     */
    protected void stopTimer() {
        synchronized(timerTaskLock) {
            try {
                if (this.timerTask != null) {
                    this.timerTask.cancel();
                    this.timerTask = null;
                }
            } catch (Exception ex) {
                if (log.isDebugEnabled())
                    log.debug(this + ": Exception occured.", ex);
            }
        }
    }

    /*
      * (non-Javadoc) Retransmissions of the reliable provisional response cease
      * when a matching PRACK is received by the UA core. PRACK is like any other
      * request within a dialog, and the UAS core processes it according to the
      * procedures of Sections 8.2 and 12.2.2 of RFC 3261. A matching PRACK is
      * defined as one within the same dialog as the response, and whose method,
      * CSeq-num, and response-num in the RAck header field match, respectively,
      * the method from the CSeq, the sequence number from the CSeq, and the
      * sequence number from the RSeq of the reliable provisional response.
      *
      * @see javax.sip.Dialog#createPrack(javax.sip.message.Response)
      */
    public Request createPrack(Response relResponse)
            throws DialogDoesNotExistException, SipException {

        if (this.getState() == null
                || this.getState().equals(DialogState.TERMINATED))
            throw new DialogDoesNotExistException(
                    "Dialog not initialized or terminated");

        try {
            SIPResponse sipResponse = (SIPResponse) relResponse;
            SIPRequest sipRequest = (SIPRequest) this.createRequest(
                    Request.PRACK, (SIPResponse) relResponse);
            String toHeaderTag = sipResponse.getTo().getTag();
            sipRequest.setToTag(toHeaderTag);
            RAck rack = new RAck();
            RSeq rseq = (RSeq) relResponse.getHeader(RSeqHeader.NAME);
            rack.setMethod(sipResponse.getCSeq().getMethod());
            rack.setCSequenceNumber((int) sipResponse.getCSeq().getSeqNumber());
            rack.setRSequenceNumber(rseq.getSeqNumber());
            sipRequest.setHeader(rack);
            return sipRequest;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }

    }

    private void updateRequest(SIPRequest sipRequest) {
        RouteList rl = this.getRouteList();
        if (rl.size() > 0) {
            sipRequest.setHeader(rl);
        } else {
            sipRequest.removeHeader(RouteHeader.NAME);
        }
    }

    /*
      * (non-Javadoc) The UAC core MUST generate an ACK request for each 2xx
      * received from the transaction layer. The header fields of the ACK are
      * constructed in the same way as for any request sent within a dialog (see
      * Section 12) with the exception of the CSeq and the header fields related
      * to authentication. The sequence number of the CSeq header field MUST be
      * the same as the INVITE being acknowledged, but the CSeq method MUST be
      * ACK. The ACK MUST contain the same credentials as the INVITE. If the 2xx
      * contains an offer (based on the rules above), the ACK MUST carry an
      * answer in its body. If the offer in the 2xx response is not acceptable,
      * the UAC core MUST generate a valid answer in the ACK and then send a BYE
      * immediately.
      *
      * Note that for the case of forked requests, you can create multiple
      * outgoing invites each with a different cseq and hence you need to supply
      * the invite.
      *
      * @see javax.sip.Dialog#createAck(long)
      */
    public Request createAck(long cseqno) throws InvalidArgumentException,
            SipException {
        if (!method.equals(Request.INVITE))
            throw new SipException("Dialog was not created with an INVITE");
        if (cseqno <= 0)
            throw new InvalidArgumentException("bad cseq <= 0 ");
        else if (cseqno > (((long) 1) << 32 - 1))
            throw new InvalidArgumentException("bad cseq > "
                    + (((long) 1) << 32 - 1));

        if (this.lastResponse.get() == null)
            throw new SipException("Dialog not yet established -- no response!");
        try {
            String transport = this.lastResponse.get().getTopmostVia().getTransport();
            ListeningPointImpl lp = (ListeningPointImpl) sipProvider
                    .getListeningPoint(transport);
            Via via = lp.getViaHeader();

            SipUri requestURI;
            if (this.getRemoteTarget() == null) {
                requestURI = (SipUri) getRemoteParty().getURI().clone();
                requestURI.clearUriParms();
            } else {
                requestURI = (SipUri) getRemoteTarget().getURI().clone();
            }
            if (!transport.equalsIgnoreCase("udp"))
                requestURI.setTransportParam(transport);
            CSeq cseq = new CSeq(cseqno, Request.ACK);
            SIPRequest sipRequest = this.lastResponse.get().createRequest(
                    requestURI, via, cseq);

            From from = (From) sipRequest.getFrom();
            from.setAddress(this.localParty);
            To to = (To) sipRequest.getTo();
            to.setAddress(remoteParty);

            /*
                * Assign the tags to the request as present in the dialog.
                */
            try {
                if (this.getLocalTag() != null)
                    from.setTag(this.getLocalTag());
                if (this.getRemoteTag() != null)
                    to.setTag(this.getRemoteTag());
            } catch (ParseException ex) {
                InternalErrorHandler.handleException(ex);
            }

            // ACKs for 2xx responses
            // use the Route values learned from the Record-Route of the 2xx
            // responses.
            this.updateRequest(sipRequest);

            return sipRequest;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            throw new SipException("unexpected exception ", ex);
        }

    }

    /**
     * Get the provider for this transaction.
     *
     * @return -- the SIP Provider associated with this transaction.
     */
    public SipProviderImpl getSipProvider() {
        return this.sipProvider;
    }

    /**
     * Check the tags of the response against the tags of the Dialog. Return
     * true if the respnse matches the tags of the dialog. We do this check wehn
     * sending out a response.
     *
     * @param sipResponse --
     *            the response to check.
     *
     */
    public boolean checkResponseTags(SIPResponse sipResponse) {
        if (this.isServer()) {

            if (sipResponse.getToTag() != null
                    && this.getLocalTag() != null
                    && !this.getLocalTag().equals(sipResponse.getToTag())
                    || (sipResponse.getFromTag() != null
                    && this.getRemoteTag() != null && !this
                    .getRemoteTag().equals(sipResponse.getFromTag()))) {
                if (log.isInfoEnabled()) {
                    log.info("sipResponse.getToTag()=" + sipResponse.getToTag() +
                            " this.localTag()=" + this.getLocalTag() +
                            " sipResponse.getFromTag() = " + sipResponse.getFromTag() +
                            " this.remoteTag = " + this.getRemoteTag());
                }
                return false;
            } else
                return true;
        } else
            return true;
    }

    /**
     * Set the last response for this dialog. This method is called for updating
     * the dialog state when a response is either sent or received from within a
     * Dialog.
     *
     * @param transaction --
     *            the transaction associated with the response
     * @param sipResponse --
     *            the last response to set.
     */
    public void setLastResponse(SIPTransaction transaction,
                                SIPResponse sipResponse) {

        int statusCode = sipResponse.getStatusCode();
        if (statusCode == 100) {
            if (log.isInfoEnabled())
                log.info("Invalid status code - 100 in setLastResponse - ignoring");
            return;
        }

        this.lastResponse.set(sipResponse);
        this.setAssigned();
        // Adjust state of the Dialog state machine.
        if (log.isDebugEnabled()) {
            log.debug("sipDialog: setLastResponse:" + this + " lastResponse = "
                    + this.lastResponse.get().getFirstLine());
        }
        if (this.getState() == DialogState.TERMINATED) {
            if (log.isDebugEnabled()) {
                log.debug("sipDialog: setLastResponse -- dialog is terminated - ignoring ");
            }
            return;
        }
        String cseqMethod = sipResponse.getCSeq().getMethod();
        if (log.isDebugEnabled()) {
            log.debug("cseqMethod = " + cseqMethod +
                    ", dialogState = " + this.getState() +
                    ", method = " + this.getMethod() +
                    ", statusCode = " + statusCode +
                    ", transaction = " + transaction);
        }

        // JvB: don't use "!this.isServer" here
        // note that the transaction can be null for forked
        // responses.
        if (transaction == null || transaction instanceof ClientTransaction) {

            if (sipStack.isDialogCreated(cseqMethod)) {
                // Make a final tag assignment.
                if (getState() == null && statusCode / 100 == 1
                        && getState() != DialogState.CONFIRMED) {
                    // Guard aginst slipping back into early state from
                    // confirmed
                    // state
                    setState(SIPDialog.EARLY_STATE);
                    if (sipResponse.getToTag() != null
                            && this.getRemoteTag() == null) {
                        setRemoteTag(sipResponse.getToTag());
                        this.setDialogId(sipResponse.getDialogId(false));
                        sipStack.putDialog(this);
                        this.addRoute(sipResponse);
                    }
                } else if (statusCode / 100 == 2) {
                    // This is a dialog creating method (such as INVITE).
                    // 2xx response -- set the state to the confirmed
                    // state. To tag is MANDATORY for the response.

                    // Only do this if method equals initial request!


                    if (cseqMethod.equals(getMethod())
                            && sipResponse.getToTag() != null
                            && this.getState() != DialogState.CONFIRMED) {
                        setRemoteTag(sipResponse.getToTag());
                        this.setDialogId(sipResponse.getDialogId(false));
                        sipStack.putDialog(this);
                        this.addRoute(sipResponse);

                        setState(SIPDialog.CONFIRMED_STATE);
                    } else if (SIPRequest.isTargetRefresh(cseqMethod)) {
                        doTargetRefresh(sipResponse);
                    }

                } else if (statusCode >= 300
                        && statusCode <= 699
                        && !isReInvite()
                        && (getState() == null || (cseqMethod
                        .equals(getMethod()) && getState().getValue() == SIPDialog.EARLY_STATE))) {
                    // This case handles 3xx, 4xx, 5xx and 6xx responses.
                    // RFC 3261 Section 12.3 - dialog termination.
                    // Independent of the method, if a request outside of a
                    // dialog generates
                    // a non-2xx final response, any early dialogs created
                    // through
                    // provisional responses to that request are terminated.
                    // Note: this does not apply to re-invite
                    setState(SIPDialog.TERMINATED_STATE);
                }

                // This code is in support of "proxy" servers
                // that are constructed as back to back user agents.
                // This could be a dialog in the middle of the call setup
                // path somewhere. Hence the incoming invite has
                // record route headers in it. The response will
                // have additional record route headers. However,
                // for this dialog only the downstream record route
                // headers matter. Ideally proxy servers should
                // not be constructed as Back to Back User Agents.
                // Remove all the record routes that are present in
                // the incoming INVITE so you only have the downstream
                // Route headers present in the dialog. Note that
                // for an endpoint - you will have no record route
                // headers present in the original request so
                // the loop will not execute.
                if (originalRequest != null) {
                    RecordRouteList rrList = originalRequest
                            .getRecordRouteHeaders();
                    if (rrList != null) {
                        ListIterator it = rrList.listIterator(rrList.size());
                        while (it.hasPrevious()) {
                            RecordRoute rr = (RecordRoute) it.previous();
                            Route route = (Route) routeList.getFirst();
                            if (route != null
                                    && rr.getAddress().equals(
                                    route.getAddress())) {
                                routeList.removeFirst();
                            } else
                                break;
                        }
                    }
                }

            } else if (cseqMethod.equals(Request.NOTIFY)
                    && (this.getMethod().equals(Request.SUBSCRIBE) || this
                    .getMethod().equals(Request.REFER))
                    && sipResponse.getStatusCode() / 100 == 2
                    && this.getState() == null) {
                // This is a notify response.
                this.setDialogId(sipResponse.getDialogId(true));
                sipStack.putDialog(this);
                this.setState(SIPDialog.CONFIRMED_STATE);

            } else if (cseqMethod.equals(Request.BYE) && statusCode / 100 == 2
                    && isTerminatedOnBye()) {
                // Dialog will be terminated when the transction is terminated.
                setState(SIPDialog.TERMINATED_STATE);
            }
        } else {
            // Processing Server Dialog.

            if (cseqMethod.equals(Request.CANCEL)
                    && statusCode / 100 == 2
                    && (!this.isReInvite())
                    && (getState() == null || getState().getValue() == SIPDialog.EARLY_STATE)) {
                // Transaction successfully cancelled but dialog has not yet
                // been established so delete the dialog.
                // Note: this does not apply to re-invite
                this.setState(SIPDialog.TERMINATED_STATE);
            } else if (cseqMethod.equals(Request.BYE) && statusCode / 100 == 2
                    && this.isTerminatedOnBye()) {
                // Only transition to terminated state when
                // 200 OK is returned for the BYE. Other
                // status codes just result in leaving the
                // state in COMPLETED state.
                this.setState(SIPDialog.TERMINATED_STATE);
            } else {
            	// TR 31843 SIP Stack in MAS sometimes leak dialogs
            	// This problem seem to be fixed in CVS rev 1.76 
            	// of SIPDialog.java on jain-sip.dev.java.net. 
            	// That fix was applied in this else-statement. 
            	// The rest of the SIPDialog code is left unchanged.
            	
            	boolean doPutDialog = false;

            	if (getLocalTag() == null
            			&& sipResponse.getTo().getTag() != null
            			&& sipStack.isDialogCreated(cseqMethod)
            			&& cseqMethod.equals(getMethod())) {

            		setLocalTag(sipResponse.getTo().getTag());
            		doPutDialog = true;
            	}
            	if (statusCode / 100 != 2) {

            		// Check if we want to put the dialog in the dialog table.
            		// A dialog is put into the dialog table when the server
            		// transaction is responded to by a provisional response
            		// or a final response. The Dialog is terminated
            		// if the response is an error response.

            		if (statusCode / 100 == 1) {
            			if (doPutDialog) {
            				setState(SIPDialog.EARLY_STATE);
            				this.setDialogId(sipResponse.getDialogId(true));
            				sipStack.putDialog(this);
            			}
            		} else {
            			/*
            			 * RFC 3265 chapter 3.1.4.1 "Non-200 class final
            			 * responses indicate that no subscription or dialog has
            			 * been created, and no subsequent NOTIFY message will
            			 * be sent. All non-200 class" + responses (with the
            			 * exception of "489", described herein) have the same
            			 * meanings and handling as described in SIP"
            			 */
            			if (statusCode != 489
            					&& (cseqMethod.equals(Request.NOTIFY) 
            					|| cseqMethod.equals(Request.SUBSCRIBE))) {
            				
            				if (log.isDebugEnabled())
            					log.debug("RFC 3265 : Not setting dialog to TERMINATED for 489");

            			} else if ((cseqMethod.equals(Request.INVITE) && !isReInvite()) || 
            					cseqMethod.equals(Request.NOTIFY) || cseqMethod.equals(Request.SUBSCRIBE)) {
                            // EMALNYF: Only if the response is sent for an initial INVITE (i.e. not re-INVITE) 
            				// or a NOTIFY or SUBSCRIBE shall the dialog be terminated.
            				this.setState(SIPDialog.TERMINATED_STATE);
            			}
            		}
            	} else {

            		/*
            		 * JvB: RFC4235 says that when sending 2xx on UAS side,
            		 * state should move to CONFIRMED
            		 */
            		if (this.dialogState <= SIPDialog.EARLY_STATE
            				&& (cseqMethod.equals(Request.INVITE)
            						|| cseqMethod.equals(Request.SUBSCRIBE) 
            						|| cseqMethod.equals(Request.REFER))) {
            			this.setState(SIPDialog.CONFIRMED_STATE);
            		}

            		if (doPutDialog) {
            			this.setDialogId(sipResponse.getDialogId(true));
            			sipStack.putDialog(this);
            		}

            	}
            }

            // In any state: start 2xx retransmission timer for INVITE
            if ((statusCode / 100 == 2) && cseqMethod.equals(Request.INVITE)) {
                SIPServerTransaction sipServerTx = (SIPServerTransaction) transaction;
                this.startTimer(sipServerTx);
            }

        }

    }

    /**
     * @return -- the last response associated with the dialog.
     */
    public SIPResponse getLastResponse() {
        return lastResponse.get();
    }

    /**
     * Do taget refresh dialog state updates.
     *
     * RFC 3261: Requests within a dialog MAY contain Record-Route and Contact
     * header fields. However, these requests do not cause the dialog's route
     * set to be modified, although they may modify the remote target URI.
     * Specifically, requests that are not target refresh requests do not modify
     * the dialog's remote target URI, and requests that are target refresh
     * requests do. For dialogs that have been established with an
     *
     * INVITE, the only target refresh request defined is re-INVITE (see Section
     * 14). Other extensions may define different target refresh requests for
     * dialogs established in other ways.
     */
    private void doTargetRefresh(SIPMessage sipMessage) {

        ContactList contactList = sipMessage.getContactHeaders();

        // INVITE is the target refresh for INVITE
        // dialogs. SUBSCRIBE is the target refresh for
        // subscribe
        // dialogs from the client side.
        // This modifies the remote target URI
        // potentially
        if (contactList != null) {
            Contact contact = (Contact) contactList.getFirst();
            this.remoteTarget = contact.getAddress();
        }

    }

    private static boolean optionPresent(ListIterator l, String option) {
        while (l.hasNext()) {
            OptionTag opt = (OptionTag) l.next();
            if (opt != null && option.equalsIgnoreCase(opt.getOptionTag()))
                return true;
        }
        return false;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#createReliableProvisionalResponse(int)
      */
    public Response createReliableProvisionalResponse(int statusCode)
            throws InvalidArgumentException, SipException {
        Transaction transaction = this.getFirstTransaction();

        if (!(transaction instanceof SIPServerTransaction)) {
            throw new SipException("Not a Server Dialog!");

        }

        /*
           * A UAS MUST NOT attempt to send a 100 (Trying) response reliably. Only
           * provisional responses numbered 101 to 199 may be sent reliably. If
           * the request did not include either a Supported or Require header
           * field indicating this feature, the UAS MUST NOT send the provisional
           * response reliably.
           */
        if (statusCode <= 100 || statusCode > 199)
            throw new InvalidArgumentException("Bad status code ");
        SIPRequest request = this.originalRequest;
        if (!request.getMethod().equals(Request.INVITE))
            throw new SipException("Bad method");

        ListIterator list = request.getHeaders(SupportedHeader.NAME);
        if (list == null || !optionPresent(list, "100rel")) {
            list = request.getHeaders(RequireHeader.NAME);
            if (list == null || !optionPresent(list, "100rel")) {
                throw new SipException(
                        "No Supported/Require 100rel header in the request");
            }
        }

        SIPResponse response = request.createResponse(statusCode);
        /*
           * The provisional response to be sent reliably is constructed by the
           * UAS core according to the procedures of Section 8.2.6 of RFC 3261. In
           * addition, it MUST contain a Require header field containing the
           * option tag 100rel, and MUST include an RSeq header field. The value
           * of the header field for the first reliable provisional response in a
           * transaction MUST be between 1 and 2**31 - 1. It is RECOMMENDED that
           * it be chosen uniformly in this range. The RSeq numbering space is
           * within a single transaction. This means that provisional responses
           * for different requests MAY use the same values for the RSeq number.
           */
        Require require = new Require();
        try {
            require.setOptionTag("100rel");
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
        response.addHeader(require);
        RSeq rseq = new RSeq();
        rseq.setSeqNumber(1L); // Note this will change when the response
        // is sent out.

        return response;
    }

    /**
     * Do the processing necessary for the PRACK
     *
     * @param prackRequest
     * @return true if this is the first time the tx has seen the prack ( and
     *         hence needs to be passed up to the TU)
     */
    public boolean handlePrack(SIPRequest prackRequest) {
        /*
           * The RAck header is sent in a PRACK request to support reliability of
           * provisional responses. It contains two numbers and a method tag. The
           * first number is the value from the RSeq header in the provisional
           * response that is being acknowledged. The next number, and the method,
           * are copied from the CSeq in the response that is being acknowledged.
           * The method name in the RAck header is case sensitive.
           */
        if (!this.isServer()) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- not a server Dialog");
            return false;
        }
        SIPServerTransaction sipServerTransaction = (SIPServerTransaction) this
                .getFirstTransaction();
        SIPResponse sipResponse = sipServerTransaction
                .getReliableProvisionalResponse();

        if (sipResponse == null) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- ReliableResponse not found");
            return false;
        }

        RAck rack = (RAck) prackRequest.getHeader(RAckHeader.NAME);

        if (rack == null) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- rack header not found");
            return false;
        }
        CSeq cseq = (CSeq) sipResponse.getCSeq();

        if (!rack.getMethod().equals(cseq.getMethod())) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- CSeq Header does not match PRACK");
            return false;
        }

        if (rack.getCSeqNumberLong() != cseq.getSeqNumber()) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- CSeq Header does not match PRACK");
            return false;
        }

        RSeq rseq = (RSeq) sipResponse.getHeader(RSeqHeader.NAME);

        if (rack.getRSequenceNumber() != rseq.getSeqNumber()) {
            if (log.isDebugEnabled())
                log.debug("Dropping Prack -- RSeq Header does not match PRACK");
            return false;
        }

        return sipServerTransaction.prackRecieved();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#sendReliableProvisionalResponse(javax.sip.message.Response)
      */
    public void sendReliableProvisionalResponse(Response relResponse)
            throws SipException {
        if (!this.isServer()) {
            throw new SipException("Not a Server Dialog");
        }

        SIPResponse sipResponse = (SIPResponse) relResponse;

        if (relResponse.getStatusCode() == 100)
            throw new SipException(
                    "Cannot send 100 as a reliable provisional response");

        if (relResponse.getStatusCode() / 100 > 2)
            throw new SipException(
                    "Response code is not a 1xx response - should be in the range 101 to 199 ");

        /*
           * Do a little checking on the outgoing response.
           */
        if (sipResponse.getToTag() == null) {
            throw new SipException(
                    "Badly formatted response -- To tag mandatory for Reliable Provisional Response");
        }
        ListIterator requireList = relResponse.getHeaders(RequireHeader.NAME);
        boolean found = false;

        if (requireList != null) {

            while (requireList.hasNext() && !found) {
                RequireHeader rh = (RequireHeader) requireList.next();
                if (rh.getOptionTag().equalsIgnoreCase("100rel")) {
                    found = true;
                }
            }
        }

        if (!found) {
            Require require = new Require("100rel");
            relResponse.addHeader(require);
            if (log.isDebugEnabled())
                log.debug("Require header with optionTag 100rel is needed -- adding one");
        }

        SIPServerTransaction serverTransaction = (SIPServerTransaction) this
                .getFirstTransaction();

        this.setLastResponse(serverTransaction, sipResponse);

        this.setDialogId(sipResponse.getDialogId(true));

        serverTransaction.sendReliableProvisionalResponse(relResponse);
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Dialog#terminateOnBye(boolean)
      */
    public void terminateOnBye(boolean terminateFlag) throws SipException {
        this.terminateOnBye = terminateFlag;
    }

    /**
     * Set the "assigned" flag to true. We do this when inserting the dialog
     * into the dialog table of the stack.
     *
     */
    public void setAssigned() {
        this.isAssigned = true;
    }

    /**
     * Return true if the dialog has already been mapped to a transaction.
     *
     */

    public boolean isAssigned() {
        return this.isAssigned;
    }

    /**
     * Get the contact header that the owner of this dialog assigned. Subsequent
     * Requests are considered to belong to the dialog if the dialog identifier
     * matches and the contact header matches the ip address and port on which
     * the request is received.
     *
     * @return contact header belonging to the dialog.
     */
    public Contact getMyContactHeader() {
        if (this.isServer()) {
            SIPServerTransaction st = (SIPServerTransaction) this
                    .getFirstTransaction();
            SIPResponse response = st.getLastResponse();
            return response != null ? response.getContactHeader() : null;
        } else {
            SIPClientTransaction ct = (SIPClientTransaction) this
                    .getFirstTransaction();
            SIPRequest sipRequest = ct.getOriginalRequest();
            return sipRequest.getContactHeader();
        }
    }

    /**
     * Do the necessary processing to handle an ACK directed at this Dialog.
     *
     * @param ackTransaction --
     *            the ACK transaction that was directed at this dialog.
     * @return -- true if the ACK was successfully consumed by the Dialog and
     *         resulted in the dialog state being changed.
     */
    public boolean handleAck(SIPServerTransaction ackTransaction) {
        SIPRequest sipRequest = ackTransaction.getOriginalRequest();

        if (isAckSeen()
                && getRemoteSeqNumber() == sipRequest.getCSeq().getSeqNumber()) {

            if (log.isDebugEnabled()) {
                log.debug("ACK already seen by dialog -- dropping Ack"
                        + " retransmission");
            }
            if (this.timerTask != null) {
                this.timerTask.cancel();
                this.timerTask = null;
            }
            return false;
        } else if (this.getState() == DialogState.TERMINATED) {
            if (log.isDebugEnabled())
                log.debug("Dialog is terminated -- dropping ACK");
            return false;

        } else {

            // This could be a re-invite processing.
            // check to see if the ack matches with the last
            // transaction.

            SIPServerTransaction tr = getInviteTransaction();

            SIPResponse sipResponse = (tr != null ? tr.getLastResponse() : null);

            // Idiot check for sending ACK from the wrong side!
            if (tr != null
                    && sipResponse != null
                    && sipResponse.getStatusCode() / 100 == 2
                    && sipResponse.getCSeq().getMethod().equals(Request.INVITE)
                    && sipResponse.getCSeq().getSeqNumber() == sipRequest
                    .getCSeq().getSeqNumber()) {

                ackTransaction.setDialog(this, sipResponse.getDialogId(false));
                // record that we already saw an ACK for
                // this dialog.
                ackReceived(sipRequest);
                if (log.isDebugEnabled())
                    log.debug("ACK for 2XX response --- sending to TU ");
                return true;

            } else {
                // This happens when the ACK is re-transmitted and
                // arrives
                // too late
                // to be processed.
                if (log.isDebugEnabled())
                    log.debug(" INVITE transaction not found  -- Discarding ACK");
                return false;
            }
        }
    }

}
