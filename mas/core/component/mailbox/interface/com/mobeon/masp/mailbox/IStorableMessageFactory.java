/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Factory for storable messages.
 * @author QHAST
 */
public interface IStorableMessageFactory {
    /**
     * Creates a new storable messsage.
     * @return
     * @throws MailboxException if a storbable message not could be created of some reason.
     */
    public IStorableMessage create() throws MailboxException;
}
