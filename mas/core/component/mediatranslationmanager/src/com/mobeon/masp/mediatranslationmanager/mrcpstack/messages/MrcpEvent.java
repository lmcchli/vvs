/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * MrcpEvents are issued by the MRCP server.
 */
public class MrcpEvent extends MrcpMessage {
    private String eventName;
    private int requestId;
    private String requestState;

    /**
     * The constructor.
     * @param eventName the type name of the event.
     * @param requestId the request which is currently in process.
     * @param requestState the current state of the processed request.
     */
    public MrcpEvent(String eventName, int requestId, String requestState) {
        super(MessageType.MRCP_EVENT);
        this.eventName = eventName;
        this.requestId = requestId;
        this.requestState = requestState;
    }

    /**
     * Event name getter.
     * @return the event type name.
     */
    public String getName() {
        return eventName;
    }

    /**
     * Event header header getter.
     * @return returns the heading line of the event message header.
     */
    public String getHeader() {
        return eventName + " " + requestId + " " + requestState + " " + mrcpVersion;
    }

    /**
     * Request state getter.
     * @return the state of the request which is processed (that caused the event).
     */
    public String getRequestState() {
        return requestState;
    }
}
