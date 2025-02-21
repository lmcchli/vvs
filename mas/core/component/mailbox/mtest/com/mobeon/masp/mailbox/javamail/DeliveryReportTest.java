/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.search.TypeCriteria;

/**
 * @author qhast
 */
public class DeliveryReportTest extends ConnectedMailboxTest {


    public DeliveryReportTest(String name) {
        super(name);
    }


    public void testGetMessageContent() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("deliveryReportTest");

        IStoredMessageList mlist = folder.getMessages();

        for(IStoredMessage m : mlist) {
            logger.debug("***"+m+"***");
            logger.debug(" subject="+m.getSubject());
            logger.debug(" type="+m.getType());
            logger.debug(" deliveryStatus="+m.getDeliveryReport());
            logger.debug(" deliveryReport="+m.isDeliveryReport());
            logger.debug(" forward="+m.isForward());
            logger.debug(" receivedDate="+m.getReceivedDate());
        }

        mbox.close();

    }

    /**
     * Manual basic test case to be able to see that delivery reports already fetched will not be fetched again
     * @throws Exception
     */
    public void testLazyRetrieval() throws Exception {
        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("deliveryReportTest");

        folder.searchMessages(new TypeCriteria(MailboxMessageType.VOICE));
        folder.searchMessages(new TypeCriteria(MailboxMessageType.EMAIL));
    }


}
