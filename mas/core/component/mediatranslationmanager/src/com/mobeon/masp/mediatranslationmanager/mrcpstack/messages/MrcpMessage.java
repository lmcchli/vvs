/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This a base class for the MRCP messages.
 */
public abstract class MrcpMessage {
    public static final String mrcpVersion = "MRCP/1.0";
    public static final String nl = "\r\n";
    public static final int SUCCESS = 0;
    public static final int NO_MATCH = 1;
    public static final int NO_INPUT_TIMEOUT = 2;
    public static final int RECOGNITION_TIMEOUT = 3;
    private MessageType messageType;
    private String content = "";
    private String contentType = "";


    protected LinkedHashMap<String, String> headerFields = new LinkedHashMap<String, String>();

    /**
     * The constructor.
     * @param messageType message type ID.
     */
    public MrcpMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Header line getter.
     * @return returns the first header line.
     */
    public abstract String getHeader();

    /**
     * Message type name getter.
     * @return the type name of the message.
     */
    public abstract String getName();

    /**
     * Message getter.
     * @return the message as a string.
     */
    public String getMessage() {
        // Calculates content type and size (in case it is not done already).
        if (content.length() > 0) {
            setHeaderField("Content-Type", contentType);
            setHeaderField("Content-Length", "" + (content.length() + 2));
        }
        // Assembling the message starting with the header.
        String message = getHeader() + nl;
        // Appending the header fields.
        message += getHeaderFields();
        if (content.length() > 0) {
            // Appending content separator.
            message += nl;
            // Appending content
            message += content;
        }
        // Appending trailer/end-of-message
        message += nl;

        // Returning the assembled message
        return message;
    }

    /**
     * Content information setter.
     * @param contentType content type ID.
     * @param content data.
     */
    public void setContent(String contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }

    /**
     * Content data getter.
     * @return content data.
     */
    public String getContent() {
        return content;
    }

    /**
     * Header field setter.
     * @param name field name.
     * @param value field value.
     */
    public void setHeaderField(String name, String value) {
        headerFields.put(name, value);
    }

    /**
     * Retrieves the value of a header field.
     * @param name the name of the field.
     * @return a String containing the value or <code>null</code> if the
     * field was not found
     */
    public String getHeaderField(String name) {
        return headerFields.get(name);
    }

    /**
     * This method collects all the message header fields in a String formatted
     * according to the MRCP/RTSP specifications.
     * @return a String containing the header field list.
     */
    public String getHeaderFields() {
        String fields = "";

        for (Map.Entry<String, String> field : headerFields.entrySet()) {
            fields += field.getKey() + ": " + field.getValue() + nl;
        }
        return fields;
    }

    /**
     * Message type ID getter.
     * @return message type ID.
     */
    public MessageType getMessageType() {
        return messageType;
    }
}
