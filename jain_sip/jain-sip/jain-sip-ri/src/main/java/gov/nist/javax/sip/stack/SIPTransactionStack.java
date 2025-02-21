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

import gov.nist.javax.sip.DefaultAddressResolver;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import gov.nist.core.net.AddressResolver;
import gov.nist.core.net.DefaultNetworkLayer;
import gov.nist.core.net.NetworkLayer;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.*;

import org.apache.log4j.Logger;

/*
 * Jeff Keyser : architectural suggestions and contributions. Pierre De Rop and
 * Thomas Froment : Bug reports. Jeyashankher < jai@lucent.com > : bug reports.
 *
 *
 */

/**
 *
 * This is the sip stack. It is essentially a management interface. It manages
 * the resources for the JAIN-SIP implementation. This is the structure that is
 * wrapped by the SipStackImpl.
 *
 * @see gov.nist.javax.sip.SipStackImpl
 *
 * @author M. Ranganathan <br/>
 *
 * @version 1.2 $Revision: 1.62 $ $Date: 2006/11/23 17:26:58 $
 */
public abstract class SIPTransactionStack implements
        SIPTransactionEventListener {

    private static final Logger log = Logger.getLogger(SIPTransactionStack.class);

    /*
     * Number of milliseconds between timer ticks (500).
     */
    public static final int BASE_TIMER_INTERVAL = 500;

    /*
     * Connection linger time (seconds) this is the time (in seconds) for which
     * we linger the TCP connection before closing it.
     */
    public static final int CONNECTION_LINGER_TIME = 8;

    // Timers
    /**
     * One timer tick.
     */
    private static final int T1 = 1;

    /**
     * The maximum retransmit interval for non-INVITE
     * requests and INVITE responses
     */
    private final AtomicInteger T2 = new AtomicInteger(8);

    /**
     * 5 sec Maximum duration a message will remain in the network
     */
    private final AtomicInteger T4 = new AtomicInteger(10);


    /**
     * INVITE transaction  timeout timer
     */
    private final AtomicInteger timerB = new AtomicInteger(64);

    /**
     * Proxy INVITE transaction  timeout timer
     */
    private final AtomicInteger timerC = new AtomicInteger(360);

    /**
     * Wait time for response retransmits
     */
    private final AtomicInteger timerD = new AtomicInteger(64);

    /**
     * Non-INVITE transaction timeout timer
     */
    private final AtomicInteger timerF = new AtomicInteger(64);

    /**
     * Wait time for ACK receipt
     */
    private final AtomicInteger timerH = new AtomicInteger(64);

    /**
     * Wait time for ACK retransmits
     */
    private final AtomicInteger timerI;

    /**
     * Wait time for non-INVITE request retransmits
     */
    private final AtomicInteger timerJ = new AtomicInteger(64);

    /**
     * Wait time for response retransmits
     */
    private final AtomicInteger timerK;

    // Client initiated connection timeout in seconds
    private final AtomicInteger clientConnectionTimeout =
            new AtomicInteger(300);

    // Server initiated connection timeout in seconds
    private final AtomicInteger serverConnectionTimeout =
            new AtomicInteger(64);

    /*
     * Table of retransmission Alert timers.
     */
    protected final ConcurrentHashMap<String, SIPServerTransaction>
            retransmissionAlertTransactions;

    // Table of dialogs.
    protected final ConcurrentHashMap<String, SIPDialog> dialogTable =
            new ConcurrentHashMap<String, SIPDialog>();

    // A set of methods that result in dialog creations.
    protected HashSet<String> dialogCreatingMethods;

    // Global timer. Use this for all timer tasks.
    protected Timer timer;

    // List of pending server transactions
    private final ConcurrentHashMap<String, SIPServerTransaction>
            pendingTransactions;

    // hashtable for fast lookup
    private final ConcurrentHashMap<String, SIPTransaction>
            clientTransactionTable;

    // Set to false if you want hiwat and lowat to be consulted.
    private boolean unlimitedTableSize = false;

    // High water mark for ServerTransaction Table
    // after which requests are dropped.
    protected int serverTransactionTableHighwaterMark = 5000;

    // Low water mark for Server Tx table size after which
    // requests are selectively dropped
    protected int serverTransactionTableLowaterMark = 4000;

    // Hashtable for server transactions.
    private final ConcurrentHashMap<String, SIPTransaction>
            serverTransactionTable;

    // A table of ongoing transactions indexed by mergeId ( for detecting merged
    // requests.
    private final ConcurrentHashMap<String, SIPServerTransaction>
            mergeTable;

    /*
     * A wrapper around log4j to help log debug.
     */
    protected LogWriter logWriter;

    /*
     * ServerLog is used just for logging stack message tracecs.
     */
    protected ServerLog serverLog;

    /*
     * We support UDP on this stack.
     */
    boolean udpFlag;

    /*
     * Internal router. Use this for all sip: request routing.
     *
     */
    protected DefaultRouter defaultRouter;

    /*
     * Global flag that turns logging off
     */
    protected boolean needsLogging;

    /*
     * Flag used for testing TI, bypasses filtering of ACK to non-2xx
     */
    protected boolean non2XXAckPassedToListener;

    /*
     * Class that handles caching of TCP/TLS connections.
     */
    protected IOHandler ioHandler;

    /*
     * Flag that indicates that the stack is active.
     */
    protected boolean toExit;

    /*
     * Name of the stack.
     */
    protected String stackName;

    /*
     * IP address of stack -- this can be re-written by stun.
     *
     * @deprecated
     */
    protected String stackAddress;

    /*
     * INET address of stack (cached to avoid repeated lookup)
     *
     * @deprecated
     */
    protected InetAddress stackInetAddress;

    /*
     * Router to determine where to forward the request.
     */
    protected javax.sip.address.Router router;

    /*
     * Number of pre-allocated threads for processing udp messages. -1 means no
     * preallocated threads ( dynamically allocated threads).
     */
    protected int threadPoolSize;

    /*
     * max number of simultaneous connections.
     */
    protected int maxConnections;

    /*
     * Close accept socket on completion.
     */
    protected boolean cacheServerConnections;

    /*
     * Close connect socket on Tx termination.
     */
    protected boolean cacheClientConnections;

    /*
     * Use the user supplied router for all out of dialog requests.
     */
    protected boolean useRouterForAll;

    /*
     * Max size of message that can be read from a TCP connection.
     */
    protected int maxContentLength;

    /*
     * Max # of headers that a SIP message can contain.
     */
    protected int maxMessageSize;

    /*
     * A collection of message processors.
     */
    private final Collection<MessageProcessor> messageProcessors =
            new ArrayList<MessageProcessor>() ;

    /*
     * Read timeout on TCP incoming sockets -- defines the time between reads
     * for after delivery of first byte of message.
     */
    protected int readTimeout;

    /*
     * The socket factory. Can be overriden by applications that want direct
     * access to the underlying socket.
     */
    protected NetworkLayer networkLayer;

    /*
     * Outbound proxy String ( to be handed to the outbound proxy class on
     * creation).
     */
    protected String outboundProxy;

    protected String routerPath;

    // Flag to indicate whether the stack will provide dialog
    // support.
    protected boolean isAutomaticDialogSupportEnabled;

    // The set of events for which subscriptions can be forked.
    protected HashSet<String> forkedEvents;

    // Generate a timestamp header for retransmitted requests.
    protected boolean generateTimeStampHeader;

    protected AddressResolver addressResolver;

    // Max time that the listener is allowed to take to respond to a
    // request. Default is "infinity". This property allows
    // containers to defend against buggy clients (that do not
    // want to respond to requests).
    protected int maxListenerResponseTime;

    /*
     * Flag to indicate whether the stack will delegate the TLS encryption/decryption
     * to external hardware.
     */
    protected boolean useTlsAccelerator;

    /// Provides a mechanism for applications to check the health of threads in the stack
    protected ThreadAuditor threadAuditor = new ThreadAuditor();

    /// Timer to regularly ping the thread auditor (on behalf of the timer thread)
    class PingTimer extends SIPStackTimerTask {
        /// Timer thread handle
        ThreadAuditor.ThreadHandle threadHandle;

        /// Constructor
        public PingTimer(ThreadAuditor.ThreadHandle a_oThreadHandle) {
            threadHandle = a_oThreadHandle;
        }

        protected void runTask() {
            // Check if we still have a timer (it may be null after shutdown)
            if (timer != null) {
                // Register the timer task if we haven't done so
                if (threadHandle == null) {
                    // This happens only once since the thread handle is passed to the next scheduled ping timer
                    threadHandle = getThreadAuditor().addCurrentThread();
                }

                // Let the thread auditor know that the timer task is alive
                threadHandle.ping();

                // Schedule the next ping
                timer.schedule(new PingTimer(threadHandle), threadHandle.getPingIntervalInMillisecs());
            }
        }

    }

    /**
     * Default constructor.
     */
    protected SIPTransactionStack() {
        this.timerI = new AtomicInteger(T4.get());
        this.timerK = new AtomicInteger(T4.get());

        this.toExit = false;

        this.forkedEvents = new HashSet<String>();
        // set of events for which subscriptions can be forked.
        // Set an infinite thread pool size.
        this.threadPoolSize = -1;
        // Close response socket after infinte time.
        // for max performance
        this.cacheServerConnections = true;
        // Close the request socket after infinite time.
        // for max performance
        this.cacheClientConnections = true;
        // Max number of simultaneous connections.
        this.maxConnections = -1;

        // The read time out is infinite.
        this.readTimeout = -1;

        this.maxListenerResponseTime = -1;

        // a set of methods that result in dialog creation.
        this.dialogCreatingMethods = new HashSet<String>();
        // Standard set of methods that create dialogs.
        this.dialogCreatingMethods.add(Request.REFER);
        this.dialogCreatingMethods.add(Request.INVITE);
        this.dialogCreatingMethods.add(Request.SUBSCRIBE);
        // The default (identity) address lookup scheme

        this.addressResolver = new DefaultAddressResolver();

        // Notify may or may not create a dialog. This is handled in
        // the code.
        // Create the transaction collections

        clientTransactionTable = new ConcurrentHashMap<String, SIPTransaction>();
        serverTransactionTable = new ConcurrentHashMap<String, SIPTransaction>();
        mergeTable = new ConcurrentHashMap<String, SIPServerTransaction>();
        retransmissionAlertTransactions =
                new ConcurrentHashMap<String, SIPServerTransaction>();

        // Start the timer event thread.

        this.timer = new Timer();
        this.pendingTransactions = new ConcurrentHashMap<String, SIPServerTransaction>();

        if (getThreadAuditor().isEnabled()) {
            // Start monitoring the timer thread
            timer.schedule(new PingTimer(null), 0);
        }
    }

    /**
     * Re Initialize the stack instance.
     */
    protected void reInit() {
        if (log.isDebugEnabled())
            log.debug("Re-initializing !");

        // Array of message processors.
        messageProcessors.clear();

        pendingTransactions.clear();
        clientTransactionTable.clear();
        serverTransactionTable.clear();
        retransmissionAlertTransactions.clear();
        mergeTable.clear();

        // Dialog dable.
        this.dialogTable.clear();

        if (log.isDebugEnabled()) {
            log.debug("Dialog table has been cleared.");
        }

        this.timer = new Timer();

    }

    /**
     * Print the dialog table.
     *
     */
    public void printDialogTable() {
        if (log.isDebugEnabled()) {
            log.debug("dialog table  = " + dialogTable);
        }
    }

    /**
     * Retrieve a transaction from our table of transactions with pending
     * retransmission alerts.
     *
     * @param dialogId
     * @return -- the RetransmissionAlert enabled transaction corresponding to
     *         the given dialog ID.
     */
    public SIPServerTransaction getRetransmissionAlertTransaction(
            String dialogId) {
        return retransmissionAlertTransactions.get(dialogId);
    }

    // Getters and setters for timers

    public int getBaseTimerInterval() {
        return BASE_TIMER_INTERVAL;
    }

    public int getT1() {
        return T1;
    }

    public int getT2() {
        return T2.get();
    }

    protected void setT2(int t2) {
        T2.set(t2);
    }

    public int getT4() {
        return T4.get();
    }

    protected void setT4(int t4) {
        T4.set(t4);
    }

    public int getTimerB() {
        return timerB.get();
    }

    public void setTimerB(int timerB) {
        this.timerB.set(timerB);
    }

    public int getTimerC() {
        return timerC.get();
    }

    public void setTimerC(int timerC) {
        this.timerC.set(timerC);
    }

    public int getTimerD() {
        return timerD.get();
    }

    public void setTimerD(int timerD) {
        this.timerD.set(timerD);
    }

    public int getTimerF() {
        return timerF.get();
    }

    public void setTimerF(int timerF) {
        this.timerF.set(timerF);
    }

    public int getTimerH() {
        return timerH.get();
    }

    public void setTimerH(int timerH) {
        this.timerH.set(timerH);
    }

    public int getTimerI() {
        return timerI.get();
    }

    public void setTimerI(int timerI) {
        this.timerI.set(timerI);
    }

    public int getTimerJ() {
        return timerJ.get();
    }

    public void setTimerJ(int timerJ) {
        this.timerJ.set(timerJ);
    }

    public int getTimerK() {
        return timerK.get();
    }

    public void setTimerK(int timerK) {
        this.timerK.set(timerK);
    }

    /**
     * Get the timeout (in seconds) for removing reliable connections
     * initiated by a remote client.
     * @return timeout
     */
    public int getClientConnectionTimeout() {
        return clientConnectionTimeout.get();
    }

    /**
     * Set the timeout (in seconds) for removing reliable connections
     * initiated by a remote client.
     * @param timeout in seconds, 0 means never timeout
     */
    public void setClientConnectionTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");
        clientConnectionTimeout.set(timeout);
    }

    /**
     * Get the timeout (in seconds) for removing reliable connections
     * initiated by the local server.
     * @return timeout
     */
    public int getServerConnectionTimeout() {
        return serverConnectionTimeout.get();
    }

    /**
     * Set the timeout (in seconds) for removing reliable connections
     * initiated by the local server.
     * @param timeout in seconds, 0 means never timeout
     */
    public void setServerConnectionTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be a positive integer");
        serverConnectionTimeout.set(timeout);
    }



    /**
     * Return true if extension is supported.
     *
     * @return true if extension is supported and false otherwise.
     */
    public boolean isDialogCreated(String method) {

        boolean retval = dialogCreatingMethods.contains(method);
        if (log.isDebugEnabled()) {
            log.debug("isDialogCreated : " + method + " returning " + retval);
        }
        return retval;
    }

    /**
     * Add an extension method.
     *
     * @param extensionMethod --
     *            extension method to support for dialog creation
     */
    public void addExtensionMethod(String extensionMethod) {
        if (extensionMethod.equals(Request.NOTIFY)) {
            if (log.isDebugEnabled())
                log.debug("NOTIFY Supported Natively");
        } else {
            this.dialogCreatingMethods
                    .add(extensionMethod.trim().toUpperCase());
        }
    }

    /**
     * Put a dialog into the dialog table.
     *
     * @param dialog --
     *            dialog to put into the dialog table.
     *
     */
    public void putDialog(SIPDialog dialog) {
        String dialogId = dialog.getDialogId();
        if (dialogTable.containsKey(dialogId)) {
            if (log.isDebugEnabled()) {
                log.debug("putDialog: dialog already exists" + dialogId
                        + " in table = " + dialogTable.get(dialogId));
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("putDialog remoteParty = " +
                    dialog.getRemoteParty().getDisplayName() +
                    ", dialogId=" + dialogId + " dialog = "
                    + dialog);
        }
        dialog.setStack(this);
        dialogTable.put(dialogId, dialog);
    }

    /**
     * Create a dialog and add this transaction to it.
     *
     * @param transaction --
     *            tx to add to the dialog.
     * @return the newly created Dialog.
     */
    public SIPDialog createDialog(SIPTransaction transaction) {
        return new SIPDialog(transaction);
    }

    /**
     * Create a new dialog for a given transaction. This is used when a forked
     * response is receieved. Note that the tx is assigned to multiple dialogs
     * at the same time when this hapens.
     *
     * @since 1.3
     *
     * @param transaction --
     *            the transaction for which we want to create the dialog.
     * @param sipResponse --
     *            the response for which we are creating the dialog.
     * @return the newly created SIP Dialog.
     *
     *
     * public SIPDialog createDialog(SIPTransaction transaction, SIPResponse
     * sipResponse) { SIPDialog retval = new SIPDialog(transaction,
     * sipResponse); return retval; }
     */

    /**
     * This is for debugging.
     */
    public Iterator getDialogs() {
        return dialogTable.values().iterator();

    }

    /**
     * Remove the dialog from the dialog table.
     *
     * @param dialog --
     *            dialog to remove.
     */
    public void removeDialog(SIPDialog dialog) {

        String id = dialog.getDialogId();
        if (id != null) {
            Object old = this.dialogTable.remove(id);

            if (old != null
                    && !dialog.testAndSetIsDialogTerminatedEventDelivered()) {
                DialogTerminatedEvent event = new DialogTerminatedEvent(dialog
                        .getSipProvider(), dialog);

                // Provide notification to the listener that the dialog has
                // ended.
                dialog.getSipProvider().handleEvent(event, null);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("removeDialog remoteParty = " +
                    dialog.getRemoteParty().getDisplayName() +
                    ", dialogId=" + id + " dialog = " + dialog);
        }
    }

    /**
     * Return the dialog for a given dialog ID. If compatibility is enabled then
     * we do not assume the presence of tags and hence need to add a flag to
     * indicate whether this is a server or client transaction.
     *
     * @param dialogId
     *            is the dialog id to check.
     */

    public SIPDialog getDialog(String dialogId) {

        SIPDialog sipDialog = dialogTable.get(dialogId);
        if (log.isDebugEnabled()) {
            log.debug("getDialog(" + dialogId + ") : returning "
                    + sipDialog);
        }
        return sipDialog;

    }

    /**
     * Find a matching client SUBSCRIBE to the incoming notify. NOTIFY requests
     * are matched to such SUBSCRIBE requests if they contain the same
     * "Call-ID", a "To" header "tag" parameter which matches the "From" header
     * "tag" parameter of the SUBSCRIBE, and the same "Event" header field.
     * Rules for comparisons of the "Event" headers are described in section
     * 7.2.1. If a matching NOTIFY request contains a "Subscription-State" of
     * "active" or "pending", it creates a new subscription and a new dialog
     * (unless they have already been created by a matching response, as
     * described above).
     *
     * @param notifyMessage
     * @return -- the matching ClientTransaction with semaphore aquired or null
     *         if no such client transaction can be found.
     */
    public SIPClientTransaction findSubscribeTransaction(
            SIPRequest notifyMessage, ListeningPointImpl listeningPoint) {
        SIPClientTransaction retval = null;
        try {
            Iterator it = clientTransactionTable.values().iterator();
            if (log.isDebugEnabled())
                log.debug("ct table size = "
                        + clientTransactionTable.size());
            String thisToTag = notifyMessage.getTo().getTag();
            if (thisToTag == null) {
                return retval;
            }
            Event eventHdr = (Event) notifyMessage.getHeader(EventHeader.NAME);
            if (eventHdr == null) {
                if (log.isDebugEnabled()) {
                    log.debug("event Header is null -- returning null");
                }

                return retval;
            }
            while (it.hasNext()) {
                SIPClientTransaction ct = (SIPClientTransaction) it.next();
                if (!ct.getMethod().equals(Request.SUBSCRIBE))
                    continue;
                SIPRequest sipRequest = ct.getOriginalRequest();
                Contact contact = sipRequest.getContactHeader();
                Address address = contact.getAddress();
                SipURI uri = (SipURI) address.getURI();
                String host = uri.getHost();
                int port = uri.getPort();
                String transport = uri.getTransportParam();
                if (transport == null)
                    transport = "udp";
                if (port == -1) {
                    if (transport.equals("udp") || transport.equals("tcp"))
                        port = 5060;
                    else
                        port = 5061;
                }
                // if ( sipProvider.getListeningPoint(transport) == null)
                String fromTag = ct.from.getTag();
                Event hisEvent = ct.event;
                // Event header is mandatory but some slopply clients
                // dont include it.
                if (hisEvent == null)
                    continue;
                if (log.isDebugEnabled()) {
                    log.debug("ct.fromTag = " + fromTag);
                    log.debug("thisToTag = " + thisToTag);
                    log.debug("hisEvent = " + hisEvent);
                    log.debug("eventHdr " + eventHdr);
                }

                // Check that the NOTIFY is directed at the contact address
                // specified by the SUBSCRIBE ( this is to prevent spurious
                // NOTOFY's
                if (listeningPoint.getPort() == port
                        && listeningPoint.getIPAddress().equals(host)
                        && fromTag.equalsIgnoreCase(thisToTag)
                        && hisEvent != null
                        && eventHdr.match(hisEvent)
                        && notifyMessage.getCallId().getCallId()
                        .equalsIgnoreCase(ct.callId.getCallId())) {
                    if (ct.acquireSem())
                        retval = ct;
                    return retval;
                }
            }

            return retval;
        } finally {
            if (log.isDebugEnabled())
                log.debug("findSubscribeTransaction : returning " + retval);
        }

    }

    /**
     * Find the transaction corresponding to a given request.
     *
     * @param sipMessage
     *            request for which to retrieve the transaction.
     *
     * @param isServer
     *            search the server transaction table if true.
     *
     * @return the transaction object corresponding to the request or null if no
     *         such mapping exists.
     */
    public SIPTransaction findTransaction(SIPMessage sipMessage,
                                          boolean isServer) {
        SIPTransaction retval;

        if (isServer) {
            Via via = sipMessage.getTopmostVia();
            if (via.getBranch() != null) {
                String key = sipMessage.getTransactionId();

                retval = serverTransactionTable.get(key);
                if (log.isDebugEnabled())
                    log.debug("serverTx: looking for key " + key);
                if (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))
                    return retval;

            }
            // Need to scan the table for old style transactions (RFC 2543
            // style)
            for (SIPTransaction sipTransaction : serverTransactionTable.values()) {
                SIPServerTransaction sipServerTransaction =
                        (SIPServerTransaction) sipTransaction;
                if (sipServerTransaction.isMessagePartOfTransaction(sipMessage))
                    return sipServerTransaction;
            }

        } else {
            Via via = sipMessage.getTopmostVia();
            if (via.getBranch() != null) {
                String key = sipMessage.getTransactionId();
                if (log.isDebugEnabled())
                    log.debug("clientTx: looking for key " + key);
                retval = clientTransactionTable.get(key);
                if (key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))
                    return retval;

            }
            // Need to scan the table for old style transactions (RFC 2543
            // style). This is terribly slow but we need to do this
            // for backasswords compatibility.
            for (SIPTransaction sipTransaction : clientTransactionTable.values()) {
                SIPClientTransaction clientTransaction = (SIPClientTransaction) sipTransaction;
                if (clientTransaction.isMessagePartOfTransaction(sipMessage))
                    return clientTransaction;
            }

        }
        return null;

    }

    /**
     * Get the transaction to cancel. Search the server transaction table for a
     * transaction that matches the given transaction.
     */
    public SIPTransaction findCancelTransaction(SIPRequest cancelRequest,
                                                boolean isServer) {

        if (log.isDebugEnabled()) {
            log.debug("findCancelTransaction request= \n"
                    + cancelRequest + "\nfindCancelRequest isServer="
                    + isServer);
        }

        if (isServer) {
            for (SIPTransaction sipTransaction : this.serverTransactionTable.values()) {
                SIPServerTransaction sipServerTransaction =
                        (SIPServerTransaction) sipTransaction;
                if (sipServerTransaction
                        .doesCancelMatchTransaction(cancelRequest))
                    return sipServerTransaction;
            }

        } else {
            for (SIPTransaction sipTransaction : this.clientTransactionTable.values()) {
                SIPClientTransaction sipClientTransaction =
                        (SIPClientTransaction) sipTransaction;
                if (sipClientTransaction
                        .doesCancelMatchTransaction(cancelRequest))
                    return sipClientTransaction;

            }

        }
        if (log.isDebugEnabled()) {
            log.debug("Could not find transaction for cancel request");
        }
        return null;
    }

    /**
     * Finds a pending server transaction. Since each request may be handled
     * either statefully or statelessly, we keep a map of pending transactions
     * so that a duplicate transaction is not created if a second request is
     * recieved while the first one is being processed.
     *
     * @param requestReceived
     * @return -- the pending transaction or null if no such transaction exists.
     */
    public SIPServerTransaction findPendingTransaction(
            SIPRequest requestReceived) {
        if (log.isDebugEnabled()) {
            log.debug("looking for pending tx for :"
                    + requestReceived.getTransactionId());
        }
        return pendingTransactions.get(requestReceived.getTransactionId());

    }

    /**
     * See if there is a pending transaction with the same Merge ID as the Merge
     * ID obtained from the SIP Request. The Merge table is for handling the
     * following condition: If the request has no tag in the To header field,
     * the UAS core MUST check the request against ongoing transactions. If the
     * From tag, Call-ID, and CSeq exactly match those associated with an
     * ongoing transaction, but the request does not match that transaction
     * (based on the matching rules in Section 17.2.3), the UAS core SHOULD
     * generate a 482 (Loop Detected) response and pass it to the server
     * transaction.
     */
    public SIPServerTransaction findMergedTransaction(SIPRequest sipRequest) {
        String mergeId = sipRequest.getMergeId();
        if (mergeId != null) {
            return this.mergeTable.get(mergeId);
        } else {
            return null;
        }
    }

    /**
     * Remove a pending Server transaction from the stack. This is called after
     * the user code has completed execution in the listener.
     *
     * @param tr --
     *            pending transaction to remove.
     */
    public void removePendingTransaction(SIPServerTransaction tr) {
        if (log.isDebugEnabled()) {
            log.debug("removePendingTx: " + tr.getTransactionId());
        }
        this.pendingTransactions.remove(tr.getTransactionId());

    }

    /**
     * Remove a transaction from the merge table.
     *
     * @param tr -- the server transaction to remove from the merge table.
     *
     */
    public void removeFromMergeTable(SIPServerTransaction tr) {
        if (log.isDebugEnabled()) {
            log.debug("Removing tx from merge table ");
        }
        String key = ((SIPRequest) tr.getRequest()).getMergeId();
        if (key != null) {
            this.mergeTable.remove(key);
        }
    }

    /**
     * Map a Server transaction (possibly sending out a 100 if the server tx is
     * an INVITE). This actually places it in the hash table and makes it known
     * to the stack.
     *
     * @param transaction --
     *            the server transaction to map.
     */
    public void mapTransaction(SIPServerTransaction transaction) {
        if (transaction.isMapped)
            return;
        addTransactionHash(transaction);
        transaction.startTransactionTimer();
        transaction.isMapped = true;
    }

    /**
     * Handles a new SIP request. It finds a server transaction to handle this
     * message. If none exists, it creates a new transaction.
     *
     * @param request
     *            Request to handle.
     * @param messageChannel
     *            Channel that received message.
     *
     * @return A server transaction.
     */
    public SIPServerTransaction getSIPServerTransactionForRequest(
            SIPRequest request, MessageChannel messageChannel) {

        String key = request.getTransactionId();

        // Make sure the request contains the message channel that it was
        // received on
        request.setMessageChannel(messageChannel);

        SIPServerTransaction currentTransaction =
                (SIPServerTransaction)serverTransactionTable.get(key);

        // Got to do this for backwards compatibility.
        if (currentTransaction == null ||
                !currentTransaction.isMessagePartOfTransaction(request)) {
            currentTransaction = null;

            if (!key.toLowerCase().startsWith(
                    SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                // Loop through all server transactions

                for (SIPTransaction s : serverTransactionTable.values()) {
                    SIPServerTransaction nextTransaction = (SIPServerTransaction)s;

                    if (nextTransaction.isMessagePartOfTransaction(request)) {
                        // Mark transaction as the one to handle this message
                        currentTransaction = nextTransaction;
                        break;
                    }
                }
            }

            // If no transaction exists to handle this message
            if (currentTransaction == null) {
                currentTransaction = findPendingTransaction(request);
                if (currentTransaction != null) {
                    // Associate the tx with the received request.
                    request.setTransaction(currentTransaction);
                    return currentTransaction;
                }

                // Creating a new server tx. May fail under heavy load.
                currentTransaction = createServerTransaction(request);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("newSIPServerRequest( "
                    + request.getMethod() + ":"
                    + request.getTopmostVia().getBranch() + "):"
                    + currentTransaction);
        }

        return currentTransaction;
    }

    /**
     * Handles a new SIP response. It finds a client transaction to handle this
     * message.
     *
     * @param response
     *            Response to handle.
     * @return A client transaction.
     */
    protected SIPClientTransaction getSIPClientTransactionForRequest(
            SIPResponse response) {
        return (SIPClientTransaction) findTransaction(response, false);
    }

    /**
     * Creates a client transaction to handle a new request that shall be sent
     * later.
     *
     * @param request
     *          Request for which the client transaction is created.
     * @param nextHop
     *          The next hop of the request. Represents the peer to contact.
     * @param listeningPoint
     *          The local listening point to use for the transaction.
     */
    public SIPClientTransaction createClientTransaction(
            SIPRequest request, Hop nextHop, ListeningPointImpl listeningPoint) {

        SIPClientTransaction clientTransaction = new SIPClientTransaction(
                this, listeningPoint, request, nextHop);
        addTransaction(clientTransaction);

        return clientTransaction;
    }

    /**
     * Creates a server transaction to handle a new received request.
     *
     * @param request
     *          The received request for which the server transaction is created.
     */
    public SIPServerTransaction createServerTransaction(SIPRequest request) {
        if (unlimitedTableSize ||
                this.serverTransactionTable.size() < serverTransactionTableLowaterMark)
            return new SIPServerTransaction(this, request);

        else if (this.serverTransactionTable.size() >= serverTransactionTableHighwaterMark) {
            if (log.isDebugEnabled())
                log.debug("ServerTransaction HWM exceeded, can't create new transaction.");
            return null;

        } else {
            float threshold = ((float) (serverTransactionTable.size() - serverTransactionTableLowaterMark))
                    / ((float) (serverTransactionTableHighwaterMark - serverTransactionTableLowaterMark));
            boolean decision = Math.random() > 1.0 - threshold;
            if (decision) {
                if (log.isDebugEnabled())
                    log.debug("ServerTransaction LWM exceeded, can't create new transaction.");
                return null;
            } else {
                return new SIPServerTransaction(this, request);
            }
        }
    }

    /**
     * Add a new transaction to the set of existing transactions.
     *
     * @param transaction -- transaction to add to the set.
     */
    public void addTransaction(SIPTransaction transaction) {
        if (log.isDebugEnabled())
            log.debug("Adding transaction = " + transaction.getTransactionId());

        if (transaction instanceof SIPServerTransaction)
            ((SIPServerTransaction)transaction).map();

        addTransactionHash(transaction);
        transaction.startTransactionTimer();
    }

    /**
     * Remove transaction. This actually gets the tx out of the search
     * structures which the stack keeps around.
     */
    public void removeTransaction(SIPTransaction sipTransaction) {
        String key = sipTransaction.getTransactionId();
        SipProviderImpl sipProvider = sipTransaction.getSipProvider();

        if (log.isDebugEnabled())
            log.debug("Removing Transaction = " + key);

        if (sipTransaction instanceof SIPServerTransaction) {
            Object removed = serverTransactionTable.remove(key);
            this.removePendingTransaction((SIPServerTransaction) sipTransaction);
            this.removeFromMergeTable((SIPServerTransaction) sipTransaction);

            // Send a notification to the listener.
            if (removed != null
                    && sipTransaction.testAndSetTransactionTerminatedEvent()) {
                TransactionTerminatedEvent event = new TransactionTerminatedEvent(
                        sipProvider, (ServerTransaction) sipTransaction);
                sipProvider.handleEvent(event, sipTransaction);
            }

        } else {
            Object removed = clientTransactionTable.remove(key);

            // Send a notification to the listener.
            if (removed != null
                    && sipTransaction.testAndSetTransactionTerminatedEvent()) {
                TransactionTerminatedEvent event = new TransactionTerminatedEvent(
                        sipProvider, (ClientTransaction) sipTransaction);
                sipProvider.handleEvent(event, sipTransaction);
            }
        }
    }

    /**
     * Hash table for quick lookup of transactions.
     */
    private void addTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        String key = sipRequest.getTransactionId();

        if (log.isDebugEnabled()) {
            log.debug(" putTransactionHash : " + " key = " + key);
        }

        if (sipTransaction instanceof SIPClientTransaction) {
            clientTransactionTable.put(key, (SIPClientTransaction)sipTransaction);
        } else {
            serverTransactionTable.put(key, (SIPServerTransaction)sipTransaction);
            String mergeKey = sipRequest.getMergeId();
            if (mergeKey != null) {
                this.mergeTable.put(mergeKey, (SIPServerTransaction)sipTransaction);
            }
        }
    }

    /**
     * Remove the transaction from transaction hash.
     */
    protected void removeTransactionHash(SIPTransaction sipTransaction) {
        String key = sipTransaction.getTransactionId();
        if (log.isDebugEnabled())
            log.debug("removing Tx : " + key);

        if (sipTransaction instanceof SIPClientTransaction) {
            clientTransactionTable.remove(key);
        } else if (sipTransaction instanceof SIPServerTransaction) {
            serverTransactionTable.remove(key);
        }
    }

    /**
     * Invoked when an error has ocurred with a transaction.
     *
     * @param errorEvent
     *            Error event.
     */
    public synchronized void transactionErrorEvent(
            SIPTransactionErrorEvent errorEvent) {
        SIPTransaction transaction = (SIPTransaction) errorEvent.getSource();

        if (errorEvent.getErrorID() == SIPTransactionErrorEvent.TRANSPORT_ERROR) {
            // Kill scanning of this transaction.
            transaction.setState(SIPTransaction.TERMINATED_STATE);
            if (transaction instanceof SIPServerTransaction) {
                // let the reaper get him
                ((SIPServerTransaction) transaction).collectionTime = 0;
            }
            transaction.disableTimeoutTimer();
            transaction.disableRetransmissionTimer();
            // Send a IO Exception to the Listener.
        }
    }

    /**
     * Stop stack. Clear all the timer stuff. Make the stack close all accept
     * connections and return. This is useful if you want to start/stop the
     * stack several times from your application. Caution : use of this function
     * could cause peculiar bugs as messages are processed asynchronously by the
     * stack.
     */
    public void stopStack() {
        // Prevent NPE on two concurrent stops
        if (this.timer != null)
            this.timer.cancel();

        // JvB: set it to null, SIPDialog tries to schedule things after stop
        timer = null;
        this.pendingTransactions.clear();
        this.toExit = true;
        synchronized (this) {
            this.notifyAll();
        }

        synchronized (messageProcessors) {
            for (MessageProcessor messageProcessor : messageProcessors) {
                removeMessageProcessor(messageProcessor);
            }
            // Let the processing complete.

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                if (log.isDebugEnabled())
                    log.debug("Thread.sleep was interrupted");
            }
        }

        this.clientTransactionTable.clear();
        this.serverTransactionTable.clear();
        this.dialogTable.clear();
        if (log.isDebugEnabled()) {
            log.debug("Dialog table has been cleared.");
        }
    }

    /**
     * Put a transaction in the pending transaction list. This is to avoid a
     * race condition when a duplicate may arrive when the application is
     * deciding whether to create a transaction or not.
     */
    public void putPendingTransaction(SIPServerTransaction tr) {
        if (log.isDebugEnabled())
            log.debug("putPendingTransaction: " + tr);

        this.pendingTransactions.put(tr.getTransactionId(), tr);
    }

    /**
     * Return the network layer (i.e. the interface for socket creation or the
     * socket factory for the stack).
     *
     * @return -- the registered Network Layer.
     */
    public NetworkLayer getNetworkLayer() {
        if (networkLayer == null) {
            return DefaultNetworkLayer.SINGLETON;
        } else {
            return networkLayer;
        }
    }

    /**
     * Return true if logging is enabled for this stack.
     *
     * @return true if logging is enabled for this stack instance.
     */
    public boolean isLoggingEnabled() {
        return logWriter != null && logWriter.isLoggingEnabled();
    }

    /**
     * Get the logger.
     *
     * @return --the logger for the sip stack. Each stack has its own logger
     *         instance.
     */
    public LogWriter getLogWriter() {
        return this.logWriter;
    }

    /**
     * Maximum size of a single TCP message. Limiting the size of a single TCP
     * message prevents flooding attacks.
     *
     * @return the size of a single TCP message.
     */
    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    /**
     * Get the default route string.
     *
     * @param sipRequest
     *            is the request for which we want to compute the next hop.
     * @throws SipException
     */
    public Hop getNextHop(SIPRequest sipRequest) throws SipException {
        if (this.useRouterForAll) {
            // Use custom router to route all messages.
            if (getRouter() != null)
                return getRouter().getNextHop(sipRequest);
            else
                return null;
        } else {
            // Also non-SIP request containing Route headers goes to the default
            // router
            if (sipRequest.getRequestURI().isSipURI()
                    || sipRequest.getRouteHeaders() != null) {
                return defaultRouter.getNextHop(sipRequest);
            } else if (getRouter() != null) {
                return getRouter().getNextHop(sipRequest);
            } else
                return null;
        }
    }

    /**
     * Set the descriptive name of the stack.
     *
     * @param stackName --
     *            descriptive name of the stack.
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /**
     * Create a standard Server header for the stack (i.e. one that takes the
     * stack name as a product token) and return it.
     *
     * @return Server header for the stack. The server header is used in
     *         automatically generated responses.
     *
     */
    public Server createServerHeaderForStack() {

        Server retval = new Server();
        retval.addProductToken(this.stackName);
        return retval;
    }

    /**
     * Set my address.
     *
     * @param stackAddress --
     *            A string containing the stack address.
     */
    protected void setHostAddress(String stackAddress)
            throws UnknownHostException {
        if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
                && stackAddress.trim().charAt(0) != '[')
            this.stackAddress = '[' + stackAddress + ']';
        else
            this.stackAddress = stackAddress;
        this.stackInetAddress = InetAddress.getByName(stackAddress);
    }

    /**
     * Get my address.
     *
     * @return hostAddress - my host address or null if no host address is
     *         defined.
     * @deprecated
     */
    public String getHostAddress() {
        // JvB: for 1.2 this may return null...
        return this.stackAddress;
    }

    /**
     * Set the router algorithm. This is meant for routing messages out of
     * dialog or for non-sip uri's.
     *
     * @param router
     *            A class that implements the Router interface.
     */
    protected void setRouter(Router router) {
        this.router = router;
    }

    /**
     * Get the router algorithm.
     *
     * @return Router router
     */
    public Router getRouter(SIPRequest request) {
        if (this.useRouterForAll) {
            return getRouter();
        } else {
            if (request.getRequestURI().getScheme().equals("sip")
                    || request.getRequestURI().getScheme().equals("sips")) {
                return this.defaultRouter;
            } else {
                if (getRouter() != null)
                    return getRouter();
                else
                    return defaultRouter;
            }
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see javax.sip.SipStack#getRouter()
      */
    public Router getRouter() {
        return this.router;
    }

    /**
     * return the status of the toExit flag.
     *
     * @return true if the stack object is alive and false otherwise.
     */
    public boolean isAlive() {
        return !toExit;
    }

    /**
     * Adds a new MessageProcessor to the list of running processors for this
     * SIPStack and starts it. You can use this method for dynamic stack
     * configuration.
     */
    protected void addMessageProcessor(MessageProcessor newMessageProcessor)
            throws IOException {
        synchronized (messageProcessors) {
            // Suggested changes by Jeyashankher, jai@lucent.com
            // newMessageProcessor.start() can fail
            // because a local port is not available
            // This throws an IOException.
            // We should not add the message processor to the
            // local list of processors unless the start()
            // call is successful.
            // newMessageProcessor.start();
            messageProcessors.add(newMessageProcessor);

        }
    }

    /**
     * Removes a MessageProcessor from this SIPStack.
     *
     * @param oldMessageProcessor
     */
    protected void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
        synchronized (messageProcessors) {
            if (messageProcessors.remove(oldMessageProcessor)) {
                oldMessageProcessor.stop();
            }
        }
    }

    /**
     * Creates the equivalent of a JAIN listening point and attaches to the
     * stack.
     */
    protected MessageProcessor createMessageProcessor(ListeningPointImpl lip)
            throws java.io.IOException {

        MessageProcessor messageProcessor;

        String transport = lip.getTransport();
        if (transport.equalsIgnoreCase("udp")) {
            messageProcessor = new UDPMessageProcessor(lip);
            this.udpFlag = true;

        } else if (transport.equalsIgnoreCase("tcp")) {
            messageProcessor = new TCPMessageProcessor(lip);
            // this.tcpFlag = true;

        } else if (transport.equalsIgnoreCase("tls")) {
            messageProcessor = new TLSMessageProcessor(lip);
            // this.tlsFlag = true;
        } else {
            throw new IllegalArgumentException("bad transport");
        }

        this.addMessageProcessor(messageProcessor);
        return messageProcessor;
    }

    /**
     * @return  Returns the MessageProcessor for a given source port and
     *          transport, or null if no MessageProcessor is found.
     */
    public MessageProcessor getMessageProcessor(int sourcePort, String transport) {
        // Search each processor for the correct transport and source port
        // TODO mmany and mmath: Perhaps we should look at the IP address as well?
        synchronized(messageProcessors) {
            for (MessageProcessor messageProcessor : messageProcessors) {
                if (transport.equalsIgnoreCase(
                        messageProcessor.getListeningPoint().getTransport())
                        && sourcePort == messageProcessor.getListeningPoint().getPort()) {
                    return messageProcessor;
                }
            }
        }
        return null;
    }

    /**
     * Return true if a given event can result in a forked subscription. The
     * stack is configured with a set of event names that can result in forked
     * subscriptions.
     *
     * @param ename --
     *            event name to check.
     *
     */
    public boolean isEventForked(String ename) {
        if (log.isDebugEnabled()) {
            log.debug("isEventForked: " + ename + " returning "
                    + this.forkedEvents.contains(ename));
        }
        return this.forkedEvents.contains(ename);
    }

    /**
     * get the address resolver interface.
     *
     * @return -- the registered address resolver.
     */
    public AddressResolver getAddressResolver() {
        return this.addressResolver;
    }

    /**
     * Set the address resolution interface
     *
     * @param addressResolver --
     *            the address resolver to set.
     */
    public void setAddressResolver(AddressResolver addressResolver) {
        this.addressResolver = addressResolver;
    }

    /**
     * get the thread auditor object
     *
     * @return -- the thread auditor of the stack
     */
    public ThreadAuditor getThreadAuditor() {
        return this.threadAuditor;
    }

    ///
    /// Stack Audit methods
    ///

    /**
     * Audits the SIP Stack for leaks
     *
     * @return Audit report, null if no leaks were found
     */
    public String auditStack(Set activeCallIDs,
                             long leakedDialogTimer,
                             long leakedTransactionTimer) {
        String auditReport = null;
        String leakedDialogs = auditDialogs(activeCallIDs, leakedDialogTimer);
        String leakedServerTransactions = auditTransactions(serverTransactionTable, leakedTransactionTimer);
        String leakedClientTransactions = auditTransactions(clientTransactionTable, leakedTransactionTimer);
        if (leakedDialogs != null || leakedServerTransactions != null || leakedClientTransactions != null) {
            auditReport = "SIP Stack Audit:\n"
                    + (leakedDialogs != null ? leakedDialogs : "")
                    + (leakedServerTransactions != null ? leakedServerTransactions : "")
                    + (leakedClientTransactions != null ? leakedClientTransactions : "");
        }
        return auditReport;
    }

    /**
     * Audits SIP dialogs for leaks
     * - Compares the dialogs in the dialogTable with a list of Call IDs
     *      passed by the application.
     * - Dialogs that are not known by the application are leak suspects.
     * - Kill the dialogs that are still around after the timer specified.
     *
     * @return Audit report, null if no dialog leaks were found
     */
    private String auditDialogs(Set activeCallIDs,
                                long leakedDialogTimer) {
        String auditReport = "  Leaked dialogs:\n";
        int leakedDialogs = 0;
        long currentTime = System.currentTimeMillis();

        // Make a shallow copy of the dialog list.
        // This copy will remain intact as leaked dialogs are removed by the stack.
        LinkedList dialogs;
        synchronized (dialogTable) {
            dialogs = new LinkedList<SIPDialog>(dialogTable.values());
        }

        // Iterate through the dialogDialog, get the callID of each dialog and
        // check if it's in the list of active calls passed by the application.
        // If it isn't, start the timer on it.
        // If the timer has expired, kill the dialog.
        for (Object dialog : dialogs) {
            // Get the next dialog
            SIPDialog itDialog = (SIPDialog) dialog;

            // Get the call id associated with this dialog
            CallIdHeader callIdHeader = (itDialog != null ? itDialog.getCallId() : null);
            String callID = (callIdHeader != null ? callIdHeader.getCallId() : null);

            // Check if the application knows about this call id
            if (callID != null && !activeCallIDs.contains(callID)) {
                // Application doesn't know anything about this dialog...
                if (itDialog.auditTag == 0) {
                    // Mark this dialog as suspect
                    itDialog.auditTag = currentTime;
                } else {
                    // We already audited this dialog before. Check if his time's up.
                    if (currentTime - itDialog.auditTag >= leakedDialogTimer) {
                        // Leaked dialog found
                        leakedDialogs++;

                        // Generate report
                        DialogState dialogState = itDialog.getState();
                        String dialogReport = "dialog id: " + itDialog.getDialogId()
                                + ", dialog state: " +
                                (dialogState != null ? dialogState.toString() : "null");
                        auditReport += "    " + dialogReport + "\n";

                        // Kill it
                        itDialog.setState(SIPDialog.TERMINATED_STATE);

                        if (log.isDebugEnabled())
                            log.debug("auditDialogs: leaked " + dialogReport);
                    }
                }
            }
        }

        // Return final report
        if (leakedDialogs > 0) {
            auditReport += "    Total: " + Integer.toString(leakedDialogs) +
                    " leaked dialogs detected and removed.\n";
        } else {
            auditReport = null;
        }
        return auditReport;
    }

    /**
     * Audits SIP transactions for leaks
     *
     * @return Audit report, null if no transaction leaks were found
     */
    private String auditTransactions(
            ConcurrentHashMap<String, SIPTransaction> transactionsMap,
            long a_nLeakedTransactionTimer) {
        String auditReport = "  Leaked transactions:\n";
        int leakedTransactions = 0;
        long currentTime = System.currentTimeMillis();

        // Make a shallow copy of the transaction list.
        // This copy will remain intact as leaked transactions are removed by the stack.
        LinkedList<SIPTransaction> transactionsList =
                new LinkedList<SIPTransaction>(transactionsMap.values());

        // Iterate through our copy
        for (SIPTransaction transaction : transactionsList) {
            if (transaction != null) {
                if (transaction.auditTag == 0) {
                    // First time we see this transaction. Mark it as audited.
                    transaction.auditTag = currentTime;
                } else {
                    // We've seen this transaction before. Check if his time's up.
                    if (currentTime - transaction.auditTag >= a_nLeakedTransactionTimer) {
                        // Leaked transaction found
                        leakedTransactions++;

                        // Generate some report
                        TransactionState transactionState = transaction.getState();
                        SIPRequest origRequest = transaction.getOriginalRequest();
                        String origRequestMethod =
                                (origRequest != null ? origRequest.getMethod() : null);
                        String transactionReport = transaction.getClass().getName()
                                + ", state: " +
                                (transactionState != null ? transactionState.toString() : "null")
                                + ", OR: " +
                                (origRequestMethod != null ? origRequestMethod : "null");
                        auditReport += "    " + transactionReport + "\n";

                        // Kill it
                        removeTransaction(transaction);
                        if (log.isDebugEnabled())
                            log.debug("auditTransactions: leaked " + transactionReport);
                    }
                }
            }
        }

        // Return final report
        if (leakedTransactions > 0) {
            auditReport += "    Total: " + Integer.toString(leakedTransactions) +
                    " leaked transactions detected and removed.\n";
        } else {
            auditReport = null;
        }
        return auditReport;
    }
}
