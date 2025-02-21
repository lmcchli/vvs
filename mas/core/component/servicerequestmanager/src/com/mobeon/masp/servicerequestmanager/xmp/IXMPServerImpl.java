/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.xmp.server.HttpServer;


/**
 * Singleton implementation of the IXMPServer interface.
 * Uses the HttpServer from the Foundation XMP API.
 *
 * @author mmawi
 */
public class IXMPServerImpl extends HttpServer implements IXMPServer {
    private boolean isStarted;

    /**
     * The singleton instance
     */
    private static IXMPServer instance;

    /**
     * Hide as singleton
     */
    protected IXMPServerImpl() {
        isStarted = false;
    }

    /**
     * Get the singleton instance of the server.
     *
     * @return The server instance
     */
    public static IXMPServer getInstance() {
        if (instance == null) {
            instance = new IXMPServerImpl();
        }
        return instance;
    }

    /**
     * Start the server thread.
     */
    public void start(String hostName, int portNumber) {
        if (isStarted) {
            return;
        } else {
            isStarted = true;
            super.setLogger(new XMPLogger());
            super.setPort(portNumber);
            super.setHost(hostName);
            super.start();
        }
    }
}
