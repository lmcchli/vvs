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

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.SIPConstants;
import javax.sip.message.*;

import java.text.ParseException;
import java.util.*;
import gov.nist.javax.sip.address.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.util.concurrent.*;

import java.io.IOException;

import org.apache.log4j.Logger;

/*
 * Jeff Keyser -- initial. Daniel J. Martinez Manzano --Added support for TLS
 * message channel. Emil Ivov -- bug fixes. Chris Beardshear -- bug fix. Andreas
 * Bystrom -- bug fixes. Matt Keller (Motorolla) -- bug fix.
 */

/**
 * Represents a client transaction. Implements the following state machines.
 * (From RFC 3261)
 *
 * <pre>
 *
 *
 *
 *
 *
 *
 *                                                  |INVITE from TU
 *                                Timer A fires     |INVITE sent
 *                                Reset A,          V                      Timer B fires
 *                                INVITE sent +-----------+                or Transport Err.
 *                                  +---------|           |---------------+inform TU
 *                                  |         |  Calling  |               |
 *                                  +--------&gt;|           |--------------&gt;|
 *                                            +-----------+ 2xx           |
 *                                               |  |       2xx to TU     |
 *                                               |  |1xx                  |
 *                       300-699 +---------------+  |1xx to TU            |
 *                      ACK sent |                  |                     |
 *                   resp. to TU |  1xx             V                     |
 *                               |  1xx to TU  -----------+               |
 *                               |  +---------|           |               |
 *                               |  |         |Proceeding |--------------&gt;|
 *                               |  +--------&gt;|           | 2xx           |
 *                               |            +-----------+ 2xx to TU     |
 *                               |       300-699    |                     |
 *                               |       ACK sent,  |                     |
 *                               |       resp. to TU|                     |
 *                               |                  |                     |      NOTE:
 *                               |  300-699         V                     |
 *                               |  ACK sent  +-----------+Transport Err. |  transitions
 *                               |  +---------|           |Inform TU      |  labeled with
 *                               |  |         | Completed |--------------&gt;|  the event
 *                               |  +--------&gt;|           |               |  over the action
 *                               |            +-----------+               |  to take
 *                               |              &circ;   |                     |
 *                               |              |   | Timer D fires       |
 *                               +--------------+   | -                   |
 *                                                  |                     |
 *                                                  V                     |
 *                                            +-----------+               |
 *                                            |           |               |
 *                                            | Terminated|&lt;--------------+
 *                                            |           |
 *                                            +-----------+
 *
 *                                    Figure 5: INVITE client transaction
 *
 *
 *                                                      |Request from TU
 *                                                      |send request
 *                                  Timer E             V
 *                                  send request  +-----------+
 *                                      +---------|           |-------------------+
 *                                      |         |  Trying   |  Timer F          |
 *                                      +--------&gt;|           |  or Transport Err.|
 *                                                +-----------+  inform TU        |
 *                                   200-699         |  |                         |
 *                                   resp. to TU     |  |1xx                      |
 *                                   +---------------+  |resp. to TU              |
 *                                   |                  |                         |
 *                                   |   Timer E        V       Timer F           |
 *                                   |   send req +-----------+ or Transport Err. |
 *                                   |  +---------|           | inform TU         |
 *                                   |  |         |Proceeding |------------------&gt;|
 *                                   |  +--------&gt;|           |-----+             |
 *                                   |            +-----------+     |1xx          |
 *                                   |              |      &circ;        |resp to TU   |
 *                                   | 200-699      |      +--------+             |
 *                                   | resp. to TU  |                             |
 *                                   |              |                             |
 *                                   |              V                             |
 *                                   |            +-----------+                   |
 *                                   |            |           |                   |
 *                                   |            | Completed |                   |
 *                                   |            |           |                   |
 *                                   |            +-----------+                   |
 *                                   |              &circ;   |                         |
 *                                   |              |   | Timer K                 |
 *                                   +--------------+   | -                       |
 *                                                      |                         |
 *                                                      V                         |
 *                                NOTE:           +-----------+                   |
 *                                                |           |                   |
 *                            transitions         | Terminated|&lt;------------------+
 *                            labeled with        |           |
 *                            the event           +-----------+
 *                            over the action
 *                            to take
 *
 *                                    Figure 6: non-INVITE client transaction
 *
 *
 *
 *
 *
 *
 * </pre>
 *
 *
 * @author M. Ranganathan
 *
 * @version 1.2 $Revision: 1.58 $ $Date: 2006/11/22 04:26:06 $
 */
public class SIPClientTransaction extends SIPTransaction implements
        javax.sip.ClientTransaction {

    private static final Logger log =
            Logger.getLogger(SIPClientTransaction.class);

    // a SIP Client transaction may belong simultaneously to multiple
    // dialogs in the early state. These dialogs all have
    // the same call ID and same From tag but different to tags.

    private final ConcurrentHashMap<String, SIPDialog> sipDialogs =
            new ConcurrentHashMap<String, SIPDialog>();

    private SIPRequest lastRequest;

    private SIPDialog defaultDialog;

    private final Hop nextHop;

    public class TransactionTimer extends SIPStackTimerTask {

        protected void runTask() {
            SIPClientTransaction clientTransaction;
            SIPTransactionStack sipStack;
            clientTransaction = SIPClientTransaction.this;
            sipStack = clientTransaction.sipStack;

            // If the transaction has terminated,
            if (clientTransaction.isTerminated()) {

                if (log.isDebugEnabled()) {
                    log.debug("removing  = "
                            + clientTransaction + " isReliable "
                            + clientTransaction.isReliable());
                }

                try {
                    this.cancel();
                } catch (IllegalStateException ex) {
                    if (!sipStack.isAlive())
                        return;
                }

                // Let the transaction linger for a while and then remove it.
                TimerTask myTimer = new LingerTimer();
                sipStack.timer.schedule(
                        myTimer,
                        SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);

            } else {
                // If this transaction has not
                // terminated,
                // Fire the transaction timer.
                clientTransaction.fireTimer();
            }
        }
    }

    /**
     * Creates a new client transaction.
     *
     * @param stack
     *            Transaction stack this transaction belongs to.
     */
    protected SIPClientTransaction(SIPTransactionStack stack,
                                   ListeningPointImpl listeningPoint,
                                   SIPRequest sipRequest, Hop nextHop) {

        super(stack, nextHop.getHost(), nextHop.getPort(), listeningPoint);

        this.nextHop = nextHop;
        setOriginalRequest(sipRequest);

        if (log.isDebugEnabled())
            log.debug("Creating clientTransaction " + this);
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
        ViaList viaHeaders = messageToTest.getViaHeaders();
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;
        String messageBranch = ((Via) viaHeaders.getFirst()).getBranch();
        boolean rfc3261Compliant = getBranch() != null
                && messageBranch != null
                && getBranch().toLowerCase().startsWith(
                SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)
                && messageBranch.toLowerCase().startsWith(
                SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE);

        transactionMatches = false;
        if (TransactionState.COMPLETED == this.getState()) {
            if (rfc3261Compliant) {
                transactionMatches = getBranch().equalsIgnoreCase(
                        ((Via) viaHeaders.getFirst()).getBranch())
                        && getMethod().equals(
                        messageToTest.getCSeq().getMethod());
            } else {
                transactionMatches = getBranch().equals(
                        messageToTest.getTransactionId());
            }
        } else if (!isTerminated()) {
            if (rfc3261Compliant) {
                if (viaHeaders != null) {
                    // If the branch parameter is the
                    // same as this transaction and the method is the same,
                    if (getBranch().equalsIgnoreCase(
                            ((Via) viaHeaders.getFirst()).getBranch())) {
                        transactionMatches = getOriginalRequest().getCSeq()
                                .getMethod().equals(
                                messageToTest.getCSeq().getMethod());

                    }
                }
            } else {
                // not RFC 3261 compliant.
                if (getBranch() != null) {
                    transactionMatches = getBranch().equalsIgnoreCase(
                            messageToTest.getTransactionId());
                } else {
                    transactionMatches = getOriginalRequest()
                            .getTransactionId().equalsIgnoreCase(
                            messageToTest.getTransactionId());
                }

            }

        }
        return transactionMatches;

    }

    /**
     * Send a request message through this transaction and onto the client.
     *
     * @param messageToSend
     *            Request to process and send.
     */
    public void sendMessage(SIPMessage messageToSend) throws IOException {

        SIPRequest transactionRequest = (SIPRequest) messageToSend;

        // Set the branch id for the top via header.
        Via topVia = (Via) transactionRequest.getViaHeaders().getFirst();
        // Tack on a branch identifier to match responses.
        try {
            topVia.setBranch(getBranch());
        } catch (java.text.ParseException ex) {
            if (log.isDebugEnabled())
                log.debug(ex.getMessage(), ex);
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending Message " + messageToSend);
            log.debug("TransactionState " + this.getState());
        }
        // If this is the first request for this transaction,
        if (TransactionState.PROCEEDING == getState()
                || TransactionState.CALLING == getState()) {

            // If this is a TU-generated ACK request,
            if (transactionRequest.getMethod().equals(Request.ACK)) {

                // Send directly to the underlying
                // transport and close this transaction
                if (isReliable()) {
                    this.setState(TransactionState.TERMINATED);
                } else {
                    this.setState(TransactionState.COMPLETED);
                }
                // BUGBUG -- This suppresses sending the ACK uncomment this to
                // test 4xx retransmission
                // if (transactionRequest.getMethod() != Request.ACK)
                sendRequestToProcessor(transactionRequest);
                return;

            }

        }
        try {

            // Send the message to the server
            lastRequest = transactionRequest;
            if (getState() == null) {
                // Save this request as the one this transaction
                // is handling
                setOriginalRequest(transactionRequest);
                // Change to trying/calling state
                // Set state first to avoid race condition..

                if (transactionRequest.getMethod().equals(Request.INVITE)) {
                    this.setState(TransactionState.CALLING);
                } else if (transactionRequest.getMethod().equals(Request.ACK)) {
                    // Acks are never retransmitted.
                    this.setState(TransactionState.TERMINATED);
                } else {
                    this.setState(TransactionState.TRYING);
                }
                if (!isReliable()) {
                    enableRetransmissionTimer();
                }
                if (isInviteTransaction()) {
                    enableTimeoutTimer(sipStack.getTimerB());
                } else {
                    enableTimeoutTimer(sipStack.getTimerF());
                }
            }
            // BUGBUG This supresses sending ACKS -- uncomment to test
            // 4xx retransmission.
            // if (transactionRequest.getMethod() != Request.ACK)
            sendRequestToProcessor(transactionRequest);

        } catch (IOException e) {
            this.setState(TransactionState.TERMINATED);
            throw e;

        }

    }

    /**
     * Process a new response message through this transaction. If necessary,
     * this message will also be passed onto the TU.
     *
     * @param transactionResponse
     *            Response to process.
     */
    public synchronized void processResponse(SIPResponse transactionResponse,
                                             SIPDialog dialog) {

        // If the state has not yet been assigned then this is a
        // spurious response.

        if (getState() == null)
            return;

        // Ignore 1xx
        if ((TransactionState.COMPLETED == this.getState() ||
                TransactionState.TERMINATED == this.getState()) &&
                transactionResponse.getStatusCode() / 100 == 1) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("processing "
                    + transactionResponse.getFirstLine() + "current state = "
                    + getState());
            log.debug("dialog = " + dialog);
        }

        this.lastResponse = transactionResponse;

        if (dialog != null && transactionResponse.getStatusCode() != 100
                && transactionResponse.getTo().getTag() != null) {
            // add the route before you process the response.
            dialog.setLastResponse(this, transactionResponse);
            this.setDialog(dialog, transactionResponse.getDialogId(false));
        }

        try {
            if (isInviteTransaction())
                inviteClientTransaction(transactionResponse,
                        dialog);
            else
                nonInviteClientTransaction(transactionResponse,
                        dialog);
        } catch (IOException ex) {
            this.setState(TransactionState.TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     *
     * <pre>
     *
     *
     *
     *
     *
     *                                  |Request from TU
     *                                  |send request
     *              Timer E             V
     *              send request  +-----------+
     *                  +---------|           |-------------------+
     *                  |         |  Trying   |  Timer F          |
     *                  +-------->|           |  or Transport Err.|
     *                            +-----------+  inform TU        |
     *               200-699         |  |                         |
     *               resp. to TU     |  |1xx                      |
     *               +---------------+  |resp. to TU              |
     *               |                  |                         |
     *               |   Timer E        V       Timer F           |
     *               |   send req +-----------+ or Transport Err. |
     *               |  +---------|           | inform TU         |
     *               |  |         |Proceeding |------------------>|
     *               |  +-------->|           |-----+          |
     *               |            +-----------+     |1xx          |
     *               |              |      ^        |resp to TU   |
     *               | 200-699      |      +--------+             |
     *               | resp. to TU  |                             |
     *               |              |                             |
     *               |              V                             |
     *               |            +-----------+                   |
     *               |            |           |                   |
     *               |            | Completed |                   |
     *               |            |           |                   |
     *               |            +-----------+                   |
     *               |              ^   |                         |
     *               |              |   | Timer K                 |
     *               +--------------+   | -                       |
     *                                  |                         |
     *                                  V                         |
     *            NOTE:           +-----------+                   |
     *                            |           |                   |
     *        transitions         | Terminated|<------------------+
     *        labeled with        |           |
     *        the event           +-----------+
     *        over the action
     *        to take
     *
     *                 Figure 6: non-INVITE client transaction
     *
     *
     *
     *
     * </pre>
     *
     * @param transactionResponse --
     */
    private void nonInviteClientTransaction(SIPResponse transactionResponse,
                                            SIPDialog sipDialog) {
        int statusCode = transactionResponse.getStatusCode();
        if (TransactionState.TRYING == this.getState()) {
            if (statusCode / 100 == 1) {
                this.setState(TransactionState.PROCEEDING);
                enableRetransmissionTimer(MAXIMUM_RETRANSMISSION_TICK_COUNT);
                enableTimeoutTimer(sipStack.getTimerF());
                // According to RFC, the TU has to be informed on
                // this transition.
                DialogFilter.processResponseWhenTransactionExists(transactionResponse,
                        sipDialog, this);
            } else if (200 <= statusCode && statusCode <= 699) {
                // Send the response up to the TU.
                DialogFilter.processResponseWhenTransactionExists(transactionResponse,
                        sipDialog, this);
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(sipStack.getTimerK());
                } else {
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.PROCEEDING == this.getState()) {
            if (statusCode / 100 == 1) {
                DialogFilter.processResponseWhenTransactionExists(transactionResponse,
                        sipDialog, this);
            } else if (200 <= statusCode && statusCode <= 699) {
                DialogFilter.processResponseWhenTransactionExists(transactionResponse,
                        sipDialog, this);
                disableRetransmissionTimer();
                disableTimeoutTimer();
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(sipStack.getTimerK());
                } else {
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(" Not sending response to TU! " + getState());
            }
            this.semaphore.release();
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     *
     * <pre>
     *
     *
     *
     *
     *
     *                                |INVITE from TU
     *              Timer A fires     |INVITE sent
     *              Reset A,          V                      Timer B fires
     *              INVITE sent +-----------+                or Transport Err.
     *                +---------|           |---------------+inform TU
     *                |         |  Calling  |               |
     *                +-------->|           |-------------->|
     *                          +-----------+ 2xx           |
     *                             |  |       2xx to TU     |
     *                             |  |1xx                  |
     *     300-699 +---------------+  |1xx to TU            |
     *    ACK sent |                  |                     |
     * resp. to TU |  1xx             V                     |
     *             |  1xx to TU  -----------+               |
     *             |  +---------|           |               |
     *             |  |         |Proceeding |-------------->|
     *             |  +-------->|           | 2xx           |
     *             |            +-----------+ 2xx to TU     |
     *             |       300-699    |                     |
     *             |       ACK sent,  |                     |
     *             |       resp. to TU|                     |
     *             |                  |                     |      NOTE:
     *             |  300-699         V                     |
     *             |  ACK sent  +-----------+Transport Err. |  transitions
     *                +---------|           |Inform TU      |  labeled with
     *             |  |         | Completed |-------------->|  the event
     *             |  +-------->|           |               |  over the action
     *             |            +-----------+               |  to take
     *             |              ^   |                     |
     *             |              |   | Timer D fires       |
     *             +--------------+   | -                   |
     *                                |                     |
     *                                V                     |
     *                          +-----------+               |
     *                          |           |               |
     *                          | Terminated|<--------------+
     *                          |           |
     *                          +-----------+
     *
     *
     *
     *
     * </pre>
     *
     * @param transactionResponse --
     *            transaction response received.
     */

    private void inviteClientTransaction(SIPResponse transactionResponse,
                                         SIPDialog dialog) throws IOException {
        int statusCode = transactionResponse.getStatusCode();

        if (TransactionState.TERMINATED == this.getState()) {
            // Do nothing in the terminated state.
            this.semaphore.release();
            return;
        } else if (TransactionState.CALLING == this.getState()) {
            if (statusCode / 100 == 2) {

                // JvB: do this ~before~ calling the application,
                // to avoid retransmissions of the INVITE after app sends ACK
                disableRetransmissionTimer();
                disableTimeoutTimer();
                this.setState(TransactionState.TERMINATED);

                // 200 responses are always seen by TU.
                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);

            } else if (statusCode / 100 == 1) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                this.setState(TransactionState.PROCEEDING);
                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);

            } else if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request

                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                // When in either the "Calling" or "Proceeding" states,
                // reception of response with status code from 300-699
                // MUST cause the client transaction to
                // transition to "Completed".
                // The client transaction MUST pass the received response up to
                // the TU, and the client transaction MUST generate an
                // ACK request.

                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);

                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                    enableTimeoutTimer(sipStack.getTimerD());
                } else {
                    // Proceed immediately to the TERMINATED state.
                    this.setState(TransactionState.TERMINATED);
                }
            }
        } else if (TransactionState.PROCEEDING == this.getState()) {
            if (statusCode / 100 == 1) {
                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);
            } else if (statusCode / 100 == 2) {
                this.setState(TransactionState.TERMINATED);
                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);
            } else if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request
                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }

                // JvB: update state before passing to app
                if (!isReliable()) {
                    this.setState(TransactionState.COMPLETED);
                } else {
                    this.setState(TransactionState.TERMINATED);
                }

                // Pass up to the TU for processing.
                DialogFilter.processResponseWhenTransactionExists(transactionResponse, dialog, this);

                if (!isReliable()) {
                    enableTimeoutTimer(sipStack.getTimerD());
                }
            }
        } else if (TransactionState.COMPLETED == this.getState()) {
            if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request
                try {
                    sendMessage((SIPRequest) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                } finally {
                    this.semaphore.release();
                }
            }

        }

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.ClientTransaction#sendRequest()
      */
    public void sendRequest() throws SipException {
        SIPRequest sipRequest = this.getOriginalRequest();

        if (log.isDebugEnabled()) {
            log.debug("sendRequest() " + sipRequest);
        }

        try {
            sipRequest.checkHeaders();
        } catch (ParseException ex) {
            log.error("missing required header");
            throw new SipException(ex.getMessage());
        }

        if (getMethod().equals(Request.SUBSCRIBE)
                && sipRequest.getHeader(ExpiresHeader.NAME) == null) {
            /*
                * If no "Expires" header is present in a SUBSCRIBE request, the
                * implied default is defined by the event package being used.
                *
                */
            log.warn("Expires header missing in outgoing subscribe --"
                    + " Notifier will assume implied value on event package");
        }
        try {
            if (this.getOriginalRequest().getMethod().equals(Request.CANCEL)) {
                SIPClientTransaction ct = (SIPClientTransaction) sipStack
                        .findCancelTransaction(this.getOriginalRequest(), false);
                if (ct == null) {
                    // If the original
                    // request has generated a final response, the CANCEL SHOULD
                    // NOT be
                    // sent, as it is an effective no-op, since CANCEL has no
                    // effect on
                    // requests that have already generated a final response.
                    throw new SipException(
                            "Could not find original tx to cancel. RFC 3261 9.1");
                } else if (ct.getState() == null) {
                    throw new SipException(
                            "State is null no provisional response yet " +
                                    "-- cannot cancel RFC 3261 9.1");
                } else if (!ct.getMethod().equals(Request.INVITE)) {
                    throw new SipException(
                            "Cannot cancel non-invite requests RFC 3261 9.1");

                }
            } else if (this.getOriginalRequest().getMethod()
                    .equals(Request.BYE)
                    || this.getOriginalRequest().getMethod().equals(
                    Request.NOTIFY)) {
                SIPDialog dialog = sipStack.getDialog(this.getOriginalRequest()
                        .getDialogId(false));
                // I want to behave like a user agent so send the BYE using the
                // Dialog
                if (this.getSipProvider().isAutomaticDialogSupportEnabled()
                        && dialog != null) {
                    throw new SipException(
                            "Dialog is present and AutomaticDialogSupport " +
                                    "is enabled for  the provider -- Send the " +
                                    "Request using the Dialog.sendRequest(transaction)");
                }
            }
            // Only map this after the fist request is sent out.
            this.isMapped = true;
            sendMessage(sipRequest);

        } catch (IOException ex) {
            this.setState(TransactionState.TERMINATED);
            throw new SipException("IO Error sending request");

        }

    }

    /**
     * Called by the transaction stack when a retransmission timer fires.
     */
    protected void fireRetransmissionTimer() {

        try {
            // Resend the last request sent
            if (this.getState() == null || !this.isMapped)
                return;
            if (TransactionState.CALLING == this.getState()
                    || TransactionState.TRYING == this.getState()) {
                // If the retransmission filter is disabled then
                // retransmission of the INVITE is the application
                // responsibility.

                if (lastRequest != null) {
                    if (sipStack.generateTimeStampHeader
                            && lastRequest.getHeader(TimeStampHeader.NAME) != null) {
                        long milisec = System.currentTimeMillis();
                        TimeStamp timeStamp = new TimeStamp();
                        try {
                            timeStamp.setTimeStamp(milisec);
                        } catch (InvalidArgumentException ex) {
                            InternalErrorHandler.handleException(ex);
                        }
                        lastRequest.setHeader(timeStamp);
                    }
                    sendRequestToProcessor(lastRequest);
                }

            }
        } catch (IOException e) {
            this.raiseIOExceptionEvent();
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }

    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {

        if (log.isDebugEnabled())
            log.debug("fireTimeoutTimer " + this);

        SIPDialog dialog = (SIPDialog) this.getDialog();
        if (TransactionState.CALLING == this.getState()
                || TransactionState.TRYING == this.getState()
                || TransactionState.PROCEEDING == this.getState()) {
            // Timeout occured. If this is asociated with a transaction
            // creation then kill the dialog.
            if (dialog != null
                    && (dialog.getState() == null ||
                    dialog.getState() == DialogState.EARLY)) {
                if (getSIPStack().isDialogCreated(this
                        .getOriginalRequest().getMethod())) {
                    // If this is a re-invite we do not delete the dialog even if the
                    // reinvite times out. Else
                    // terminate the enclosing dialog.
                    dialog.delete();
                }
            } else if (dialog != null) {
                // Guard against the case of BYE time out.

                if (getOriginalRequest().getMethod().equalsIgnoreCase(
                        Request.BYE)
                        && dialog.isTerminatedOnBye()) {
                    // Terminate the associated dialog on BYE Timeout.
                    dialog.delete();
                }
            }
        }
        if (TransactionState.COMPLETED != this.getState()) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
        } else {
            this.setState(TransactionState.TERMINATED);
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.ClientTransaction#createCancel()
      */
    public Request createCancel() throws SipException {
        SIPRequest originalRequest = this.getOriginalRequest();
        if (originalRequest == null)
            throw new SipException("Bad state " + getState());
        if (!originalRequest.getMethod().equals(Request.INVITE))
            throw new SipException("Only INIVTE may be cancelled");

        if (originalRequest.getMethod().equalsIgnoreCase(Request.ACK))
            throw new SipException("Cannot Cancel ACK!");
        else
            return originalRequest.createCancelRequest();
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.ClientTransaction#createAck()
      */
    public Request createAck() throws SipException {
        SIPRequest originalRequest = this.getOriginalRequest();
        if (originalRequest == null)
            throw new SipException("bad state " + getState());
        if (getMethod().equalsIgnoreCase(Request.ACK)) {
            throw new SipException("Cannot ACK an ACK!");
        } else if (lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (lastResponse.getStatusCode() < 200) {
            if (log.isDebugEnabled()) {
                log.debug("lastResponse = " + lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        }
        SIPRequest ackRequest = originalRequest
                .createAckRequest((To) lastResponse.getTo());
        // Pull the record route headers from the last reesponse.
        RecordRouteList recordRouteList = lastResponse.getRecordRouteHeaders();
        if (recordRouteList == null) {

            // QMAFL:
            // Address ACK to contact header received in response.
            // BUT, do not select the new contact header if the response is
            // a redirect (300-399) since in that case the ACK would be sent
            // to the redirected address.
            if ((lastResponse.getContactHeaders() != null) &&
                    (lastResponse.getStatusCode() > 399)) {

                Contact contact =
                        (Contact) lastResponse.getContactHeaders().getFirst();
                javax.sip.address.URI uri = (javax.sip.address.URI) contact
                        .getAddress().getURI().clone();
                ackRequest.setRequestURI(uri);
            }

            return ackRequest;
        }

        ackRequest.removeHeader(RouteHeader.NAME);
        RouteList routeList = new RouteList();
        // start at the end of the list and walk backwards
        ListIterator li = recordRouteList.listIterator(recordRouteList.size());
        while (li.hasPrevious()) {
            RecordRoute rr = (RecordRoute) li.previous();

            Route route = new Route();
            route.setAddress((AddressImpl)rr.getAddress().clone());
            route.setParameters((NameValueList) rr.getParameters().clone());
            routeList.add(route);
        }

        Contact contact = null;
        if (lastResponse.getContactHeaders() != null) {
            contact = (Contact) lastResponse.getContactHeaders().getFirst();
        }

        if (!((SipURI) ((Route) routeList.getFirst()).getAddress().getURI())
                .hasLrParam()) {

            // Contact may not yet be there (bug reported by Andreas B).

            Route route = null;
            if (contact != null) {
                route = new Route();
                route.setAddress((AddressImpl) (contact
                        .getAddress()).clone());
            }

            Route firstRoute = (Route) routeList.getFirst();
            routeList.removeFirst();
            javax.sip.address.URI uri = firstRoute.getAddress().getURI();
            ackRequest.setRequestURI(uri);

            if (route != null)
                routeList.add(route);

            ackRequest.addHeader(routeList);
        } else {
            if (contact != null) {
                javax.sip.address.URI uri = (javax.sip.address.URI) contact
                        .getAddress().getURI().clone();
                ackRequest.setRequestURI(uri);
                ackRequest.addHeader(routeList);
            }
        }
        return ackRequest;

    }

    /**
     * get the via header for an outgoing request.
     */
    public Via getOutgoingViaHeader() {
        return this.getListeningPoint().getViaHeader();
    }

    /**
     * This is called by the stack after a non-invite client transaction goes to
     * completed state.
     */
    public void clearState() {
        // reduce the state to minimum
        // This assumes that the application will not need
        // to access the request once the transaction is
        // completed.
        // TODO -- revisit this - results in a null pointer
        // occuring occasionally.
        // this.lastRequest = null;
        // this.originalRequest = null;
        // this.lastResponse = null;
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
                && (!getSIPStack().cacheClientConnections)) {
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
        TimerTask myTimer = new TransactionTimer();
        sipStack.timer.schedule(myTimer,
                sipStack.getBaseTimerInterval(),
                sipStack.getBaseTimerInterval());
    }

    /*
      * Terminate a transaction. This marks the tx as terminated The tx scanner
      * will run and remove the tx. (non-Javadoc)
      *
      * @see javax.sip.Transaction#terminate()
      */
    public void terminate() throws ObjectInUseException {
        this.setState(TransactionState.TERMINATED);

    }

    public void processResponse(SIPResponse sipResponse) {

        // Prune unhealthy responses early if handling statefully.
        // If the state has not yet been assigned then this is a
        // spurious response.
        if (getState() == null) {
            if (log.isDebugEnabled())
                log.debug("Dropping response - null transaction state");
            return;

            // Ignore 1xx
        } else if (TransactionState.COMPLETED == getState()
                && sipResponse.getStatusCode() / 100 == 1) {
            if (log.isDebugEnabled())
                log.debug("Dropping response - late arriving "
                        + sipResponse.getStatusCode());
            return;
        }



        SipStackImpl sipStack = (SipStackImpl) this.getSIPStack();

        // If a dialog has already been created for this response,
        // pass it up

        String dialogId = sipResponse.getDialogId(false);
        SIPDialog dialog = this.getDialog(dialogId);
        if (dialog == null) {

            // Dialog cannot be found for the response.
            // This must be a forked response.
            // no dialog assigned to this response but a default dialog has been
            // assigned. Note that if automatic dialog support is configured
            // then
            // a default dialog is always created.

            synchronized (this) {
                // We need synchronization here because two responses
                // may compete for the default dialog simultaneously
                if (defaultDialog != null
                        && sipStack.isDialogCreated(sipResponse.getCSeq()
                        .getMethod())
                        && sipResponse.getStatusCode() != 100) {
                    if (sipResponse.getFromTag() != null
                            && sipResponse.getToTag() != null) {
                        SIPResponse dialogResponse = defaultDialog
                                .getLastResponse();
                        String defaultDialogId = defaultDialog.getDialogId();
                        if (dialogResponse == null
                                || (sipResponse.getCSeq().getMethod().equals(
                                Request.SUBSCRIBE)
                                && dialogResponse.getCSeq().getMethod()
                                .equals(Request.NOTIFY) && defaultDialogId
                                .equals(dialogId))) {
                            // The default dialog has not been claimed yet.
                            defaultDialog.setLastResponse(this, sipResponse);
                            dialog = defaultDialog;
                        } else {
                            if (this.getDialog(dialogId) == null) {
                                // we dont previously have a dialog for this
                                // response.
                                // check if we have created one previously
                                // (happens in the
                                // case of REINVITE processing.
                                dialog = sipStack.getDialog(dialogId);
                                if (dialog == null) {
                                    // Nop we dont have one. so go ahead and
                                    // allocate a new one.
                                    dialog = new SIPDialog(this, sipResponse);
                                }
                            } else {
                                // Yes we do have a dialog for this id.
                                dialog = this.getDialog(dialogId);
                                // go ahead and use the dialog.
                                dialog.setLastResponse(this, sipResponse);
                            }

                        }

                        this.setDialog(dialog, dialog.getDialogId());
                    }

                }
            }

        }
        if (dialog == null) {
            dialog = this.defaultDialog;
        }

        this.processResponse(sipResponse, dialog);

    }

    /*
      * (non-Javadoc)
      *
      * @see gov.nist.javax.sip.stack.SIPTransaction#getDialog()
      */
    public synchronized Dialog getDialog() {
        // This is for backwards compatibility.
        Dialog retval = null;
        if (this.lastResponse != null && this.lastResponse.getFromTag() != null
                && this.lastResponse.getToTag() != null
                && this.lastResponse.getStatusCode() != 100) {
            String dialogId = this.lastResponse.getDialogId(false);
            retval = getDialog(dialogId);
        }

        if (retval == null) {
            retval = this.defaultDialog;

        }
        if (log.isDebugEnabled()) {
            log.debug(" sipDialogs =  " + sipDialogs
                    + " default dialog " + this.defaultDialog + " retval "
                    + retval);
        }
        return retval;

    }

    /*
      * (non-Javadoc)
      *
      * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
      *      gov.nist.javax.sip.message.SIPMessage)
      */
    public synchronized SIPDialog getDialog(String dialogId) {
        return this.sipDialogs.get(dialogId);
    }

    /*
      * (non-Javadoc)
      *
      * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
      *      gov.nist.javax.sip.message.SIPMessage)
      */
    public synchronized void setDialog(SIPDialog sipDialog, String dialogId) {
        if (log.isDebugEnabled())
            log.debug("setDialog: " + dialogId + "sipDialog = " + sipDialog);

        if (sipDialog == null) {
            log.error("NULL DIALOG!!");
            throw new NullPointerException("bad dialog null");
        }
        if (this.defaultDialog == null)
            this.defaultDialog = sipDialog;
        if (dialogId != null && sipDialog.getDialogId() != null) {
            this.sipDialogs.put(dialogId, sipDialog);

        }

    }

    public synchronized SIPDialog getDefaultDialog() {
        // TODO Auto-generated method stub
        return this.defaultDialog;
    }

    /**
     * Reeturn the previously computed next hop (avoid computing it twice).
     *
     * @return -- next hop previously computed.
     */
    public Hop getNextHop() {
        return nextHop;
    }

    public void sendRequestToProcessor(SIPRequest request) throws IOException {
        getListeningPoint().getMessageProcessor().sendMessage(
                getNextHop(),
                sipStack.getRouter(originalRequest).getOutboundProxy(),
                request);
    }
}
