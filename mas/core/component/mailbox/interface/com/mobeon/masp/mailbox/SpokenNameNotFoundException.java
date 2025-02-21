/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * The exception is thrown when caller attempts to access a spoken name of sender
 * on a message and no spoken name is set.
 * @author QHAST
 * @see IMailboxMessage
 */
public class SpokenNameNotFoundException extends MailboxException {
    public SpokenNameNotFoundException() {
        super("Spoken name of sender not found.");
    }
}
