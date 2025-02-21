/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP STOP request message.
 */
public class StopRequest extends RtspRequest {
    /**
     * The constructor.
     * The constructor takes a falag which indicates if the request is intended for
     * the recognizer (ASR) or the sythesizer (TTS).
     * @param isRecognition true=>ASR and false=>TTS.
     */
    public StopRequest(boolean isRecognition) {
        super("ANNOUNCE", isRecognition);
        MrcpMessage mrcp = new MrcpRequest("STOP");
        setMrcpMessage(mrcp);
    }
}

