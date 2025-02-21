/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.search.ConfidentialCriteria;

/**
 * @author qhast
 */
public class CopyTest extends ConnectedMailboxTest {

    private IFolder sourceFolder;
    private IFolder targetFolder;

    public CopyTest(String name) {
        super(name);
    }


    public void testSimpleCopyMessage() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        sourceFolder = mbox.getFolder("inbox");
        targetFolder = mbox.getFolder("copyTest");
        IStoredMessageList targetList = targetFolder.getMessages();
        IStoredMessageList sourceList = sourceFolder.getMessages();
        IStoredMessage m = sourceList.get(0);
        m.copy(targetFolder);
        IStoredMessageList targetListAfter = targetFolder.getMessages();
        assertEquals(1, targetListAfter.size()-targetList.size());

        mbox.close();

    }

    public void testComplexCopyMessage() throws Exception {

        mailboxProfile = new MailboxProfile("302102054", "abcd", "302102054@lab.mobeon.com");
        mbox = getMailbox();

        sourceFolder = mbox.getFolder("inbox");
        mbox.getQuotaUsageInventory();
        targetFolder = mbox.getFolder("copyTest");

        IStoredMessageList sourceList = sourceFolder.getMessages();
        targetFolder.searchMessages(ConfidentialCriteria.CONFIDENTIAL);

        IStoredMessage message = sourceList.get(0);
        message.copy(targetFolder);

        IStoredMessageList targetList = targetFolder.searchMessages(ConfidentialCriteria.CONFIDENTIAL);
        mbox.getQuotaUsageInventory();
        for(IStoredMessage storedMessage : targetList) {
            storedMessage.getSpokenNameOfSender();            
        }

        message.getSpokenNameOfSender();

        mbox.close();

    }

}
