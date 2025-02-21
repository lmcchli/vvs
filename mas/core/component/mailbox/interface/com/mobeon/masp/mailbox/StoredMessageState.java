/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * A mailbox message state.
 * The state of a mailbox message can be set to mark if message
 * is deleted, saved, read or new.
 * A mailbox message can be in one of the following states:
 * <ul>
 * <li>{@link #DELETED}</li>
 * <li>{@link #SAVED}</li>
 * <li>{@link #READ}</li>
 * <li>{@link #NEW}</li>
 * </ul>
 * A mailbox message can be in only one state at a given point in time.
 */
 public enum StoredMessageState {

    /**
     * Message is marked for deletion.
     */
    DELETED,

    /**
     * Message is marked to be saved.
     */
    SAVED,

    /**
     * Message is marked as read.
     */
    READ,

    /**
     * Message is not marked at all.
     */
    NEW
}
