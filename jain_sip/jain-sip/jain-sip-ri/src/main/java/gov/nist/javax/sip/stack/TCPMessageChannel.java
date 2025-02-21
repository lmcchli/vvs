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
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.DialogFilter;
import gov.nist.core.*;
import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sip.address.Hop;
import javax.sip.message.Response;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;

/* Ahmet Uyar
* <auyar@csit.fsu.edu>sent in a bug report for TCP operation of the
* JAIN sipStack. Niklas Uhrberg suggested that a mechanism be added to
* limit the number of simultaneous open connections. The TLS
* Adaptations were contributed by Daniel Martinez. Hagai Sela
* contributed a bug fix for symmetric nat. Jeroen van Bemmel
* added compensation for buggy clients ( Microsoft RTC clients ).
* Bug fixes by viswashanti.kadiyala@antepo.com, Joost Yervante Damand				
*/


/**
 * This is a stack abstraction for TCP connections. This abstracts a stream of parsed
 * messages. The SIP sipStack starts this from the main SIPStack class for each
 * connection that it accepts. It starts a message parser in its own thread and
 * talks to the message parser via a pipe. The message parser calls back via the
 * parseError or processMessage functions that are defined as part of the
 * SIPMessageListener interface.
 * 
 * @see gov.nist.javax.sip.parser.PipelinedMsgParser
 * 
 * 
 * @author M. Ranganathan <br/>
 * 
 * @version 1.2 $Revision: 1.36 $ $Date: 2006/07/25 19:47:00 $ 
 */
public class TCPMessageChannel extends MessageChannel implements
        SIPMessageListener, Runnable {

    private static final Logger log = Logger.getLogger(TCPMessageChannel.class);

    private final Socket mySock;
    private final InputStream myClientInputStream;
    private final String key;
    private final Thread myThread;
    private volatile boolean isStarted = false;
    private final TCPMessageProcessor tcpMessageProcessor;

    private boolean isRunning;

    private final InetAddress myAddress;

    private InetAddress peerAddress;
    private int peerPort;

    // Removal timer task
    private ChannelRemovalTimerTask channelRemovalTimerTask;
    private final Object channelRemovalTimerTaskLock = new Object();

    private final boolean isSocketRemotelyInitiated;

    // Keep a unique number for this TCPMessageProcessor
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private final int myId;


    /**
     * This is a timer task responsible for removing a channel after period of
     * inactivity.
     * The timeout should be longer than the maximum survival time for a transaction
     * The task should be rescheduled whenever there is activity on the channel
     * (Message sent or received).
     */
    class ChannelRemovalTimerTask extends SIPStackTimerTask {

        private final Logger log = Logger.getLogger(ChannelRemovalTimerTask.class);

            public ChannelRemovalTimerTask() {
                if (log.isDebugEnabled())
                    log.debug("ChannelRemovalTimerTask() entered");
            }

            protected void runTask() {

                if (log.isDebugEnabled())
                    log.debug("ChannelRemovalTimerTask: Removing TCPMessageChannel-" +
                            myId + " with key=" + getKey());

                // Remove from cache
                uncache();

                // Close connection
                close();

            }

    }

    /**
     * Constructor - gets called from the SIPStack class with a socket on
     * accepting a new client. All the processing of the message is done here
     * with the sipStack being freed up to handle new connections. The sock
     * input is the socket that is returned from the accept. Global data that is
     * shared by all threads is accessible in the Server structure.
     *
     * @param sock
     *            Socket from which to read and write messages. The socket is
     *            already connected (was created as a result of an accept).
     */

    protected TCPMessageChannel(Socket sock, TCPMessageProcessor msgProcessor)
            throws IOException {

        super(msgProcessor);
        myId = idCounter.getAndIncrement();

        if (log.isDebugEnabled())
            log.debug("Creating new TCPMessageChannel-" + myId);

        // The channel was created due to an accept on a server socket
        isSocketRemotelyInitiated = true;

        mySock = sock;
        peerAddress = mySock.getInetAddress();
        peerPort = mySock.getPort();
        key = MessageChannel.getKey(peerAddress, peerPort, ListeningPoint.TCP);

        myClientInputStream = mySock.getInputStream();
        myAddress = msgProcessor.getListeningPoint().getInetAddress();
        tcpMessageProcessor = msgProcessor;

        myThread = new Thread(this);
        myThread.setDaemon(true);
        myThread.setName("TCPMessageChannelThread-" + myId);

        scheduleRemovalTimer();
        start();
    }

    /**
     * Constructor - connects to the given inet address. Acknowledgement --
     * Lamine Brahimi (IBM Zurich) sent in a bug fix for this method. A thread
     * was being uncessarily created.
     *
     * @param peerAddress
     *            inet address to connect to.
     */
    protected TCPMessageChannel(InetAddress peerAddress, int peerPort,
                                TCPMessageProcessor messageProcessor)
            throws IOException {

        super(messageProcessor);
        myId = idCounter.getAndIncrement();

        // The channel was not created due to an accept on a server socket
        isSocketRemotelyInitiated = false;

        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        key = MessageChannel.getKey(peerAddress, peerPort, ListeningPoint.TCP);

        if (log.isDebugEnabled())
            log.debug("Creating new TCPMessageChannel-" + myId + " " + key);

        myAddress = messageProcessor.getListeningPoint().getInetAddress();
        tcpMessageProcessor = messageProcessor;

        mySock = getMessageProcessor().getListeningPoint().getSipStack().
                getNetworkLayer().createSocket(peerAddress, peerPort,
                messageProcessor.getListeningPoint().getInetAddress());
        myClientInputStream = mySock.getInputStream();

        myThread = new Thread(this);
        myThread.setDaemon(true);
        myThread.setName("TCPMessageChannelThread-" + myId);

        // Reader thread should be started after we have sent out first message
        // on this channel.

        scheduleRemovalTimer();
    }

    /**
     * Get the unique id number for this TCPMessageChannel
     * @return id number
     */
    public int getId() {
        return myId;
    }

    /**
     * TCP is a reliable transport.
     */
    public boolean isReliable() {
        return true;
    }

    /**
     * TCP is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }

    /**
     * Simple check to see if channel seems to be ok.
     * @return true if channel seems to be usable (Socket exists and is connected)
     */
    public boolean isOk() {
        return (mySock != null) && mySock.isConnected() && !mySock.isClosed();
    }

    /**
     * Get an identifying key. This key is used to cache the connection and
     * re-use it if necessary.
     */
    public String getKey() {
        return key;
    }

    /**
     * get the address of the client that sent the data to us.
     *
     * @return Address of the client that sent us data that resulted in this
     *         channel being created.
     */
    public String getPeerAddress() {
        if (peerAddress != null) {
            return peerAddress.getHostAddress();
        } else
            return getHost();
    }

    protected InetAddress getPeerInetAddress() {
        return peerAddress;
    }

    /**
     * Get the port of the peer to whom we are sending messages.
     *
     * @return the peer port.
     */
    public int getPeerPort() {
        return peerPort;
    }

    /**
     * Get the host to assign to outgoing messages.
     *
     * @return the host to assign to the via header.
     */
    public String getViaHost() {
        return myAddress.getHostAddress();
    }

    /**
     * Close the message channel.
     */
    public void close() {

        if (log.isDebugEnabled())
            log.debug("Closing message Channel " + this);

        channelRemovalTimerTask.cancel();

        try {
            if (mySock != null)
                mySock.close();
        } catch (IOException e) {
            if (log.isDebugEnabled())
                log.debug("Error closing socket " + e);
        }
    }

    /**
     * get the transport string.
     *
     * @return "tcp" in this case.
     */
    public String getTransport() {
        return ListeningPoint.TCP;
    }

    /**
     * Return a formatted message to the client. We try to re-connect with the
     * peer on the other end if possible.
     *
     * @param sipMessage
     *            Message to send.
     * @throws IOException
     *             If there is an error sending the message
     */
    public void sendMessage(SIPMessage sipMessage) throws IOException {

        byte[] msg = sipMessage.encodeAsBytes();
        sendMessage(msg);

        // TODO: Remove or alter
        long time = System.currentTimeMillis();
        if (getMessageProcessor().getListeningPoint().getSipStack().
                serverLog.needsLogging(ServerLog.TRACE_MESSAGES))
            logMessage(sipMessage, peerAddress, peerPort, time);
    }

    /**
     * Send a message on the channel
     * @param message - Message to send.
     * @throws IOException
     *             If there is an error sending the message
     */
    public void sendMessage(byte[] message) throws IOException {

        try {
            IOHandler.sendBytes(mySock, message);
            if (log.isDebugEnabled())
                log.debug("sendMessage to " +
                        mySock.getInetAddress().getHostAddress() +
                        ":" + mySock.getPort() + "\n" + new String(message));

            // Reschedule removal timer
            scheduleRemovalTimer();

            // Start reader thread if not already started
            start();

        } catch (IOException e) {
            log.warn("sendMessage failed, closing channel", e);
            uncache();
            close();
            throw e;
        }

    }

    /**
     * Exception processor for exceptions detected from the parser. (This is
     * invoked by the parser when an error is detected).
     *
     * @param sipMessage --
     *            the message that incurred the error.
     * @param ex --
     *            parse exception detected by the parser.
     * @param header --
     *            header that caused the error.
     * @throws ParseException
     *             Thrown if we want to reject the message.
     */
    public void handleException(ParseException ex, SIPMessage sipMessage,
                                Class hdrClass, String header, String message)
            throws ParseException {

        log.error(ex);
        // Log the bad message for later reference.
        if ((hdrClass != null) &&
                (hdrClass.equals(From.class)
                || hdrClass.equals(To.class)
                || hdrClass.equals(CSeq.class)
                || hdrClass.equals(Via.class)
                || hdrClass.equals(CallID.class)
                || hdrClass.equals(RequestLine.class)
                || hdrClass.equals(StatusLine.class))) {
            if (log.isDebugEnabled()) {
                log.debug("Encountered Bad Message \n" + sipMessage.toString());
            }
            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
    }

    /**
     * Gets invoked by the parser as a callback on successful message parsing
     * (i.e. no parser errors).
     *
     * @param sipMessage
     *            Mesage to process (this calls the application for processing
     *            the message).
     */
    public void processMessage(SIPMessage sipMessage) throws Exception {

        // Reschedule removal timer
        scheduleRemovalTimer();

        if (sipMessage.getFrom() == null
                || sipMessage.getTo() == null
                || sipMessage.getCallId() == null
                || sipMessage.getCSeq() == null
                || sipMessage.getViaHeaders() == null) {

            if (log.isInfoEnabled()) {
                String badmsg = sipMessage.encode();
                log.info("Dropped Bad Msg:\n" + badmsg);
            }

            return;
        }

        ViaList viaList = sipMessage.getViaHeaders();
        // For a request
        // first via header tells where the message is coming from.
        // For response, this has already been recorded in the outgoing
        // message.
        if (sipMessage instanceof SIPRequest) {
            Via v = (Via) viaList.getFirst();
            Hop hop = getAddressResolver().resolveAddress(v.getHop());
            this.peerPort = hop.getPort();
//            this.peerProtocol = v.getTransport();
            try {
                this.peerAddress = mySock.getInetAddress();
                // Check to see if the received parameter matches
                // the peer address and tag it appropriately.

                // JvB: dont do this. It is both costly and incorrect
                // Must set received also when it is a FQDN, regardless whether
                // it resolves to the correct IP address
                // InetAddress sentByAddress = InetAddress.getByName(hop.getHost());
                // JvB: if sender added 'rport', must always set received
                if ( v.hasParameter(Via.RPORT)
                        || !hop.getHost().equals(this.peerAddress.getHostAddress())) {
                    v.setParameter(Via.RECEIVED, this.peerAddress.getHostAddress() );
                }
                // @@@ hagai
                // JvB: technically, may only do this when Via already contains
                // rport
                v.setParameter(Via.RPORT, Integer.toString(this.peerPort));
            } catch (java.text.ParseException ex) {
                InternalErrorHandler
                        .handleException(ex, getMessageProcessor().getListeningPoint().getSipStack().logWriter);
            }

        }

        // Foreach part of the request header, fetch it and process it

        long receptionTime = System.currentTimeMillis();

        if (sipMessage instanceof SIPRequest) {
            // This is a request - process the request.
            SIPRequest sipRequest = (SIPRequest) sipMessage;
            // Create a new sever side request processor for this
            // message and let it handle the rest.

            if (log.isDebugEnabled())
                log.debug("----Processing Message---");

            // Check for reasonable size - reject message
            // if it is too long.
            if (getMessageProcessor().getListeningPoint().getSipStack().getMaxMessageSize() > 0
                    && sipRequest.getSize()
                    + (sipRequest.getContentLength() == null ? 0
                    : sipRequest.getContentLength()
                    .getContentLength()) > getMessageProcessor().getListeningPoint().getSipStack()
                    .getMaxMessageSize()) {
                SIPResponse sipResponse = sipRequest
                        .createResponse(SIPResponse.MESSAGE_TOO_LARGE);
                sendMessage(sipResponse);
                throw new Exception("Message size exceeded");
            }

            SIPServerTransaction sipServerTransaction =
                    getMessageProcessor().getListeningPoint().getSipStack()
                            .getSIPServerTransactionForRequest(sipRequest, this);

            if (log.isDebugEnabled())
                log.debug("---- sipServerTransaction = " +
                        sipServerTransaction);

            if (sipServerTransaction != null && sipServerTransaction.acquireSem()) {
                if (log.isDebugEnabled()) {
                    log.debug("About to process request: "
                            + sipRequest.getFirstLine() + "/"
                            + sipServerTransaction);
                }

                try {
                    sipServerTransaction.processRequest(sipRequest);
                } finally {
                    if (!sipServerTransaction.passToListener())
                        sipServerTransaction.releaseSem();
                }

                if (log.isDebugEnabled()) {
                    log.debug("Done processing "
                            + sipRequest.getFirstLine() + "/"
                            + sipServerTransaction);
                }
            } else {
                log.warn("Dropping request (No server transaction to use.)");
                SIPResponse  response =
                        sipRequest.createResponse(Response.SERVICE_UNAVAILABLE);
                // Service is overloaded -- send back an error and drop the request.
                RetryAfter retryAfter = new RetryAfter();

                // Be a good citizen and send a decent response code back.
                try {
                    retryAfter.setRetryAfter((int) (10 * (Math.random())));
                    response.setHeader(retryAfter);
                    this.sendMessage(response);
                } catch (Exception e) {
                    // IGNore
                }

            }

            // TODO: Remove or alter?
            if (getMessageProcessor().getListeningPoint().getSipStack().serverLog
                    .needsLogging(ServerLog.TRACE_MESSAGES)) {

                getMessageProcessor().getListeningPoint().getSipStack().
                        serverLog.logMessage(sipMessage,
                        this.getPeerHostPort().toString() , this
                        .getMessageProcessor().getListeningPoint().getInetAddress()
                        .getHostAddress()
                        + ":" + this.getMessageProcessor().getListeningPoint().getPort(),
                        false, receptionTime);

            }

        } else {
            SIPResponse sipResponse = (SIPResponse) sipMessage;
            if (sipResponse.getStatusCode() == 100)
                sipResponse.getTo().removeParameter("tag");
            try {
                sipResponse.checkHeaders();
            } catch (ParseException ex) {
                log.error("Dropping Badly formatted response message >>> " +
                        sipResponse);
                return;
            }
            // This is a response message - process it.
            // Check the size of the response.
            // If it is too large dump it silently.
            if (getMessageProcessor().getListeningPoint().getSipStack().getMaxMessageSize() > 0
                    && sipResponse.getSize()
                    + (sipResponse.getContentLength() == null ? 0
                    : sipResponse.getContentLength()
                    .getContentLength()) > getMessageProcessor().getListeningPoint().getSipStack()
                    .getMaxMessageSize()) {
                if (log.isInfoEnabled())
                    log.info("Message size exceeded");
                return;

            }

            SIPClientTransaction sipClientTransaction =
                    getMessageProcessor().getListeningPoint().getSipStack()
                            .getSIPClientTransactionForRequest(sipResponse);

            if (sipClientTransaction != null) {
                if (sipClientTransaction.acquireSem()) {
                    try {
                        sipClientTransaction.processResponse(sipResponse);
                    } finally {
                        if (!sipClientTransaction.passToListener())
                            sipClientTransaction.releaseSem();
                    }
                } else {
                    log.warn("Dropping response (Client transaction could not be acquired)");
                }
            } else {
                DialogFilter.processResponse(
                        sipResponse,
                        getMessageProcessor().getListeningPoint().getSipProvider());
            }
        }
    }

    /**
     * This gets invoked when thread.start is called from the constructor.
     * Implements a message loop - reading the tcp connection and processing
     * messages until we are done or the other end has closed.
     */
    public void run() {

        // Create a pipelined message parser to read and parse
        // messages

        BufferedInputStream in = new BufferedInputStream(myClientInputStream);
        PipelinedMsgParser myParser = new PipelinedMsgParser(this, in, getMessageProcessor().
                getListeningPoint().getSipStack().getMaxMessageSize());
        // Start running the parser thread.
        this.tcpMessageProcessor.useCount++;
        this.isRunning = true;
        try {
            myParser.processInput();

        } finally {
            this.isRunning = false;
            // TODO: Do we have to close the BufferedInputStream???
            uncache();
            close();
            this.tcpMessageProcessor.useCount--;
        }

    }

    /**
     * Remove myself from the channel cache in the message processor
     */
    private void uncache() {
        tcpMessageProcessor.uncacheChannel(this);
    }

    /**
     * Equals predicate.
     *
     * @param other
     *            is the other object to compare ourselves to for equals
     */
    public boolean equals(Object other) {

        if (!this.getClass().equals(other.getClass()))
            return false;
        else {
            TCPMessageChannel that = (TCPMessageChannel) other;
            return this.mySock == that.mySock;
        }
    }


    private void start() {
        // Only create a thread if not already started
        try {
            synchronized(myThread) {
                if (!isStarted) {
                    isStarted = true;
                    myThread.start();
                }
                if (log.isDebugEnabled())
                    log.debug("Started thread " + myThread.getName());
            }
        } catch(IllegalThreadStateException e) {
            log.warn("Failed to start already started thread " + myThread.getName());
        }
    }

    private void scheduleRemovalTimer() {

        synchronized(channelRemovalTimerTaskLock) {

            // Use different delay depending on who created the connection
            // Note! Config values are in seconds but schedule takes milliseconds
            long delay;
            if (isSocketRemotelyInitiated)
                delay = getMessageProcessor().getListeningPoint().
                        getSipStack().getClientConnectionTimeout() * 1000;
            else
                delay = getMessageProcessor().getListeningPoint().
                        getSipStack().getServerConnectionTimeout() * 1000;

            if (delay == 0) {
                if (log.isDebugEnabled())
                    log.debug("channelRemovalTimerTask disabled, channel will " +
                            "live forever or until closed by other side.");
                return;
            }

            boolean cancelFailed = false;
            if (channelRemovalTimerTask != null) {
                if (log.isDebugEnabled())
                    log.debug("Rescheduling channelRemovalTimerTask with delay="
                            + delay);
                cancelFailed = !channelRemovalTimerTask.cancel();
            } else {
                if (log.isDebugEnabled())
                log.debug("Scheduling channelRemovalTimerTask with delay=" + delay);
            }

            if (!cancelFailed) {
                // If the task did not exist, schedule it
                // If it did exist and have not been executed yet, reschedule the task
                channelRemovalTimerTask = new ChannelRemovalTimerTask();
                try {
                    getMessageProcessor().getListeningPoint().getSipStack().timer.
                            schedule(channelRemovalTimerTask, delay);
                } catch(IllegalStateException e) {
                    // This should not happen
                    log.warn("Could not schedule the channel removal task", e);
                }

            } else {
                // The task have probably already been executed
                if (log.isDebugEnabled())
                    log.debug("ChannelRemovalTimerTask have already been executed, " +
                            "cannot be rescheduled");
            }

        }

    }

}
