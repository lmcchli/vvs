/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport.udp;

import com.mobeon.masp.callmanager.component.environment.EnvironmentConstants;

import java.io.IOException;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public class UdpFactory {

    public static UdpConnection createUdpServer(
            String localHost, int localPort)
            throws IOException {
        UdpConnection udpServer = new UdpConnection(
                localHost, localPort,
                EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);
        udpServer.start();
        return udpServer;
    }
}
