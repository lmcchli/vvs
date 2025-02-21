package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is an abstraction of the server side of the RTSP session.
 * In the real application the server side will be implemented by a
 * Socket. In the test cases this class will be implemented by an
 * RTSP/MRCP mock.
 */
public interface RtspConnection {
    /**
     * Open a connection to the server.
     */
    public boolean open();

    /**
     * Get an RTSP input stream.
     * @return an input stream.
     */
    public InputStream getInputStream();

    /**
     * Get an RTSP output stream.
     * @return an output stream.
     */
    public OutputStream getOutputStream();

    /**
     * Returns the port number of the connection.
     * @return a port number.
     */
    public int getPortNumber();

    /**
     * Returns the host name of the connection.
     * @return a host name.
     */
    public String getHostName();

    /**
     * Closing the connection.
     */
    public void close();

    /**
     * Setter for the socket receive timeout.
     * @param timeout the timeout in millseconds.
     */
    void setReceiveTimeout(int timeout);
}
