/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnection;

public class RtspSessionMock extends RtspSession {
    public RtspSessionMock(RtspConnection rtspConnection) {
        super(rtspConnection);
    }

    /**
     * Why do we do this?
     * To simulate receival of MRCP events such as RECOGNITION complete?
     * @param event
     */
    public void receive(MrcpEvent event) {
        RtspRequest request = new RtspRequest("ANNOUNCE", false);
        request.setMrcpMessage(event);
        messageReceiver.receive(request);
    }
}
