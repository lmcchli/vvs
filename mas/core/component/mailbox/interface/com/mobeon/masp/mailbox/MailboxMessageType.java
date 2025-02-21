/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Mailbox message type.
 * The type of a mailbox message can be set to mark
 * which type of message it represents.
 * A mailbox message can be in one of the following typess:
 * <ul>
 * <li>{@link #VOICE}</li>
 * <li>{@link #VIDEO}</li>
 * <li>{@link #FAX}</li>
 * <li>{@link #EMAIL}</li>
 * </ul>
 * A mailbox message can be in only one state at a given point in time.
 */
public enum MailboxMessageType {

    /**
     * Message is marked as voice.
     */
    VOICE,

    /**
     * Message is marked as video.
     */
    VIDEO,

    /**
     * Message is marked as fax.
     */
    FAX,

    /**
     * Message is not marked as email.
     */
    EMAIL
}
