/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP RESUME request message.
 */
public class ResumeRequest extends RtspRequest {
    public ResumeRequest() {
        super("ANNOUNCE", false);
        MrcpMessage mrcp = new MrcpRequest("RESUME");
        setMrcpMessage(mrcp);
    }
}

