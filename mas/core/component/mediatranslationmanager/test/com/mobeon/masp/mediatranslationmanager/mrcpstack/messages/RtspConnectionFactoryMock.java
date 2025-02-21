/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnectionFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnection;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.RtspConnectionMock;

public class RtspConnectionFactoryMock implements RtspConnectionFactory {
    public RtspConnection create(String hostName, int portNumber) {
        return new RtspConnectionMock();
    }
}
