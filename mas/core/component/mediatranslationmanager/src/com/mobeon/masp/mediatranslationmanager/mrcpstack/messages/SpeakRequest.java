/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is an MRCP request message for a TTS speak.
 */
public class SpeakRequest extends RtspRequest {
    /**
     * The constructor.
     * Initializes a SPEAK request with the text which is to be spoken.
     * @param mimeType the mime type of the text (speech)
     * @param text the text to be spoken.
     */
    public SpeakRequest(String mimeType, String text) {
        super("ANNOUNCE", false);
        MrcpMessage mrcp = new MrcpRequest("SPEAK");
        mrcp.setContent(mimeType, text);
        setMrcpMessage(mrcp);
    }
}
