/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Represents a mailbox.
 * @author qhast
 */
public interface IMailbox extends IFolderParent {


    /**
     * Closes this mailbox if open and releases all allocated resources.
     * Deletes all messages marked for deletion.
     * @throws MailboxException if a problem occur.
     * @see StoredMessageState
     */
    public void close() throws MailboxException;



    /**
     * Makes an inventory of the current usage of all configured
     * mailbox quotas. The inventory object is complete after method
     * has returned the inventory object.
     * @return a inventory object.
     * @throws MailboxException
     */
    public IQuotaUsageInventory getQuotaUsageInventory() throws MailboxException;

}
