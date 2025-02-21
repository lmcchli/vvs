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
/* This class is entirely derived from TCPMessageProcessor,
 *  by making some minor changes.
 *
 *               Daniel J. Martinez Manzano <dani@dif.um.es>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
*/

/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;
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


/**
 * Sit in a loop waiting for incoming tls connections and start a
 * new thread to handle each new connection. This is the active
 * object that creates new TLS MessageChannels (one for each new
 * accept socket).  
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006/11/02 21:17:54 $
 *
 * @author M. Ranganathan   <br/>
 * 
 */
public class TLSMessageProcessor extends MessageProcessor {

    private static final Logger log = Logger.getLogger(TLSMessageProcessor.class);

    protected int nConnections;

    private boolean isRunning;

    private final ConcurrentHashMap<String, TLSMessageChannel> tlsMessageChannels;

    private ServerSocket sock;

    protected int useCount=0;

    // Keep a unique number for this MessageProcessor
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private final int myId;

    /**
     * Constructor.
     */
    protected TLSMessageProcessor(ListeningPointImpl lip) {
        super(lip);

        myId = idCounter.getAndIncrement();
        if (log.isDebugEnabled())
            log.debug("Creating new TLSMessageProcessor-" + myId +
                    " for ListeningPoint with key=" + lip.getKey());

        tlsMessageChannels = new ConcurrentHashMap<String, TLSMessageChannel>();
    }

    /**
     * Start the processor.
     */
    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("TLSMessageProcessorThread-" + myId);
        thread.setDaemon(true);
        if (!getListeningPoint().getSipStack().useTlsAccelerator) {
            sock = getNetworkLayer().createSSLServerSocket(
                    getListeningPoint().getPort(), 0,
                    getListeningPoint().getInetAddress());
        } else {
            sock = getNetworkLayer().createServerSocket(
                    getListeningPoint().getPort(), 0,
                    getListeningPoint().getInetAddress());
        }
        isRunning = true;
        thread.start();

    }



    /**
     * Run method for the thread that gets created for each accept
     * socket.
     */
    public void run() {
        // Accept new connectins on our socket.
        while (this.isRunning) {
            try {
                synchronized (this)
                {
                    // sipStack.maxConnections == -1 means we are
                    // willing to handle an "infinite" number of
                    // simultaneous connections (no resource limitation).
                    // This is the default behavior.
                    while (this.isRunning
                        && getListeningPoint().getSipStack().maxConnections != -1
                        && this.nConnections >= getListeningPoint().getSipStack().maxConnections) {
                        try {
                            this.wait();

                            if (!this.isRunning)
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

                new TLSMessageChannel(newsock, this);

            } catch (SocketException ex) {
                isRunning = false;
                if (log.isDebugEnabled())
                    log.info("SocketException",ex);
            } catch (IOException ex) {
                log.error("Problem accepting connection",ex);
            } catch (Exception ex) {
                log.error("Unexpected Exception!",ex);
            }
        }
    }

    public void sendResponse(MessageChannel messageChannel, Hop hop, SIPResponse response) throws IOException {
        // TODO: Copied from TCP, should anything be changed?
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
        TLSMessageChannel msgChan = getMessageChannel(hop);
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

    public void sendMessage(Hop hop, Hop outboundProxy, SIPMessage sipMessage) throws IOException {
        // TODO: Copied from TCP, should anything be changed?
        // TODO: We are currently not using outboundProxy, remove?

        TLSMessageChannel msgChan = getMessageChannel(hop);
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

        Collection<TLSMessageChannel> channels = tlsMessageChannels.values();
        for (TLSMessageChannel channel : channels) {
            channel.close();
        }
        tlsMessageChannels.clear();
        this.notify();
    }


    private TLSMessageChannel getCachedMessageChannel(Hop hop) {

        String key = TLSMessageChannel.getKey(hop);
        if (key != null)
            return tlsMessageChannels.get(key);
        else
            return null;

    }


    /**
     * Get a message channel. First tries to find a cached one matching the hop.
     * If that fails it tries to create a new one. If that also fails it will
     * return null.
     * @param hop - Hop to find/create a TLSMessageChannel for.
     * @return the channel
     */
    private synchronized TLSMessageChannel getMessageChannel(Hop hop) {

        // First try to find a matching cached channel
        TLSMessageChannel channel = getCachedMessageChannel(hop);

        // If that fails, try creating a new one
        if (channel == null) {
            try {
                InetAddress address = InetAddress.getByName(hop.getHost());
                channel = new TLSMessageChannel(address, hop.getPort(), this);
                cacheChannel(channel);
                if (log.isDebugEnabled())
                    log.debug("Created new TLSMessageChannel " + channel);

            } catch (UnknownHostException e) {
                log.warn("Unable to create new TLSMessageChannel, unknown host: " +
                        hop.getHost());
                return null;

            } catch (IOException e) {
                log.warn("Unable to create new TLSMessageChannel, IOException", e);
                return null;
            } catch (Exception e) {
                log.warn("Unexpected exception: ", e);
                return null;
            }

        } else {
            if (log.isDebugEnabled())
                log.debug("Reusing the existing TLSMessageChannel-" +
                        channel.getId() + " matching hop " + hop.toString());

        }

        return channel;

    }


    /**
     * Add a TLSMessageChannel to the cache. If a cached entry with same key
     * already exists, the old cached channel will be removed
     * (if it is not the same channel).
     * @param channel to cache
     */
    private synchronized void cacheChannel(TLSMessageChannel channel) {
        String key = channel.getKey();

        if (log.isDebugEnabled())
            log.debug("Caching channel " + channel + "with key=" + key);

        TLSMessageChannel cachedChannel = tlsMessageChannels.put(key, channel);
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
            log.debug("TLS channel cache for " + this + " now contain: " +
                    tlsMessageChannels);


    }

    /**
     * Remove a channel from the cache.
     *
     * @param tlsMessageChannel to remove
     */
    protected synchronized void uncacheChannel(TLSMessageChannel tlsMessageChannel) {

        String key = tlsMessageChannel.getKey();

        if (key != null) {
            TLSMessageChannel channel = tlsMessageChannels.remove(key);
            if (log.isDebugEnabled()) {
                if (channel != null)
                    log.debug("Removed <" + key + "," + channel + "> from cache in " + this);
                else
                    log.debug(key + " was not found in cache in " + this);

                log.debug("TLS channel cache for " + this +
                        " now contain: " + tlsMessageChannels);
            }

        } else {
            log.warn("Cannot remove " + tlsMessageChannel + " with key=null");
        }

    }


    public boolean inUse() {
        return this.useCount != 0;
    }
}
