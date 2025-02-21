/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.mock;

import jakarta.mail.Quota;

import jakarta.mail.MessagingException;


/**
 * IMAPStore interface for mocking purposes
 *
 * @author mande
 */
public interface ImapStoreInterface extends StoreInterface {
    Quota[] getQuota(String root) throws MessagingException;
}
