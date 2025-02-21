/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

public class NotifierConstants {

    public static final String PHONEON_LOCK_FILE = "phoneon.lock";
    
    public static final String DATE_FORMAT = "yyyyMMdd_HH_mm_ss_SSS";

    /**
     * Event properties names.
     */
    public static final String NOTIFIER_TYPE_NAME_PROPERTY = "ntn";
    public static final String NOTIFIER_CPHR_TEMPLATE_NAME = "template_name";
    public static final String SENDER_VISIBILITY_PROPERTY = "sender_visibility";
    public static final String SENDER_PHONE_NUMBER_PROPERTY = "sender";
    public static final String SENDER_DISPLAY_NAME_PROPERTY = "senderdname";
    public static final String RECEIVER_PHONE_NUMBER_PROPERTY = "rnb";
    public static final String NOTIFICATION_PHONE_NUMBER_PROPERTY = "nnb";
    public static final String URGENT_PROPERTY = "urg";
    public static final String DATE_PROPERTY = "date";
    public static final String MESSAGE_PAYLOAD_FILE_PROPERTY = "eventfile";
    public static final String ACQUIRED_SENDING_FILE = "asf"; //used to indicate if this event created the in progress file.
    
}
