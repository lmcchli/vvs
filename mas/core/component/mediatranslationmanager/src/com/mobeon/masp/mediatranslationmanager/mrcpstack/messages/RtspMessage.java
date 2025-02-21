/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

import com.mobeon.sdp.SessionDescription;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a base class for RTSP messages.
 */
public abstract class RtspMessage {
    public static final String rtspVersion = "RTSP/1.0";
    public static final String nl = "\r\n";
    // TODO: this should be configurable (?)
    public final String synthesizerPath = "/media/speechsynthesizer";
    public final String recognizerPath = "/media/speechrecognizer";
    public final String protocol = "rtsp";
    private MrcpMessage mrcpMessage = null;
    private SessionDescription sessionDescription = null;
    MessageType messageType;
    protected LinkedHashMap<String, String> headerFields = new LinkedHashMap<String, String>();

    /**
     * A constructor.
     * @param messageType the type ID of the RTSP message.
     */
    public RtspMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Getter for the message header.
     * @return the message header.
     */
    public abstract String getHeader();

    /**
     * Appending/setting a message header field in an MRCP/RTSP message.
     * @param name the name of the field.
     * @param value the value of the field.
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
     * Returns the message content. The message content is a sub message.
     * <p>
     * An MRCP message is for example a sub message to a RTP ANNOUNCE.
     */
    public MrcpMessage getMrcpMessage() {
        return mrcpMessage;
    }

    /**
     * Setting the message content. The message content is a sub message.
     * <p>
     * An MRCP message is for example a sub message to a RTP ANNOUNCE.
     */
    public void setMrcpMessage(MrcpMessage mrcpMessage) {
        this.mrcpMessage = mrcpMessage;
    }

    /**
     * A getter for the session description (SDP) of the message.
     * @return a SessionDescription object or <code>null</code>.
     */
    public SessionDescription getSDP() {
        return sessionDescription;
    }

    /**
     * A setter for the session description (SDP) of the message.
     * @param sdp a SessionDescription object.
     */
    public void setSDP(SessionDescription sdp) {
        sessionDescription = sdp;
    }

    /**
     * This method compiles the header, header fields, session description and sub messages
     * into an RTSP message (MRCP is a sub message in an RTSP message).
     * @return a String containing the message
     */
    public String getMessage() {
        // Collecting message content which can either be session description
        // or an MRCP message.
        String content = "";
        if (sessionDescription != null) {
            // Content is session description
            content = sessionDescription.toString();
            // Append/update the content information in the header fields
            // with description of content type and the size of the content.
            if (content.length() > 0) {
                setHeaderField("Content-Type", "application/sdp");
                setHeaderField("Content-Length", "" + (content.length()+2));
            }
        } else if (getMrcpMessage() != null) {
            // Content is a sub message (MRCP)
            content = getMrcpMessage().getMessage();
            // Append/update the content information in the header fields
            // with description of content type and the size of the content.
            if (content.length() > 0) {
                setHeaderField("Content-Type", "application/mrcp");
                setHeaderField("Content-Length", "" + (content.length()+2));
            }
        }
        // The message header
        String message = getHeader() + nl;

        // Get the header field list (String) and append it to the message.
        message += getHeaderFields();
        // The previously collected content is appended to the message
        // (if any).
        if (content.length() > 0) {
            message += nl;
            message += content;
        }

        // A message (and sub massage) is ended/terminated by "\r\n".
        message += nl;
        return message;
    }

    /**
     * Generates the message as a string.
     * @return the same as <code>getMessage()</code>
     */
    public String toString() {
        return getMessage();
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
