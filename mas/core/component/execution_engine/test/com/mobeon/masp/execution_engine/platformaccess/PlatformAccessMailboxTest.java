/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.UserProvisioningException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

/**
 * Tests the Mailbox related functions in PlatformAccess
 * <p/>
 * User: ermmaha
 */
public class PlatformAccessMailboxTest extends PlatformAccessMailboxBaseTest {

    public PlatformAccessMailboxTest(String name) {
        super(name);
    }

    /**
     * Tests the subscriberGetMailbox function.
     *
     * @throws Exception
     */
    public void testSubscriberGetMailbox() throws Exception {

        PlatformAccess platformAccess1 = createPlatformAccess();
        PlatformAccess platformAccess2 = createPlatformAccess();

        int id = platformAccess1.subscriberGetMailbox("161074");
        assertEquals(id, 0);

        //test that same user get same id also check that getMailbox is not called
        jmockProfileId0.expects(never()).method("getMailbox");
        id = platformAccess2.subscriberGetMailbox("161074");
        assertEquals(id, 0);

        //test another user
        id = platformAccess1.subscriberGetMailbox("161075");
        assertEquals(id, 1);
        //check that getMailbox is not called
        jmockProfileId1.expects(never()).method("getMailbox");
        id = platformAccess2.subscriberGetMailbox("161075");
        assertEquals(id, 1);

        //test faulty user
        try {
            platformAccess.subscriberGetMailbox("55512345");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }
    }

    /**
     * Tests the subscriberGetMailbox function. But with some exceptions
     *
     * @throws Exception
     */
    public void testSubscriberGetMailboxExceptions() throws Exception {
        jmockProfileId0.expects(once()).method("getMailbox").
                will(throwException(new UserProvisioningException("Error")));
        try {
            platformAccess.subscriberGetMailbox("161074");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetMailbox") > -1);
        }

        jmockProfileId0.expects(once()).method("getMailbox").
                will(throwException(new HostException("Error")));
        try {
            platformAccess.subscriberGetMailbox("161074");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetMailbox") > -1);
        }
    }

    /**
     * Tests the mailboxUsage, mailboxGetByteUsage and the mailboxGetMessageUsage functions.
     *
     * @throws Exception
     */
    public void testMailboxGetByteUsage() throws Exception {
        Mock jmockQuotaUsageId0 = mock(IQuotaUsageInventory.class);
        Mock jmockQuotaUsageId1 = mock(IQuotaUsageInventory.class);
        jmockQuotaUsageId0.stubs().method("getQuota").will(returnValue(new MyQuotaUsage(QuotaName.TOTAL, 10, 666)));
        jmockQuotaUsageId1.stubs().method("getQuota").will(returnValue(new MyQuotaUsage(QuotaName.TOTAL, 20, 777)));

        jmockMailboxId0.expects(once()).method("getQuotaUsageInventory").will(returnValue(jmockQuotaUsageId0.proxy()));
        jmockMailboxId1.expects(once()).method("getQuotaUsageInventory").will(returnValue(jmockQuotaUsageId1.proxy()));

        //test mailboxid0
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        int usageId0 = platformAccess.mailboxUsage(mailboxId0);
        int msgs = platformAccess.mailboxGetMessageUsage(usageId0);
        assertEquals(10, msgs);

        //test mailboxid1
        int mailboxId1 = platformAccess.subscriberGetMailbox("161075");
        platformAccess.mailboxUsage(mailboxId1);
        msgs = platformAccess.mailboxGetMessageUsage(mailboxId1);
        assertEquals(20, msgs);

        //test invalid mailboxId
        try {
            platformAccess.mailboxUsage(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("mailBoxUsage") > -1);
        }

        try {
            platformAccess.mailboxGetMessageUsage(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        //test MailboxException
        jmockMailboxId1.expects(once()).method("getQuotaUsageInventory").will(
                throwException(new MailboxException("Error")));
        try {
            platformAccess.mailboxUsage(mailboxId1);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("mailBoxUsage") > -1);
        }
    }

    /**
     * Tests the mailboxGetFolder function. Asserts the folderId that is returned
     *
     * @throws Exception
     */
    public void testMailboxGetFolder() throws Exception {
        //test INBOX folderid from mailbox 0
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        int folderId = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        assertEquals(0, mailboxId0);
        assertEquals(0, folderId);

        //test INBOX folderid from mailbox 1
        int mailboxId1 = platformAccess.subscriberGetMailbox("161075");
        folderId = platformAccess.mailboxGetFolder(mailboxId1, INBOX);
        assertEquals(1, mailboxId1);
        assertEquals(1, folderId);

        //test TRASH folderid from mailbox 1
        folderId = platformAccess.mailboxGetFolder(mailboxId1, TRASH);
        assertEquals(2, folderId);

        //test TRASH folderid from mailbox 1 again (should be same)
        folderId = platformAccess.mailboxGetFolder(mailboxId1, TRASH);
        assertEquals(2, folderId);

        //test TRASH folderid from mailbox 0
        folderId = platformAccess.mailboxGetFolder(mailboxId0, TRASH);
        assertEquals(3, folderId);

        //test INBOX folderid from mailbox 0 again (should be same as first test)
        folderId = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        assertEquals(0, folderId);

        //test a mailboxid that does not exist
        try {
            platformAccess.mailboxGetFolder(10, TRASH);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        //test FolderNotFoundException
        jmockMailboxId1.expects(once()).method("getFolder").will(
                throwException(new FolderNotFoundException("Error")));
        try {
            platformAccess.mailboxGetFolder(mailboxId1, "nofolder");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("mailboxGetFolder") > -1);
            assertTrue(e.getDescription().indexOf("folderName") > -1);
        }

        //test MailboxException
        jmockMailboxId1.expects(once()).method("getFolder").will(
                throwException(new MailboxException("Error")));
        try {
            platformAccess.mailboxGetFolder(mailboxId1, "nofolder");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }
    }

    /**
     * Tests the mailboxAddFolder functions.
     *
     * @throws Exception
     */
    public void testMailboxAddFolder() throws Exception {
        String newFolderName = "Video";

        //add a folder to mailbox0
        jmockMailboxId0.expects(once()).method("addFolder").with(eq(newFolderName));
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        platformAccess.mailboxAddFolder(mailboxId0, newFolderName);

        //test adding null as foldername, an exception should be handled
        jmockMailboxId0.stubs().method("addFolder").with(eq(null)).
                will(throwException(new MailboxException("Folder name can't be null")));
        try {
            platformAccess.mailboxAddFolder(mailboxId0, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        //test adding a folder to a mailbox that does not exist. addFolder should never be called
        jmockMailboxId0.expects(never()).method("addFolder");
        try {
            platformAccess.mailboxAddFolder(99, newFolderName);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        //test adding subfolder to the INBOX of mailbox0
        newFolderName = "subfolder";
        jmockFolderId0Inbox.expects(once()).method("addFolder").with(eq(newFolderName));
        int folderId = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        platformAccess.mailboxAddFolder(mailboxId0, folderId, newFolderName);

        //test adding null as foldername, an exception should be handled
        jmockFolderId0Inbox.stubs().method("addFolder").with(eq(null)).
                will(throwException(new MailboxException("Folder name can't be null")));
        try {
            platformAccess.mailboxAddFolder(mailboxId0, folderId, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        //test adding a folder to a mailbox that does not exist. addFolder should never be called
        jmockFolderId0Inbox.expects(never()).method("addFolder");
        try {
            platformAccess.mailboxAddFolder(99, folderId, newFolderName);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        //test adding a folder to a folder that does not exist. addFolder should never be called
        jmockFolderId0Inbox.expects(never()).method("addFolder");
        try {
            platformAccess.mailboxAddFolder(mailboxId0, 99, newFolderName);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessMailboxTest.class);
    }
}

class MyQuotaUsage extends QuotaUsage {

    MyQuotaUsage(QuotaName name, long messageUsage, long byteUsage) {
        super(name, messageUsage, byteUsage);
    }
}