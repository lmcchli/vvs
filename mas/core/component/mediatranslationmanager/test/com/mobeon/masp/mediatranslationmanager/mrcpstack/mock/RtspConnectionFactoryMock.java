/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnection;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnectionFactory;

public class RtspConnectionFactoryMock implements RtspConnectionFactory {
    ServerMock serverMock = null;

    public RtspConnection create(String hostName, int portNumber) {
        RtspConnectionMock rtspConnection = new RtspConnectionMock();
        if (serverMock != null) rtspConnection.setMessageHandler(serverMock);
        return rtspConnection;
    }

    public void setMessageHandler(ServerMock serverMock) {
        this.serverMock = serverMock;
    }

}
