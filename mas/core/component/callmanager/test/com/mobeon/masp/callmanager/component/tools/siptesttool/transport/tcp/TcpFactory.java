/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport.tcp;

import com.mobeon.masp.callmanager.component.environment.EnvironmentConstants;

import java.io.IOException;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public class TcpFactory {

    public static TcpServer createTcpServer(
            String localHost, int localPort)
            throws IOException {
        TcpServer tcpServer = new TcpServer(localHost, localPort,
                EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);
        tcpServer.start();
        return tcpServer;
    }

    public static TcpConnection createTcpClient(
            String remoteHost, int remotePort, String localHost)
            throws IOException {
        TcpConnection tcpClient = new TcpConnection(
                remoteHost, remotePort, localHost,
                EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);
        tcpClient.start();
        return tcpClient;
    }
}
