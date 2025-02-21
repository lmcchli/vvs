package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

/**
 * This implements the RTSP server side of the RTSP session by wrapping a Socket.
 */
public class RtspConnectionImpl implements RtspConnection {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspConnectionImpl.class);
    private Socket socket = null;
    private String hostName;
    private int portNumber;
    private int socketTimeout = 0;

    /**
     * The constructor.
     * @param hostName the name of the RTSP server host.
     * @param portNumber the port number of the RTSP server host.
     */
    public RtspConnectionImpl(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    /**
     * Open a connection to the server.
     */
    public boolean open() {
        if (logger.isInfoEnabled()) logger.info("Creating RTSP session [" + hostName + "]:" + portNumber);
        InetAddress serverAddress = null;
        boolean ok = true;
        try {
            serverAddress = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            logger.error("Failed to get InetAddress for " + hostName, e);
            ok = false;
        }
        try {
            socket = new Socket(serverAddress, portNumber);
        } catch (IOException e) {
            logger.error("Failed create socket for " + portNumber, e);
            ok = false;
        }
        setReceiveTimeout(socketTimeout);
        return ok;
    }

    /**
     * Get an RTSP input stream.
     *
     * @return an input stream.
     */
    public InputStream getInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            logger.error("Failed to get Socket InputStream", e);
        }
        return inputStream;
    }

    /**
     * Get an RTSP output stream.
     *
     * @return an output stream.
     */
    public OutputStream getOutputStream() {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            logger.error("Failed to get Socket InputStream", e);
        }
        return outputStream;
    }

    /**
     * Returns the port number of the connection.
     *
     * @return a port number.
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Returns the host name of the connection.
     *
     * @return a host name.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Closing the connection.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Failed to close Socket", e);
        }
    }

    public void setReceiveTimeout(int timeout) {
        socketTimeout = timeout;
        try {
            if (socket != null) socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            logger.warn("Failed to set socket timeout", e);
        }
    }
}
