/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the response to an MRCP request.
 */
public class MrcpResponse extends MrcpMessage {
    private int requestId;
    private int statusCode;
    private String requestState;

    /**
     * The constructor.
     * @param requestId the request ID.
     * @param statusCode the status code of the request processing.
     * @param requestState the current state of the request (processing of).
     */
    public MrcpResponse(int requestId, int statusCode, String requestState) {
        super(MessageType.MRCP_RESPONSE);
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.requestState = requestState;
    }

    /**
     * Message type name getter.
     * @return the type name of the response.
     */
    public String getName() {
        return requestState;
    }

    /**
     * Response header getter.
     * @return the response message header.
     */
    public String getHeader() {
        return mrcpVersion + " " + requestId + " " + statusCode + " " + requestState;
    }

    /**
     * Request ID getter.
     * @return the ID of the originating request.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Getter for the resulting status of the request.
     * @return the status code (200 is OK, 20n is almost OK, >= 300 is an error).
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Getter for the state of the request (porcessing of).
     * @return the state (se the MRCP spec).
     */
    public String getRequestState() {
        return requestState;
    }
}
