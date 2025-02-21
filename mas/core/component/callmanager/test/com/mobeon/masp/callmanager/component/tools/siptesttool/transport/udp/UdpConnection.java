/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.TransportConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This is a representation of a UDP message channel that can be used to
 * send and receive UDP messages.
 * <p>
 * The UDP message channel is at construction bound to a specific local host
 * and port.
 *
 * @author Malin Nyfeldt
 */
public class UdpConnection implements Runnable, TransportConnection {

    /** A logger instance. */
    private static final ILogger log =
            ILoggerFactory.getILogger(UdpConnection.class);

    private final DatagramSocket socket;
    private final LinkedBlockingQueue<String> receivedSipMessages =
        new LinkedBlockingQueue<String>();
    private final int timeout;

    private AtomicBoolean closed = new AtomicBoolean(true);

    // TODO: Document, timeout in milliseconds, must be > 0, 0 is infinite
    public UdpConnection(String localhost, int localport, int timeout)
            throws IOException {
        this.timeout = timeout;
        this.socket = new DatagramSocket(
                new InetSocketAddress(localhost, localport));
        socket.setReceiveBufferSize(8 * 1024);
        socket.setSoTimeout(timeout);
    }

    public void start() throws IOException {
        closed.set(false);
        Thread thread = new Thread(this);
        thread.start();
    }

    public void shutdown() {
        closed.set(true);
        socket.close();
    }

    public void run() {
        if(log.isDebugEnabled()) log.debug("Start running " + this.toString());

        while (!closed.get()) {
            try {
                receivedSipMessages.put(readSipMessage());
            } catch (Exception e) {
                if(log.isDebugEnabled()) log.debug(e.getMessage(), e);
                shutdown();
            }
        }
    }

    private String readSipMessage() throws IOException {
        int bufsize = socket.getReceiveBufferSize();
        byte message[] = new byte[bufsize];
        DatagramPacket packet = new DatagramPacket(message, bufsize);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public String waitForSipMessage() throws InterruptedException {
        return receivedSipMessages.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public void send(String host, int port, String data) throws IOException {
        DatagramPacket packet = new DatagramPacket(
                data.getBytes(), data.length(),
                InetAddress.getByName(host), port);
        socket.send(packet);
    }

    public boolean isAlive() {
        return !(closed.get());
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public String getLocalHost() {
        String localHost = null;

        InetSocketAddress localSockAddr =
                (InetSocketAddress)socket.getLocalSocketAddress();
        if (localSockAddr != null)
            localHost = localSockAddr.getAddress().getHostAddress();

        return localHost;
    }

    public String toString() {
        return "UdpConnection <localHost=" + getLocalHost() +
                ", localPort=" + getLocalPort() + ">";
    }

}
