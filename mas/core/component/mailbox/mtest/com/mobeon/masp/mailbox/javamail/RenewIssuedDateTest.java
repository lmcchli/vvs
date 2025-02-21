/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.search.UrgentCriteria;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

import java.util.Date;

/**
 * @author qhast
 */
public class RenewIssuedDateTest extends ConnectedMailboxTest {


    public RenewIssuedDateTest(String name) {
        super(name);
    }


    public void testRenewIssuedDateSimple() throws Exception {

        mailboxProfile = new MailboxProfile("302102054","abcd","302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("renewIssuedDateTest");
        IStoredMessageList mlist = folder.getMessages();
        IStoredMessage m = mlist.get(0);
        Date receivedDate1 = m.getReceivedDate();
        m.renewIssuedDate();
        assertEquals("Recieved date has changed after calling IStroredMessage.renewIssuedDate().",receivedDate1,m.getReceivedDate());
        m.setState(StoredMessageState.SAVED);
        m.saveChanges();
        mbox.close();

        IMailbox mbox2 = accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);
        IFolder folder2 = mbox2.getFolder("renewIssuedDateTest");
        IStoredMessageList mlist2 = folder2.getMessages();
        assertEquals("Folder should not contain more messages", mlist.size(), mlist2.size());
        IStoredMessage m2 = mlist2.get(0);
        Date receivedDate2 = m2.getReceivedDate();
        assertEquals("Recieved date has changed after calling IStroredMessage.renewIssuedDate().",receivedDate1,receivedDate2);
        assertEquals("Message state should be " + StoredMessageState.SAVED, StoredMessageState.SAVED, m2.getState());
        m2.setState(StoredMessageState.READ);
        mbox2.close();
    }

    public void testRenewIssuedDateComplex() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        IFolder folder = mbox.getFolder("renewIssuedDateTest");
        IStoredMessageList mlist = folder.getMessages();
        mbox.getQuotaUsageInventory();
        IFolder folder2 = mbox.getFolder("inbox");
        folder2.searchMessages(UrgentCriteria.URGENT);
        for(IStoredMessage m : mlist) {
            m.renewIssuedDate();
        }

        mbox.close();

    }

}
