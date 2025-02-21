/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP SET-PARAMS request message.
 */
public class SetParamsRequest extends RtspRequest {
    /**
     * Default constructor.
     */
    public SetParamsRequest() {
        super("ANNOUNCE", false);
        MrcpMessage mrcp = new MrcpRequest("SET-PARAMS");
        setMrcpMessage(mrcp);
    }

    /**
     * A constructor.
     * @param contentType MRCP message content type
     * @param content MRCP message content
     */
    public SetParamsRequest(String contentType, String content) {
        super("ANNOUNCE", false);
        MrcpMessage mrcp = new MrcpRequest("SET-PARAMS");
        mrcp.setContent(contentType, content);
        setMrcpMessage(mrcp);
    }
}
