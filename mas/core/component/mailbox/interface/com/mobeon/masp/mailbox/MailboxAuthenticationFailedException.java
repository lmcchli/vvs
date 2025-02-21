/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Thrown from the Mailbox account manager when authentication fails.
 * @author qhast
 * @see IMailboxAccountManager
 */
public class MailboxAuthenticationFailedException extends MailboxException {

    private String host;
    private String accountId;

    public MailboxAuthenticationFailedException(String host, String accountId) {
        super("Authentication failed for "+accountId+"@"+host+".");
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
