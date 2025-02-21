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
/*******************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).     *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import java.io.IOException;
import java.util.LinkedList;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gov.nist.core.*;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.message.SIPMessage;

import org.apache.log4j.Logger;

import javax.sip.address.Hop;


/**
 * Sit in a loop and handle incoming udp datagram messages. For each Datagram
 * packet, a new UDPMessageChannel is created (upto the max thread pool size).
 * Each UDP message is processed in its own thread).
 *
 * @version 1.2 $Revision: 1.28 $ $Date: 2006/11/05 23:40:43 $
 *
 * @author M. Ranganathan  <br/>
 *
 *
 *
 * <a href="{@docRoot}/../uml/udp-request-processing-sequence-diagram.jpg">
 * See the implementation sequence diagram for processing incoming requests.
 * </a>
 *
 *
 * Acknowledgement: Jeff Keyser contributed ideas on starting and stoppping the
 * stack that were incorporated into this code. Niklas Uhrberg suggested that
 * thread pooling be added to limit the number of threads and improve
 * performance.
 */
public class UDPMessageProcessor extends MessageProcessor {

    private static final Logger log = Logger.getLogger(UDPMessageProcessor.class);

    private static final int HWM = 150 ; // High water mark for queue size.

    private static final int LWM = 100 ; // Low water mark for queue size

    /**
     * Incoming messages are queued here.
     */
    private final ArrayBlockingQueue<DatagramPacket> messageQueue;

    /**
     * A list of message channels that we have started.
     */
    protected LinkedList<UDPMessageChannel> messageChannels;

    /**
     * Max datagram size.
     */
    protected static final int MAX_DATAGRAM_SIZE = 8 * 1024;


    protected final DatagramSocket sock;

    /**
     * A flag that is set to false to exit the message processor (suggestion by
     * Jeff Keyser).
     */
    protected AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Constructor.
     */
    protected UDPMessageProcessor(ListeningPointImpl lip) throws IOException {

        super(lip);

        this.messageQueue = new ArrayBlockingQueue<DatagramPacket>(HWM);

        try {
            this.sock = getNetworkLayer().createDatagramSocket(
                    lip.getPort(),lip.getInetAddress());

            // Create a new datagram socket.
            sock.setReceiveBufferSize(MAX_DATAGRAM_SIZE * 8);

            // TODO: What does thread auditor do?
            /**
             * If the thread auditor is enabled, define a socket timeout value in order to
             * prevent sock.receive() from blocking forever
             */
            if (lip.getSipStack().getThreadAuditor().isEnabled()) {
                sock.setSoTimeout((int) lip.getSipStack().getThreadAuditor().getPingIntervalInMillisecs());
            }
        } catch (SocketException ex) {
            throw new IOException(ex.getMessage());
        }
    }



    /**
     * Start our processor thread.
     */
    public void start() throws IOException {
        this.isRunning.set(true);
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        // Issue #32 on java.net
        thread.setName("UDPMessageProcessorThread");
        thread.start();
    }

    /**
     * Thread main routine.
     */
    public void run() {

        // Sanity check on the threadpool size. We never want to use
        // -1 (=infinite) since that would only make things slower...
        int threadPoolSize = getListeningPoint().getSipStack().threadPoolSize;
        if (threadPoolSize < 1) {
            threadPoolSize = 1;
        } else if (threadPoolSize > 256) {
            threadPoolSize = 256;
        }

        // Create a pool of messageChannels and start them up
        messageChannels = new LinkedList<UDPMessageChannel>();
        for (int i = 0; i < threadPoolSize; i++) {
            UDPMessageChannel channel = new UDPMessageChannel(this);
            channel.start();
            messageChannels.add(channel);
        }

        // Ask the auditor to monitor this thread
        ThreadAuditor.ThreadHandle threadHandle = getListeningPoint().getSipStack().getThreadAuditor().addCurrentThread();

        // Somebody asked us to exit. if isRunnning is set to false.
        while (this.isRunning.get()) {

            try {
                // Let the thread auditor know we're up and running
                threadHandle.ping();

                int bufsize = MAX_DATAGRAM_SIZE;//sock.getReceiveBufferSize();
                byte message[] = new byte[bufsize];
                DatagramPacket packet = new DatagramPacket(message, bufsize);
                sock.receive(packet);

                
                // This is a simplistic congestion control algorithm.
                // It accepts packets if queuesize is < LWM. It drops
                // requests if the queue size exceeds a HWM and accepts
                // requests with probability p proportional to the difference
                // between current queue size and LWM in the range
                // of queue sizes between HWM and LWM.
                // TODO -- penalize spammers by looking at the source
                // port and IP address.
                if ( this.messageQueue.size() >= HWM) {
                    if (log.isInfoEnabled())
                        log.info("Dropping message. Message queue exceeds HWM.");
                    continue;
                } else if (this.messageQueue.size() > LWM &&
                        this .messageQueue.size() < HWM ) {
                    // Drop the message with a probabilty that is linear in the range 0 to 1
                    float threshold = ((float)(messageQueue.size() - LWM))/ ((float)(HWM - LWM));
                    boolean decision = Math.random() > 1.0 - threshold;
                    if ( decision ) {
                        if (log.isInfoEnabled())
                            log.info("Dropping message. Message queue " +
                                    "exceeds LWM. Probability=" + (1.0 - threshold));
                        continue;
                    }

                }

                if (!messageQueue.offer(packet)) {
                    log.warn("Message dropped. Message queue full.");
                }

            } catch (java.net.SocketException t) {
                // Socket exception means the socket is closed or dead.
                // Catch everything, never let this thread die.
                if (isRunning.get()) {
                    log.error("Exception caught in UDPMessageProcessor.run(): " +
                              t.getMessage(), t);
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Exception caught in UDPMessageProcessor.run(): " +
                                t.getMessage(), t);
                }
            } catch (Throwable t) {
                // Catch everything, never let this thread die.
                log.error("Exception caught in UDPMessageProcessor.run(): " +
                          t.getMessage(), t);
            }
        }

        // Stop all pool threads
        for (UDPMessageChannel messageChannel : messageChannels) {
            messageChannel.stop();
        }

        if (log.isDebugEnabled()) log.debug(
                "Stopped " + messageChannels.size() + " UDPMessageChannel threads");
    }

    /**
     * Take a message from the message queue. Block if no message is available.
     * @return a new DatagramPacket for processing.
     * @throws InterruptedException
     */
    public DatagramPacket takeMessage() throws InterruptedException {
        return messageQueue.take();
    }


    /**
     * Shut down the message processor. Close the socket for recieving incoming
     * messages.
     */
    public void stop() {
        this.isRunning.set(false);
        sock.close();
    }

    /**
     * Return the transport string.
     *
     * @return the transport string
     */
    public String getTransport() {
        return "udp";
    }

    /**
     * Create and return new UDPMessageChannel for the given host/port.
     */
    public MessageChannel createMessageChannel(HostPort targetHostPort)
        throws UnknownHostException {
        return new UDPMessageChannel(targetHostPort.getInetAddress(),
                                     targetHostPort.getPort(), this);
    }

    public MessageChannel createMessageChannel(InetAddress host, int port)
        throws IOException {
        return new UDPMessageChannel(host, port, this);
    }

    public void sendResponse(MessageChannel messageChannel,
                             Hop hop, SIPResponse response) throws IOException {
        sendMessage(hop, null, response);
    }

    public void sendMessage(Hop hop,
                            Hop outboundProxy,
                            SIPMessage message) throws IOException {
        String host = hop.getHost();
        int port = hop.getPort();
        MessageChannel newChannel = new UDPMessageChannel(
                InetAddress.getByName(host), port, this);
        newChannel.sendMessage(message);
    }


    /**
     * Return true if there are any messages in use.
     */
    public boolean inUse() {
        return messageQueue.size() != 0;
    }

}
