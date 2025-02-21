/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP pause request message.
 */
public class PauseRequest extends RtspRequest {
    /**
     * The constructor.
     */
    public PauseRequest() {
        super("ANNOUNCE", false);
        MrcpMessage mrcp = new MrcpRequest("PAUSE");
        setMrcpMessage(mrcp);
    }
}
