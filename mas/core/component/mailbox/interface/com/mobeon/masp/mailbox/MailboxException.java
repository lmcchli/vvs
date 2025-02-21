/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The base class for all exceptions thrown by the Mailbox classes
 */
public class MailboxException extends Exception {

    /**
     * The set of recipients for which message delivery to their mailbox failed.
     */
    private Set<String> failedRecipientsSet = new HashSet<String>();

    public MailboxException(String message) {
        super(message);
    }

    public MailboxException(String message, String[] failedRecipients, Throwable t) {
        super(message+" : "+t.getClass().getName()+(t.getMessage()!=null?": "+t.getMessage():""));

        if (failedRecipients != null) {
            failedRecipientsSet.addAll(Arrays.asList(failedRecipients));
        }
    }

    public MailboxException(String message, Throwable t) {
        this(message, null, t);
    }

    public String[] getFailedRecipients() {
        return failedRecipientsSet.toArray(new String[failedRecipientsSet.size()]);
    }
}
