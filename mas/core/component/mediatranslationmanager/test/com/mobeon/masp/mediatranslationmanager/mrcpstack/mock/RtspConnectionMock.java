/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.DummyServerMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.InputStreamMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.OutputStreamMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class mocks an RTSP connection.
 * Since the connection is mocked the MRCP/RTSP server is also mocked.
 * The behaviour of RtspConnectionMock simulates an MRCP server.
 * Hence it could be renamed to MrcpServer.
 */
public class RtspConnectionMock implements Runnable, RtspConnection  {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspConnectionMock.class);
    private InputStreamMock inputStream = null;
    private OutputStreamMock outputStream = null;
    private boolean isOpened = false;
    private Thread server;
    private ServerMock serverMock = null;

    /**
     * The MRCP server thread loop.
     */
    public void run() {
        if (logger.isInfoEnabled()) logger.info("run() --- started");
        boolean takeNextStep;
        do {
            if (logger.isInfoEnabled()) logger.info("Step ...");
            takeNextStep = serverMock.stepSimulation();
        } while(isOpened && takeNextStep);
        if (logger.isInfoEnabled()) logger.info("run() --- stopped");
    }

    /**
     * The default constructor.
     */
    public RtspConnectionMock() {
        inputStream = new InputStreamMock();
        outputStream = new OutputStreamMock();
    }
    /**
     * Open a connection to the server.
     */
    public boolean open() {
        if (logger.isDebugEnabled()) logger.debug("open()");
        isOpened = true;
        server = new Thread(this, "RtspConnectionMock");
        server.start();
        return true;
    }


    /**
     * Get an RTSP input stream.
     *
     * @return an input stream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Get an RTSP output stream.
     *
     * @return an output stream.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns the port number of the connection.
     *
     * @return a port number.
     */
    public int getPortNumber() {
        return 4711;
    }

    /**
     * Returns the host name of the connection.
     *
     * @return a host name.
     */
    public String getHostName() {
        return "mockingbird.mobeon.com";
    }

    /**
     * Closing the connection.
     */
    public void close() {
        if (logger.isDebugEnabled()) logger.debug("******* close() *******");
        isOpened = false;
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReceiveTimeout(int timeout) {
    }

    /**
     * Connecting a mocked server to this connection
     * @param serverMock
     */
    public void setMessageHandler(ServerMock serverMock) {
        if (this.serverMock != null && this.serverMock != serverMock) {
//            this.serverMock.stopServer();
        }
        this.serverMock = serverMock;
        serverMock.setOutput(inputStream);
        serverMock.setInput(outputStream);
    }
}
