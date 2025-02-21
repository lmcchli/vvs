/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.compare.ConfidentialComparator;
import com.mobeon.masp.mailbox.compare.UrgentComparator;
import com.mobeon.masp.mailbox.search.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * @author qhast
 */
public class SearchAndSortMessageTest extends ConnectedMailboxTest {


    public SearchAndSortMessageTest(String name) {
        super(name);
    }


    public void testSearchUrgentMessages() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("urgentTest");
        IStoredMessageList mlist = folder.searchMessages(UrgentCriteria.URGENT, ConfidentialComparator.CONFIDENTIAL_FIRST);

        int mNumber = 0;
        for(IStoredMessage m : mlist) {
            logger.debug("Message #"+mNumber+++":"+m);
            logger.debug(" urgent:       "+m.isUrgent());
            logger.debug(" confidential: "+m.isConfidential());
            }
        mbox.close();
    }

    public void testSearchConfidentialMessages() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("urgentTest");
        IStoredMessageList mlist = folder.searchMessages(ConfidentialCriteria.CONFIDENTIAL, UrgentComparator.URGENT_FIRST);

        int mNumber = 0;
        for(IStoredMessage m : mlist) {
            logger.debug("Message #"+mNumber+++":"+m);
            logger.debug(" urgent:       "+m.isUrgent());
            logger.debug(" confidential: "+m.isConfidential());
            }

        mbox.close();
    }

    public void testSearchMessages1() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("urgentTest");

        Criteria<MessagePropertyCriteriaVisitor> criteria = new AndCriteria(
                ConfidentialCriteria.CONFIDENTIAL,
                new OrCriteria(TypeCriteria.VIDEO,TypeCriteria.VOICE),
                UrgentCriteria.NON_URGENT                
        );

        IStoredMessageList mlist = folder.searchMessages(criteria, UrgentComparator.URGENT_FIRST);

        int mNumber = 0;
        for(IStoredMessage m : mlist) {
            logger.debug("Message #"+mNumber+++":"+m);
            logger.debug(" urgent:       "+m.isUrgent());
            logger.debug(" confidential: "+m.isConfidential());
            }

        mbox.close();
    }

}
