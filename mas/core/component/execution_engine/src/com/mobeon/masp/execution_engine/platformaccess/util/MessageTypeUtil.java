/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import com.mobeon.masp.mailbox.DeliveryStatus;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;

/**
 * Utility class. Contains different functions to convert strings into some typed classes that is defined in the external
 * interfaces. Also to get the string representation for such a class.
 *
 * @author ermmaha
 */
public class MessageTypeUtil {
    private final static String VOICE = "voice";
    private final static String VIDEO = "video";
    private final static String FAX = "fax";
    private final static String EMAIL = "email";
    private final static String NEW = "new";
    private final static String READ = "read";
    private final static String SAVED = "saved";
    private final static String DELETED = "deleted";
    private final static String STORE_FAILED = "store-failed";
    private final static String PRINT_FAILED = "print-failed";

    /**
     * Converts a string to a MailboxMessageType enum
     *
     * @param value
     * @return one the MailboxMessageType enums
     */
    public static MailboxMessageType stringToMessageType(String value) {
        if (value.equals(VOICE)) {
            return MailboxMessageType.VOICE;
        } else if (value.equals(VIDEO)) {
            return MailboxMessageType.VIDEO;
        } else if (value.equals(FAX)) {
            return MailboxMessageType.FAX;
        } else if (value.equals(EMAIL)) {
            return MailboxMessageType.EMAIL;
        }
        throw new IllegalArgumentException("Wrong argument to stringToMessageType " + value);
    }

    /**
     * Retrieves the string value for a MailboxMessageType enum
     *
     * @param value
     * @return one of the strings voice, video, fax, email
     */
    public static String messageTypeToString(MailboxMessageType value) {
        if (value == MailboxMessageType.VOICE) {
            return VOICE;
        } else if (value == MailboxMessageType.VIDEO) {
            return VIDEO;
        } else if (value == MailboxMessageType.FAX) {
            return FAX;
        }
        return EMAIL;
    }

    /**
     * Converts a string to a StoredMessageState enum
     *
     * @param value
     * @return one the StoredMessageState enums
     */
    public static StoredMessageState stringToMessageState(String value) {
        if (value.equals(NEW)) {
            return StoredMessageState.NEW;
        } else if (value.equals(READ)) {
            return StoredMessageState.READ;
        } else if (value.equals(SAVED)) {
            return StoredMessageState.SAVED;
        } else if (value.equals(DELETED)) {
            return StoredMessageState.DELETED;
        }
        throw new IllegalArgumentException("Wrong argument to stringToMessageState " + value);
    }

    /**
     * Retrieves the string value for a StoredMessageState enum
     *
     * @param value
     * @return one of the strings new, read, saved, deleted
     */
    public static String messageStateToString(StoredMessageState value) {
        if (value == StoredMessageState.NEW) {
            return NEW;
        } else if (value == StoredMessageState.READ) {
            return READ;
        } else if (value == StoredMessageState.SAVED) {
            return SAVED;
        }
        return DELETED;
    }

    /**
     * Retrieves the string value for a DeliveryStatus enum
     *
     * @param value
     * @return one of the strings store-failed, print-failed
     */
    public static String deliveryStatusToString(DeliveryStatus value) {
        if (value == DeliveryStatus.STORE_FAILED) {
            return STORE_FAILED;
        }
        return PRINT_FAILED;
    }
}
