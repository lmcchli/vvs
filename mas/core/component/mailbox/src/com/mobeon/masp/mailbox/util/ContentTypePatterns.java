/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.util;

import com.mobeon.masp.mailbox.MailboxMessageType;
import static com.mobeon.masp.mailbox.MailboxMessageType.*;

import java.util.regex.Pattern;

/**
 * A collection of compiled Content-Type string patterns. 
 * @author qhast
 */
public class ContentTypePatterns {


    /**
     * Compiled pattern for voice message ContentType string.
     */
    public static final Pattern VOICE_MESSAGE_PATTERN =
            Pattern.compile(getContentType(VOICE)+".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Compiled pattern for video message ContentType string.
     */
    public static final Pattern VIDEO_MESSAGE_PATTERN =
            Pattern.compile(getContentType(VIDEO)+".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Compiled pattern for fax message ContentType string.
     */
    public static final Pattern FAX_MESSAGE_PATTERN =
            Pattern.compile(getContentType(FAX)+".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Compiled pattern for delivery report ContentType string.
     */
    public static final Pattern DELIVERY_REPORT_PATTERN =
            Pattern.compile("multipart/report; report-type=delivery-status.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            
    public static final Pattern DELIVERY_REPORT_MULTIPART_PATTERN =
            Pattern.compile("multipart/report;.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            
    public static final Pattern DELIVERY_REPORT_REPORT_TYPE_PATTERN =
            Pattern.compile(".*report-type=delivery-status.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);            
//DELIVERY_REPORT_MULTIPART_PATTERN            "multipart/report;.*"
//DELIVERY_REPORT_REPORT_TYPE_PATTERN          "report-type=delivery-status"            
            

    /**
     * Compiled pattern for delivery status part ContentType string.
     */
    public static final Pattern DELIVERY_STATUS_PART_PATTERN =
            Pattern.compile("message/delivery-status.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Compiled pattern for RFC822 message ContentType string.
     */
    public static final Pattern RFC822_MESSAGE_PATTERN =
            Pattern.compile("message/rfc822.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            
    /**
     * Compiled pattern for RFC822-Headers ContentType string.
     */
    public static final Pattern RFC822_HEADERS_PATTERN =
            Pattern.compile("message/rfc822-headers.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);            

    /**
     * Compiled pattern for multipart ContentType string.
     */
    public static final Pattern MULTIPART_MESSAGE_PATTERN =
            Pattern.compile("multipart/.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    /**
     * Gets the multipart subtype string for the given Mailbox message type.
     * @param type
     * @return subtype string.
     */
    public static String getMultipartSubType(MailboxMessageType type) {
        switch(type) {
            case VOICE:
                return "voice-message";
            case VIDEO:
                return "x-video-message";
            case FAX:
                return "fax-message";    
            default:
                return "mixed";
        }
    }

    /**
     * Gets the content type string for the given Mailbox message type.
     * @param type
     * @return content type string.
     */
    public static String getContentType(MailboxMessageType type) {
        return "multipart/"+getMultipartSubType(type);
    }


}
