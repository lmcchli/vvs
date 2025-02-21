/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.imap;

import com.mobeon.masp.mailbox.QuotaUsageInventory;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.QuotaName;

/**
 * @author Håkan Stolt
 */
public abstract class ImapQuotaUsageInventory<T extends IMailbox, C extends ImapContext> extends QuotaUsageInventory<T,C> {

    protected ImapQuotaUsageInventory(T mailbox, C context) {
        super(mailbox, context);
    }

    protected String composeQuotaName(QuotaName name) {
        switch(name) {
            case TOTAL:
                return TotalQuotaNameTemplate.parse(getContext());
            default:
                // Since this assumes that QuotaName enum has been extended, this is hard to test
                throw new IllegalArgumentException("composeQuotaName("+name+") is not implemented!");
        }
    }

    static class TotalQuotaNameTemplate {

        private static final String ACCOUNTID_VAR_STRING = "${accountid}";
        private static final String ACCOUNTID_REGEXP = "[$][{]accountid[}]";
        private static final String EMAILADDRESS_VAR_STRING = "${email}";
        private static final String EMAILADDRESS_REGEXP = "[$][{]email[}]";

        static final String DEFAULT_TEMPLATE_STRING = "user/"+ACCOUNTID_VAR_STRING;

        private static String parse(ImapContext context) {
            String result = context.getImapProperties().getTotalQuotaNameTemplate();
            result = result.replaceAll(ACCOUNTID_REGEXP,context.getMailboxProfile().getAccountId());
            result = result.replaceAll(EMAILADDRESS_REGEXP,context.getMailboxProfile().getEmailAddress());
            return result;
        }


    }

}
