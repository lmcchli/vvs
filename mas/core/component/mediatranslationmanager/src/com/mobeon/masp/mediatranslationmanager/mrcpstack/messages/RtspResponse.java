/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is an MRCP/RTSP response message.
 */
public class RtspResponse extends RtspMessage {
    private int statusCode;
    private String statusText;

    /**
     * The constructor.
     * @param statusCode the request response status code.
     * @param statusText the request response status text.
     */
    public RtspResponse(int statusCode, String statusText) {
        super(MessageType.RTSP_RESPONSE);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    /**
     * The header of the RTSP response message.
     * @return the heading line.
     */
    public String getHeader() {
        return rtspVersion + " " + statusCode + " " + statusText;
    }

    /**
     * Response status code getter.
     * @return the status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Response status text getter.
     * @return the status text.
     */
    public String getStatusText() {
        return statusText;
    }
}
