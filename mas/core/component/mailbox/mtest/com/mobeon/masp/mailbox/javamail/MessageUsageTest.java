/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.search.ConfidentialCriteria;
import com.mobeon.masp.mailbox.search.TypeCriteria;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.io.InputStream;

/**
 * @author qhast
 */
public class MessageUsageTest extends ConnectedMailboxTest {

    public MessageUsageTest(String name) {
        super(name);
    }

    public void testGetQuotas() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IQuotaUsageInventory quotaUsageInventory = mbox.getQuotaUsageInventory();
        for(QuotaUsage q : quotaUsageInventory.getQuotas()) {
            logger.debug(q);
        }
        IQuotaUsageInventory quotaUsageInventory2 = mbox.getQuotaUsageInventory();
        for(QuotaUsage q : quotaUsageInventory2.getQuotas()) {
            logger.debug(q);
        }
        IFolder inbox = mbox.getFolder("inbox");
        IStoredMessageList storedMessageList = inbox.searchMessages(TypeCriteria.VOICE);
        IQuotaUsageInventory quotaUsageInventory3 = mbox.getQuotaUsageInventory();
        for(QuotaUsage q : quotaUsageInventory3.getQuotas()) {
            logger.debug(q);
        }
        //mbox.close();
        for(IStoredMessage message : storedMessageList) {
            IMediaObject spokenNameOfSender = message.getSpokenNameOfSender();
        }

        IQuotaUsageInventory quotaUsageInventory4 = mbox.getQuotaUsageInventory();
        for(QuotaUsage q : quotaUsageInventory4.getQuotas()) {
            logger.debug(q);
        }
        IFolder addressTest = mbox.getFolder("addressTest");
        addressTest.searchMessages(TypeCriteria.VOICE);
        IQuotaUsageInventory quotaUsageInventory5 = mbox.getQuotaUsageInventory();
        for(QuotaUsage q : quotaUsageInventory5.getQuotas()) {
            logger.debug(q);
        }

        mbox.close();

    }

    public void testGetTotalQuota() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        QuotaUsage q = mbox.getQuotaUsageInventory().getQuota(QuotaName.TOTAL);
        logger.debug(q);

        mbox.close();
    }

}
