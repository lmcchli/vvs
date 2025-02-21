/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.component.tools.siptesttool.transport.TransportConnection;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * TODO: Document, purpose, thread-safety, error handling. Also doc methods
 * @author Malin Nyfeldt
 */
public class TcpConnection implements Runnable, TransportConnection {

    /** A logger instance. */
    private static final ILogger log =
            ILoggerFactory.getILogger(TcpConnection.class);

    private final String remoteHost;
    private final int remotePort;
    private final Socket socket;
    private final OutputStream outStream;
    private final BufferedReader inReader;
    private final LinkedBlockingQueue<String> receivedSipMessages =
        new LinkedBlockingQueue<String>();
    private final int timeout;

    private AtomicBoolean closed = new AtomicBoolean(true);

    private static Pattern contentLengthPattern =
            Pattern.compile("Content-Length:\\s*(\\d+)\\s*");


    // TODO: Document that this is used to create a client tcp connection
    public TcpConnection(String remoteHost, int remotePort,
                         String localHost, int timeout)
            throws UnknownHostException, IOException {

        if (remoteHost == null)
                throw new NullPointerException("Remote host must not be null.");

        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.timeout = timeout;

        socket = new Socket(
                remoteHost, remotePort, InetAddress.getByName(localHost), 0);
        outStream = socket.getOutputStream();
        inReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()),
                socket.getReceiveBufferSize());
    }


    // TODO: Document that this is used to create a "server" tcp connection
    public TcpConnection(Socket socket, int timeout)
            throws NullPointerException, IOException {

        if (socket == null)
            throw new NullPointerException("Socket is null.");

        this.socket = socket;
        InetSocketAddress remoteSockAddr =
                (InetSocketAddress)socket.getRemoteSocketAddress();

        if (remoteSockAddr == null)
            throw new NullPointerException("Remote address of socket is null.");

        remoteHost = remoteSockAddr.getAddress().getHostAddress();
        remotePort = remoteSockAddr.getPort();
        this.timeout = timeout;
        outStream = socket.getOutputStream();
        inReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()),
                socket.getReceiveBufferSize());
    }

    // TODO: Document, timeout in milliseconds, must be > 0, 0 is infinite
    public void start() throws SocketException {
        closed.set(false);
        socket.setSoTimeout(timeout);
        Thread thread = new Thread(this);
        thread.start();
    }

    public void shutdownInput() {
        closed.set(true);
        try {
            socket.shutdownInput();
        } catch (IOException e) {
            if (log.isDebugEnabled()) log.debug(
                    "Received exception when shutting down input on socket " +
                            socket, e);
        }
    }

    public void shutdownOutput() {
        closed.set(true);
        try {
            socket.shutdownOutput();
        } catch (IOException e) {
            if (log.isDebugEnabled()) log.debug(
                    "Received exception when shutting down output on socket " +
                            socket, e);
        }
    }

    public void shutdown() {
        closed.set(true);
        try {
            socket.close();
        } catch (IOException e) {
            if (log.isDebugEnabled()) log.debug(
                    "Received exception when closing socket " + socket, e);
        }
    }

    public void hardReset() {
        closed.set(true);
        try {
            // The below linger settings causes the close to be a hard close
            // A RST is issued rather than the normal FIN-ACK exchange.
            socket.setSoLinger(true, 0);
            socket.close();
        } catch (IOException e) {
            if (log.isDebugEnabled()) log.debug(
                    "Received exception when closing socket " + socket, e);
        }
    }

    public boolean isAlive() {
        return !(closed.get());
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

    public void send(String data) throws IOException {
        if (data != null) {
            outStream.write(data.getBytes());
            outStream.flush();
            if (log.isDebugEnabled()) log.debug("Data sent: " + data);
        }
    }

    private String readSipMessage() throws IOException {
        StringBuffer inputBuffer = new StringBuffer();

        if (log.isDebugEnabled()) log.debug("Reading SIP message.");

        // Read the SIP headers
        while (true) {
            String line = inReader.readLine();

            if (line == null) {
                throw new NullPointerException("Read line is null.");
            }

            inputBuffer.append(line);
            inputBuffer.append("\r\n");

            // Stop when emtpy line between headers and bodies is encountered
            String tmp = line.trim();
            if (tmp.equals(""))
                break;
        }

        // Read the SIP body if any
        String sipMessage = inputBuffer.toString();
        int contentLength = getContentLength(sipMessage);

        if (contentLength != 0)
            sipMessage += readSize(contentLength);

        if (log.isDebugEnabled()) log.debug("Received SIP message: " + sipMessage);
        return sipMessage;
    }

    private int getContentLength(String sipMessage) {
        int length = 0;

        // Get a Matcher for the content legnth based on the sip message.
        Matcher matcher = contentLengthPattern.matcher(sipMessage);

        if (matcher.find())
            if (matcher.end() >= 1)
                length = Integer.parseInt(matcher.group(1));

        if (log.isDebugEnabled()) log.debug(
                "The retreived content-length is " + length);

        return length;
    }

    private String readSize(int size) throws IOException {
        char[] data = new char[size];

        int dataOffset = 0;
        while (dataOffset < size ) {
            int readLength = inReader.read(data, dataOffset, size - dataOffset);

            if (readLength > 0) {
                dataOffset += readLength;
            } else {
                throw new IOException("End of stream.");
            }
        }
        return data.toString();
    }

    public String waitForSipMessage() throws InterruptedException {
        return receivedSipMessages.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
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
        return "TcpConnection <remoteHost=" + getRemoteHost() +
                ", remotePort=" + getRemotePort() +
                ", localHost=" + getLocalHost() +
                ", localPort=" + getLocalPort() + ">";
    }

}
