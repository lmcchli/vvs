/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mfs;

import jakarta.mail.Flags;

/**
 * Convinence class with JavaMail Flags for expressing the stored message state.
 * @author qhast
 */
public class MfsFlags {

    /**
     * These flags MUST be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#DELETED}
     */
    public static final Flags DELETED_SET_FLAGS;

    /**
     * These flags MUST NOT be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#NEW}
     */
    public static final Flags NEW_NOT_SET_FLAGS;

    /**
     * These flags MUST NOT be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#READ}
     */
    public static final Flags READ_NOT_SET_FLAGS;

    /**
     * These flags MUST be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#READ}
     */
    public static final Flags READ_SET_FLAGS;

    /**
     * These flags MUST NOT be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#SAVED}
     */
    public static final Flags SAVED_NOT_SET_FLAGS;

    /**
     * These flags MUST be set to consider the message as {@link com.mobeon.masp.mailbox.StoredMessageState#SAVED}
     */
    public static final Flags SAVED_SET_FLAGS;

    static {

        DELETED_SET_FLAGS = new Flags(Flags.Flag.DELETED);

        NEW_NOT_SET_FLAGS = new Flags();
        NEW_NOT_SET_FLAGS.add(Flags.Flag.SEEN);
        NEW_NOT_SET_FLAGS.add(Flags.Flag.DELETED);
        NEW_NOT_SET_FLAGS.add("Saved");

        READ_NOT_SET_FLAGS = new Flags();
        READ_NOT_SET_FLAGS.add("Saved");
        READ_NOT_SET_FLAGS.add(Flags.Flag.DELETED);

        READ_SET_FLAGS = new Flags();
        READ_SET_FLAGS.add(Flags.Flag.SEEN);

        SAVED_NOT_SET_FLAGS = new Flags(Flags.Flag.DELETED);
        SAVED_SET_FLAGS = new Flags("Saved");

    }



}
