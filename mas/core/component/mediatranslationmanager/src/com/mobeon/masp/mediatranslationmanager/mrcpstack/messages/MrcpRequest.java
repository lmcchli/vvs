/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is an MRCP request message.
 */
public class MrcpRequest extends MrcpMessage {
    private String method;
    private int requestId;
    private static int requestCounter;

    static {
        // The request counter is used to generate request IDs
        requestCounter = 0;
    }

    /**
     * A constructor.
     * This constructor creates an outgoing MRCP request.
     * @param method the type name of the request.
     */
    public MrcpRequest(String method) {
        super(MessageType.MRCP_REQUEST);
        this.method = method;
        requestId = ++requestCounter;
    }

    /**
     * A constructor.
     * This constructor creates an incloming MRCP request.
     * @param method the type name of the request.
     * @param requestId the ID of the request.
     */
    public MrcpRequest(String method, int requestId) {
        super(MessageType.MRCP_REQUEST);
        this.method = method;
        this.requestId = requestId;
    }

    /**
     * Type name getter.
     * @return the request type name.
     */
    public String getName() {
        return method;
    }

    /**
     * Request header getter.
     * @return the header.
     */
    public String getHeader() {
        return method + " " + requestId + " " + mrcpVersion;
    }

    /**
     * Resets the request counter to zero (0).
     * Intended for test purposes only.
     */
    public static void reset() {
        requestCounter = 0;
    }

    /**
     * Request ID getter.
     * @return the request ID.
     */
    public int getRequestId() {
        return requestId;
    }
}
