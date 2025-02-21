/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is an RTSP request message.
 */
public class RtspRequest extends RtspMessage {
    private String url = "";
    private String command;
    static int sequenceCounter;
    boolean isRecognition = false;

    static {
        // Initialization of the sequence counter.
        sequenceCounter = 0;
    }

    /**
     * A constructor.
     * The constructor intializes an RTSP request message from the given request type name
     * and a boolean which states if the message is intended for the speech recognizer or
     * synthesizer.
     * The message is supposed to be used as an outgoing message.
     * @param command the request type name.
     * @param isRecognition the recognizer or synthesizer flag.
     */
    public RtspRequest(String command, boolean isRecognition) {
        super(MessageType.RTSP_REQUEST);
        this.command = command;
        this.isRecognition = isRecognition;
        sequenceCounter++;
        setHeaderField("CSeq", "" + sequenceCounter);
     }

    /**
     * A constructor.
     * The constructor is supposed to initialize an incoming RTSP request message.
     * @param command the command type name.
     * @param url the receiver of the request.
     */
    public RtspRequest(String command, String url) {
        super(MessageType.RTSP_REQUEST);
        this.command = command;
        this.url = url;
     }

    /**
     * Setter for the service URL.
     * @param host the URL host part.
     * @param port the URL port part.
     */
    public void setUrl(String host, int port) {
        String path = isRecognition ? recognizerPath : synthesizerPath;
        url = protocol + "://" + host + ":" + port + path;
    }

    /**
     * Request header getter.
     * @return the request header.
     */
    public String getHeader() {
        return command + " " + url + " " + rtspVersion;
    }

    /**
     * Resets the sequence counter to zero (0).
     * Intended for test purposes only.
     */
    public static void reset() {
        sequenceCounter = 0;
    }

    public String getCommand() {
        return command;
    }
}
