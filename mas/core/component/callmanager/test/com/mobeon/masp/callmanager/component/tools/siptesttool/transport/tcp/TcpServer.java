/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * TODO: Document, purpose, thread-safety, error handling. Also doc methods
 * @author Malin Nyfeldt
 */
public class TcpServer implements Runnable {

    /** A logger instance. */
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final ServerSocket serverSocket;
    private final LinkedBlockingQueue<TcpConnection> receivedTcpConnections =
        new LinkedBlockingQueue<TcpConnection>();
    private final int timeout;

    private AtomicBoolean closed = new AtomicBoolean(true);


    // TODO: Document, timeout in milliseconds, must be > 0, 0 is infinite
    public TcpServer(String host, int port, int timeout) throws IOException {
        this.serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(host, port));
        this.timeout = timeout;
    }

    public void start() {
        closed.set(false);
        Thread thread = new Thread(this);
        thread.start();
    }


    public void run() {
        while (!closed.get()) {
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                TcpConnection connection = new TcpConnection(socket, timeout);
                connection.start();
                receivedTcpConnections.put(connection);

            } catch (Exception e) {
                if(log.isDebugEnabled()) log.debug(e.getMessage(), e);
                closeSocket(socket);
                shutdown();
            }
        }
    }

    public void shutdown() {
        closed.set(true);
        try {
            serverSocket.close();
        } catch (IOException e) {
            if (log.isDebugEnabled()) log.debug(
                    "Received exception when closing server socket. " + this, e);
        }
    }

    public boolean isAlive() {
        return !(closed.get());
    }

    public TcpConnection waitForNewConnection()
            throws InterruptedException {
        return receivedTcpConnections.poll(timeout, TimeUnit.MILLISECONDS);
    }



    private void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                if (log.isDebugEnabled()) log.debug(
                        "Received exception when closing socket " + socket, e);
            }
        }
    }

}
