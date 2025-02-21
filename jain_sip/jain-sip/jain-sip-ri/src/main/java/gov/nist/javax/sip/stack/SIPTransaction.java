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

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;

import java.util.*;

import javax.sip.*;
import javax.sip.message.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

/*
 * Modifications for TLS Support added by Daniel J. Martinez Manzano
 *         <dani@dif.um.es>
 * Bug fixes by Jeroen van Bemmel (JvB) and others.
 */

/**
 * Abstract class to support both client and server transactions. Provides an
 * encapsulation of a message channel, handles timer events, and creation of the
 * Via header for a message.
 *
 * @author Jeff Keyser
 * @author M. Ranganathan
 *
 *
 * @version 1.2 $Revision: 1.47 $ $Date: 2006/11/23 17:26:58 $
 */
public abstract class SIPTransaction implements javax.sip.Transaction {

    private static final Logger log = Logger.getLogger(SIPTransaction.class);

    private final ListeningPointImpl listeningPoint;

    // Flag to indicate that the listener gets to see the event.
    protected boolean toListener;


    // Proposed feature for next release.
    protected Object applicationData;

    protected SIPResponse lastResponse;

    protected boolean isMapped;

    protected Semaphore semaphore;

    protected boolean isSemaphoreAquired;

    protected String transactionId; // Transaction Id.

    // Audit tag used by the SIP Stack audit
    public long auditTag = 0;

    /**
     * Initialized but no state assigned.
     */
    public static final TransactionState INITIAL_STATE = null;

    /**
     * Trying state.
     */
    public static final TransactionState TRYING_STATE = TransactionState.TRYING;

    /**
     * CALLING State.
     */
    public static final TransactionState CALLING_STATE = TransactionState.CALLING;

    /**
     * Proceeding state.
     */
    public static final TransactionState PROCEEDING_STATE = TransactionState.PROCEEDING;

    /**
     * Completed state.
     */
    public static final TransactionState COMPLETED_STATE = TransactionState.COMPLETED;

    /**
     * Confirmed state.
     */
    public static final TransactionState CONFIRMED_STATE = TransactionState.CONFIRMED;

    /**
     * Terminated state.
     */
    public static final TransactionState TERMINATED_STATE = TransactionState.TERMINATED;

    /**
     * Maximum number of ticks between retransmissions.
     */
    protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;

    // Parent stack for this transaction
    protected final SIPTransactionStack sipStack;

    // Original request that is being handled by this transaction
    protected SIPRequest originalRequest;

    // Port of peer
    protected int peerPort;

    // Address of peer as a string
    protected String peerAddress;

    // Transaction branch ID
    private String branch;

    // Method of the Request used to create the transaction.
    private String method;

    // Sequence number of request used to create the transaction
    private long cSeq;

    // Current transaction state
    private AtomicReference<TransactionState> currentState =
            new AtomicReference<TransactionState>(null);

    // Number of ticks the retransmission timer was set to last
    private volatile int retransmissionTimerLastTickCount;

    // Number of ticks before the message is retransmitted
    private volatile int retransmissionTimerTicksLeft;

    private final Object retransmissionTimerLock = new Object();

    // Number of ticks before the transaction times out
    private volatile int timeoutTimerTicksLeft;

    // List of event listeners for this transaction
    private final Set<SIPTransactionEventListener> eventListeners =
            Collections.
                    synchronizedSet(new HashSet<SIPTransactionEventListener>());

    // Hang on to these - we clear out the request URI after
    // transaction goes to final state. Pointers to these are kept around
    // for transaction matching as long as the transaction is in
    // the transaction table.
    protected From from;

    protected To to;

    protected Event event;

    protected CallID callId;

    // Counter for caching of connections.
    // Connection lingers for collectionTime
    // after the Transaction goes to terminated state.
    protected int collectionTime;


    protected String toTag;

    protected String fromTag;

    private boolean terminatedEventDelivered;

    public String getBranchId() {
        return this.branch;
    }

    /**
     * The linger timer is used to remove the transaction from the transaction
     * table after it goes into terminated state. This allows connection caching
     * and also takes care of race conditins.
     *
     *
     */
    class LingerTimer extends SIPStackTimerTask {

        public LingerTimer() {
            SIPTransaction sipTransaction = SIPTransaction.this;
            if (log.isDebugEnabled()) {
                log.debug("LingerTimer : "
                        + sipTransaction.getTransactionId());
            }

        }

        protected void runTask() {
            if (log.isDebugEnabled()) {
                log.debug("LingerTimer: run() : "
                        + getTransactionId());
            }

            // release the connection associated with this transaction.
            SIPTransaction transaction = SIPTransaction.this;
            transaction.getSIPStack().removeTransaction(SIPTransaction.this);
        }
    }

    /**
     * Transaction constructor.
     *
     * @param stack
     *            Parent stack for this transaction.
     */
    protected SIPTransaction(
            SIPTransactionStack stack,
            String peerAddress,
            int peerPort,
            ListeningPointImpl listeningPoint) {

        sipStack = stack;
        this.semaphore = new Semaphore(1);

        this.listeningPoint = listeningPoint;
        this.peerPort = peerPort;
        this.peerAddress = peerAddress;

        disableRetransmissionTimer();
        disableTimeoutTimer();

        // Always add the parent stack as a listener
        // of this transaction
        addEventListener(stack);
    }

    /**
     * Sets the request message that this transaction handles.
     *
     * @param newOriginalRequest
     *            Request being handled.
     */
    public void setOriginalRequest(SIPRequest newOriginalRequest) {

        // Branch value of topmost Via header
        String newBranch;

        if (this.originalRequest != null
                && (!this.originalRequest.getTransactionId().equals(
                newOriginalRequest.getTransactionId()))) {
            sipStack.removeTransactionHash(this);
        }
        // This will be cleared later.

        this.originalRequest = newOriginalRequest;

        // just cache the control information so the
        // original request can be released later.
        this.method = newOriginalRequest.getMethod();
        this.from = (From) newOriginalRequest.getFrom();
        this.to = (To) newOriginalRequest.getTo();
        // Save these to avoid concurrent modification exceptions!
        this.toTag = this.to.getTag();
        this.fromTag = this.from.getTag();
        this.callId = (CallID) newOriginalRequest.getCallId();
        this.cSeq = newOriginalRequest.getCSeq().getSeqNumber();
        this.event = (Event) newOriginalRequest.getHeader("Event");
        this.transactionId = newOriginalRequest.getTransactionId();

        originalRequest.setTransaction(this);

        // If the message has an explicit branch value set,
        newBranch = ((Via) newOriginalRequest.getViaHeaders().getFirst())
                .getBranch();
        if (newBranch != null) {
            if (log.isDebugEnabled())
                log.debug("Setting Branch id : " + newBranch);

            // Override the default branch with the one
            // set by the message
            setBranch(newBranch);

        } else {
            if (log.isDebugEnabled())
                log.debug("Branch id is null - compute TID!"
                        + newOriginalRequest.encode());
            setBranch(newOriginalRequest.getTransactionId());
        }
    }

    /**
     * Gets the request being handled by this transaction.
     *
     * @return -- the original Request associated with this transaction.
     */
    public SIPRequest getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Get the original request but cast to a Request structure.
     *
     * @return the request that generated this transaction.
     */
    public Request getRequest() {
        return originalRequest;
    }

    /**
     * Returns a flag stating whether this transaction is for an INVITE request
     * or not.
     *
     * @return -- true if this is an INVITE request, false if not.
     */
    public final boolean isInviteTransaction() {
        return getMethod().equals(Request.INVITE);
    }

    /**
     * Return a flag that states if this is a BYE transaction.
     *
     * @return true if the transaciton is a BYE transaction.
     */
    public final boolean isByeTransaction() {
        return getMethod().equals(Request.BYE);
    }

    /**
     * Sets the Via header branch parameter used to identify this transaction.
     *
     * @param newBranch
     *            New string used as the branch for this transaction.
     */
    public final void setBranch(String newBranch) {
        branch = newBranch;
    }

    /**
     * Gets the current setting for the branch parameter of this transaction.
     *
     * @return Branch parameter for this transaction.
     */
    public final String getBranch() {
        if (this.branch == null) {
            this.branch = getOriginalRequest().getTopmostVia().getBranch();
        }
        return branch;
    }

    /**
     * Get the method of the request used to create this transaction.
     *
     * @return the method of the request for the transaction.
     */
    public final String getMethod() {
        return this.method;
    }

    /**
     * Get the Sequence number of the request used to create the transaction.
     *
     * @return the cseq of the request used to create the transaction.
     */
    public final long getCSeq() {
        return this.cSeq;
    }

    /**
     * Changes the state of this transaction.
     *
     * @param newState
     *            New state of this transaction.
     */
    public void setState(TransactionState newState) {
        currentState.set(newState);
        if (log.isDebugEnabled()) {
            log.debug("Transaction:setState " + newState
                    + " " + this + " branchID = " + this.getBranch()
                    + " isClient = " + (this instanceof SIPClientTransaction));
        }
    }

    /**
     * Gets the current state of this transaction.
     *
     * @return Current state of this transaction.
     */
    public TransactionState getState() {
        return this.currentState.get();
    }

    /**
     * Enables retransmission timer events for this transaction to begin in one
     * tick.
     */
    protected final void enableRetransmissionTimer() {
        enableRetransmissionTimer(1);
    }

    /**
     * Enables retransmission timer events for this transaction to begin after
     * the number of ticks passed to this routine.
     *
     * @param tickCount
     *            Number of ticks before the next retransmission timer event
     *            occurs.
     */
    protected final void enableRetransmissionTimer(int tickCount) {
        if (log.isDebugEnabled())
            log.debug("enableRetransmissionTimer(" + tickCount + ") entered for " + this);

        synchronized(retransmissionTimerLock) {
            retransmissionTimerTicksLeft = Math.min(tickCount,
                    MAXIMUM_RETRANSMISSION_TICK_COUNT);
            retransmissionTimerLastTickCount = retransmissionTimerTicksLeft;
        }
    }

    /**
     * Turns off retransmission events for this transaction.
     */
    protected final void disableRetransmissionTimer() {
        if (log.isDebugEnabled())
            log.debug("disableRetransmissionTimer() entered for " + this);
        synchronized(retransmissionTimerLock) {
            retransmissionTimerTicksLeft = -1;
        }
    }

    /**
     * Enables a timeout event to occur for this transaction after the number of
     * ticks passed to this method.
     *
     * @param tickCount
     *            Number of ticks before this transaction times out.
     */
    protected final void enableTimeoutTimer(int tickCount) {
        if (log.isDebugEnabled())
            log.debug("enableTimeoutTimer() entered for " + this
                    + " tickCount " + tickCount + " currentTickCount = "
                    + timeoutTimerTicksLeft);

        timeoutTimerTicksLeft = tickCount;
    }

    /**
     * Disabled the timeout timer.
     */
    protected final void disableTimeoutTimer() {
        timeoutTimerTicksLeft = -1;
    }

    /**
     * Fired after each timer tick. Checks the retransmission and timeout timers
     * of this transaction, and fired these events if necessary.
     */
    final synchronized void fireTimer() {

        // If the timeout timer is enabled,
        if (timeoutTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--timeoutTimerTicksLeft == 0) {

                if (log.isDebugEnabled()) {
                    log.debug("fireTimer() caused TIMEOUT for" + this);
                }

                // Fire the timeout timer
                fireTimeoutTimer();
            }

        }
        
        // If the retransmission timer is enabled,
        if (retransmissionTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--retransmissionTimerTicksLeft == 0) {
                // Enable this timer to fire again after
                // twice the original time
                enableRetransmissionTimer(retransmissionTimerLastTickCount * 2);

                if (log.isDebugEnabled()) {
                    log.debug("fireTimer() caused RETRANSMISSION for" + this +
                            " retransmissionTimerTicksLeft=" +
                            retransmissionTimerTicksLeft +
                            " retransmissionTimerLastTickCount=" +
                            retransmissionTimerLastTickCount);
                }
                // Fire the timeout timer
                fireRetransmissionTimer();
            }
        }
    }
    
    /**
     * Fired after each timer tick. Checks the retransmission and timeout timers
     * of this transaction, and fired these events if necessary.
     */
    final synchronized void fireTimeoutTimerTick() {

        // If the timeout timer is enabled,
        if (timeoutTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--timeoutTimerTicksLeft == 0) {

                if (log.isDebugEnabled()) {
                    log.debug("fireTimer() caused TIMEOUT for" + this);
                }

                // Fire the timeout timer
                fireTimeoutTimer();
            }

        }

    }

    /**
     * Fired after each timer tick. Checks the retransmission and timeout timers
     * of this transaction, and fired these events if necessary.
     */
    final synchronized void fireRetransmissionTimerTick() {

        // If the retransmission timer is enabled,
        if (retransmissionTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--retransmissionTimerTicksLeft == 0) {
                // Enable this timer to fire again after
                // twice the original time
                enableRetransmissionTimer(retransmissionTimerLastTickCount * 2);

                if (log.isDebugEnabled()) {
                    log.debug("fireTimer() caused RETRANSMISSION for" + this +
                            " retransmissionTimerTicksLeft=" +
                            retransmissionTimerTicksLeft +
                            " retransmissionTimerLastTickCount=" +
                            retransmissionTimerLastTickCount);
                }
                // Fire the timeout timer
                fireRetransmissionTimer();
            }
        }
    }

    /**
     * Tests if this transaction has terminated.
     *
     * @return Trus if this transaction is terminated, false if not.
     */
    public final boolean isTerminated() {
        return getState() == TERMINATED_STATE;
    }

    public ListeningPointImpl getListeningPoint() {
        return listeningPoint;
    }

    public SIPTransactionStack getSIPStack() {
        return sipStack;
    }

    public String getPeerAddress() {
        return this.peerAddress;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public boolean isReliable() {
        return MessageChannel.isReliable(listeningPoint.getTransport());
    }

    /**
     * Adds a new event listener to this transaction.
     *
     * @param newListener
     *            Listener to add.
     */
    public void addEventListener(SIPTransactionEventListener newListener) {
        eventListeners.add(newListener);
    }

    /**
     * Creates a SIPTransactionErrorEvent and sends it to all of the listeners
     * of this transaction. This method also flags the transaction as
     * terminated.
     *
     * @param errorEventID
     *            ID of the error to raise.
     */
    protected void raiseErrorEvent(int errorEventID) {

        // Create the error event
        SIPTransactionErrorEvent newErrorEvent =
                new SIPTransactionErrorEvent(this, errorEventID);

        // Loop through all listeners of this transaction
        synchronized (eventListeners) {
            for (SIPTransactionEventListener eventListener : eventListeners) {
                // Send the event to the next listener
                if (log.isDebugEnabled())
                    log.debug("Calling listener for a new transaction " +
                            "error event. Listener=" + eventListener);
                eventListener.transactionErrorEvent(newErrorEvent);
            }
        }
        // Clear the event listeners after propagating the error.
        // Retransmit notifications are just an alert to the
        // application (they are not an error).
        if (errorEventID != SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT) {
            eventListeners.clear();

            // Errors always terminate a transaction
            this.setState(TransactionState.TERMINATED);

            if (this instanceof SIPServerTransaction && this.isByeTransaction()
                    && this.getDialog() != null)
                ((SIPDialog) this.getDialog()).setState(SIPDialog.TERMINATED_STATE);
        }
    }

    /**
     * Creates a SIPTransactionErrorEvent and sends it to all of the listeners
     * of this transaction. The transaction is NOT flagged as terminated.
     *
     * @param errorEventID
     *            ID of the error to raise.
     */
    protected void raiseErrorEventWithoutTerminatingTransaction(int errorEventID) {

        // Create the error event
        SIPTransactionErrorEvent newErrorEvent =
                new SIPTransactionErrorEvent(this, errorEventID);

        // Loop through all listeners of this transaction
        synchronized (eventListeners) {
            for (SIPTransactionEventListener eventListener : eventListeners) {
                // Send the event to the next listener
                if (log.isDebugEnabled())
                    log.debug("Calling listener for a new transaction " +
                            "error event. Listener=" + eventListener);
                eventListener.transactionErrorEvent(newErrorEvent);
            }
        }
    }

    /**
     * A shortcut way of telling if we are a server transaction.
     */
    protected boolean IsServerTransaction() {
        return this instanceof SIPServerTransaction;
    }

    /**
     * Gets the dialog object of this Transaction object. This object returns
     * null if no dialog exists. A dialog only exists for a transaction when a
     * session is setup between a User Agent Client and a User Agent Server,
     * either by a 1xx Provisional Response for an early dialog or a 200OK
     * Response for a committed dialog.
     *
     * @return the Dialog Object of this Transaction object.
     * @see Dialog
     */
    public abstract Dialog getDialog();

    /**
     * set the dialog object.
     *
     * @param sipDialog --
     *            the dialog to set.
     * @param dialogId --
     *            the dialog id ot associate with the dialog.s
     */
    public abstract void setDialog(SIPDialog sipDialog, String dialogId);

    /**
     * Returns the current value of the retransmit timer in milliseconds used to
     * retransmit messages over unreliable transports.
     *
     * @return the integer value of the retransmit timer in milliseconds.
     */
    public int getRetransmitTimer() {
        return sipStack.getBaseTimerInterval();
    }

    /**
     * Get the last response. This is used internally by the implementation.
     * Dont rely on it.
     *
     * @return the last response received (for client transactions) or sent (for
     *         server transactions).
     */
    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    /**
     * Get the transaction Id.
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    /**
     * Hashcode method for fast hashtable lookup.
     */
    public int hashCode() {
        if (this.transactionId == null)
            return -1;
        else
            return this.transactionId.hashCode();
    }

    /**
     * A method that can be used to test if an incoming request belongs to this
     * transction. This does not take the transaction state into account when
     * doing the check otherwise it is identical to isMessagePartOfTransaction.
     * This is useful for checking if a CANCEL belongs to this transaction.
     *
     * @param requestToTest
     *            is the request to test.
     * @return true if the the request belongs to the transaction.
     *
     */
    public boolean doesCancelMatchTransaction(SIPRequest requestToTest) {

        // List of Via headers in the message to test
        ViaList viaHeaders;
        // Topmost Via header in the list
        Via topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;

        transactionMatches = false;

        if (this.getOriginalRequest() == null
                || this.getOriginalRequest().getMethod().equals(
                Request.CANCEL)) {
            if (log.isDebugEnabled())
                log.debug("Cancel does not match transaction. OriginalRequest=" +
                        this.getOriginalRequest());
            return false;
        }

        // Get the topmost Via header and its branch parameter
        viaHeaders = requestToTest.getViaHeaders();
        if (viaHeaders != null) {

            topViaHeader = (Via) viaHeaders.getFirst();
            messageBranch = topViaHeader.getBranch();
            if (messageBranch != null) {

                // If the branch parameter exists but
                // does not start with the magic cookie,
                if (!messageBranch.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE)) {

                    // Flags this as old
                    // (RFC2543-compatible) client
                    // version
                    messageBranch = null;

                }

            }

            // If a new branch parameter exists,
            if (messageBranch != null && this.getBranch() != null) {

                // If the branch equals the branch in
                // this message,
                if (getBranch().equalsIgnoreCase(messageBranch)
                        && topViaHeader.getSentBy().equals(
                        ((Via) getOriginalRequest().getViaHeaders()
                                .getFirst()).getSentBy())) {
                    transactionMatches = true;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Branches does not match.");
                }

            } else {
                // If this is an RFC2543-compliant message,
                // If RequestURI, To tag, From tag,
                // CallID, CSeq number, and top Via
                // headers are the same,
                if (log.isDebugEnabled())
                    log.debug("testing against "
                            + getOriginalRequest());

                if (getOriginalRequest().getRequestURI().equals(
                        requestToTest.getRequestURI())
                        && getOriginalRequest().getTo().equals(
                        requestToTest.getTo())
                        && getOriginalRequest().getFrom().equals(
                        requestToTest.getFrom())
                        && getOriginalRequest().getCallId().getCallId().equals(
                        requestToTest.getCallId().getCallId())
                        && getOriginalRequest().getCSeq()
                        .getSeqNumber() == requestToTest
                        .getCSeq().getSeqNumber()
                        && topViaHeader.equals(getOriginalRequest()
                        .getViaHeaders().getFirst())) {

                    transactionMatches = true;
                }

            }

        }

        // JvB: Need to pass the CANCEL to the listener! Retransmitted INVITEs
        // set it to false
        if (transactionMatches) {
            this.setPassToListener();
        }
        return transactionMatches;
    }

    /**
     * Sets the value of the retransmit timer to the newly supplied timer value.
     * The retransmit timer is expressed in milliseconds and its default value
     * is 500ms. This method allows the application to change the transaction
     * retransmit behavior for different networks. Take the gateway proxy as an
     * example. The internal intranet is likely to be reatively uncongested and
     * the endpoints will be relatively close. The external network is the
     * general Internet. This functionality allows different retransmit times
     * for either side.
     *
     * @param retransmitTimer -
     *            the new integer value of the retransmit timer in milliseconds.
     */
    public void setRetransmitTimer(int retransmitTimer) {
        throw new UnsupportedOperationException("Feature not supported");
    }

    /**
     * Set the application data pointer. This is un-interpreted by the stack.
     * This is provided as a conveniant way of keeping book-keeping data for
     * applications. Note that null clears the application data pointer
     * (releases it).
     *
     * @param applicationData --
     *            application data pointer to set. null clears the applicationd
     *            data pointer.
     *
     */

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    /**
     * Get the application data associated with this transaction.
     *
     * @return stored application data.
     */
    public Object getApplicationData() {
        return this.applicationData;
    }

    /**
     * Return the SipProvider for which the transaction is assigned.
     *
     * @return the SipProvider for the transaction.
     */
    public SipProviderImpl getSipProvider() {
        return this.getListeningPoint().getSipProvider();
    }

    /**
     * Raise an IO Exception event - this is used for reporting asynchronous IO
     * Exceptions that are attributable to this transaction.
     *
     */
    public void raiseIOExceptionEvent() {
        setState(TransactionState.TERMINATED);
        String host = getPeerAddress();
        int port = getPeerPort();
        String transport = getListeningPoint().getTransport();
        IOExceptionEvent exceptionEvent = new IOExceptionEvent(this, host,
                port, transport);
        getSipProvider().handleEvent(exceptionEvent, this);
    }

    /**
     * A given tx can process only a single outstanding event at a time. This
     * semaphore gaurds re-entrancy to the transaction.
     *
     */
    public boolean acquireSem() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("acquireSem [[[[" + this);
            }
            return this.semaphore.tryAcquire(10000L, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return false;
        } finally {
            this.isSemaphoreAquired = true;
        }

    }

    /**
     * Release the transaction semaphore.
     *
     */
    public void releaseSem() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("releaseSem [[[[" + this);
            }
            this.toListener = false;
            this.isSemaphoreAquired = false;
            this.semaphore.release();

        } catch (Exception ex) {
            log.warn("Exception in releaseSem()",ex);
        }

    }

    /**
     * Set true to pass the request up to the listener. False otherwise.
     *
     */

    public boolean passToListener() {
        return toListener;
    }

    /**
     * Set the passToListener flag to true.
     */
    public void setPassToListener() {
        if (log.isDebugEnabled()) {
            log.debug("setPassToListener()");
        }
        this.toListener = true;

    }

    /**
     * Flag to test if the terminated event is delivered.
     *
     */
    protected synchronized boolean testAndSetTransactionTerminatedEvent() {
        boolean retval = !this.terminatedEventDelivered;
        this.terminatedEventDelivered = true;
        return retval;
    }


    /**
     * Start the timer that runs the transaction state machine.
     *
     */

    protected abstract void startTransactionTimer();

    /**
     * Tests a message to see if it is part of this transaction.
     *
     * @return True if the message is part of this transaction, false if not.
     */
    public abstract boolean isMessagePartOfTransaction(SIPMessage messageToTest);

    /**
     * This method is called when this transaction's retransmission timer has
     * fired.
     */
    protected abstract void fireRetransmissionTimer();

    /**
     * This method is called when this transaction's timeout timer has fired.
     */
    protected abstract void fireTimeoutTimer();

}
