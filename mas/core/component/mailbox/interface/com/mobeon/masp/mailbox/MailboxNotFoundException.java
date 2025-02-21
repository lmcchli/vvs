/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Thrown from the Mailbox account manager when a mailbox not can be found.
 * @author qhast
 * @see IMailboxAccountManager
 */
public class MailboxNotFoundException extends MailboxException {

    private String host;
    private String accountId;

    public MailboxNotFoundException(String host, String accountId) {
        super(accountId+"@"+host+" not found.");
        this.host = host;
        this.accountId = accountId;
    }

    public String getHost() {
        return host;
    }

    public String getAccountId() {
        return accountId;
    }
}
