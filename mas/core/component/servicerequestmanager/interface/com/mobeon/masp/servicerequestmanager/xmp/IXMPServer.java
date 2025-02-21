/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

/**
 * Interface for an XMP server.
 *
 * @author mmawi
 */
public interface IXMPServer extends Runnable {
    /**
     * Start listening for connections.
     *
     * @param hostName      The hostname of the server
     * @param portNumber    The listening port of the server
     */
    public void start(String hostName, int portNumber);
}
