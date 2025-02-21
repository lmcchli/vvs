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
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.SocketException;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.message.SIPMessage;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import javax.sip.address.Hop;
import javax.sip.ListeningPoint;

/**
 * Sit in a loop waiting for incoming tcp connections and start a
 * new thread to handle each new connection. This is the active
 * object that creates new TCP MessageChannels (one for each new
 * accept socket).
 *
 * @version 1.2 $Revision: 1.24 $ $Date: 2006/07/13 09:01:01 $
 *
 * @author M. Ranganathan   <br/>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
 *
 */
public class TCPMessageProcessor extends MessageProcessor {

    private static final Logger log = Logger.getLogger(TCPMessageProcessor.class);

    private int nConnections;

    private boolean isRunning;

    private final ConcurrentHashMap<String, TCPMessageChannel> tcpMessageChannels;

    private ServerSocket sock;

    protected int useCount;

    // Keep a unique number for this TCPMessageProcessor
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private final int myId;


    /**
     * Constructor.
     */
    public TCPMessageProcessor(ListeningPointImpl lip) {
        super(lip);

        myId = idCounter.getAndIncrement();
        if (log.isDebugEnabled())
            log.debug("Creating new TCPMessageProcessor-" + myId +
                    " for ListeningPoint with key=" + lip.getKey());

        tcpMessageChannels = new ConcurrentHashMap<String, TCPMessageChannel>();
    }

    /**
     * Start the processor.
     */
    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("TCPMessageProcessorThread-" + myId);
        thread.setDaemon(true);
        sock = getNetworkLayer().createServerSocket(
                getListeningPoint().getPort(), 0,
                getListeningPoint().getInetAddress());
        isRunning = true;
        thread.start();
    }




    /**
     * Run method for the thread that gets created for each accept
     * socket.
     */
    public void run() {
        // Accept new connectins on our socket.
        while (isRunning) {
            try {
                synchronized (this) {
                    // sipStack.maxConnections == -1 means we are
                    // willing to handle an "infinite" number of
                    // simultaneous connections (no resource limitation).
                    // This is the default behavior.
                    while (isRunning
                        && getListeningPoint().getSipStack().maxConnections != -1
                        && this.nConnections >= getListeningPoint().getSipStack().maxConnections) {
                        try {
                            this.wait();

                            if (!isRunning)
                                return;
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    this.nConnections++;
                }

                Socket newsock = sock.accept();
                if (log.isDebugEnabled())
                    log.debug("Accepting new connection!");

                new TCPMessageChannel(newsock, this);

            } catch (SocketException ex) {
                isRunning = false;
                if (log.isDebugEnabled())
                    log.debug("accept resulted in a SocketException, the socket was probably closed.");
            } catch (IOException ex) {
                log.error("IO problem accepting connection ", ex);
            } catch (Exception ex) {
                log.error("Unexpected exception in run()", ex);
            }
        }
    }

    public void sendResponse(MessageChannel messageChannel,
                             Hop hop, SIPResponse response)
            throws IOException {

        if (messageChannel != null && messageChannel instanceof TCPMessageChannel) {

            // First try original incoming connection

            TCPMessageChannel origChannel = (TCPMessageChannel) messageChannel;
            // TODO: Check if channel is ok? isOk()
            try {
                origChannel.sendMessage(response);
                if (log.isDebugEnabled())
                    log.debug("sendResponse: Sent message using original channel " +
                            origChannel.getKey());
                return;

            } catch(IOException e) {
                if (log.isDebugEnabled())
                    log.debug("sendResponse: IOException when trying to send to original " +
                            "connection, try connecting to hop instead.");

            }
        }

        // If original channel fails, try finding a cached channel matching the hop
        TCPMessageChannel msgChan = getMessageChannel(hop);
        if (msgChan == null) {
            String errMsg = "sendResponse: Cannot find/create new channel for hop=" + hop;
            log.warn(errMsg);
            throw new IOException(errMsg);
        }

        // First try
        try {
            msgChan.sendMessage(response);
            if (log.isDebugEnabled())
                log.debug("sendResponse: Sent message using hop " + hop);
            return;

        } catch (IOException e) {
            // Close the failing channel
            uncacheChannel(msgChan);
            msgChan.close();
            if (log.isDebugEnabled())
                log.debug("sendResponse failed due to IOException, " +
                        "retrying with new channel");

        }

        // If send failed with IOException we should try once more with a
        // new channel.
        msgChan = getMessageChannel(hop);

        if (msgChan == null) {
            String errMsg = "sendResponse: Cannot create new channel for hop=" + hop;
            log.warn(errMsg);
            throw new IOException(errMsg);
        }

        // Second try, if this also fails we throw an
        // IOException and let the caller deal with it
        try {
            msgChan.sendMessage(response);
            if (log.isDebugEnabled())
                log.debug("sendResponse: Sent message using hop " + hop + " (2nd try)");

        } catch (IOException e) {
            uncacheChannel(msgChan);
            msgChan.close();
            if (log.isDebugEnabled())
                log.debug("sendResponse failed due to IOException (2nd try)");
            throw e;
        }

    }

    public void sendMessage(Hop hop, Hop outboundProxy, SIPMessage sipMessage)
            throws IOException {

        // TODO: We are currently not using outboundProxy, remove?

        TCPMessageChannel msgChan = getMessageChannel(hop);
        if (msgChan == null) {
            String errMsg = "sendMessage: Cannot find/create new channel for hop=" + hop;
            log.warn(errMsg);
            throw new IOException(errMsg);
        }

        // First try
        try {
            msgChan.sendMessage(sipMessage);
            if (log.isDebugEnabled())
                log.debug("sendMessage: Sent message using hop " + hop);
            return;

        } catch (IOException e) {
            // Close the failing channel
            uncacheChannel(msgChan);
            msgChan.close();
            if (log.isDebugEnabled())
                log.debug("sendMessage failed due to IOException, " +
                        "retrying with new channel");

        }

        // If send failed with IOException we should try once more with a
        // new channel. (For example the connection could have been closed
        // timeout just before we wrote to it)
        msgChan = getMessageChannel(hop);

        if (msgChan == null) {
            String errMsg = "sendMessage: Cannot create new channel for hop=" + hop;
            log.warn(errMsg);
            throw new IOException(errMsg);
        }

        // Second try, if this also fails we throw an
        // IOException and let the caller deal with it
        try {
            msgChan.sendMessage(sipMessage);
            if (log.isDebugEnabled())
                log.debug("sendMessage: Sent message using hop " + hop + " (2nd try)");

        } catch (IOException e) {
            uncacheChannel(msgChan);
            msgChan.close();
            if (log.isDebugEnabled())
                log.debug("sendMessage failed due to IOException (2nd try)");
            throw e;
        }

    }

    /**
     * Return the transport string.
     * @return the transport string
     */
    public String getTransport() {
        return ListeningPoint.TCP;
    }


    /**
     * Stop the message processor.
     * Feature suggested by Jeff Keyser.
     */
    public synchronized void stop() {
        isRunning = false;
        try {
            sock.close();
        } catch (IOException e) {
            // Do nothing
        }

        Collection<TCPMessageChannel> channels = tcpMessageChannels.values();
        for (TCPMessageChannel channel : channels) {
            channel.close();
        }
        tcpMessageChannels.clear();
        this.notify();
    }



    public boolean inUse() {
        return this.useCount != 0;
    }


    private TCPMessageChannel getCachedMessageChannel(Hop hop) {

        String key = TCPMessageChannel.getKey(hop);
        if (key != null)
            return tcpMessageChannels.get(key);
        else
            return null;

    }


    /**
     * Get a message channel. First tries to find a cached one matching the hop.
     * If that fails it tries to create a new one. If that also fails it will
     * return null.
     * @param hop - Hop to find/create a TCPMessageChannel for.
     * @return the channel
     */
    private synchronized TCPMessageChannel getMessageChannel(Hop hop) {

        // First try to find a matching cached channel
        TCPMessageChannel channel = getCachedMessageChannel(hop);

        // If that fails, try creating a new one
        if (channel == null) {
            try {
                InetAddress address = InetAddress.getByName(hop.getHost());
                channel = new TCPMessageChannel(address, hop.getPort(), this);
                cacheChannel(channel);
                if (log.isDebugEnabled())
                    log.debug("Created new TCPMessageChannel " + channel);

            } catch (UnknownHostException e) {
                log.warn("Unable to create new TCPMessageChannel, unknown host: " +
                        hop.getHost());
                return null;

            } catch (IOException e) {
                log.warn("Unable to create new TCPMessageChannel, IOException", e);
                return null;
            } catch (Exception e) {
                log.warn("Unexpected exception: ", e);
                return null;
            }

        } else {
            if (log.isDebugEnabled())
                log.debug("Reusing the existing TCPMessageChannel-" +
                        channel.getId() + " matching hop " + hop.toString());

        }

        return channel;

    }


    /**
     * Add a TCPMessageChannel to the cache. If a cached entry with same key
     * already exists, the old cached channel will be removed
     * (if it is not the same channel).
     * @param channel to cache
     */
    private synchronized void cacheChannel(TCPMessageChannel channel) {
        String key = channel.getKey();

        if (log.isDebugEnabled())
            log.debug("Caching channel " + channel + "with key=" + key);

        TCPMessageChannel cachedChannel = tcpMessageChannels.put(key, channel);
        if (cachedChannel != null) {
            if (cachedChannel == channel) {
                if (log.isDebugEnabled())
                    log.debug("Channel with key=" + key +
                            " was already cached (same channel)");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Cached channel entry with key=" + key +
                            " already existed (not the same channel)");
                    log.debug("Closing old cached channel " + cachedChannel);
                }
                cachedChannel.close();
            }
        }

        if (log.isDebugEnabled())
            log.debug("TCP channel cache for " + this + " now contain: " +
                    tcpMessageChannels);


    }

    /**
     * Remove a TCPMessageChannel from the cache.
     *
     * @param tcpMessageChannel to remove
     */
    protected synchronized void uncacheChannel(TCPMessageChannel tcpMessageChannel) {

        String key = tcpMessageChannel.getKey();

        if (key != null) {
            TCPMessageChannel channel = tcpMessageChannels.remove(key);
            if (log.isDebugEnabled()) {
                if (channel != null)
                    log.debug("Removed <" + key + "," + channel + "> from cache in " + this);
                else
                    log.debug(key + " was not found in cache in " + this);

                log.debug("TCP channel cache for " + this +
                        " now contain: " + tcpMessageChannels);
            }

        } else {
            log.warn("Cannot remove " + tcpMessageChannel + " with key=null");
        }

    }

}
