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
package gov.nist.javax.sip.stack;

import gov.nist.core.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.DialogFilter;

import javax.sip.address.Hop;
import javax.sip.header.*;
import javax.sip.message.*;
import javax.sip.*;

import java.text.ParseException;
import java.io.IOException;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/*
 * Bug fixes / enhancements:Emil Ivov, Antonis Karydas, Daniel J. Martinez
 * Manzano, Daniel, Hagai Sela, Vazques-Illa, Bill Roome, Thomas Froment and
 * Pierre De Rop, Christophe Anzille and Jeroen van Bemmel, Frank Reif.
 *
 */

/**
 * Represents a server transaction. Implements the following state machines.
 *
 * <pre>
 *
 *
 *
 *                                                  |INVITE
 *                                                  |pass INV to TU
 *                               INVITE             V send 100 if TU won't in 200ms
 *                               send response+-----------+
 *                                   +--------|           |--------+101-199 from TU
 *                                   |        | Proceeding|        |send response
 *                                   +------->|           |<-------+
 *                                            |           |          Transport Err.
 *                                            |           |          Inform TU
 *                                            |           |--------------->+
 *                                            +-----------+                |
 *                               300-699 from TU |     |2xx from TU        |
 *                               send response   |     |send response      |
 *                                               |     +------------------>+
 *                                               |                         |
 *                               INVITE          V          Timer G fires  |
 *                               send response+-----------+ send response  |
 *                                   +--------|           |--------+       |
 *                                   |        | Completed |        |       |
 *                                   +------->|           |<-------+       |
 *                                            +-----------+                |
 *                                               |     |                   |
 *                                           ACK |     |                   |
 *                                           -   |     +------------------>+
 *                                               |        Timer H fires    |
 *                                               V        or Transport Err.|
 *                                            +-----------+  Inform TU     |
 *                                            |           |                |
 *                                            | Confirmed |                |
 *                                            |           |                |
 *                                            +-----------+                |
 *                                                  |                      |
 *                                                  |Timer I fires         |
 *                                                  |-                     |
 *                                                  |                      |
 *                                                  V                      |
 *                                            +-----------+                |
 *                                            |           |                |
 *                                            | Terminated|<---------------+
 *                                            |           |
 *                                            +-----------+
 *
 *                                 Figure 7: INVITE server transaction
 *
 *
 *                      		Request received
 *                                                     |pass to TU
 *
 *                                                     V
 *                                               +-----------+
 *                                               |           |
 *                                               | Trying    |-------------+
 *                                               |           |             |
 *                                               +-----------+             |200-699 from TU
 *                                                     |                   |send response
 *                                                     |1xx from TU        |
 *                                                     |send response      |
 *                                                     |                   |
 *                                  Request            V      1xx from TU  |
 *                                  send response+-----------+send response|
 *                                      +--------|           |--------+    |
 *                                      |        | Proceeding|        |    |
 *                                      +------->|           |<-------+    |
 *                               +<--------------|           |             |
 *                               |Trnsprt Err    +-----------+             |
 *                               |Inform TU            |                   |
 *                               |                     |                   |
 *                               |                     |200-699 from TU    |
 *                               |                     |send response      |
 *                               |  Request            V                   |
 *                               |  send response+-----------+             |
 *                               |      +--------|           |             |
 *                               |      |        | Completed |<------------+
 *                               |      +------->|           |
 *                               +<--------------|           |
 *                               |Trnsprt Err    +-----------+
 *                               |Inform TU            |
 *                               |                     |Timer J fires
 *                               |                     |-
 *                               |                     |
 *                               |                     V
 *                               |               +-----------+
 *                               |               |           |
 *                               +-------------->| Terminated|
 *                                               |           |
 *                                               +-----------+
 *
 *
 *
 *
 *
 * </pre>
 *
 * @version 1.2 $Revision: 1.81 $ $Date: 2006/11/22 04:28:08 $
 * @author M. Ranganathan
 *
 */
public class SIPServerTransaction extends SIPTransaction implements
        javax.sip.ServerTransaction {

    private static final Logger log =
            Logger.getLogger(SIPServerTransaction.class);

    // force the listener to see transaction

    private int rseqNumber;

    private SIPDialog dialog;

    // the unacknowledged SIPResponse

    private SIPResponse pendingReliableResponse;

    // The pending reliable Response Timer
    private ProvisionalResponseTask provisionalResponseTask;
    private final Object provisionalResponseTaskLock = new Object();

    private boolean retransmissionAlertEnabled;

    private RetransmissionAlertTimerTask retransmissionAlertTimerTask;

    protected boolean isAckSeen;

    private SIPClientTransaction pendingSubscribeTransaction;

    private SIPServerTransaction inviteTransaction;
    
    protected RetransmissionTimer retransmissionTimerTask = null;
    
    /**
     * This timer task is used for alerting the application to send
     * retransmission alerts.
     *
     *
     */
    class RetransmissionAlertTimerTask extends SIPStackTimerTask {

        private final Logger log =
                Logger.getLogger(RetransmissionAlertTimerTask.class);

        String dialogId;

        int ticks;

        int ticksLeft;

        public RetransmissionAlertTimerTask() {
            if (log.isDebugEnabled())
                log.debug("RetransmissionAlertTimerTask() entered");
            this.ticks = sipStack.getT1();
            this.ticksLeft = this.ticks;
        }

        protected void runTask() {
            // todo mmath: Should this be modified to count from 1 to 0, and instead
            // change the scheduling of this task to have a initial delay of BASE_TIMER?
            // Not used now, maybe fix later...

            if (log.isDebugEnabled())
                log.debug("run() entered");

            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            ticksLeft--;
            if (ticksLeft == -1) {
                serverTransaction.fireRetransmissionTimer();
                this.ticksLeft = 2 * ticks;
            }

        }

    }

    class ProvisionalResponseTask extends SIPStackTimerTask {
        int nExpirations;

        int ticks;

        int ticksLeft;

        public ProvisionalResponseTask() {
            this.ticks = sipStack.getT1();
            this.ticksLeft = this.ticks;
            nExpirations = 0;
        }

        protected void runTask() {
            // todo mmath: Should this be modified to count from 1 to 0, and instead
            // change the scheduling of this task to have a initial delay of BASE_TIMER?
            // Not used now, maybe fix later...

            nExpirations++;
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            /*
                * The reliable provisional response is passed to the transaction
                * layer periodically with an interval that starts at T1 seconds and
                * doubles for each retransmission (T1 is defined in Section 17 of
                * RFC 3261). Once passed to the server transaction, it is added to
                * an internal list of unacknowledged reliable provisional
                * responses. The transaction layer will forward each retransmission
                * passed from the UAS core.
                */
            // If the transaction has terminated,
            if (serverTransaction.isTerminated()) {

                this.cancel();

            } else if (nExpirations >= sipStack.getTimerH()) {
                serverTransaction.raiseErrorEvent(
                        SIPTransactionErrorEvent.TIMEOUT_ERROR);
            } else {

                ticksLeft--;
                if (ticksLeft == 0) {
                    serverTransaction.fireReliableResponseRetransmissionTimer();
                    ticks = 2 * ticks;
                    this.ticksLeft = ticks;
                }

            }

        }

    }

    /**
     * This timer task will terminate the transaction if the listener does not
     * respond in a pre-determined time period. This helps prevent buggy
     * listeners (who fail to respond) from causing memory leaks. This allows a
     * container to protect itself from buggy code ( that fails to respond to a
     * server transaction).
     *
     */
    class ListenerExecutionMaxTimer extends SIPStackTimerTask {
        SIPServerTransaction serverTransaction = SIPServerTransaction.this;

        ListenerExecutionMaxTimer() {
        }

        protected void runTask() {
            try {
                if (serverTransaction.getState() == null) {
                    serverTransaction.terminate();
                    SIPTransactionStack sipStack = serverTransaction
                            .getSIPStack();
                    sipStack.removePendingTransaction(serverTransaction);
                    sipStack.removeTransaction(serverTransaction);

                }
            } catch (Exception ex) {
                log.error("unexpected exception", ex);
            }
        }
    }

    /**
     * This timer task is for INVITE server transactions. It will send a trying
     * in 200 ms. if the TU does not do so.
     *
     */
    class SendTrying extends SIPStackTimerTask {

        protected SendTrying() {
            if (log.isDebugEnabled())
                log.debug("Scheduled timer for "+ SIPServerTransaction.this);

        }

        protected void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;

            if (serverTransaction.getRealState() == null
                    || TransactionState.TRYING == serverTransaction
                    .getRealState()) {
                if (log.isDebugEnabled())
                    log.debug(" sending Trying current state = "
                            + serverTransaction.getRealState());
                try {
                    serverTransaction
                            .sendMessage(serverTransaction.getOriginalRequest()
                                    .createResponse(100, "Trying"));
                    if (log.isDebugEnabled())
                        log.debug(" trying sent " + serverTransaction.getRealState());
                } catch (IOException ex) {
                    log.warn("IO error sending TRYING");
                }
            }
        }
    }

    class TransactionTimer extends SIPStackTimerTask {

        public TransactionTimer() {
            if (log.isDebugEnabled()) {
                log.debug("TransactionTimer() : " + getTransactionId());
            }
        }

        protected void runTask() {
            // If the transaction has terminated,
            if (isTerminated()) {
                // Keep the transaction hanging around in the transaction table
                // to catch the incoming ACK -- this is needed for tcp only.
                // Note that the transaction record is actually removed in
                // the connection linger timer.
                try {
                    this.cancel();
                    
                    if(retransmissionTimerTask != null){
                        retransmissionTimerTask.cancel();
                    }
                } catch (IllegalStateException ex) {
                    if (!sipStack.isAlive())
                        return;
                }

                // Oneshot timer that garbage collects the SeverTransaction
                // after a scheduled amount of time. The linger timer allows
                // the client side of the tx to use the same connection to
                // send an ACK and prevents a race condition for creation
                // of new server tx
                TimerTask myTimer = new LingerTimer();

                sipStack.timer.schedule(myTimer,
                        SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);

            } else {
                // Add to the fire list -- needs to be moved
                // outside the synchronized block to prevent
                // deadlock.
                //fireTimer();
                fireTimeoutTimerTick();

            }
        }

    }

    /**
     * Adding new retransmission timer to start the tick only when the response is sent not before. 
     * 
     *
     * @author esebpar
     */
    class RetransmissionTimer extends SIPStackTimerTask {

        public RetransmissionTimer() {
            if (log.isDebugEnabled()) {
                log.debug("RetransmissionTimer() : " + getTransactionId());
            }
        }

        protected void runTask() {
            // If the transaction has terminated,
            if (isTerminated()) {
                // Keep the transaction hanging around in the transaction table
                // to catch the incoming ACK -- this is needed for tcp only.
                // Note that the transaction record is actually removed in
                // the connection linger timer.
                try {
                    this.cancel();
                } catch (IllegalStateException ex) {
                    if (!sipStack.isAlive())
                        return;
                }
            } else {
                // Add to the fire list -- needs to be moved
                // outside the synchronized block to prevent
                // deadlock.
                fireRetransmissionTimerTick();

            }
        }

    }
    
    /**
     * Send a response.
     *
     * @param transactionResponse --
     *            the response to send
     *
     */

    private void sendResponse(SIPResponse transactionResponse)
            throws IOException {

        // RFC18.2.2. Sending Responses
        // The server transport uses the value of the top Via header field in
        // order
        // to determine where to send a response.
        // It MUST follow the following process:
        // If the "sent-protocol" is a reliable transport
        // protocol such as TCP or SCTP,
        // or TLS over those, the response MUST be
        // sent using the existing connection
        // to the source of the original request
        // that created the transaction, if that connection is still open.
        if (isReliable()) {

            sendResponseToProcessor(transactionResponse,
                    originalRequest.getMessageChannel());

            // TODO If that connection attempt fails, the server SHOULD
            // use SRV 3263 procedures
            // for servers in order to determine the IP address
            // and port to open the connection and send the response to.

        } else {
            Hop hop = getNextHopFromVia(transactionResponse);

            getListeningPoint().getMessageProcessor().sendMessage(
                    hop, null, transactionResponse);
        }
    }

    private Hop getNextHopFromVia(SIPResponse transactionResponse)
            throws IOException {

        Via via = transactionResponse.getTopmostVia();
        String transport = via.getTransport();
        if (transport == null)
            throw new IOException("missing transport!");
        // @@@ hagai Symmetric NAT support
        int port = via.getRPort();
        if (port == -1)
            port = via.getPort();
        if (port == -1) {
            if (transport.equalsIgnoreCase("TLS"))
                port = 5061;
            else
                port = 5060;
        }

        // Otherwise, if the Via header field value contains a
        // "maddr" parameter, the response MUST be forwarded to
        // the address listed there, using the port indicated in "sent-by",
        // or port 5060 if none is present. If the address is a multicast
        // address, the response SHOULD be sent using
        // the TTL indicated in the "ttl" parameter, or with a
        // TTL of 1 if that parameter is not present.
        String host;
        if (via.getMAddr() != null) {
            host = via.getMAddr();
        } else {
            // Otherwise (for unreliable unicast transports),
            // if the top Via has a "received" parameter, the response MUST
            // be sent to the
            // address in the "received" parameter, using the port indicated
            // in the
            // "sent-by" value, or using port 5060 if none is specified
            // explicitly.
            host = via.getParameter(Via.RECEIVED);
            if (host == null) {
                // Otherwise, if it is not receiver-tagged, the response
                // MUST be
                // sent to the address indicated by the "sent-by" value,
                // using the procedures in Section 5
                // RFC 3263 PROCEDURE TO BE DONE HERE
                host = via.getHost();
            }
        }

        return sipStack.addressResolver.resolveAddress(new HopImpl(host,
                port, transport));
    }

    /**
     * Creates a new server transaction.
     *
     * @param sipStack
     *            Transaction stack this transaction belongs to.
     */
    protected SIPServerTransaction(SIPTransactionStack sipStack,
                                   SIPRequest request) {

        super(sipStack,
                request.getMessageChannel().getPeerAddress(),
                request.getMessageChannel().getPeerPort(),
                request.getMessageChannel().getMessageProcessor().getListeningPoint());

        if (sipStack.maxListenerResponseTime != -1) {
            sipStack.timer.schedule(new ListenerExecutionMaxTimer(),
                    sipStack.maxListenerResponseTime * 1000);
        }

        this.rseqNumber = (int) (Math.random() * 1000);
        // Only one outstanding request for a given server tx.

        setOriginalRequest(request);
        if (log.isDebugEnabled()) {
            log.debug("Creating Server Transaction "
                    + this.getBranchId());
        }

    }

    /**
     * Deterines if the message is a part of this transaction.
     *
     * @param messageToTest
     *            Message to check if it is part of this transaction.
     *
     * @return True if the message is part of this transaction, false if not.
     */
    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {

        // List of Via headers in the message to test
        ViaList viaHeaders;
        // Topmost Via header in the list
        Via topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;

        transactionMatches = false;

        String method = messageToTest.getCSeq().getMethod();
        // Invite Server transactions linger in the terminated state in the
        // transaction
        // table and are matched to compensate for
        // http://bugs.sipit.net/show_bug.cgi?id=769
        if ((method.equals(Request.INVITE) || !isTerminated())) {

            // Get the topmost Via header and its branch parameter
            viaHeaders = messageToTest.getViaHeaders();
            if (viaHeaders != null) {

                topViaHeader = (Via) viaHeaders.getFirst();
                messageBranch = topViaHeader.getBranch();
                if (messageBranch != null) {

                    // If the branch parameter exists but
                    // does not start with the magic cookie,
                    if (!messageBranch.toLowerCase().startsWith(
                            SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {

                        // Flags this as old
                        // (RFC2543-compatible) client
                        // version
                        messageBranch = null;

                    }

                }

                // If a new branch parameter exists,
                if (messageBranch != null && this.getBranch() != null) {
                    if (method.equals(Request.CANCEL)) {
                        // Cancel is handled as a special case because it
                        // shares the same same branch id of the invite
                        // that it is trying to cancel.
                        transactionMatches = this.getMethod().equals(
                                Request.CANCEL)
                                && getBranch().equalsIgnoreCase(messageBranch)
                                && topViaHeader.getSentBy().equals(
                                ((Via) getOriginalRequest()
                                        .getViaHeaders().getFirst())
                                        .getSentBy());

                    } else {
                        // Matching server side transaction with only the
                        // branch parameter.
                        transactionMatches = getBranch().equalsIgnoreCase(
                                messageBranch)
                                && topViaHeader.getSentBy().equals(
                                ((Via) getOriginalRequest()
                                        .getViaHeaders().getFirst())
                                        .getSentBy());

                    }

                } else {
                    // If this is an RFC2543-compliant message,
                    // This code is really here for backwards compatibility. It
                    // is a weak check.
                    // If RequestURI, To tag, From tag,
                    // CallID, CSeq number, and top Via
                    // headers are the same,
                    String originalFromTag = super.fromTag;

                    String thisFromTag = messageToTest.getFrom().getTag();

                    boolean skipFrom = (originalFromTag == null || thisFromTag == null);

                    String originalToTag = super.toTag;

                    String thisToTag = messageToTest.getTo().getTag();

                    boolean skipTo = (originalToTag == null || thisToTag == null);
                    boolean isResponse = (messageToTest instanceof SIPResponse);
                    if ((isResponse || getOriginalRequest().getRequestURI()
                            .equals(
                                    ((SIPRequest) messageToTest)
                                            .getRequestURI()))
                            && (skipFrom || originalFromTag
                            .equalsIgnoreCase(thisFromTag))
                            && (skipTo || originalToTag
                            .equalsIgnoreCase(thisToTag))
                            && getOriginalRequest().getCallId().getCallId()
                            .equalsIgnoreCase(
                                    messageToTest.getCallId()
                                            .getCallId())
                            && getOriginalRequest().getCSeq().getSeqNumber() == messageToTest
                            .getCSeq().getSeqNumber()
                            && ( (!messageToTest.getCSeq().getMethod().equals(Request.CANCEL))
                            || getOriginalRequest().getMethod().equals(messageToTest.getCSeq().getMethod()))
                            && topViaHeader.equals(getOriginalRequest()
                            .getViaHeaders().getFirst())) {

                        transactionMatches = true;
                    }

                }

            }

        }
        return transactionMatches;

    }

    /**
     * Send out a trying response (only happens when the transaction is mapped).
     * Otherwise the transaction is not known to the stack.
     */
    protected void map() {
        // note that TRYING is a pseudo-state for invite transactions

        if (getRealState() == null || getRealState() == TransactionState.TRYING) {
            // JvB: Removed the condition 'dialog!=null'. Trying should also be
            // sent by intermediate proxies. This fixes some TCK tests
            // null check added as the stack may be stopped.
            if (isInviteTransaction() && !this.isMapped
                    && sipStack.timer != null) {
                this.isMapped = true;
                // Schedule a timer to fire in 200 ms if the
                // TU did not send a trying in that time.
                sipStack.timer.schedule(new SendTrying(), 200);

            } else {
                isMapped = true;
            }
        }
        // Pull it out of the pending transactions list.
        sipStack.removePendingTransaction(this);
    }

    /**
     * Return true if the transaction is known to stack.
     */
    public boolean isTransactionMapped() {
        return this.isMapped;
    }

    /**
     * Process a new request message through this transaction. If necessary,
     * this message will also be passed onto the TU.
     *
     * @param transactionRequest
     *            Request to process.
     */
    public void processRequest(SIPRequest transactionRequest) {
        boolean toTu = false;

        // Can only process a single request directed to the
        // transaction at a time. For a given server transaction
        // the listener sees only one event at a time.

        if (log.isDebugEnabled()) {
            log.debug("processRequest: " + transactionRequest.getFirstLine());
            log.debug("tx state = " + this.getRealState());
        }

        try {

            // If this is the first request for this transaction,
            if (getRealState() == null) {
                this.setState(TransactionState.TRYING);
                toTu = true;
                this.setPassToListener();
                if (isInviteTransaction() && this.isMapped) {
                    // JvB: also
                    // proxies need
                    // to do this

                    // Has side-effect of setting
                    // state to "Proceeding"
                    sendMessage(transactionRequest.createResponse(100, "Trying"));

                }
                // If an invite transaction is ACK'ed while in
                // the completed state,
            } else if (isInviteTransaction()
                    && TransactionState.COMPLETED == getRealState()
                    && transactionRequest.getMethod().equals(Request.ACK)) {

                // @jvB bug fix
                this.setState(TransactionState.CONFIRMED);
                disableRetransmissionTimer();
                if (!isReliable()) {
                    enableTimeoutTimer(sipStack.getTimerI());

                } else {

                    this.setState(TransactionState.TERMINATED);

                }


                // JvB: For the purpose of testing a TI, added a property to
                // pass it anyway
                if (sipStack.non2XXAckPassedToListener) {
                    // This is useful for test applications that want to see
                    // all messages.
                    DialogFilter.processRequest(transactionRequest, this);
                } else {
                    // According to RFC3261 Application should not Ack in
                    // CONFIRMED state
                    if (log.isDebugEnabled()) {
                        log.debug("ACK received for server Tx "
                                + this.getTransactionId()
                                + " not delivering to application!");
                    }

                    this.semaphore.release();
                }
                return;

                // If we receive a retransmission of the original
                // request,
            } else if (transactionRequest.getMethod().equals(
                    getOriginalRequest().getMethod())) {

                if (TransactionState.PROCEEDING == getRealState()
                        || TransactionState.COMPLETED == getRealState()) {
                    this.semaphore.release();
                    // Resend the last response to
                    // the client
                    if (lastResponse != null) {

                        // Send the message to the client
                        sendResponseToProcessor(lastResponse,
                                originalRequest.getMessageChannel());

                    }
                } else if (transactionRequest.getMethod().equals(Request.ACK)) {
                    // This is passed up to the TU to suppress
                    // retransmission of OK
                    DialogFilter.processRequest(transactionRequest, this);
                }
                if (log.isDebugEnabled()) {
                    log.debug("completed processing retransmitted request : "
                            + transactionRequest.getFirstLine()
                            + this
                            + " txState = "
                            + this.getState()
                            + " lastResponse = " + this.getLastResponse());
                }
                return;

            }

            // Pass message to the TU
            if (TransactionState.COMPLETED != getRealState()
                    && TransactionState.TERMINATED != getRealState()) {
                if (getOriginalRequest().getMethod().equals(
                        transactionRequest.getMethod())) {
                    // Only send original request to TU once!
                    if (toTu) {
                        DialogFilter.processRequest(transactionRequest, this);
                    } else
                        this.semaphore.release();
                } else {
                    DialogFilter.processRequest(transactionRequest, this);
                }
            } else {
                // This seems like a common bug so I am allowing it through!
                if (getSIPStack()
                        .isDialogCreated(getOriginalRequest().getMethod())
                        && getRealState() == TransactionState.TERMINATED
                        && transactionRequest.getMethod().equals(Request.ACK)) {
                    SIPDialog thisDialog = this.dialog;

                    if (thisDialog == null || !thisDialog.ackProcessed) {
                        // Filter out duplicate acks
                        if (thisDialog != null) {
                            thisDialog.ackReceived(transactionRequest);
                            thisDialog.ackProcessed = true;
                        }
                        DialogFilter.processRequest(transactionRequest, this);
                    } else {
                        this.semaphore.release();
                    }

                } else if (transactionRequest.getMethod()
                        .equals(Request.CANCEL)) {
                    if (log.isDebugEnabled())
                        log.debug("Too late to cancel Transaction");
                    this.semaphore.release();
                    // send OK and just ignore the CANCEL.
                    try {
                        sendMessage(transactionRequest.createResponse(Response.OK));
                    } catch (IOException ex) {
                        // Transaction is already terminated
                        // just ignore the IOException.
                    }
                }
                if (log.isDebugEnabled())
                    log.debug("Dropping request " + getRealState());
            }

        } catch (IOException e) {
            this.semaphore.release();
            this.raiseIOExceptionEvent();
        }

    }

    /**
     * Send a response message through this transactionand onto the client. The
     * response drives the state machine.
     *
     * @param messageToSend
     *            Response to process and send.
     */
    public void sendMessage(SIPMessage messageToSend) throws IOException {

        // Message typecast as a response
        SIPResponse transactionResponse;
        // Status code of the response being sent to the client
        int statusCode;

        // Get the status code from the response
        transactionResponse = (SIPResponse) messageToSend;
        statusCode = transactionResponse.getStatusCode();

        try {
            // Provided we have set the banch id for this we set the BID for the
            // outgoing via.
            if (this.getOriginalRequest().getTopmostVia().getBranch() != null)
                transactionResponse.getTopmostVia().setBranch(this.getBranch());
            else
                transactionResponse.getTopmostVia().removeParameter(
                        ParameterNames.BRANCH);

            // Make the topmost via headers match identically for the
            // transaction rsponse.
            if (!this.getOriginalRequest().getTopmostVia().hasPort())
                transactionResponse.getTopmostVia().removePort();
        } catch (ParseException ex) {
            log.warn("ParseException in sendMessage()",ex);
        }

        // Method of the response does not match the request used to
        // create the transaction - transaction state does not change.
        if (!transactionResponse.getCSeq().getMethod().equals(
                getOriginalRequest().getMethod())) {
            sendResponse(transactionResponse);
            return;
        }

        // If the TU sends a provisional response while in the
        // trying state,
        if (getRealState() == TransactionState.TRYING) {
            if (statusCode / 100 == 1) {
                this.setState(TransactionState.PROCEEDING);
            } else if (200 <= statusCode && statusCode <= 699) {
                // INVITE ST has TRYING as a Pseudo state
                // (See issue 76). We are using the TRYING
                // pseudo state invite Transactions
                // to signal if the application
                // has sent trying or not and hence this
                // check is necessary.
                if (!isInviteTransaction()) {
                    if (!isReliable()) {
                        // Linger in the completed state to catch
                        // retransmissions if the transport is not reliable.
                        this.setState(TransactionState.COMPLETED);
                        // Note that Timer J is only set for Unreliable
                        // transports -- see Issue 75.
                        /*
                               * From RFC 3261 Section 17.2.2 (non-invite server
                               * transaction)
                               *
                               * When the server transaction enters the "Completed"
                               * state, it MUST set Timer J to fire in 64*T1 seconds
                               * for unreliable transports, and zero seconds for
                               * reliable transports. While in the "Completed" state,
                               * the server transaction MUST pass the final response
                               * to the transport layer for retransmission whenever a
                               * retransmission of the request is received. Any other
                               * final responses passed by the TU to the server
                               * transaction MUST be discarded while in the
                               * "Completed" state. The server transaction remains in
                               * this state until Timer J fires, at which point it
                               * MUST transition to the "Terminated" state.
                               */
                        enableTimeoutTimer(sipStack.getTimerJ());
                    } else {
                        this.setState(TransactionState.TERMINATED);
                    }
                } else {
                    // This is the case for INVITE server transactions.
                    // essentially, it duplicates the code in the PROCEEDING
                    // case below. There is no TRYING state for INVITE
                    // transactions
                    // in the RFC. We are using it to signal whether the
                    // application
                    // has sent a provisional response or not. Hence this is
                    // treated
                    // the same as as Proceeding.
                    if (statusCode / 100 == 2) {
                        // Status code is 2xx means that the
                        // transaction transitions to TERMINATED
                        // for both Reliable as well as unreliable
                        // transports. Note that the dialog layer
                        // takes care of retransmitting 2xx final responses.
                        /*
                               * RFC 3261 Section 13.3.1.4 Note, however, that the
                               * INVITE server transaction will be destroyed as soon
                               * as it receives this final response and passes it to
                               * the transport. Therefore, it is necessary to
                               * periodically pass the response directly to the
                               * transport until the ACK arrives. The 2xx response is
                               * passed to the transport with an interval that starts
                               * at T1 seconds and doubles for each retransmission
                               * until it reaches T2 seconds (T1 and T2 are defined in
                               * Section 17). Response retransmissions cease when an
                               * ACK request for the response is received. This is
                               * independent of whatever transport protocols are used
                               * to send the response.
                               */
                        this.disableRetransmissionTimer();
                        this.disableTimeoutTimer();
                        this.collectionTime = sipStack.getTimerJ();
                        this.setState(TransactionState.TERMINATED);
                        if (this.dialog != null)
                            this.dialog.setRetransmissionTicks();
                    } else {
                        // This an error final response.
                        this.setState(TransactionState.COMPLETED);
                        if (!isReliable()) {
                            /*
                                    * RFC 3261
                                    *
                                    * While in the "Proceeding" state, if the TU passes
                                    * a response with status code from 300 to 699 to
                                    * the server transaction, the response MUST be
                                    * passed to the transport layer for transmission,
                                    * and the state machine MUST enter the "Completed"
                                    * state. For unreliable transports, timer G is set
                                    * to fire in T1 seconds, and is not set to fire for
                                    * reliable transports.
                                    */

                            enableRetransmissionTimer();
                            startRetransmissionTimer();

                        }
                        enableTimeoutTimer(sipStack.getTimerH());
                    }
                }

            }

            // If the transaction is in the proceeding state,
        } else if (getRealState() == TransactionState.PROCEEDING) {

            if (isInviteTransaction()) {

                // If the response is a failure message,
                if (statusCode / 100 == 2) {
                    // Set up to catch returning ACKs
                    // The transaction lingers in the
                    // terminated state for some time
                    // to catch retransmitted INVITEs
                    this.disableRetransmissionTimer();
                    this.disableTimeoutTimer();
                    this.collectionTime = sipStack.getTimerJ();
                    this.setState(TransactionState.TERMINATED);
                    if (this.dialog != null)
                        this.dialog.setRetransmissionTicks();

                } else if (300 <= statusCode && statusCode <= 699) {

                    // Set up to catch returning ACKs
                    this.setState(TransactionState.COMPLETED);
                    if (!isReliable()) {
                        /*
                               * While in the "Proceeding" state, if the TU passes a
                               * response with status code from 300 to 699 to the
                               * server transaction, the response MUST be passed to
                               * the transport layer for transmission, and the state
                               * machine MUST enter the "Completed" state. For
                               * unreliable transports, timer G is set to fire in T1
                               * seconds, and is not set to fire for reliable
                               * transports.
                               */

                        enableRetransmissionTimer();
                        startRetransmissionTimer();

                    }
                    enableTimeoutTimer(sipStack.getTimerH());

                }

                // If the transaction is not an invite transaction
                // and this is a final response,
            } else if (200 <= statusCode && statusCode <= 699) {
                // This is for Non-invite server transactions.

                // Set up to retransmit this response,
                // or terminate the transaction
                this.setState(TransactionState.COMPLETED);
                if (!isReliable()) {

                    disableRetransmissionTimer();
                    enableTimeoutTimer(sipStack.getTimerJ());

                } else {

                    this.setState(TransactionState.TERMINATED);

                }

            }

            // If the transaction has already completed,
        } else if (TransactionState.COMPLETED == this.getRealState()) {

            return;
        }
        try {
            // Send the message to the client.
            // Record the last message sent out.
            if (log.isDebugEnabled()) {
                log.debug("sendMessage : tx = " + this + " getState = "
                        + this.getState());
            }
            lastResponse = transactionResponse;
            this.sendResponse(transactionResponse);

        } catch (IOException e) {

            this.setState(TransactionState.TERMINATED);
            this.collectionTime = 0;
            throw e;

        }

    }

    /**
     * Called by the transaction stack when a retransmission timer fires. This
     * retransmits the last response when the retransmission filter is enabled.
     */
    protected void fireRetransmissionTimer() {

        try {
            if (log.isDebugEnabled()) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    log.debug("fireRetransmissionTimer() -- ",e);
                }
            }

            // Resend the last response sent by this transaction
            if (isInviteTransaction() && lastResponse != null) {
                // null can happen if this is terminating when the timer fires.
                if (!this.retransmissionAlertEnabled) {
                    // Retransmit last response until ack.
                    if (lastResponse.getStatusCode() / 100 > 2)
                        sendResponseToProcessor(lastResponse,
                                originalRequest.getMessageChannel());
                } else {
                    // alert the application to retransmit the last response
                    SipProviderImpl sipProvider = this.getSipProvider();
                    TimeoutEvent txTimeout = new TimeoutEvent(sipProvider,
                            this, Timeout.RETRANSMIT);
                    sipProvider.handleEvent(txTimeout, this);
                }

            }
        } catch (IOException e) {
            log.warn(e);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

        }

    }

    private void fireReliableResponseRetransmissionTimer() {
        try {

            sendResponseToProcessor(this.pendingReliableResponse,
                    originalRequest.getMessageChannel());

        } catch (IOException e) {
            log.warn(e);
            this.setState(TransactionState.TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

        }
    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {

        if (log.isDebugEnabled())
            log.debug("SIPServerTransaction.fireTimeoutTimer this = "
                    + this + " current state = " + this.getRealState()
                    + " method = "
                    + this.getOriginalRequest().getMethod());

        SIPDialog dialog = this.dialog;
        if (getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())
                && (TransactionState.CALLING == this.getRealState() || TransactionState.TRYING == this
                .getRealState())) {
            dialog.setState(SIPDialog.TERMINATED_STATE);
        } else if (getOriginalRequest().getMethod().equals(Request.BYE)) {
            if (dialog != null && dialog.isTerminatedOnBye())
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

        if (TransactionState.COMPLETED == this.getRealState()
                && isInviteTransaction()) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            this.setState(TransactionState.TERMINATED);
            sipStack.removeTransaction(this);

            // MMANY: NOTE this occurs when no ACK is received for a retransmitted
            // error response. Therefore, the dialog should be terminated
            // if this is the transaction that created the dialog.
            if ((dialog != null) && (dialog.getFirstTransaction().equals(this)))
                dialog.setState(SIPDialog.TERMINATED_STATE);

        } else if (TransactionState.COMPLETED == this.getRealState()
                && !isInviteTransaction()) {
            this.setState(TransactionState.TERMINATED);
            sipStack.removeTransaction(this);

        } else if (TransactionState.CONFIRMED == this.getRealState()
                && isInviteTransaction()) {
            // TIMER_I should not generate a timeout
            // exception to the application when the
            // Invite transaction is in Confirmed state.
            // Just transition to Terminated state.
            this.setState(TransactionState.TERMINATED);
            sipStack.removeTransaction(this);
        } else if (!isInviteTransaction()
                && (TransactionState.COMPLETED == this.getRealState() || TransactionState.CONFIRMED == this
                .getRealState())) {
            this.setState(TransactionState.TERMINATED);
        } else if (isInviteTransaction()
                && TransactionState.TERMINATED == this.getRealState()) {
            // This state could be reached when retransmitting

            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            if (dialog != null)
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

    }

    /**
     * Get the last response.
     */
    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.ServerTransaction#sendResponse(javax.sip.message.Response)
      */
    public void sendResponse(Response response) throws SipException {
        SIPResponse sipResponse = (SIPResponse) response;

        SIPDialog dialog = this.dialog;
        if (response == null)
            throw new NullPointerException("null response");

        try {
            sipResponse.checkHeaders();
        } catch (ParseException ex) {
            throw new SipException(ex.getMessage());
        }

        // check for meaningful response.
        if (!sipResponse.getCSeq().getMethod().equals(this.getMethod())) {
            throw new SipException(
                    "CSeq method does not match Request method of request that created the tx.");
        }

        /*
           * 200-class responses to SUBSCRIBE requests also MUST contain an
           * "Expires" header. The period of time in the response MAY be shorter
           * but MUST NOT be longer than specified in the request.
           */
        if (this.getMethod().equals(Request.SUBSCRIBE)
                && response.getStatusCode() / 100 == 2) {

            if (response.getHeader(ExpiresHeader.NAME) == null) {
                throw new SipException(
                        "Expires header is mandatory in 2xx response of SUBSCRIBE");
            } else {
                Expires requestExpires = (Expires) this.getOriginalRequest()
                        .getExpires();
                Expires responseExpires = (Expires) response.getExpires();
                /*
                     * If no "Expires" header is present in a SUBSCRIBE request, the
                     * implied default is defined by the event package being used.
                     */
                if (requestExpires != null
                        && responseExpires.getExpires() > requestExpires
                        .getExpires()) {
                    throw new SipException(
                            "Response Expires time exceeds request Expires time : See RFC 3265 3.1.1");
                }
            }

        }

        // Check for mandatory header.
        if (sipResponse.getStatusCode() == 200
                && sipResponse.getCSeq().getMethod().equals(Request.INVITE)
                && sipResponse.getHeader(ContactHeader.NAME) == null)
            throw new SipException(
                    "Contact Header is mandatory for the OK to the INVITE");

        if (!this.isMessagePartOfTransaction((SIPMessage) response)) {
            throw new SipException(
                    "Response does not belong to this transaction.");
        }

        // Fix up the response if the dialog has already been established.
        try {
            /*
                * TODO Check this. The UAS MAY send a final response to the initial
                * request before having received PRACKs for all unacknowledged
                * reliable provisional responses, unless the final response is 2xx
                * and any of the unacknowledged reliable provisional responses
                * contained a session description. In that case, it MUST NOT send a
                * final response until those provisional responses are
                * acknowledged.
                */           
            if (this.pendingReliableResponse != null
                    && response.getStatusCode() / 100 == 2
                    && this.pendingReliableResponse.getContentTypeHeader() != null
                    && this.pendingReliableResponse.getContentTypeHeader().getContentType().equalsIgnoreCase("application")
                    && this.pendingReliableResponse.getContentTypeHeader().getContentSubType().equalsIgnoreCase("sdp")) {
                throw new SipException(
                        "cannot send response -- unacked povisional");
            } else {
                // Sending the final response cancels the
                // pending response task.
                if (this.pendingReliableResponse != null
                        && sipResponse.isFinalResponse()) {
                    synchronized(provisionalResponseTaskLock) {
                        this.provisionalResponseTask.cancel();
                        this.provisionalResponseTask = null;
                    }
                }
            }

            // Dialog checks. These make sure that the response
            // being sent makes sense.
            if (dialog != null) {
                if (sipResponse.getStatusCode() / 100 == 2
                        && sipStack.isDialogCreated(sipResponse.getCSeq()
                        .getMethod())) {
                    if (dialog.getLocalTag() == null
                            && sipResponse.getTo().getTag() == null) {
                        // Trying to send final response and user forgot to set
                        // to
                        // tag on the response -- be nice and assign the tag for
                        // the user.
                        sipResponse.getTo().setTag(Utils.generateTag());
                    } else if (dialog.getLocalTag() != null
                            && sipResponse.getToTag() == null) {
                        sipResponse.setToTag(dialog.getLocalTag());
                    } else if (dialog.getLocalTag() != null
                            && sipResponse.getToTag() != null
                            && !dialog.getLocalTag().equals(
                            sipResponse.getToTag())) {
                        throw new SipException("Tag mismatch dialogTag is "
                                + dialog.getLocalTag() + " responseTag is "
                                + sipResponse.getToTag());
                    }
                }

                if (!sipResponse.getCallId().getCallId().equals(
                        dialog.getCallId().getCallId())) {
                    throw new SipException("Dialog mismatch!");
                }
            }

            // If sending the response within an established dialog, then
            // set up the tags appropriately. Should probably throw exception
            // here if tags do not match instead of setting the tags??
            // lets be nice and set the tag if the user forgot to do it.

            if (dialog != null && dialog.getLocalTag() != null
                    && sipResponse.getTo().getTag() == null
                    && sipResponse.getStatusCode() != 100)
                sipResponse.getTo().setTag(dialog.getLocalTag());

            // Backward compatibility slippery slope....
            // Only set the from tag in the response when the
            // incoming request has a from tag.
            String fromTag = ((SIPRequest) this.getRequest()).getFrom()
                    .getTag();
            if (fromTag != null && sipResponse.getFromTag() != null
                    && !sipResponse.getFromTag().equals(fromTag)) {
                throw new SipException(
                        "From tag of response does not match sipResponse from tag");
            } else if (fromTag != null) {
                sipResponse.getFrom().setTag(fromTag);
            } else {
                if (log.isDebugEnabled())
                    log.debug("WARNING -- Null From tag in request!!");
            }

            // See if the dialog needs to be inserted into the dialog table
            // or if the state of the dialog needs to be changed.
            if (dialog != null && response.getStatusCode() != 100) {
                if (!dialog.checkResponseTags(sipResponse))
                    throw new SipException(
                            "Response tags dont match with Dialog tags");

                DialogState oldState = dialog.getState();
                dialog.setLastResponse(this, (SIPResponse) response);
                if (oldState == null
                        && dialog.getState() == DialogState.TERMINATED) {
                    DialogTerminatedEvent event = new DialogTerminatedEvent(
                            dialog.getSipProvider(), dialog);

                    // Provide notification to the listener that the dialog has
                    // ended.
                    dialog.getSipProvider().handleEvent(event, this);

                }

            } else if (dialog == null
                    && this.getMethod().equals(Request.INVITE)
                    && this.retransmissionAlertEnabled
                    && this.retransmissionAlertTimerTask == null
                    && response.getStatusCode() / 100 == 2) {
                String dialogId = ((SIPResponse) response).getDialogId(true);

                this.retransmissionAlertTimerTask =
                        new RetransmissionAlertTimerTask();
                sipStack.retransmissionAlertTransactions.put(dialogId, this);
                sipStack.timer.schedule(this.retransmissionAlertTimerTask, 0,
                        SIPTransactionStack.BASE_TIMER_INTERVAL);

            }

            // Send message after possibly inserting the Dialog
            // into the dialog table to avoid a possible race condition.
            sendMessage((SIPResponse) response);

        } catch (IOException ex) {
            throw new SipException(ex.getMessage());
        } catch (java.text.ParseException ex1) {
            throw new SipException(ex1.getMessage());
        }
    }

    /**
     * Return the book-keeping information that we actually use.
     */
    private TransactionState getRealState() {
        return super.getState();
    }

    /**
     * Return the current transaction state according to the RFC 3261
     * transaction state machine. Invite transactions do not have a trying
     * state. We just use this as a pseudo state for processing requests.
     *
     * @return the state of the transaction.
     */
    public TransactionState getState() {
        // Trying is a pseudo state for INVITE transactions.
        if (this.isInviteTransaction()
                && TransactionState.TRYING == super.getState())
            return TransactionState.PROCEEDING;
        else
            return super.getState();
    }

    /**
     * Sets a timeout after which the connection is closed (provided the server
     * does not use the connection for outgoing requests in this time period)
     * and calls the superclass to set state.
     */
    public void setState(TransactionState newState) {
        // Set this timer for connection caching
        // of incoming connections.
        if (newState == TransactionState.TERMINATED && this.isReliable()
                && (!getSIPStack().cacheServerConnections)) {
            // Set a time after which the connection
            // is closed.
            this.collectionTime = sipStack.getTimerJ();
        }

        super.setState(newState);

    }

    /**
     * Start the timer task.
     */
    protected void startTransactionTimer() {
        if (sipStack.timer != null) {
            // The timer is set to null when the Stack is
            // shutting down.
            if (log.isDebugEnabled())
                log.debug("Scheduling TransactionTimer for " + this);
            TimerTask myTimer = new TransactionTimer();

            // mmath: We do not want to fire the transaction timer
            // immediately or do we. Changed delay from 0 to BASE_TIMER_INTERVAL
            // since it caused unnecessary response retransmissions.
            sipStack.timer.schedule(myTimer, SIPTransactionStack.BASE_TIMER_INTERVAL,
                    SIPTransactionStack.BASE_TIMER_INTERVAL);
        }
    }
    
    /**
     * Start the timer task.
     */
    protected void startRetransmissionTimer() {
        if (sipStack.timer != null) {
            
            if(retransmissionTimerTask != null){
                retransmissionTimerTask.cancel();
                retransmissionTimerTask = null;
            }
            
            // The timer is set to null when the Stack is
            // shutting down.
            if (log.isDebugEnabled())
                log.debug("Scheduling TransactionTimer for " + this);
            retransmissionTimerTask = new RetransmissionTimer();

            // mmath: We do not want to fire the transaction timer
            // immediately or do we. Changed delay from 0 to BASE_TIMER_INTERVAL
            // since it caused unnecessary response retransmissions.
            sipStack.timer.schedule(retransmissionTimerTask, SIPTransactionStack.BASE_TIMER_INTERVAL,
                    SIPTransactionStack.BASE_TIMER_INTERVAL);
            
        }
    }

    public boolean equals(Object other) {
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }
        SIPServerTransaction sst = (SIPServerTransaction) other;
        return this.getBranch().equalsIgnoreCase(sst.getBranch());
    }

    /*
      * (non-Javadoc)
      *
      * @see gov.nist.javax.sip.stack.SIPTransaction#getDialog()
      */
    public Dialog getDialog() {
        return this.dialog;
    }

    /*
      * (non-Javadoc)
      *
      * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
      *      gov.nist.javax.sip.message.SIPMessage)
      */
    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (log.isDebugEnabled())
            log.debug("setDialog " + this + " dialog = " + sipDialog);
        this.dialog = sipDialog;
        if (dialogId != null)
            this.dialog.setAssigned();
        if (this.retransmissionAlertEnabled
                && this.retransmissionAlertTimerTask != null) {
            this.retransmissionAlertTimerTask.cancel();
            sipStack.retransmissionAlertTransactions
                    .remove(retransmissionAlertTimerTask.dialogId);
            this.retransmissionAlertTimerTask = null;
        }
        this.retransmissionAlertEnabled = false;

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.Transaction#terminate()
      */
    public void terminate() throws ObjectInUseException {
        this.setState(TransactionState.TERMINATED);
        if (this.retransmissionAlertTimerTask != null) {
            this.retransmissionAlertTimerTask.cancel();
            this.sipStack.retransmissionAlertTransactions
                    .remove(retransmissionAlertTimerTask.dialogId);
            this.retransmissionAlertTimerTask = null;
        }

    }

    protected void sendReliableProvisionalResponse(Response relResponse)
            throws SipException {

        /*
           * After the first reliable provisional response for a request has been
           * acknowledged, the UAS MAY send additional reliable provisional
           * responses. The UAS MUST NOT send a second reliable provisional
           * response until the first is acknowledged.
           */
        if (this.pendingReliableResponse != null) {
            throw new SipException("Unacknowledged response");

        } else
            this.pendingReliableResponse = (SIPResponse) relResponse;
        /*
           * In addition, it MUST contain a Require header field containing the
           * option tag 100rel, and MUST include an RSeq header field.
           */
        RSeq rseq = (RSeq) relResponse.getHeader(RSeqHeader.NAME);
        if (relResponse.getHeader(RSeqHeader.NAME) == null) {
            rseq = new RSeq();
            relResponse.setHeader(rseq);
        }

        try {
            this.rseqNumber++;
            rseq.setSeqNumber(this.rseqNumber);

            // start the timer task which will retransmit the reliable response
            // until the PRACK is received
            this.lastResponse = (SIPResponse) relResponse;
            sendMessage((SIPResponse)relResponse);
            synchronized(provisionalResponseTaskLock) {
                this.provisionalResponseTask = new ProvisionalResponseTask();
                this.sipStack.timer.schedule(provisionalResponseTask,
                        SIPTransactionStack.BASE_TIMER_INTERVAL,
                        SIPTransactionStack.BASE_TIMER_INTERVAL);
            }

        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }

    }

    public SIPResponse getReliableProvisionalResponse() {
        return this.pendingReliableResponse;
    }

    /**
     * Cancel the retransmit timer for the provisional response task.
     *
     * @return true if the tx has seen the prack for the first time and false
     *         otherwise.
     *
     */
    public boolean prackRecieved() {

        if (this.pendingReliableResponse == null)
            return false;

        synchronized(provisionalResponseTaskLock) {
            if (this.provisionalResponseTask != null) {
                this.provisionalResponseTask.cancel();
                this.provisionalResponseTask = null;
            }
        }
        this.pendingReliableResponse = null;
        return true;
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.ServerTransaction#enableRetransmissionAlerts()
      */

    public void enableRetransmissionAlerts() throws SipException {
        if (this.getDialog() != null)
            throw new SipException("Dialog associated with tx");

        else if (!this.getMethod().equals(Request.INVITE))
            throw new SipException("Request Method must be INVITE");

        this.retransmissionAlertEnabled = true;

    }

    public boolean isRetransmissionAlertEnabled() {
        return this.retransmissionAlertEnabled;
    }

    /**
     * Disable retransmission Alerts and cancel associated timers.
     *
     */
    public void disableRetransmissionAlerts() {
        if (this.retransmissionAlertTimerTask != null
                && this.retransmissionAlertEnabled) {
            this.retransmissionAlertTimerTask.cancel();
            this.retransmissionAlertEnabled = false;

            // MMANY
            String dialogId = this.retransmissionAlertTimerTask.dialogId;
            sipStack.retransmissionAlertTransactions.remove(dialogId);
            this.retransmissionAlertTimerTask = null;
        }
    }

    /**
     * This is book-keeping for retransmission filter management.
     */
    public void setAckSeen() {
        this.isAckSeen = true;
    }

    /**
     * This is book-keeping for retransmission filter management.
     */
    public boolean ackSeen() {
        return this.isAckSeen;
    }

    public void setMapped(boolean b) {
        this.isMapped = true;

    }

    public void setPendingSubscribe(
            SIPClientTransaction pendingSubscribeClientTx) {
        this.pendingSubscribeTransaction = pendingSubscribeClientTx;

    }

    public void releaseSem() {
        if (this.pendingSubscribeTransaction != null) {
            /*
                * When a notify is being processed we take a lock on the subscribe
                * to avoid racing with the OK of the subscribe.
                */
            pendingSubscribeTransaction.releaseSem();
        } else if (this.inviteTransaction != null
                && this.getMethod().equals(Request.CANCEL)) {
            /*
                * When a CANCEL is being processed we take a nested lock on the
                * associated INVITE server tx.
                */
            this.inviteTransaction.releaseSem();
        }
        super.releaseSem();
    }

    /**
     * The INVITE Server Transaction corresponding to a CANCEL Server
     * Transaction.
     *
     * @param st --
     *            the invite server tx corresponding to the cancel server
     *            transaction.
     */
    public void setInviteTransaction(SIPServerTransaction st) {
        this.inviteTransaction = st;

    }

    public void sendResponseToProcessor(SIPResponse response,
                                        MessageChannel messageChannel)
            throws IOException {
        Hop hop = getNextHopFromVia(response);
        getListeningPoint().getMessageProcessor().sendResponse(
                    messageChannel, hop, response);
    }

}
